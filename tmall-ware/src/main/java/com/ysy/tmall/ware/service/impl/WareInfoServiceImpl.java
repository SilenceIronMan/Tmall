package com.ysy.tmall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.ysy.tmall.common.utils.R;
import com.ysy.tmall.ware.feign.MemberFeignService;
import com.ysy.tmall.ware.vo.FareVo;
import com.ysy.tmall.ware.vo.MemberAddressVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.common.utils.Query;

import com.ysy.tmall.ware.dao.WareInfoDao;
import com.ysy.tmall.ware.entity.WareInfoEntity;
import com.ysy.tmall.ware.service.WareInfoService;

import javax.annotation.Resource;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Resource
    private MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            wrapper.and(w ->
                    w.eq("id", key)
                            .or().like("name",key)
                            .or().like("address",key)
                            .or().like("areacode",key)
                    );
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wrapper
        );



        return new PageUtils(page);
    }

    @Override
    public FareVo getFare(Long addrId) {
        R r = memberFeignService.addrInfo(addrId);
        MemberAddressVo member = r.getData("memberReceiveAddress", new TypeReference<MemberAddressVo>(){});
        FareVo fareVo = new FareVo();
        if(member!=null){
            //调用第三方物流接口 计算运费
            //这里模拟
            String phone = member.getPhone();
            String substring = phone.substring(phone.length() - 1);
            BigDecimal bigDecimal = new BigDecimal(substring);
            fareVo.setAddress(member);
            fareVo.setFare(bigDecimal);
            return fareVo;
        }
        return null;
    }

}
