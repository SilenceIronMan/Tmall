package com.ysy.tmall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbitmq.client.Channel;
import com.ysy.tmall.common.exception.NoStockException;
import com.ysy.tmall.common.to.producttocoupon.SkuHasStockVo;
import com.ysy.tmall.common.to.producttocoupon.mq.StockDetailTo;
import com.ysy.tmall.common.to.producttocoupon.mq.StockLockedTo;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.common.utils.Query;
import com.ysy.tmall.common.utils.R;
import com.ysy.tmall.ware.dao.WareSkuDao;
import com.ysy.tmall.ware.entity.WareOrderTaskDetailEntity;
import com.ysy.tmall.ware.entity.WareOrderTaskEntity;
import com.ysy.tmall.ware.entity.WareSkuEntity;
import com.ysy.tmall.ware.feign.OrderFeignService;
import com.ysy.tmall.ware.service.WareOrderTaskDetailService;
import com.ysy.tmall.ware.service.WareOrderTaskService;
import com.ysy.tmall.ware.service.WareSkuService;
import com.ysy.tmall.ware.vo.OrderItemVo;
import com.ysy.tmall.ware.vo.OrderVo;
import com.ysy.tmall.ware.vo.WareSkuLockVo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service("wareSkuService")
@Slf4j
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Resource
    private WareOrderTaskService wareOrderTaskService;

    @Resource
    private WareOrderTaskDetailService wareOrderTaskDetailService;

    @Resource
    private OrderFeignService orderFeignService;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();

        String wareId = (String) params.get("wareId");
        if (StringUtils.isNotEmpty(wareId)) {
            wrapper.eq("ware_id", wareId);

        }

        String skuId = (String) params.get("skuId");
        if (StringUtils.isNotEmpty(skuId)) {
            wrapper.eq("sku_id", skuId);

        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void addStocks(Long skuId, Long wareId, Integer stock) {

        WareSkuEntity wareSkuEntity = this.baseMapper
                .selectOne(new QueryWrapper<WareSkuEntity>()
                        .eq("sku_id", skuId).eq("ware_id", wareId));
        if (Objects.isNull(wareSkuEntity)) {
            WareSkuEntity newWare = new WareSkuEntity();
            newWare.setSkuId(skuId);
            newWare.setWareId(wareId);
            newWare.setSkuName("");
            newWare.setStock(stock);
            this.save(newWare);
        } else {

            wareSkuEntity.setStock(wareSkuEntity.getStock() + stock);
            this.updateById(wareSkuEntity);

        }

    }

    @Override
    public List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds) {

        List<SkuHasStockVo> skuHasStockVos = skuIds.stream().map(skuId -> {
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();

            Long count = baseMapper.getSkuStock(skuId);
            skuHasStockVo.setSkuId(skuId);

            if (count > 0) {
                skuHasStockVo.setHasStock(true);
            } else {
                skuHasStockVo.setHasStock(false);
            }

            return skuHasStockVo;
        }).collect(Collectors.toList());
        return skuHasStockVos;
    }


    @Override
    public void unlockStock(StockLockedTo to) {

        Long id = to.getId();
        WareOrderTaskEntity byId = wareOrderTaskService.getById(id);
        StockDetailTo detail = to.getDetail();
        if (byId != null) {
            // 解锁
            String orderSn = byId.getOrderSn();
            R orderStatus = orderFeignService.getOrderStatus(orderSn);
            if (orderStatus.getCode() == 0) {
                OrderVo order = orderStatus.getData("order", new TypeReference<OrderVo>() {
                });

                // 订单不存在(回滚)库存未回滚 or  订单存在(不是取消状态)
                if (order == null || order.getStatus() == 4) {
                    Long detailId = detail.getId();
                    WareOrderTaskDetailEntity detailEntity = wareOrderTaskDetailService.getById(detailId);
                    // 未解锁的库存任务才需要解锁
                    if (detailEntity.getLockStatus() == 1) {
                        //当前库存工作单详情，状态为1：已锁定但是未解锁才可以解锁
                        unLockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detailId);
                    }


                }


            } else {
                throw new RuntimeException("订单状态远程服务调用失败");
            }




        } else {
            // 无需解锁


        }
    }



    /**
     * 解锁库存
     * @param skuId
     * @param wareId
     * @param num
     * @param taskDetailId
     */
    public void unLockStock(Long skuId, Long wareId, Integer num, Long taskDetailId) {
        //库存解锁
        baseMapper.unLockStock(skuId, wareId, num);
        //更新库存工作单的状态
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        entity.setId(taskDetailId);
        entity.setLockStatus(2);//变为已解锁
        wareOrderTaskDetailService.updateById(entity);
    }


    /**
     * 库存锁定解锁场景
     * 1.下订单成功 订单过期没有支付被系统自动取消 被用户手动取消
     *    需要解锁库存
     * 2.下订单成功 库存锁定成功 其他业务调用失败 导致订单回滚
     *    锁定库存需要解锁
     *
     * @param wareSkuLockVo
     * @return
     */
    @Override
    @Transactional
    public Boolean orderLockStock(WareSkuLockVo wareSkuLockVo) {

        // 保存库存工作单信息(类似历史记录)
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        // 订单号
        wareOrderTaskEntity.setOrderSn(wareSkuLockVo.getOrderSn());

        wareOrderTaskService.save(wareOrderTaskEntity);
        // 1.根据下单地址 找一个就近的仓库 锁定库存
        // 1.找到每个商品在哪个仓库都有库存
        List<OrderItemVo> locks = wareSkuLockVo.getLocks();
        List<SkuWareHasStock> skuWareHasStockList = locks.stream().map(item -> {
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            skuWareHasStock.setSkuId(skuId);
            skuWareHasStock.setNum(item.getCount());

            // 查询该skuId商品所在仓库
            List<Long> wareId = this.listWareIdHasSkuStock(skuId);
            skuWareHasStock.setWareId(wareId);
            return skuWareHasStock;
        }).collect(Collectors.toList());

        // 锁定库存
        for (SkuWareHasStock skuWareHasStock : skuWareHasStockList) {



            // 有一个仓库有库存就设置为true
            Boolean skuLock = false;
            Long skuId = skuWareHasStock.getSkuId();
            List<Long> wareIds = skuWareHasStock.getWareId();
            if (wareIds == null && wareIds.size() > 0) {
                // 直接抛出无库存异常
                throw new NoStockException(skuId);

            }
            //1.如果每一件商品都锁定成功，将当前商品锁定了的工作单记录发送给MQ
            //2.锁定失败。前面保存的工作单信息就回滚了。发送出去的消息，即使要解锁记录，由于在数据库查不到id，所有就不用解锁
            Integer num = skuWareHasStock.getNum();
            for (Long wareId : wareIds) {
                // 成功就返回1, 失败返回 0
                Long count = this.lockSkuStock(skuId, wareId, num);
                if (count == 1) {
                    skuLock = true;
                    // 成功跳出当前循环

                    // 保存库存工作单详情信息
                    WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity(null, skuId,"",num, wareOrderTaskEntity.getId(), wareId, 1);
                    wareOrderTaskDetailService.save(wareOrderTaskDetailEntity);

                    // 发送消息给RabbitMQ
                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(wareOrderTaskEntity.getId());
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(wareOrderTaskDetailEntity, stockDetailTo);
                    stockLockedTo.setDetail(stockDetailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange","stock.locked",stockLockedTo);

                    break;
                }
                // 当前仓库锁定失败 锁下一个


            }
            if (!skuLock) {
                // 如果该商品所有仓库都没有办法锁定库存
                throw new NoStockException(skuId);
            }

        }
        // 到这说明 无功

        return true;
    }

    @Override
    public List<Long> listWareIdHasSkuStock(Long skuId) {
        return this.baseMapper.listWareIdHasSkuStock(skuId);
    }

    @Override
    public Long lockSkuStock(Long skuId, Long wareId, Integer num) {
        return this.baseMapper.lockSkuStock(skuId, wareId, num);
    }

    @Data
    class SkuWareHasStock {
        /**
         * skuId
         */
        private Long skuId;

        /**
         * 商品数量(需要扣减的库存数量)
         */
        private Integer num;

        /**
         * sku商品所在仓库列表
         */
        private List<Long> wareId;
    }
}
