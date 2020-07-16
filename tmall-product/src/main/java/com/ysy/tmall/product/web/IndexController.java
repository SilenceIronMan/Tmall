package com.ysy.tmall.product.web;

import com.ysy.tmall.product.entity.CategoryEntity;
import com.ysy.tmall.product.service.CategoryService;
import com.ysy.tmall.product.vo.web.Catalog2Vo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @anthor silenceYin
 * @date 2020/7/14 - 0:55
 */
@Controller
@Slf4j
public class IndexController {

    @Resource
    private CategoryService categoryService;

    @Resource
    private RedissonClient redisson;

    @GetMapping(value = {"/", "/index.html"})
    public String indexPage(Model model){
        // 查出首页1级分类
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categorys();

        model.addAttribute("categorys", categoryEntities);
        return "index";
    }

    @GetMapping(value = "/index/catalog.json")
    @ResponseBody
    public Map<String, List<Catalog2Vo>> getCatalogJson(){
        // 查出首页1级分类
        Map<String, List<Catalog2Vo>> catalogJson = categoryService.getCatalogJson();

        return catalogJson;
    }

    @GetMapping(value = "/hello")
    @ResponseBody
    public String hello(){
        RLock lock = redisson.getLock("my_lock");
        lock.lock(); // 阻塞等待获取锁
        // 锁自动续期 如果业务超长 运行期间 自动给锁加上新的30s 不用担心业务时间长 锁自动过期被删掉
        // 加锁的业务只要运行完成 就不会给当前锁续期 即使不手动解锁 锁默认会在30s后自动删除
        try {
            log.info("redis 加锁成功" + Thread.currentThread().getId());
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 解锁
            log.info("redis 释放锁" + Thread.currentThread().getId());
            lock.unlock();
        }

        return "hello";
    }

    /**
     * 写锁
     * 保证一定能读到最新的数据, 修改期间, 写锁是一个排它锁(互斥锁, 排它锁).
     * 读锁是一个共享锁
     * 写锁没释放读就必须等待
     * 读 + 读 : 相当于 无锁, 并发读, 只会在redis中记录所有当前的读锁, 他们会同时枷锁成功.
     * 写 + 读 : 等待写锁释放
     * 写 + 写 : 阻塞方式
     * 读 + 写 : 有读锁. 写也需要等待.
     *
     * @return
     */
    @GetMapping(value = "/write")
    @ResponseBody
    public String writeValue() {
        RReadWriteLock readWriteLock = redisson.getReadWriteLock("rw-lock");
        String s = "";
        RLock rLock = readWriteLock.writeLock();

        try {
            // 改数据加写锁,读数据加读锁
            rLock.lock();
            log.info("写锁加锁成功...." + Thread.currentThread().getId());
            s = UUID.randomUUID().toString();
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
            log.info("写锁释放........" + Thread.currentThread().getId());
        }
        return  s;

    }

    @GetMapping(value = "/read")
    @ResponseBody
    public String readValue() {
        RReadWriteLock readWriteLock = redisson.getReadWriteLock("rw-lock");
        String s = "";
        // 加读锁
        RLock rLock = readWriteLock.readLock();

        try {
            // 改数据加写锁,读数据加读锁
            rLock.lock();
            log.info("读锁加锁成功...." + Thread.currentThread().getId());
            s = UUID.randomUUID().toString();
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
            log.info("读锁释放........" + Thread.currentThread().getId());
        }
        return  s;

    }

}
