package com.circustar.mybatis_accessor.provider.parameter;

import java.util.List;

public interface IEntityProviderParam extends IProviderParam {
    boolean isUpdateChildrenOnly();

    void setUpdateChildrenOnly(boolean updateChildrenOnly);

    boolean isIncludeAllChildren();

    void setIncludeAllChildren(boolean includeAllChildren);

    List<String> getUpdateChildrenNames();

    void setUpdateChildrenNames(List<String> updateChildrenNames);
}
