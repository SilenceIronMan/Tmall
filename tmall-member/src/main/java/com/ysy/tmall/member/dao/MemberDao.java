package com.ysy.tmall.member.dao;

import com.ysy.tmall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author SilenceIronMan
 * @email yinshiyu_2008@126.com
 * @date 2020-06-26 21:45:12
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
