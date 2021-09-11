package com.circustar.mybatis_accessor.support;

import com.circustar.common_utils.collection.CollectionUtils;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.provider.parameter.DefaultUpdateProviderParam;

import java.util.*;

public class MybatisAccessorUpdateManager {
    private MybatisAccessorService mybatisAccessorService;
    private DtoClassInfoHelper dtoClassInfoHelper;
    public MybatisAccessorUpdateManager(MybatisAccessorService mybatisAccessorService, DtoClassInfoHelper dtoClassInfoHelper) {
        this.mybatisAccessorService = mybatisAccessorService;
        this.dtoClassInfoHelper = dtoClassInfoHelper;
    }

    public static class DtoWithOption {
        private Object dto;
        private DefaultUpdateProviderParam param;
        private DtoClassInfo dtoClassInfo;
        public DtoWithOption(Object dto, DefaultUpdateProviderParam param) {
            this.dto = dto;
            this.param = param;
        }

        public Object getDto() {
            return dto;
        }

        public DefaultUpdateProviderParam getParam() {
            return param;
        }

        public DtoClassInfo getDtoClassInfo() {
            return dtoClassInfo;
        }

        public void setDtoClassInfo(DtoClassInfo dtoClassInfo) {
            this.dtoClassInfo = dtoClassInfo;
        }
    }

    private static List<DtoWithOption> updateTargetList = Collections.synchronizedList(new ArrayList<>());

    private final static DefaultUpdateProviderParam defaultUpdateProviderParam = new DefaultUpdateProviderParam(false
            , true , null).setDelegateMode(true);

    public void putDto(Object dto) {
        putDto(dto, defaultUpdateProviderParam);
    }

    public void putDto(Object dto, DefaultUpdateProviderParam option) {
        updateTargetList.add(new DtoWithOption(dto, option));
    }

    public synchronized void submit() {
        List<DtoWithOption> dtoWithOptions = new ArrayList<>(updateTargetList);
        for(DtoWithOption dtoWithOption : dtoWithOptions) {
            Object dto = dtoWithOption.getDto();
            if(CollectionUtils.isCollection(dto)) {
                dto = ((Collection)dto).iterator().next();
            }
            dtoWithOption.setDtoClassInfo(dtoClassInfoHelper.getDtoClassInfo(dto.getClass()));
        }
        dtoWithOptions.stream().sorted(Comparator.comparingInt((DtoWithOption x) -> x.getDtoClassInfo().getUpdateOrder())
                .thenComparing(x -> x.getDtoClassInfo().getEntityClassInfo().getEntityClass().getSimpleName()))
                .forEach(dtoWithOption -> {
            List dtoList = new ArrayList<>(CollectionUtils.convertToCollection(dtoWithOption.getDto()));
            if(dtoList == null || dtoList.isEmpty()) {
                throw new RuntimeException("update list cannot be empty");
            }
            mybatisAccessorService.updateList(dtoList
                    , dtoWithOption.getParam().isIncludeAllChildren()
                    , dtoWithOption.getParam().getUpdateChildrenNames()
                    , dtoWithOption.getParam().isUpdateChildrenOnly());
        });
        updateTargetList.removeAll(dtoWithOptions);
    }
}
