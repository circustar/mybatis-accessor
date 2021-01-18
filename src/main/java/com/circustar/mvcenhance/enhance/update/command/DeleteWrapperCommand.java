package com.circustar.mvcenhance.enhance.update.command;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mvcenhance.enhance.utils.FieldUtils;
import com.circustar.mvcenhance.enhance.utils.MybatisPlusUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DeleteWrapperCommand extends DeleteByIdCommand {
    private static DeleteWrapperCommand batchCommand = new DeleteWrapperCommand();
    public static DeleteWrapperCommand getInstance() {
        return batchCommand;
    }

    public static final String PHYSIC_DELETE_FLAG = "PHYSIC_DELETE_FLAG";

    @Override
    public <T extends Collection> boolean update(IService service, T collection, Object option) throws Exception {
        List<Wrapper> physicDeleteCollection = new ArrayList();
        List<Wrapper> deleteCollection = new ArrayList();
        for(Object var1 : collection) {
            boolean physicDelete = (boolean) option;
            if(physicDelete) {
                physicDeleteCollection.add((Wrapper) var1);
            } else {
                deleteCollection.add((Wrapper) var1);
            }
        }
        for(Wrapper var2 : physicDeleteCollection) {
            boolean result = MybatisPlusUtils.delete(service, var2, true);
            if (!result) {
                return false;
            }
        }
        for(Wrapper var3 : deleteCollection) {
            boolean result = MybatisPlusUtils.delete(service, var3, false);
            if (!result) {
                return false;
            }
        }
        return true;
    }
}
