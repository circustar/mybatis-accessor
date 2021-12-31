package com.circustar.mybatis_accessor.provider.parameter;

import java.util.List;

public class DefaultEntityProviderParam extends DefaultAbstractUpdateProviderParam implements IEntityProviderParam {
    public static final IEntityProviderParam INCLUDE_ALL_ENTITY_PROVIDER_PARAM = new DefaultEntityProviderParam(false, true , null);

    public DefaultEntityProviderParam(DefaultAbstractUpdateProviderParam another) {
        super(another.isUpdateChildrenOnly(), another.isIncludeAllChildren(), another.getUpdateChildrenNames());
    }
    public DefaultEntityProviderParam(boolean updateChildrenOnly, boolean includeAllChildren, List<String> updateChildrenNames) {
        super(updateChildrenOnly, includeAllChildren, updateChildrenNames);
    }
}
