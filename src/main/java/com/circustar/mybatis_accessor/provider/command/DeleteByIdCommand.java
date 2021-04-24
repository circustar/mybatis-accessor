package com.circustar.mybatis_accessor.provider.command;

import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mybatis_accessor.error.UpdateTargetNotFoundException;
import com.circustar.mybatis_accessor.utils.MybatisPlusUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;

public class DeleteByIdCommand implements IUpdateCommand {
    private static DeleteByIdCommand batchCommand = new DeleteByIdCommand();
    public static DeleteByIdCommand getInstance() {
        return batchCommand;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <T extends Collection> boolean update(IService service, T collection, Object option) throws Exception {
        for(Object var1 : collection) {
            boolean physicDelete = (boolean) option;
            Serializable id = (Serializable) var1;
            boolean result = MybatisPlusUtils.deleteById(service, id, physicDelete);
            if(!result) {
                throw  new UpdateTargetNotFoundException("updateTragetNotFound");
            }
        }
        return true;
    }
}
