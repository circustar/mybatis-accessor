package com.circustar.mybatis_accessor.provider.command;

import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mybatis_accessor.error.UpdateTargetNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

public class InsertCommand implements IUpdateCommand {

    private static InsertCommand batchCommand = new InsertCommand();
    public static InsertCommand getInstance() {
        return batchCommand;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <T extends Collection> boolean update(IService service, T collection, Object option) throws Exception {
        boolean result = service.saveBatch(collection);
        if(!result) {
            throw new UpdateTargetNotFoundException("UpdateTargetNotFound");
        }
        return result;
    }
}
