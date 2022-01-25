package com.circustar.mybatis_accessor.provider.command;

import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mybatis_accessor.common.MvcEnhanceConstants;
import com.circustar.mybatis_accessor.common.MybatisAccessorException;


import java.util.Collection;

public class InsertCommand implements IUpdateCommand {

    private static InsertCommand batchCommand = new InsertCommand();
    public static InsertCommand getInstance() {
        return batchCommand;
    }

    @Override
    public UpdateType getUpdateType() {return UpdateType.INSERT;}

    @Override
    public <T extends Collection> boolean update(IService service, T collection, Object option) throws MybatisAccessorException {
        boolean result = service.saveBatch(collection);
        if(!result) {
            throw new MybatisAccessorException(MybatisAccessorException.ExceptionType.TARGET_NOT_FOUND
                    , String.format(MvcEnhanceConstants.UPDATE_TARGET_NOT_FOUND
                    , "Mapper - " + service.getBaseMapper().getClass().getSimpleName()));
        }
        return result;
    }
}
