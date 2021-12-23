package com.circustar.mybatis_accessor.provider;

import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.provider.parameter.DefaultEntityProviderParam;
import com.circustar.mybatis_accessor.provider.parameter.IEntityProviderParam;
import com.circustar.mybatis_accessor.updateProcessor.DefaultEntityCollectionUpdateProcessor;
import com.circustar.mybatis_accessor.updateProcessor.IEntityUpdateProcessor;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.provider.command.InsertCommand;
import com.circustar.common_utils.collection.CollectionUtils;
import com.circustar.common_utils.reflection.FieldUtils;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultInsertProcessorProvider extends AbstractUpdateEntityProvider<IEntityProviderParam> {

    public DefaultInsertProcessorProvider(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public List<IEntityUpdateProcessor> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object dto, IEntityProviderParam options) {
        return this.createUpdateProcessors(relation, dtoClassInfoHelper, dto, options);
    }

    protected List<IEntityUpdateProcessor> createUpdateProcessors(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object dto, IEntityProviderParam options) {
        List<IEntityUpdateProcessor> result = new ArrayList<>();
        List dtoList = CollectionUtils.convertToList(dto);
        if(dtoList.isEmpty()) {return result;}

        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relation);
        List<String> children = null;
        if(!options.isIncludeAllChildren()) {
            children = options.getUpdateChildrenNames();
        }
        boolean updateChildrenOnly = options.isUpdateChildrenOnly();
        boolean hasChildren = false;

        List<String> topEntities = this.getTopEntities(dtoClassInfo, children, DEFAULT_DELIMITER);
        List<DtoField> dtoFields = DtoClassInfo.getDtoFieldsByName(dtoClassInfo, options.isIncludeAllChildren(), true, topEntities);
        for(Object value : dtoList) {
            if(dtoClassInfo.getVersionField() != null) {
                FieldUtils.setFieldValue(value
                        , dtoClassInfo.getVersionField().getPropertyDescriptor().getWriteMethod()
                        , dtoClassInfo.getVersionDefaultValue());
            }
            DefaultEntityCollectionUpdateProcessor defaultEntityCollectionUpdater = new DefaultEntityCollectionUpdateProcessor(relation.getServiceBean(applicationContext)
                    , InsertCommand.getInstance()
                    , null
                    , dtoClassInfo
                    , Collections.singletonList(value)
                    , true
                    , this.getUpdateChildrenFirst()
                    , updateChildrenOnly);
            for(DtoField dtoField : dtoFields) {
                Object subValue = FieldUtils.getFieldValue(value, dtoField.getPropertyDescriptor().getReadMethod());
                if(subValue == null) {continue;}
                Collection childList = CollectionUtils.convertToList(subValue);
                if(childList.isEmpty()) {continue;}
                hasChildren = true;
                IEntityProviderParam subOptions = new DefaultEntityProviderParam(false, options.isIncludeAllChildren(), this.getChildren(children
                        , dtoField.getField().getName(), DEFAULT_DELIMITER));
                defaultEntityCollectionUpdater.addSubUpdateEntities(this.createUpdateProcessors(
                        dtoField.getEntityDtoServiceRelation()
                        , dtoClassInfoHelper, childList, subOptions));
            }
            result.add(defaultEntityCollectionUpdater);
        }

        if(!hasChildren) {
            if(updateChildrenOnly) {
                return Collections.emptyList();
            } else {
                DefaultEntityCollectionUpdateProcessor defaultEntityCollectionUpdater = new DefaultEntityCollectionUpdateProcessor(relation.getServiceBean(applicationContext)
                        , InsertCommand.getInstance()
                        , null
                        , dtoClassInfo
                        , dtoList
                        , true
                        , this.getUpdateChildrenFirst()
                        , false);
                return Collections.singletonList(defaultEntityCollectionUpdater);
            }
        }

        return result;
    }

}
