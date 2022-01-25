package com.circustar.mybatis_accessor.provider.command;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mybatis_accessor.common.MvcEnhanceConstants;
import com.circustar.mybatis_accessor.utils.MybatisPlusUtils;

import java.util.Collection;

public class DeleteWrapperCommand implements IUpdateCommand {
    private static DeleteWrapperCommand batchCommand = new DeleteWrapperCommand();
    public static DeleteWrapperCommand getInstance() {
        return batchCommand;
    }

    @Override
    public UpdateType getUpdateType() {return UpdateType.DELETE;}

    @Override
    public <T extends Collection> boolean update(IService service, T collection, Object option) {
        boolean physicDelete = option != null && (boolean) option;
        for(Object var0 : collection) {
            boolean result = MybatisPlusUtils.delete(service, (Wrapper) var0, physicDelete);
            if (!result) {
                throw new RuntimeException(String.format(MvcEnhanceConstants.UPDATE_TARGET_NOT_FOUND
                        , "Mapper - " + service.getBaseMapper().getClass().getSimpleName()));
            }
        }
        return true;
    }
}
