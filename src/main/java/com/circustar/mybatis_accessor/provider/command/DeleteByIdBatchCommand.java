package com.circustar.mybatis_accessor.provider.command;

import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mybatis_accessor.error.UpdateTargetNotFoundException;
import com.circustar.mybatis_accessor.utils.MybatisPlusUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DeleteByIdBatchCommand extends DeleteByIdCommand {
    private static DeleteByIdBatchCommand batchCommand = new DeleteByIdBatchCommand();
    public static DeleteByIdBatchCommand getInstance() {
        return batchCommand;
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public <T extends Collection> boolean update(IService service, T obj, Object option) throws Exception {
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
                throw  new UpdateTargetNotFoundException("updateTragetNotFound");
            }
        }
        if(deleteCollection.size() > 0) {
            boolean result = MybatisPlusUtils.deleteBatchIds(service, deleteCollection, false);
            if (!result) {
                throw  new UpdateTargetNotFoundException("updateTragetNotFound");
            }
        }
        return true;
    }
}
