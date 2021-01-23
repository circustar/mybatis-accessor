package com.circustar.mvcenhance.injector.methods;

public class SelectObjsWithJoin extends SelectListWithJoin {
    @Override
    protected CSSqlMethod getSqlMethod() {
        return CSSqlMethod.SELECT_OBJS_WITH_JOIN;
    }
}
