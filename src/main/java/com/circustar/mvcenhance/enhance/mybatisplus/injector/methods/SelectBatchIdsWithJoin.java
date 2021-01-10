package com.circustar.mvcenhance.enhance.mybatisplus.injector.methods;

public class SelectBatchIdsWithJoin extends SelectListWithJoin {
    @Override
    protected CSSqlMethod getSqlMethod() {
        return CSSqlMethod.SELECT_BATCH_BY_IDS_WITH_JOIN;
    }
}
