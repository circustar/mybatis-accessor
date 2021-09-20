package com.circustar.mybatis_accessor.provider.parameter;

public interface IEntityProviderParam extends IProviderParam {
    boolean isUpdateChildrenOnly();

    IEntityProviderParam setUpdateChildrenOnly(boolean updateChildrenOnly);

    boolean isIncludeAllChildren();

    IEntityProviderParam setIncludeAllChildren(boolean includeAllChildren);

    String[] getUpdateChildrenNames();

    IEntityProviderParam setUpdateChildrenNames(String[] updateChildrenNames);
}
