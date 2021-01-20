package com.circustar.mvcenhance.enhance.update.command;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Collection;

public class InsertCommand implements IUpdateCommand {

    private static InsertCommand batchCommand = new InsertCommand();
    public static InsertCommand getInstance() {
        return batchCommand;
    }

    @Override
    public <T extends Collection> boolean update(IService service, T collection, Object option) throws Exception {
        return service.saveBatch(collection);
    }
}
