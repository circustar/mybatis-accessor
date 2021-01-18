package com.circustar.mvcenhance.enhance.update.command;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Collection;
import java.util.Map;

public class UpdateByIdBatchCommand implements IUpdateCommand {

    private static UpdateByIdBatchCommand batchCommand = new UpdateByIdBatchCommand();
    public static UpdateByIdBatchCommand getInstance() {
        return batchCommand;
    }

    @Override
    public <T extends Collection> boolean update(IService service, T collection, Object option) throws Exception {
        return service.updateBatchById(collection);
    }
}
