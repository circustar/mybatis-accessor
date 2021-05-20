package com.circustar.mybatis_accessor.provider.command;

import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mybatis_accessor.common.MessageProperties;
import com.circustar.mybatis_accessor.utils.MybatisPlusUtils;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DeleteByIdBatchCommand extends DeleteByIdCommand {
    private static DeleteByIdBatchCommand batchCommand = new DeleteByIdBatchCommand();
    public static DeleteByIdBatchCommand getInstance() {
        return batchCommand;
    }
    @Override

    public <T extends Collection> boolean update(IService service, T obj, Object option) {
        List physicDeleteCollection = new ArrayList();
        List deleteCollection = new ArrayList();
        boolean physicDelete = (boolean) option;
        if(physicDelete) {
            physicDeleteCollection.addAll(obj);
        } else {
            deleteCollection.addAll(obj);
        }

        if(physicDeleteCollection.size() > 0) {
            boolean result = MybatisPlusUtils.deleteBatchIds(service, physicDeleteCollection, true);
            if (!result) {
                throw new RuntimeException(String.format(MessageProperties.UPDATE_TARGET_NOT_FOUND
                        , "Mapper - " + service.getBaseMapper().getClass().getSimpleName()));
            }
        }
        if(deleteCollection.size() > 0) {
            boolean result = MybatisPlusUtils.deleteBatchIds(service, deleteCollection, false);
            if (!result) {
                throw new RuntimeException(String.format(MessageProperties.UPDATE_TARGET_NOT_FOUND
                        , "Mapper - " + service.getBaseMapper().getClass().getSimpleName()));
            }
        }
        return true;
    }
}
