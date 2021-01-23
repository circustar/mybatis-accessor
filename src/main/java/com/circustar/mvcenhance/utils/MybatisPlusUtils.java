package com.circustar.mvcenhance.utils;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.circustar.mvcenhance.mapper.MybatisPlusMapper;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public class MybatisPlusUtils {
    public static Boolean deleteById(IService service, Serializable id, boolean physic) {
        BaseMapper mapper = service.getBaseMapper();
        if(physic && mapper instanceof MybatisPlusMapper) {
            return SqlHelper.retBool(((MybatisPlusMapper)mapper).physicDeleteById(id));
        } else {
            return service.removeById(id);
        }
    }
    public static Boolean deleteByMap(IService service, Map<String, Object> columnMap, boolean physic) {
        BaseMapper mapper = service.getBaseMapper();
        if(physic && mapper instanceof MybatisPlusMapper) {
            return SqlHelper.retBool(((MybatisPlusMapper)mapper).physicDeleteByMap(columnMap));
        } else {
            return service.removeByMap(columnMap);
        }
    }
    public static Boolean delete(IService service, Wrapper wrapper, boolean physic) {
        BaseMapper mapper = service.getBaseMapper();
        if(physic && mapper instanceof MybatisPlusMapper) {
            return SqlHelper.retBool(((MybatisPlusMapper)mapper).physicDelete(wrapper));
        } else {
            return service.remove(wrapper);
        }
    }
    public static Boolean deleteBatchIds(IService service, Collection<? extends Serializable> idList, boolean physic) {
        BaseMapper mapper = service.getBaseMapper();
        if(physic && mapper instanceof MybatisPlusMapper) {
            return SqlHelper.retBool(((MybatisPlusMapper)mapper).physicDeleteBatchByIds(idList));
        } else {
            return service.removeByIds(idList);
        }
    }
}
