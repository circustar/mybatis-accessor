package com.circustar.mybatis_accessor.provider.command;

import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mybatis_accessor.common.MessageProperties;
import com.circustar.mybatis_accessor.utils.MybatisPlusUtils;


import java.io.Serializable;
import java.util.Collection;

public class DeleteByIdCommand implements IUpdateCommand {
    private static DeleteByIdCommand batchCommand = new DeleteByIdCommand();
    public static DeleteByIdCommand getInstance() {
        return batchCommand;
    }

    @Override
    public UpdateType getUpdateType() {return UpdateType.DELETE;}

    @Override
    public <T extends Collection> boolean update(IService service, T collection, Object option) {
        for(Object var1 : collection) {
            boolean physicDelete = (boolean) option;
            Serializable id = (Serializable) var1;
            boolean result = MybatisPlusUtils.deleteById(service, id, physicDelete);
            if(!result) {
                throw new RuntimeException(String.format(MessageProperties.UPDATE_TARGET_NOT_FOUND
                        , "Mapper - " + service.getBaseMapper().getClass().getSimpleName()));
            }
        }
        return true;
    }
}
