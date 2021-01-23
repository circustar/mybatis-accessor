package com.circustar.mvcenhance.provider.command;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Collection;

public class UpdateByIdCommand implements IUpdateCommand {

    private static UpdateByIdCommand batchCommand = new UpdateByIdCommand();
    public static UpdateByIdCommand getInstance() {
        return batchCommand;
    }

    @Override
    public <T extends Collection> boolean update(IService service, T collection, Object option) throws Exception {
        for(Object var1 : collection) {
            boolean result = service.updateById(var1);
            if(!result) return false;
        }
        return true;
    }
}
