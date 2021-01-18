package com.circustar.mvcenhance.enhance.update.command;

import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mvcenhance.enhance.utils.FieldUtils;
import com.circustar.mvcenhance.enhance.utils.MybatisPlusUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DeleteByIdBatchCommand extends DeleteByIdCommand {
    private static DeleteByIdBatchCommand batchCommand = new DeleteByIdBatchCommand();
    public static DeleteByIdBatchCommand getInstance() {
        return batchCommand;
    }
    @Override
    public <T extends Collection> boolean update(IService service, T obj, Object option) throws Exception {
        List physicDeleteCollection = new ArrayList();
        List deleteCollection = new ArrayList();
        for(Object var1 : obj) {
            boolean physicDelete = (boolean) option;
            Serializable id = (Serializable) var1;
            if(physicDelete) {
                physicDeleteCollection.add(id);
            } else {
                deleteCollection.add(id);
            }
        }

        if(physicDeleteCollection.size() > 0) {
            boolean result = MybatisPlusUtils.deleteBatchIds(service, physicDeleteCollection, true);
            if (!result) {
                return false;
            }
        }
        if(deleteCollection.size() > 0) {
            boolean result = MybatisPlusUtils.deleteBatchIds(service, deleteCollection, false);
            if (!result) {
                return false;
            }
        }
        return true;
    }
}
