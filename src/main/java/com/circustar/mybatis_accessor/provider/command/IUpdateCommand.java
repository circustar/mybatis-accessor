package com.circustar.mybatis_accessor.provider.command;

import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mybatis_accessor.common.MybatisAccessorException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

public interface IUpdateCommand {
    String KEY_FIELD_READ_METHOD = "KEY_FIELD_READ_METHOD";
    String PHYSIC_DELETE = "PHYSIC_DELETE";

    <T extends Collection> boolean update(IService service, T obj, Method keyReadMethod, Object option) throws MybatisAccessorException;
    UpdateType getUpdateType();

    enum UpdateType {
        INSERT("INSERT"),
        DELETE("DELETE"),
        UPDATE("UPDATE");

        private String name;
        UpdateType(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
    }

    static String getKeyValue(Object obj, Method keyReadMethod) {
        try {
            return keyReadMethod.invoke(obj, null).toString();
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
        return "";
    }
}
