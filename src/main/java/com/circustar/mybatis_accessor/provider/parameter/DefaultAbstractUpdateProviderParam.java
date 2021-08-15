package com.circustar.mybatis_accessor.provider.parameter;

public abstract class DefaultAbstractUpdateProviderParam implements IProviderParam {
    private boolean updateChildrenOnly = false;
    private boolean includeAllChildren = false;
    private String[] updateChildrenNames = null;

    public DefaultAbstractUpdateProviderParam(DefaultAbstractUpdateProviderParam another) {
        this(another.isUpdateChildrenOnly(), another.isIncludeAllChildren(), another.getUpdateChildrenNames());
    }

    public DefaultAbstractUpdateProviderParam(boolean updateChildrenOnly, boolean includeAllChildren, String[] updateChildrenNames) {
        this.updateChildrenOnly = updateChildrenOnly;
        this.includeAllChildren = includeAllChildren;
        this.updateChildrenNames = updateChildrenNames;
    }

    public boolean isUpdateChildrenOnly() {
        return updateChildrenOnly;
    }

    public DefaultAbstractUpdateProviderParam setUpdateChildrenOnly(boolean updateChildrenOnly) {
        this.updateChildrenOnly = updateChildrenOnly;
        return this;
    }

    public boolean isIncludeAllChildren() {
        return includeAllChildren;
    }

    public DefaultAbstractUpdateProviderParam setIncludeAllChildren(boolean includeAllChildren) {
        this.includeAllChildren = includeAllChildren;
        return this;
    }

    public String[] getUpdateChildrenNames() {
        return updateChildrenNames;
    }

    public DefaultAbstractUpdateProviderParam setUpdateChildrenNames(String[] updateChildrenNames) {
        this.updateChildrenNames = updateChildrenNames;
        return this;
    }
}
