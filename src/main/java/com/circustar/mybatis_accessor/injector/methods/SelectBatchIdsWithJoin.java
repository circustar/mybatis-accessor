package com.circustar.mybatis_accessor.injector.methods;

public class SelectBatchIdsWithJoin extends SelectListWithJoin {
    private static final long serialVersionUID = -1L;
    @Override
    protected CSSqlMethod getSqlMethod() {
        return CSSqlMethod.SELECT_BATCH_BY_IDS_WITH_JOIN;
    }
}
