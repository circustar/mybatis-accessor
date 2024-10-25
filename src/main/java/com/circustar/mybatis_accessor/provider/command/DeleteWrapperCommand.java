package com.circustar.mybatis_accessor.provider.command;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mybatis_accessor.common.MvcEnhanceConstants;
import com.circustar.mybatis_accessor.common.MybatisAccessorException;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.stream.Collectors;

public class DeleteWrapperCommand implements IUpdateCommand {
    private static DeleteWrapperCommand batchCommand = new DeleteWrapperCommand();
    public static DeleteWrapperCommand getInstance() {
        return batchCommand;
    }

    @Override
    public UpdateType getUpdateType() {return UpdateType.DELETE;}

    @Override
    public <T extends Collection> boolean update(IService service, T collection, Method keyReadMethod, Object option) throws MybatisAccessorException {
        for(Object var0 : collection) {
            boolean result = service.remove((Wrapper) var0);
            if (!result) {
                throw new MybatisAccessorException(MybatisAccessorException.ExceptionType.TARGET_NOT_FOUND
                        , String.format(MvcEnhanceConstants.UPDATE_TARGET_NOT_FOUND
                        , "Service - " + service.getClass().getSimpleName())
                        + ",IDS : " + collection.stream().map(x -> IUpdateCommand.getKeyValue(x, keyReadMethod))
                        .collect(Collectors.joining(",")));
            }
        }
        return true;
    }
}
