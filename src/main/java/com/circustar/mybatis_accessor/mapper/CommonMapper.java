package com.circustar.mybatis_accessor.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.circustar.mybatis_accessor.common.MvcEnhanceConstants;
import org.apache.ibatis.annotations.Param;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface CommonMapper<T> extends BaseMapper<T> {

    int physicDeleteById(Serializable id);

    int physicDeleteByMap(@Param("cm") Map<String, Object> columnMap);

    int physicDelete(@Param("ew") Wrapper<T> wrapper);

    int physicDeleteBatchByIds(@Param("coll") Collection<? extends Serializable> idList);

    List<T> selectListWithJoin(@Param("ew") Wrapper<T> queryWrapper
            , @Param(MvcEnhanceConstants.MYBATIS_ENHANCE_JOIN_TABLE) String joinTable
            , @Param(MvcEnhanceConstants.MYBATIS_ENHANCE_JOIN_COLUMNS) String joinColumns);

    <E extends IPage<T>> E selectPageWithJoin(E page, @Param("ew") Wrapper<T> queryWrapper
            , @Param(MvcEnhanceConstants.MYBATIS_ENHANCE_JOIN_TABLE) String joinTable
            , @Param(MvcEnhanceConstants.MYBATIS_ENHANCE_JOIN_COLUMNS) String joinColumns);

    List<T> selectBatchIdsWithJoin(@Param("coll") Collection<? extends Serializable> idList);

    <E extends IPage<Map<String, Object>>> E selectMapsPageWithJoin(E page, @Param("ew") Wrapper<T> queryWrapper);

    List<Map<String, Object>> selectMapsWithJoin(@Param("ew") Wrapper<T> queryWrapper);

    List<Object> selectObjsWithJoin(@Param("ew") Wrapper<T> queryWrapper);

    default T selectOneWithJoin(@Param("ew") Wrapper<T> queryWrapper
            , @Param(MvcEnhanceConstants.MYBATIS_ENHANCE_JOIN_TABLE) String joinTable
            , @Param(MvcEnhanceConstants.MYBATIS_ENHANCE_JOIN_COLUMNS) String joinColumns) {
        List<T> list = selectListWithJoin(queryWrapper, joinTable, joinColumns);
        if(!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    Long selectCountWithJoin(@Param("ew") Wrapper<T> queryWrapper
            , @Param(MvcEnhanceConstants.MYBATIS_ENHANCE_JOIN_TABLE) String joinTable
            , @Param(MvcEnhanceConstants.MYBATIS_ENHANCE_JOIN_COLUMNS) String joinColumns);
}
