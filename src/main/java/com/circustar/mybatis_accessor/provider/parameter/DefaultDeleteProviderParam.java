package com.circustar.mybatis_accessor.provider.parameter;

public class DefaultDeleteProviderParam extends DefaultAbstractUpdateProviderParam {

    public DefaultDeleteProviderParam(DefaultAbstractUpdateProviderParam another) {
        super(another.isUpdateChildrenOnly(), another.isIncludeAllChildren(), another.getUpdateChildrenNames());
    }
    public DefaultDeleteProviderParam(boolean updateChildrenOnly, boolean includeAllChildren, String[] updateChildrenNames) {
        super(updateChildrenOnly, includeAllChildren, updateChildrenNames);
    }
}
