package com.circustar.mybatis_accessor.injector.methods;

public class SelectObjsWithJoin extends SelectListWithJoin {
    private static final long serialVersionUID = -1L;
    public SelectObjsWithJoin() {
        super("SelectObjsWithJoin");
    }
    @Override
    protected CSSqlMethod getSqlMethod() {
        return CSSqlMethod.SELECT_OBJS_WITH_JOIN;
    }
}
