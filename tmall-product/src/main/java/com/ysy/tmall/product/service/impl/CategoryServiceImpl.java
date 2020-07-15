package com.ysy.tmall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mysql.cj.util.TimeUtil;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.common.utils.Query;
import com.ysy.tmall.product.dao.CategoryDao;
import com.ysy.tmall.product.entity.CategoryEntity;
import com.ysy.tmall.product.service.CategoryBrandRelationService;
import com.ysy.tmall.product.service.CategoryService;
import com.ysy.tmall.product.vo.web.Catalog2Vo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {


    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1.查出所有分类
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);

        //2.生成树状结构
        //一级目录 Category
        List<CategoryEntity> categoryTreeList = categoryEntities.stream().filter(c -> c.getParentCid().equals(0L)).map(menu -> {
            menu.setChildren(getChildren(menu, categoryEntities));
            return menu;
        }).sorted((menu1, menu2) ->
                (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort())
        ).collect(Collectors.toList());


        return categoryTreeList;

    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        // 1.TODO 检查是否有子菜单
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {

        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);


        return parentPath.toArray(new Long[parentPath.size()]);
    }

    @Override
    @Transactional
    public void updateCascade(CategoryEntity category) {
        updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    @Override
    public List<CategoryEntity> getLevel1Categorys() {

        List<CategoryEntity> categoryEntities = this.list(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0L));
        return categoryEntities;
    }

    /**
     *
     * @return
     */
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {

        /*
         *  1.空结果缓存 : 解决缓存穿透
         *  2.设置过期时间(加随机值) : 解决缓存雪崩
         *  3.加锁 : 解决缓存击穿
         *
         */

        // 差生堆外内存异常 outofdirectmemoryError TODO lettuce 使用 netty 時 用了 JVM 配置的 -Xms參數
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        // 读取缓存
        String categorys = ops.get("categorys");
        // 读取数据库
        Map<String, List<Catalog2Vo>> map;
        // 缓存没有
        if (StringUtils.isEmpty(categorys)) {
            log.info("缓存未命中........将要去查询数据库..");
            // 在锁里面 进行redis Set 值 防止 释放了锁 但是redis 却还没set
            map = getCatalogJsonFromDbWithRedisLock();

        } else {
            log.info("缓存命中........直接返回....");
            map = JSON.parseObject(categorys, new TypeReference<Map<String, List<Catalog2Vo>>>(){}.getType());
        }






        return  map;
    }


    /**
     * synchronized 本地锁 只会锁当前JVM 分布式下多个jvm机器 你锁不了
     * (就会多放走多个请求 (不过影响不大)) 解决方案 是采用分布式锁
     * 从数据库查询 (加锁 防止击穿)
     * @return
     */
    public  Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithRedisLock() {

        // 唯一区分
        String token = UUID.randomUUID().toString();
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        // 尝试加锁并设置过期时间
        Boolean categoryLock = ops.setIfAbsent("categoryLock", token,  300, TimeUnit.SECONDS);

        if (categoryLock) { // 加锁成功
            Map<String, List<Catalog2Vo>> categoryFromDb;

            try {
                categoryFromDb = getCategoryFromDb();
                // 查询完毕释放锁 删除之前判断是否是自己的锁 (业务执行时间可能超过了 锁过期的时间, 有别的进程进来了并获得了锁)
                // 必须原子操作 获取值+删除锁 Lua脚本
//            String lock = ops.get("categoryLock");
//            if (token.equals(lock)) {
//                stringRedisTemplate.delete("categoryLock");
//            }
            } finally {
                log.info("分布式锁解锁.........");
                // 解锁
                String script = "if redis.call('get',KEYS[1]) == ARGV[1] " +
                        "then " +
                        "return redis.call('del',KEYS[1]) " +
                        "else " +
                        "return 0 " +
                        "end";
                Long unLock = stringRedisTemplate
                        .execute(new DefaultRedisScript<>(script,
                                Long.class),
                                Arrays.asList("categoryLock"), token);
            }

            return categoryFromDb;
        } else { // 加锁失败
            try {
                log.info("获取分布式锁失败.......等待重试");
                // 休眠100ms
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromDbWithRedisLock(); // 自旋的方式
        }

    }

    /**
     * 本地锁 synchronized 分布式下不够完善
     * @return
     */
    public  Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithLocalLock () {
        synchronized (this) {
            return getCategoryFromDb();
        }

    }



    private Map<String, List<Catalog2Vo>> getCategoryFromDb() {
        // 判断缓存是否有数据
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        // 读取缓存
        String categorys = ops.get("categorys");
        if (StringUtils.isNotEmpty(categorys)) {
            log.info("缓存命中........直接返回....");
            Map<String, List<Catalog2Vo>> redisMap = JSON.parseObject(categorys, new TypeReference<Map<String, List<Catalog2Vo>>>(){}.getType());
            return redisMap;
        }
        log.info("缓存未命中........查询数据库.........");
        List<CategoryEntity> categoryEntityList = this.baseMapper.selectList(null);

        List<CategoryEntity> byParentCid = getByParentCid(categoryEntityList, 0L);

        Map<String, List<Catalog2Vo>> map = byParentCid.stream().collect(Collectors.toMap(k -> k.getCatId().toString()
                , v -> {


                    // 2及分類集合
                    List<CategoryEntity> categoryEntities = getByParentCid(categoryEntityList, v.getCatId());

                    List<Catalog2Vo> catalog2Vos = categoryEntities.stream().map(c2 -> {
                        // 二級分類id
                        Long cat2Id = c2.getCatId();
                        //查找三級分類集合
                        List<CategoryEntity> category3List = getByParentCid(categoryEntityList, c2.getCatId());
                        List<Catalog2Vo.Catalog3Vo> catalog3Vos = category3List.stream().map(c3 -> new Catalog2Vo.Catalog3Vo(cat2Id.toString(), c3.toString(), c3.getName())).collect(Collectors.toList());

                        Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(), catalog3Vos, c2.getCatId().toString(),
                                c2.getName());


                        return catalog2Vo;
                    }).collect(Collectors.toList());


                    return catalog2Vos;
                }));

        // redis 设值
        String categorysJson = JSON.toJSONString(map);
        ops.set("categorys", categorysJson, 1, TimeUnit.DAYS);
        return map;
    }

    /**
     * 根据父id查询分类列表
     * @param categoryEntityList
     * @param parentCid
     * @return
     */
    private List<CategoryEntity> getByParentCid(List<CategoryEntity> categoryEntityList, Long parentCid) {

        List<CategoryEntity> list = categoryEntityList.stream().filter(cate -> cate.getParentCid() == parentCid).collect(Collectors.toList());
        return list;
    }

    /**
     * 递归记录自身id 和父id
     *
     * @param catelogId
     * @param paths     存储容器
     * @return
     */
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {

        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        Long parentCid = byId.getParentCid();
        if (parentCid != 0) {
            findParentPath(parentCid, paths);
        }
        Collections.reverse(paths);
        return paths;
    }

    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {

        List<CategoryEntity> children = all.stream().filter(c ->
                c.getParentCid().equals(root.getCatId())
        ).map(menu -> {
            menu.setChildren(getChildren(menu, all));
            return menu;
        }).sorted((menu1, menu2) ->
                (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort())
        ).collect(Collectors.toList());
        return children;

    }

}
