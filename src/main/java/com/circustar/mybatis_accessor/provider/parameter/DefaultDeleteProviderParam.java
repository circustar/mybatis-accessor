package com.circustar.mybatis_accessor.provider.parameter;

public class DefaultDeleteProviderParam extends DefaultAbstractUpdateProviderParam {

    public DefaultDeleteProviderParam(DefaultAbstractUpdateProviderParam another) {
        this(another.isUpdateChildrenOnly(), another.getUpdateChildrenNames());
    }

    public DefaultDeleteProviderParam(boolean updateChildrenOnly, String[] updateChildrenNames) {
        super(updateChildrenOnly, false, updateChildrenNames);
    }

    @Override
    public boolean isIncludeAllChildren() {
        return false;
    }
}
