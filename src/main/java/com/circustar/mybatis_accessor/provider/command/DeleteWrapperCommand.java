package com.circustar.mybatis_accessor.provider.command;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mybatis_accessor.common.MessageProperties;
import com.circustar.mybatis_accessor.utils.MybatisPlusUtils;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DeleteWrapperCommand extends DeleteByIdCommand {
    private static DeleteWrapperCommand batchCommand = new DeleteWrapperCommand();
    public static DeleteWrapperCommand getInstance() {
        return batchCommand;
    }

    @Override
    public UpdateType getUpdateType() {return UpdateType.DELETE;}

    @Override
    public <T extends Collection> boolean update(IService service, T collection, Object option) {
        List<Wrapper> physicDeleteCollection = new ArrayList();
        List<Wrapper> deleteCollection = new ArrayList();
        for(Object var1 : collection) {
            boolean physicDelete = (boolean) option;
            if(physicDelete) {
                physicDeleteCollection.add((Wrapper) var1);
            } else {
                deleteCollection.add((Wrapper) var1);
            }
        }
        for(Wrapper var2 : physicDeleteCollection) {
            boolean result = MybatisPlusUtils.delete(service, var2, true);
            if (!result) {
                throw new RuntimeException(String.format(MessageProperties.UPDATE_TARGET_NOT_FOUND
                        , "Mapper - " + service.getBaseMapper().getClass().getSimpleName()));
            }
        }
        for(Wrapper var3 : deleteCollection) {
            boolean result = MybatisPlusUtils.delete(service, var3, false);
            if (!result) {
                throw new RuntimeException(String.format(MessageProperties.UPDATE_TARGET_NOT_FOUND
                        , "Mapper - " + service.getBaseMapper().getClass().getSimpleName()));
            }
        }
        return true;
    }
}
