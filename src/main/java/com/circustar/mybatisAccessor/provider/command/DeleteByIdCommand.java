package com.circustar.mybatisAccessor.provider.command;

import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mybatisAccessor.utils.FieldUtils;
import com.circustar.mybatisAccessor.utils.MybatisPlusUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Collection;

public class DeleteByIdCommand implements IUpdateCommand {
    private static DeleteByIdCommand batchCommand = new DeleteByIdCommand();
    public static DeleteByIdCommand getInstance() {
        return batchCommand;
    }

    @Override
    public <T extends Collection> boolean update(IService service, T collection, Object option) throws Exception {
        for(Object var1 : collection) {
            boolean physicDelete = (boolean) option;
            Serializable id = (Serializable) var1;
            boolean result = MybatisPlusUtils.deleteById(service, id, physicDelete);
            if(!result) return false;
        }
        return true;
    }

    protected boolean getPhysicDeleteFlag(Object obj, String deleteFlagField) {
        boolean result =false;
        try {
            if(deleteFlagField != null) {
                Object value = FieldUtils.getValueByName(obj, deleteFlagField);
                result = !StringUtils.isEmpty(value);
            }
        } catch (Exception ex) {
        }
        return result;
    }
}
