package com.atguigu.mapper;

import com.atguigu.entity.TOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author lucky845
 * @since 2022-04-23
 */
public interface TOrderMapper extends BaseMapper<TOrder> {

    List<TOrder> queryOrderByUserId(@Param("userId") long userId);
}
