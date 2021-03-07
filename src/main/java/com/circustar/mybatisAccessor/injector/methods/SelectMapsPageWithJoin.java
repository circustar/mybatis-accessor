package com.circustar.mybatisAccessor.injector.methods;

public class SelectMapsPageWithJoin extends SelectListWithJoin {
    @Override
    protected CSSqlMethod getSqlMethod() {
        return CSSqlMethod.SELECT_MAPS_PAGE_WITH_JOIN;
    }
}
