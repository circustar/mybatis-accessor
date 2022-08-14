package com.circustar.mybatis_accessor.update_processor;

import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mybatis_accessor.class_info.DtoClassInfo;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;

import java.util.List;

public class InsertDtoUpdateProcessor extends AbstractDtoUpdateProcessor {
    public InsertDtoUpdateProcessor(IService service, IUpdateCommand updateCommand, Object option, DtoClassInfo dtoClassInfo, List updateDtoList, Boolean updateChildrenFirst, boolean updateChildrenOnly) {
        super(service, updateCommand, option, dtoClassInfo, updateDtoList, updateChildrenFirst, updateChildrenOnly);
    }
}
