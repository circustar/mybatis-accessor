package com.circustar.mvcenhance.enhance.mybatisplus.injector.methods;


import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

public class SelectPageWithJoin extends SelectListWithJoin {
    public SelectPageWithJoin() {
    }

    @Override
    protected CSSqlMethod getSqlMethod() {
        return CSSqlMethod.SELECT_PAGE_WITH_JOIN;
    }
}
