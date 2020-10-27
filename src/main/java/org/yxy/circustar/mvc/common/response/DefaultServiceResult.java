package org.yxy.circustar.mvc.common.response;

import org.yxy.circustar.mvc.common.error.IErrorInfo;

public class DefaultServiceResult<T> implements IServiceResult<T> {
    private T data;
    private IErrorInfo e;
    private PageInfo pageInfo;
    @Override
    public void setData(T data) {
        this.data = data;
    }

    @Override
    public void setError(IErrorInfo e) {
        this.e = e;
    }

    @Override
    public T getData() {
        return data;
    }

    @Override
    public IErrorInfo getError() {
        return e;
    }

    @Override
    public void setPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }

    @Override
    public PageInfo getPageInfo() {
        return pageInfo;
    }
}
