package com.circustar.mybatis_accessor.provider.command;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Collection;

public interface IUpdateCommand {
    <T extends Collection> boolean update(IService service, T obj, Object option);
    UpdateType getUpdateType();

    String KEY_FIELD_READ_METHOD = "KEY_FIELD_READ_METHOD";
    String PHYSIC_DELETE = "PHYSIC_DELETE";

    enum UpdateType {
        INSERT,
        DELETE,
        UPDATE
    }
}
