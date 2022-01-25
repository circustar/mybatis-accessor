package com.circustar.mybatis_accessor.provider.command;

import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mybatis_accessor.common.MvcEnhanceConstants;
import com.circustar.mybatis_accessor.common.MybatisAccessorException;


import java.util.Collection;

public class UpdateByIdCommand implements IUpdateCommand {

    private static UpdateByIdCommand batchCommand = new UpdateByIdCommand();
    public static UpdateByIdCommand getInstance() {
        return batchCommand;
    }

    @Override
    public UpdateType getUpdateType() {return UpdateType.UPDATE;}

    @Override
    public <T extends Collection> boolean update(IService service, T collection, Object option) throws MybatisAccessorException {
        for(Object var1 : collection) {
            boolean result = service.updateById(var1);
            if(!result) {
                throw new MybatisAccessorException(MybatisAccessorException.ExceptionType.TARGET_NOT_FOUND
                        , String.format(MvcEnhanceConstants.UPDATE_TARGET_NOT_FOUND
                        , "Mapper - " + service.getBaseMapper().getClass().getSimpleName()));
            }
        }
        return true;
    }
}
