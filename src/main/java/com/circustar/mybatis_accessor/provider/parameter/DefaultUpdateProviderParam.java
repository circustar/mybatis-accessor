package com.circustar.mybatis_accessor.provider.parameter;

public class DefaultUpdateProviderParam extends DefaultEntityProviderParam implements IEntityProviderParam {
    private boolean delegateMode = false;
    public DefaultUpdateProviderParam(DefaultAbstractUpdateProviderParam another) {
        super(another);
    }

    public DefaultUpdateProviderParam(boolean updateChildrenOnly, boolean includeAllChildren, String[] updateChildrenNames) {
        super(updateChildrenOnly, includeAllChildren, updateChildrenNames);
    }

    public boolean isDelegateMode() {
        return delegateMode;
    }

    public DefaultUpdateProviderParam setDelegateMode(boolean delegateMode) {
        this.delegateMode = delegateMode;
        return this;
    }
}
