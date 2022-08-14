package com.circustar.mybatis_accessor.update_processor;

import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mybatis_accessor.class_info.DtoClassInfo;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;

import java.io.Serializable;
import java.util.List;

public class DeleteDtoUpdateProcessor extends AbstractDtoUpdateProcessor {
    public DeleteDtoUpdateProcessor(IService service, IUpdateCommand updateCommand, Object option, DtoClassInfo dtoClassInfo, List updateDtoList, Boolean updateChildrenFirst, boolean updateChildrenOnly) {
        super(service, updateCommand, option, dtoClassInfo, updateDtoList, updateChildrenFirst, updateChildrenOnly);
    }

    @Override
    public List convertToEntity() {
        return this.getUpdateDtoList();
    }

    @Override
    public Serializable getUpdateKey(Object dto) {
        return (Serializable)dto;
    }
}
