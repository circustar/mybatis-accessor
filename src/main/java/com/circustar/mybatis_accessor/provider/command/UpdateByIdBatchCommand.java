package com.circustar.mybatis_accessor.provider.command;

import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mybatis_accessor.common.MessageProperties;


import java.util.Collection;

public class UpdateByIdBatchCommand implements IUpdateCommand {

    private static UpdateByIdBatchCommand batchCommand = new UpdateByIdBatchCommand();
    public static UpdateByIdBatchCommand getInstance() {
        return batchCommand;
    }

    @Override

    public <T extends Collection> boolean update(IService service, T collection, Object option) {
        boolean result = service.updateBatchById(collection);
        if(!result) {
            throw new RuntimeException(String.format(MessageProperties.UPDATE_TARGET_NOT_FOUND
                    , "Mapper - " + service.getBaseMapper().getClass().getSimpleName()));
        }
        return result;
    }
}
