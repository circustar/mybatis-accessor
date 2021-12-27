package com.circustar.mybatis_accessor.injector.methods;


public class SelectPageWithJoin extends SelectListWithJoin {
    private static final long serialVersionUID = -1L;
    public SelectPageWithJoin() {
    }

    @Override
    protected CSSqlMethod getSqlMethod() {
        return CSSqlMethod.SELECT_PAGE_WITH_JOIN;
    }
}
