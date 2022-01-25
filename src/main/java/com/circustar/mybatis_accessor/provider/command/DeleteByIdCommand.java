package com.circustar.mybatis_accessor.provider.command;

import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mybatis_accessor.common.MvcEnhanceConstants;
import com.circustar.mybatis_accessor.common.MybatisAccessorException;
import com.circustar.mybatis_accessor.utils.MybatisPlusUtils;


import java.io.Serializable;
import java.util.Collection;

public class DeleteByIdCommand extends DeleteByIdBatchCommand {
    private static DeleteByIdCommand batchCommand = new DeleteByIdCommand();
    public static DeleteByIdCommand getInstance() {
        return batchCommand;
    }

    @Override
    public <T extends Collection> boolean update(IService service, T collection, Object option) throws MybatisAccessorException {
        boolean physicDelete = (boolean) option;
        if(collection.size() > 1) {
            return super.update(service, collection, physicDelete);
        } else {
            for (Object var1 : collection) {
                Serializable id = (Serializable) var1;
                boolean result = MybatisPlusUtils.deleteById(service, id, physicDelete);
                if (!result) {
                    throw new MybatisAccessorException(MybatisAccessorException.ExceptionType.TARGET_NOT_FOUND
                            ,String.format(MvcEnhanceConstants.UPDATE_TARGET_NOT_FOUND
                            , "Mapper - " + service.getBaseMapper().getClass().getSimpleName()));
                }
            }
        }
        return true;
    }
}
