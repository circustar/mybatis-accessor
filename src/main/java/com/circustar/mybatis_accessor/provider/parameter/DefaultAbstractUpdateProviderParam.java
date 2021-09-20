package com.circustar.mybatis_accessor.provider.parameter;

public abstract class DefaultAbstractUpdateProviderParam implements IEntityProviderParam {
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

    @Override
    public boolean isUpdateChildrenOnly() {
        return updateChildrenOnly;
    }

    @Override
    public DefaultAbstractUpdateProviderParam setUpdateChildrenOnly(boolean updateChildrenOnly) {
        this.updateChildrenOnly = updateChildrenOnly;
        return this;
    }

    @Override
    public boolean isIncludeAllChildren() {
        return includeAllChildren;
    }

    @Override
    public DefaultAbstractUpdateProviderParam setIncludeAllChildren(boolean includeAllChildren) {
        this.includeAllChildren = includeAllChildren;
        return this;
    }

    @Override
    public String[] getUpdateChildrenNames() {
        return updateChildrenNames;
    }

    @Override
    public DefaultAbstractUpdateProviderParam setUpdateChildrenNames(String[] updateChildrenNames) {
        this.updateChildrenNames = updateChildrenNames;
        return this;
    }
}
