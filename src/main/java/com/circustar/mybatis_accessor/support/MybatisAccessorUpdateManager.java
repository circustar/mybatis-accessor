package com.circustar.mybatis_accessor.support;

import com.circustar.common_utils.collection.CollectionUtils;
import com.circustar.mybatis_accessor.class_info.DtoClassInfo;
import com.circustar.mybatis_accessor.class_info.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.provider.parameter.DefaultEntityProviderParam;

import java.util.*;

public class MybatisAccessorUpdateManager {
    private final static ThreadLocal<List<DtoWithOption>> UPDATE_TARGET_LIST = ThreadLocal.withInitial(() -> new ArrayList<>());

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

    public void putDto(Object dto) {
        putDto(dto, DEFAULT_UPDATE_PROVIDER_PARAM);
    }

    public void putDto(Object dto, DefaultEntityProviderParam option) {
        UPDATE_TARGET_LIST.get().add(new DtoWithOption(dto, option));
    }

    public boolean isEmpty() {
        return UPDATE_TARGET_LIST.get().isEmpty();
    }

    public void submit() {
        String updateEventLogId = UUID.randomUUID().toString();
        List<DtoWithOption> dtoWithOptions = new ArrayList<>(UPDATE_TARGET_LIST.get());
        for (DtoWithOption dtoWithOption : dtoWithOptions) {
            Object dto = dtoWithOption.getDto();
            if (CollectionUtils.isCollection(dto)) {
                dto = ((Collection) dto).iterator().next();
            }
            dtoWithOption.setDtoClassInfo(dtoClassInfoHelper.getDtoClassInfo(dto.getClass()));
        }
        dtoWithOptions.stream().sorted(Comparator.comparingInt((DtoWithOption x) -> x.getDtoClassInfo().getUpdateOrder())
                .thenComparing(x -> x.getDtoClassInfo().getEntityClassInfo().getEntityClass().getSimpleName()))
                .forEach(dtoWithOption -> {
                    List dtoList = new ArrayList<>(CollectionUtils.convertToList(dtoWithOption.getDto()));
                    if (dtoList == null || dtoList.isEmpty()) {
                        return;
                    }
                    mybatisAccessorService.updateList(dtoList
                            , dtoWithOption.getParam().isIncludeAllChildren()
                            , dtoWithOption.getParam().getUpdateChildrenNames()
                            , dtoWithOption.getParam().isUpdateChildrenOnly(), updateEventLogId);
                });
        UPDATE_TARGET_LIST.get().removeAll(dtoWithOptions);
    }
}
