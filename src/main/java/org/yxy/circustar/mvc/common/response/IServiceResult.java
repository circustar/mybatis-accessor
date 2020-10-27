package org.yxy.circustar.mvc.common.response;

import org.yxy.circustar.mvc.common.error.IErrorInfo;

public interface IServiceResult<T> {
    void setData(T data);
    void setError(IErrorInfo e);
    default boolean containError() {
        return getError() != null;
    };
    T getData();
    IErrorInfo getError();
    void setPageInfo(PageInfo pageInfo);
    PageInfo getPageInfo();
}
