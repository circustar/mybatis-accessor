package com.circustar.mybatis_accessor.utils;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.circustar.mybatis_accessor.mapper.CommonMapper;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public abstract class MybatisPlusUtils {
    public static Boolean deleteById(IService service, Serializable id, boolean physic) {
        BaseMapper mapper = service.getBaseMapper();
        if(physic && mapper instanceof CommonMapper) {
            return SqlHelper.retBool(((CommonMapper)mapper).physicDeleteById(id));
        } else {
            return service.removeById(id);
        }
    }
    public static Boolean deleteByMap(IService service, Map<String, Object> columnMap, boolean physic) {
        BaseMapper mapper = service.getBaseMapper();
        if(physic && mapper instanceof CommonMapper) {
            return SqlHelper.retBool(((CommonMapper)mapper).physicDeleteByMap(columnMap));
        } else {
            return service.removeByMap(columnMap);
        }
    }
    public static Boolean delete(IService service, Wrapper wrapper, boolean physic) {
        BaseMapper mapper = service.getBaseMapper();
        if(physic && mapper instanceof CommonMapper) {
            return SqlHelper.retBool(((CommonMapper)mapper).physicDelete(wrapper));
        } else {
            return service.remove(wrapper);
        }
    }
    public static Boolean deleteBatchIds(IService service, Collection<? extends Serializable> idList, boolean physic) {
        if(idList == null || idList.isEmpty()) {
          return true;
        } else if(idList.size() == 1) {
            return deleteById(service, idList.iterator().next(), physic);
        }
        BaseMapper mapper = service.getBaseMapper();
        if(physic && mapper instanceof CommonMapper) {
            return SqlHelper.retBool(((CommonMapper)mapper).physicDeleteBatchByIds(idList));
        } else {
            return service.removeByIds(idList);
        }
    }
}
