package com.circustar.mvcenhance.enhance.mybatisplus;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public interface MybatisPlusMapper<T> extends BaseMapper<T> {
    int physicDeleteById(Serializable id);

    int physicDeleteByMap(@Param("cm") Map<String, Object> columnMap);

    int physicDelete(@Param("ew") Wrapper<T> wrapper);

    int physicDeleteBatchByIds(@Param("coll") Collection<? extends Serializable> idList);
}
