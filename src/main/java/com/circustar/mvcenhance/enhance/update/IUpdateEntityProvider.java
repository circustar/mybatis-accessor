package com.circustar.mvcenhance.enhance.update;

import com.circustar.mvcenhance.enhance.field.DtoClassInfoHelper;
import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;
import org.springframework.validation.BindingResult;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public interface IUpdateEntityProvider {
    String INSERT = "CS_INSERT";
    String UPDATE = "CS_UPDATE";
    String SAVE_UPDATE_DELETE_LIST = "SAVE_UPDATE_DELETE_LIST";
    String UPDATE_SUB_ENTITIES = "UPDATE_SUB_ENTITIES";
    String DELETE_BY_ID = "CS_DELETE_BY_ID";
    String DELETE_LIST_BY_IDS = "CS_DELETE_LIST_BY_IDS";

    String AUTO_DETECT_UPDATE_ENTITY = "CS_AUTO_DETECT";

    String defineUpdateName();
    default boolean match(String[] updateNames) {
        return Arrays.stream(updateNames).anyMatch(updateName -> defineUpdateName().equals(updateName));
    };
    List<UpdateEntity> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object obj, Map options) throws Exception;
    default <S> void validateAndSet(Object obj, BindingResult bindingResult, Map options){};
    default void onSuccess() {};
    default void onException(Exception ex) {}
    default void onEnd() {};
}
