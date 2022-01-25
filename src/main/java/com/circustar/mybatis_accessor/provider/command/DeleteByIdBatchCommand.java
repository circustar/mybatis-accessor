package com.circustar.mybatis_accessor.provider.command;

import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mybatis_accessor.common.MvcEnhanceConstants;
import com.circustar.mybatis_accessor.utils.MybatisPlusUtils;

import java.util.Collection;

public class DeleteByIdBatchCommand implements IUpdateCommand {
    private static DeleteByIdBatchCommand batchCommand = new DeleteByIdBatchCommand();
    public static DeleteByIdBatchCommand getInstance() {
        return batchCommand;
    }

    @Override
    public UpdateType getUpdateType() {return UpdateType.DELETE;}

    @Override
    public <T extends Collection> boolean update(IService service, T obj, Object option) {
        boolean physicDelete = option != null && (boolean) option;
        boolean result = MybatisPlusUtils.deleteBatchIds(service, obj, physicDelete);
        if (!result) {
            throw new RuntimeException(String.format(MvcEnhanceConstants.UPDATE_TARGET_NOT_FOUND
                    , "Mapper - " + service.getBaseMapper().getClass().getSimpleName()));
        }
        return true;
    }
}
