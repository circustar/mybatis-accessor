package com.circustar.mybatis_accessor.provider.command;

import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.common.MybatisAccessorException;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DeleteEntityCommand implements IUpdateCommand {
    private static DeleteEntityCommand batchCommand = new DeleteEntityCommand();
    public static DeleteEntityCommand getInstance() {
        return batchCommand;
    }

    @Override
    public UpdateType getUpdateType() {return UpdateType.DELETE;}

    @Override
    public <T extends Collection> boolean update(IService service, T collection, Object option) throws MybatisAccessorException {
        Map<String, Object> map = (Map<String, Object>) option;
        Method readMethod = (Method) map.get(IUpdateCommand.KEY_FIELD_READ_METHOD);
        boolean physicDelete = map.get(IUpdateCommand.PHYSIC_DELETE) != null && (boolean) map.get(IUpdateCommand.PHYSIC_DELETE);
        List idList = (List) collection.stream().map(x -> FieldUtils.getFieldValue(x, readMethod)).collect(Collectors.toList());
        return DeleteByIdBatchCommand.getInstance().update(service, idList, physicDelete);
    }
}
