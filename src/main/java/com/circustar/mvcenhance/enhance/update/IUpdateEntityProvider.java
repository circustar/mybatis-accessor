package com.circustar.mvcenhance.enhance.update;

import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;
import org.springframework.validation.BindingResult;

import java.util.Arrays;
import java.util.List;

public interface IUpdateEntityProvider {
    String INSERT = "CS_INSERT";
    String INSERT_LIST = "CS_INSERT_LIST";
    String UPDATE = "CS_UPDATE";
    String SAVE_UPDATE_DELETE_LIST = "SAVE_UPDATE_DELETE_LIST";
    String UPDATE_SUB_ENTITIES = "UPDATE_SUB_ENTITIES";
    String DELETE_BY_ID = "CS_DELETE_BY_ID";
    String DELETE_LIST_BY_IDS = "CS_DELETE_LIST_BY_IDS";
//    String DELETE_LIST_BY_WRAPPER = "CS_DELETE_LIST_BY_WRAPPER";

    String AUTO_DETECT_UPDATE_ENTITY = "CS_AUTO_DETECT";

    String defineUpdateName();
    default boolean match(String[] updateNames) {
        return Arrays.stream(updateNames).anyMatch(updateName -> defineUpdateName().equals(updateName));
    };
    List<UpdateEntity> createUpdateEntities(EntityDtoServiceRelation relation, Object obj, Object... options) throws Exception;
    default <S> void validateAndSet(Object obj, BindingResult bindingResult, Object... options){};
    default void onSuccess() {};
    default void onException(Exception ex) {}
    default void onEnd() {};
}
