package com.circustar.mvcenhance.injector.methods;

public class SelectMapsWithJoin extends SelectListWithJoin {
    @Override
    protected CSSqlMethod getSqlMethod() {
        return CSSqlMethod.SELECT_MAPS_WITH_JOIN;
    }
}