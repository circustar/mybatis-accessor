package com.circustar.mybatis_accessor.provider.command;

import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mybatis_accessor.common.MvcEnhanceConstants;
import com.circustar.mybatis_accessor.common.MybatisAccessorException;

import java.util.Collection;

public class DeleteByIdBatchCommand implements IUpdateCommand {
    private static DeleteByIdBatchCommand batchCommand = new DeleteByIdBatchCommand();
    public static DeleteByIdBatchCommand getInstance() {
        return batchCommand;
    }

    @Override
    public UpdateType getUpdateType() {return UpdateType.DELETE;}

    @Override
    public <T extends Collection> boolean update(IService service, T obj, Object option) throws MybatisAccessorException {
        boolean result;
        if(obj == null || obj.isEmpty()) {
            return true;
        } else if(obj.size() == 1) {
            result = service.removeById(obj.iterator().next());
        } else {
            result = service.removeByIds(obj);
        }
        if (!result) {
            throw new MybatisAccessorException(MybatisAccessorException.ExceptionType.TARGET_NOT_FOUND
                    ,String.format(MvcEnhanceConstants.UPDATE_TARGET_NOT_FOUND
                    , "Mapper - " + service.getBaseMapper().getClass().getSimpleName()));
        }
        return true;
    }
}
