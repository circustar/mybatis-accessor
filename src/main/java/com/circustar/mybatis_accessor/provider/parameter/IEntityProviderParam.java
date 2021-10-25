package com.circustar.mybatis_accessor.provider.parameter;

import java.util.List;

public interface IEntityProviderParam extends IProviderParam {
    boolean isUpdateChildrenOnly();

    IEntityProviderParam setUpdateChildrenOnly(boolean updateChildrenOnly);

    boolean isIncludeAllChildren();

    IEntityProviderParam setIncludeAllChildren(boolean includeAllChildren);

    List<String> getUpdateChildrenNames();

    IEntityProviderParam setUpdateChildrenNames(List<String> updateChildrenNames);
}
