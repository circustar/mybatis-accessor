package com.circustar.mybatis_accessor.provider.parameter;

public class DefaultInsertProviderParam extends DefaultAbstractUpdateProviderParam {

    public DefaultInsertProviderParam(DefaultAbstractUpdateProviderParam another) {
        super(another);
    }

    public DefaultInsertProviderParam(boolean updateChildrenOnly, boolean includeAllChildren, String[] updateChildrenNames) {
        super(updateChildrenOnly, includeAllChildren, updateChildrenNames);
    }
}
