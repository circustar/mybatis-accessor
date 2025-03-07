package com.circustar.mybatis_accessor.support;

import com.circustar.common_utils.collection.CollectionUtils;
import com.circustar.mybatis_accessor.class_info.DtoClassInfo;
import com.circustar.mybatis_accessor.class_info.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.common.MybatisAccessorException;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;
import com.circustar.mybatis_accessor.provider.parameter.DefaultEntityProviderParam;

import java.util.*;
import java.util.stream.Collectors;

public class MybatisAccessorUpdateManager {
    private final static ThreadLocal<List<DtoWithOption>> INSERT_TARGET_LIST = ThreadLocal.withInitial(() -> new ArrayList<>());
    private final static ThreadLocal<List<DtoWithOption>> UPDATE_TARGET_LIST = ThreadLocal.withInitial(() -> new ArrayList<>());
    private final static ThreadLocal<List<DtoWithOption>> SAVE_OR_UPDATE_TARGET_LIST = ThreadLocal.withInitial(() -> new ArrayList<>());

    private final static DefaultEntityProviderParam DEFAULT_UPDATE_PROVIDER_PARAM = new DefaultEntityProviderParam(false
            , true , null);

    private final MybatisAccessorService mybatisAccessorService;
    private final DtoClassInfoHelper dtoClassInfoHelper;
    public MybatisAccessorUpdateManager(MybatisAccessorService mybatisAccessorService, DtoClassInfoHelper dtoClassInfoHelper) {
        this.mybatisAccessorService = mybatisAccessorService;
        this.dtoClassInfoHelper = dtoClassInfoHelper;
    }

    public static class DtoWithOption {
        private final Object dto;
        private final DefaultEntityProviderParam param;
        private DtoClassInfo dtoClassInfo;
        public DtoWithOption(Object dto, DefaultEntityProviderParam param) {
            this.dto = dto;
            this.param = param;
        }

        public Object getDto() {
            return dto;
        }

        public DefaultEntityProviderParam getParam() {
            return param;
        }

        public DtoClassInfo getDtoClassInfo() {
            return dtoClassInfo;
        }

        public void setDtoClassInfo(DtoClassInfo dtoClassInfo) {
            this.dtoClassInfo = dtoClassInfo;
        }
    }

    public void putDto(Object dto, IUpdateCommand.UpdateType updateType) {
        putDto(dto, updateType, DEFAULT_UPDATE_PROVIDER_PARAM);
    }

    public void putDto(Object dto, IUpdateCommand.UpdateType updateType, DefaultEntityProviderParam option) {
        if(IUpdateCommand.UpdateType.UPDATE.equals(updateType)) {
            UPDATE_TARGET_LIST.get().add(new DtoWithOption(dto, option));
        } else if (IUpdateCommand.UpdateType.INSERT.equals(updateType)) {
            INSERT_TARGET_LIST.get().add(new DtoWithOption(dto, option));
        } else {
            SAVE_OR_UPDATE_TARGET_LIST.get().add(new DtoWithOption(dto, option));
        }
    }

    public boolean isEmpty() {
        return UPDATE_TARGET_LIST.get().isEmpty();
    }

    public void submit() throws MybatisAccessorException {
        String updateEventLogId = UUID.randomUUID().toString();
        submitDetail(updateEventLogId, IUpdateCommand.UpdateType.INSERT);
        submitDetail(updateEventLogId, IUpdateCommand.UpdateType.UPDATE);
        submitDetail(updateEventLogId, null);
    }

    private void submitDetail(String updateEventLogId, IUpdateCommand.UpdateType updateType) throws MybatisAccessorException {
        List<DtoWithOption> dtoWithOptions;
        if(IUpdateCommand.UpdateType.INSERT.equals(updateType)) {
            dtoWithOptions = new ArrayList<>(INSERT_TARGET_LIST.get());
        } else if(IUpdateCommand.UpdateType.UPDATE.equals(updateType)) {
            dtoWithOptions = new ArrayList<>(INSERT_TARGET_LIST.get());
        } else {
            dtoWithOptions = new ArrayList<>(SAVE_OR_UPDATE_TARGET_LIST.get());
        }
        for (DtoWithOption dtoWithOption : dtoWithOptions) {
            Object dto = dtoWithOption.getDto();
            if (CollectionUtils.isCollection(dto)) {
                dto = ((Collection) dto).iterator().next();
            }
            dtoWithOption.setDtoClassInfo(dtoClassInfoHelper.getDtoClassInfo(dto.getClass()));
        }
        final List<DtoWithOption> dtoWithOptionList = dtoWithOptions.stream().sorted(Comparator.comparingInt((DtoWithOption x) -> x.getDtoClassInfo().getUpdateOrder())
                        .thenComparing(x -> x.getDtoClassInfo().getEntityClassInfo().getEntityClass().getSimpleName()))
                .collect(Collectors.toList());
        for(DtoWithOption dtoWithOption: dtoWithOptionList) {
            List dtoList = new ArrayList<>(CollectionUtils.convertToList(dtoWithOption.getDto()));
            if (dtoList == null || dtoList.isEmpty()) {
                return;
            }
            if(IUpdateCommand.UpdateType.INSERT.equals(updateType)) {
                mybatisAccessorService.saveList(dtoList
                        , dtoWithOption.getParam().getUpdateChildrenNames()
                        , dtoWithOption.getParam().isUpdateChildrenOnly(), updateEventLogId);
                INSERT_TARGET_LIST.get().remove(dtoWithOption);
            } else if(IUpdateCommand.UpdateType.UPDATE.equals(updateType)) {
                mybatisAccessorService.updateList(dtoList
                        , dtoWithOption.getParam().getUpdateChildrenNames()
                        , dtoWithOption.getParam().isUpdateChildrenOnly(), updateEventLogId);
                UPDATE_TARGET_LIST.get().remove(dtoWithOption);
            } else {
                mybatisAccessorService.saveOrUpdateList(dtoList
                        , dtoWithOption.getParam().getUpdateChildrenNames()
                        , dtoWithOption.getParam().isUpdateChildrenOnly(), updateEventLogId);
                SAVE_OR_UPDATE_TARGET_LIST.get().remove(dtoWithOption);
            }
        }
    }
}
