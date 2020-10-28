package com.circustar.mvcenhance.common.response;

import com.circustar.mvcenhance.common.error.IErrorInfo;

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
