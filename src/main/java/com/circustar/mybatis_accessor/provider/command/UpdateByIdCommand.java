package com.circustar.mybatis_accessor.provider.command;

import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mybatis_accessor.common.MessageProperties;


import java.util.Collection;

public class UpdateByIdCommand implements IUpdateCommand {

    private static UpdateByIdCommand batchCommand = new UpdateByIdCommand();
    public static UpdateByIdCommand getInstance() {
        return batchCommand;
    }

    @Override

    public <T extends Collection> boolean update(IService service, T collection, Object option) {
        for(Object var1 : collection) {
            boolean result = service.updateById(var1);
            if(!result) {
                throw new RuntimeException(String.format(MessageProperties.UPDATE_TARGET_NOT_FOUND
                        , "Mapper - " + service.getBaseMapper().getClass().getSimpleName()));
            }
        }
        return true;
    }
}
