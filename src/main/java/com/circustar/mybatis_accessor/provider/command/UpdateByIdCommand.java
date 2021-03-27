package com.circustar.mybatis_accessor.provider.command;

import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mybatis_accessor.error.UpdateTargetNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

public class UpdateByIdCommand implements IUpdateCommand {

    private static UpdateByIdCommand batchCommand = new UpdateByIdCommand();
    public static UpdateByIdCommand getInstance() {
        return batchCommand;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <T extends Collection> boolean update(IService service, T collection, Object option) throws Exception {
        for(Object var1 : collection) {
            boolean result = service.updateById(var1);
            if(!result) {
                throw  new UpdateTargetNotFoundException("updateTragetNotFound");
            }
        }
        return true;
    }
}
