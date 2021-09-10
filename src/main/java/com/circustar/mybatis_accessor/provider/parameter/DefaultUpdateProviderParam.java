package com.circustar.mybatis_accessor.provider.parameter;

public class DefaultUpdateProviderParam extends DefaultAbstractUpdateProviderParam {
    private boolean deleteBeforeUpdate = false;
    private boolean delegateMode = false;

    public DefaultUpdateProviderParam(DefaultAbstractUpdateProviderParam another) {
        this(another, false);
    }

    public DefaultUpdateProviderParam(DefaultAbstractUpdateProviderParam another, boolean deleteBeforeUpdate) {
        super(another);
        this.deleteBeforeUpdate = deleteBeforeUpdate;
    }

    public DefaultUpdateProviderParam(boolean updateChildrenOnly, boolean includeAllChildren, String[] updateChildrenNames, boolean deleteBeforeUpdate) {
        super(updateChildrenOnly, includeAllChildren, updateChildrenNames);
        this.deleteBeforeUpdate = deleteBeforeUpdate;
    }

    public boolean isDeleteBeforeUpdate() {
        return deleteBeforeUpdate;
    }

    public DefaultUpdateProviderParam setDeleteBeforeUpdate(boolean deleteBeforeUpdate) {
        this.deleteBeforeUpdate = deleteBeforeUpdate;
        return this;
    }

    public boolean isDelegateMode() {
        return delegateMode;
    }

    public DefaultUpdateProviderParam setDelegateMode(boolean delegateMode) {
        this.delegateMode = delegateMode;
        return this;
    }
}
