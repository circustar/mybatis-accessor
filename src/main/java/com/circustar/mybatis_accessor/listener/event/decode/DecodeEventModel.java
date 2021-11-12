package com.circustar.mybatis_accessor.listener.event.decode;

import com.circustar.mybatis_accessor.annotation.event.DecodeEvent;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.listener.ExecuteTiming;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DecodeEventModel {
    private String onExpression;
    private String targetProperty;
    private List<String> matchProperties;
    private Class sourceDtoClass;
    private String sourceProperty;
    private List<String> matchSourceProperties;
    private List<IUpdateCommand.UpdateType> updateTypes;
    private ExecuteTiming executeTiming;
    private DefaultDecodeEvent defaultDecodeEvent;
    private boolean errorWhenNotExist;

    private DtoClassInfo dtoClassInfo;
    private DtoClassInfo sourceDtoClassInfo;
    private DtoField targetPropertyDtoField;
    private DtoField sourcePropertyDtoField;
    private List<DtoField> matchSourcePropertyDtoFields;
    private List<DtoField> matchPropertyDtoFields;

    public DecodeEventModel(DtoClassInfo dtoClassInfo
            , String onExpression
            , String targetProperty
            , String[] matchProperties, Class sourceDtoClass, String sourceProperty
            , String[] matchSourceProperties, boolean errorWhenNotExist
            , List<IUpdateCommand.UpdateType> updateTypes
            , ExecuteTiming executeTiming) {
        this.dtoClassInfo = dtoClassInfo;
        this.onExpression = onExpression;
        this.targetProperty = targetProperty;
        this.matchProperties = Arrays.asList(matchProperties);
        this.sourceDtoClass = sourceDtoClass;
        this.sourceProperty = sourceProperty;
        this.errorWhenNotExist = errorWhenNotExist;
        if(matchSourceProperties == null || matchSourceProperties.length == 0) {
            this.matchSourceProperties = this.matchProperties;
        } else {
            this.matchSourceProperties = Arrays.asList(matchSourceProperties);
        }
        this.updateTypes = updateTypes;
        this.executeTiming = executeTiming;
        this.defaultDecodeEvent = DefaultDecodeEvent.getInstance();
    }

    public String getOnExpression() {
        return onExpression;
    }

    public DefaultDecodeEvent getDefaultDecodeEvent() {
        return defaultDecodeEvent;
    }

    public DtoClassInfo getSourceDtoClassInfo(DtoClassInfoHelper dtoClassInfoHelper) {
        if(this.sourceDtoClassInfo == null) {
            this.sourceDtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(this.sourceDtoClass);
        }
        return this.sourceDtoClassInfo;
    }

    public DtoField getTargetPropertyDtoField() {
        if(targetPropertyDtoField == null) {
            targetPropertyDtoField = dtoClassInfo.getDtoField(this.targetProperty);
        }
        return targetPropertyDtoField;
    }

    public DtoField getSourcePropertyDtoField() {
        if(sourcePropertyDtoField == null) {
            sourcePropertyDtoField = sourceDtoClassInfo.getDtoField(this.sourceProperty);
        }
        return sourcePropertyDtoField;
    }

    public List<DtoField> getMatchSourcePropertyDtoFields() {
        if(matchSourcePropertyDtoFields == null) {
            matchSourcePropertyDtoFields = new ArrayList<DtoField>();
            for(String property : matchSourceProperties) {
                matchSourcePropertyDtoFields.add(sourceDtoClassInfo.getDtoField(property));
            }
        }
        return matchSourcePropertyDtoFields;
    }

    public List<DtoField> getMatchPropertyDtoFields() {
        if(matchPropertyDtoFields == null) {
            matchPropertyDtoFields = new ArrayList<DtoField>();
            for(String property : matchProperties) {
                matchPropertyDtoFields.add(dtoClassInfo.getDtoField(property));
            }
        }
        return matchPropertyDtoFields;
    }

    public boolean isErrorWhenNotExist() {
        return errorWhenNotExist;
    }

    public List<IUpdateCommand.UpdateType> getUpdateTypes() {
        if(this.updateTypes == null || this.updateTypes.isEmpty()) {
            this.updateTypes = Arrays.asList(defaultDecodeEvent.getDefaultUpdateTypes());
        }
        return this.updateTypes;
    }

    public ExecuteTiming getExecuteTiming() {
        if(ExecuteTiming.NONE.equals(this.executeTiming)) {
            this.executeTiming = defaultDecodeEvent.getDefaultExecuteTiming();
        }
        return this.executeTiming;
    }
}
