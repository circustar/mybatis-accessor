package com.circustar.mybatis_accessor.listener.event.decode;

import com.circustar.mybatis_accessor.annotation.event.DecodeEvent;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.listener.ExecuteTiming;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DecodeEventModel {
    private String onExpression;
    private List<String> targetProperties;
    private List<String> matchProperties;
    private Class sourceDtoClass;
    private List<String> sourceProperties;
    private List<String> matchSourceProperties;
    private List<IUpdateCommand.UpdateType> updateTypes;
    private ExecuteTiming executeTiming;
    private DefaultDecodeEvent defaultDecodeEvent;
    private boolean errorWhenNotExist;

    private DtoClassInfo dtoClassInfo;
    private DtoClassInfo sourceDtoClassInfo;
    private List<DtoField> targetPropertyDtoFieldList;
    private List<DtoField> sourcePropertyDtoFieldList;
    private List<DtoField> matchSourcePropertyDtoFields;
    private List<DtoField> matchPropertyDtoFields;

    public DecodeEventModel(DtoClassInfo dtoClassInfo
            , String onExpression
            , String[] targetProperties
            , String[] matchProperties, Class sourceDtoClass, String[] sourceProperties
            , String[] matchSourceProperties, boolean errorWhenNotExist
            , IUpdateCommand.UpdateType[] updateTypes
            , ExecuteTiming executeTiming) {
        this.dtoClassInfo = dtoClassInfo;
        this.onExpression = onExpression;
        this.targetProperties = Arrays.asList(targetProperties);
        this.matchProperties = Arrays.asList(matchProperties);
        this.sourceDtoClass = sourceDtoClass;
        if(sourceProperties == null || sourceProperties.length == 0
                || !StringUtils.hasLength(sourceProperties[0])) {
            this.sourceProperties = this.targetProperties;
        } else {
            this.sourceProperties = Arrays.asList(sourceProperties);
        }
        this.errorWhenNotExist = errorWhenNotExist;
        if(matchSourceProperties == null || matchSourceProperties.length == 0
                || !StringUtils.hasLength(matchSourceProperties[0])) {
            this.matchSourceProperties = this.matchProperties;
        } else {
            this.matchSourceProperties = Arrays.asList(matchSourceProperties);
        }
        this.updateTypes = Arrays.asList(updateTypes);
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

    public List<DtoField> getTargetPropertyDtoFieldList() {
        if(targetPropertyDtoFieldList == null) {
            targetPropertyDtoFieldList = this.targetProperties.stream().map(x -> dtoClassInfo.getDtoField(x))
                    .collect(Collectors.toList());
        }
        return targetPropertyDtoFieldList;
    }

    public List<DtoField> getSourcePropertyDtoFieldList() {
        if(sourcePropertyDtoFieldList == null) {
            sourcePropertyDtoFieldList = this.sourceProperties.stream().map(x -> sourceDtoClassInfo.getDtoField(x))
                    .collect(Collectors.toList());
        }
        return sourcePropertyDtoFieldList;
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
        if(ExecuteTiming.DEFAULT.equals(this.executeTiming)) {
            this.executeTiming = defaultDecodeEvent.getDefaultExecuteTiming();
        }
        return this.executeTiming;
    }
}
