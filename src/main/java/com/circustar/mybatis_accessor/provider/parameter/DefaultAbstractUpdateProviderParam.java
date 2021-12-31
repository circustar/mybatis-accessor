package com.circustar.mybatis_accessor.provider.parameter;

import java.util.List;

public abstract class DefaultAbstractUpdateProviderParam implements IEntityProviderParam {
    private boolean updateChildrenOnly;
    private boolean includeAllChildren;
    private List<String> updateChildrenNames;

    public DefaultAbstractUpdateProviderParam(DefaultAbstractUpdateProviderParam another) {
        this(another.isUpdateChildrenOnly(), another.isIncludeAllChildren(), another.getUpdateChildrenNames());
    }

    public DefaultAbstractUpdateProviderParam(boolean updateChildrenOnly, boolean includeAllChildren
            , List<String> updateChildrenNames) {
        this.updateChildrenOnly = updateChildrenOnly;
        this.includeAllChildren = includeAllChildren;
        this.updateChildrenNames = updateChildrenNames;
    }

    @Override
    public boolean isUpdateChildrenOnly() {
        return updateChildrenOnly;
    }

    @Override
    public void setUpdateChildrenOnly(boolean updateChildrenOnly) {
        this.updateChildrenOnly = updateChildrenOnly;
    }

    @Override
    public boolean isIncludeAllChildren() {
        return includeAllChildren;
    }

    @Override
    public void setIncludeAllChildren(boolean includeAllChildren) {
        this.includeAllChildren = includeAllChildren;
    }

    @Override
    public List<String> getUpdateChildrenNames() {
        return updateChildrenNames;
    }

    @Override
    public void setUpdateChildrenNames(List<String> updateChildrenNames) {
        this.updateChildrenNames = updateChildrenNames;
    }
}
