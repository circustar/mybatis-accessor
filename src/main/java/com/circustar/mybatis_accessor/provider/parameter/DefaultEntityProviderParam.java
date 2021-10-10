package com.circustar.mybatis_accessor.provider.parameter;

public class DefaultEntityProviderParam extends DefaultAbstractUpdateProviderParam implements IEntityProviderParam {
    public static final IEntityProviderParam IncludeAllEntityProviderParam = new DefaultEntityProviderParam(false, true , null);

    public DefaultEntityProviderParam(DefaultAbstractUpdateProviderParam another) {
        super(another.isUpdateChildrenOnly(), another.isIncludeAllChildren(), another.getUpdateChildrenNames());
    }
    public DefaultEntityProviderParam(boolean updateChildrenOnly, boolean includeAllChildren, String[] updateChildrenNames) {
        super(updateChildrenOnly, includeAllChildren, updateChildrenNames);
    }
}