package com.circustar.mybatis_accessor.provider.command;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Collection;

public interface IUpdateCommand {
    String KEY_FIELD_READ_METHOD = "KEY_FIELD_READ_METHOD";
    String PHYSIC_DELETE = "PHYSIC_DELETE";

    <T extends Collection> boolean update(IService service, T obj, Object option);
    UpdateType getUpdateType();

    enum UpdateType {
        INSERT("insert"),
        DELETE("delete"),
        UPDATE("update");

        private String name;
        UpdateType(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
    }
}
