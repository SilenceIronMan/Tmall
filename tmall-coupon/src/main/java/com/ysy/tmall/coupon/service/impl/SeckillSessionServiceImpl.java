package com.ysy.tmall.coupon.service.impl;

import com.ysy.tmall.coupon.entity.SeckillSkuRelationEntity;
import com.ysy.tmall.coupon.service.SeckillSkuRelationService;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.common.utils.Query;

import com.ysy.tmall.coupon.dao.SeckillSessionDao;
import com.ysy.tmall.coupon.entity.SeckillSessionEntity;
import com.ysy.tmall.coupon.service.SeckillSessionService;

import javax.annotation.Resource;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Resource
    private SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLatest3DaysSession() {
        //计算最近三天
        QueryWrapper<SeckillSessionEntity> wrapper = new QueryWrapper<>();
        wrapper.between("start_time", startTime(), endTime());
        List<SeckillSessionEntity> list = list(wrapper);

        list.stream().forEach(session -> {
            Long sessionId = session.getId();
            List<SeckillSkuRelationEntity> seckillSkuRelationEntities = seckillSkuRelationService
                    .list(new QueryWrapper<SeckillSkuRelationEntity>()
                            .eq("promotion_session_id", sessionId));
            session.setRelationSkus(seckillSkuRelationEntities);

        });



        return list;
    }

    private String startTime(){
        LocalDate now = LocalDate.now();
        LocalTime min = LocalTime.MIN;
        LocalDateTime startTime = LocalDateTime.of(now, min);
        String format = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return  format;
    }

    public static void main(String[] args) {
        LocalDate now = LocalDate.now();
        System.out.println(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        LocalTime min = LocalTime.MIN;
        LocalDateTime startTime = LocalDateTime.of(now, min);
        System.out.println(startTime);
        LocalDateTime plus = startTime.plus(Duration.ofSeconds(60));
        System.out.println(plus);
        String format = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println(format);
        LocalDate date1 = LocalDate.of(2019, 1, 1);
        LocalDate date2 = LocalDate.of(2019, 3, 1);
        Period period = Period.between(date1, date2);
        System.out.println(period.getYears() + "-" + period.getMonths() + "-" + period.getDays());
    }

    private String endTime(){
        LocalDate now = LocalDate.now();
        LocalDate localDate = now.plusDays(2);
        LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
        String format = endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return  format;
    }

}
