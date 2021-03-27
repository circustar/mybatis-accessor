package com.circustar.mybatis_accessor.provider.command;

import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mybatis_accessor.error.UpdateTargetNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

public class UpdateByIdBatchCommand implements IUpdateCommand {

    private static UpdateByIdBatchCommand batchCommand = new UpdateByIdBatchCommand();
    public static UpdateByIdBatchCommand getInstance() {
        return batchCommand;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <T extends Collection> boolean update(IService service, T collection, Object option) throws Exception {
        boolean result = service.updateBatchById(collection);
        if(!result) {
            throw new UpdateTargetNotFoundException("UpdateTargetNotFound");
        }
        return result;
    }
}
