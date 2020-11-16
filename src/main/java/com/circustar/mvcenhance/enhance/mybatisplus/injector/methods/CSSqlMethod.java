package com.circustar.mvcenhance.enhance.mybatisplus.injector.methods;

public enum CSSqlMethod {
    SELECT_BY_ID("selectById", "根据ID 查询一条数据", "SELECT %s FROM %s %s WHERE %s=#{%s} %s"),
    SELECT_BY_MAP("selectByMap", "根据columnMap 查询一条数据", "<script>SELECT %s FROM %s %s %s\n</script>"),
    SELECT_BATCH_BY_IDS("selectBatchIds", "根据ID集合，批量查询数据", "<script>SELECT %s FROM %s %s WHERE %s IN (%s) %s</script>"),
    SELECT_ONE("selectOne", "查询满足条件一条数据", "<script>%s SELECT %s FROM %s %s %s %s\n</script>"),
    SELECT_LIST("selectList", "查询满足条件所有数据", "<script>%s SELECT %s FROM %s %s %s %s\n</script>"),
    SELECT_PAGE("selectPage", "查询满足条件所有数据（并翻页）", "<script>%s SELECT %s %s FROM %s %s %s\n</script>"),
    SELECT_MAPS("selectMaps", "查询满足条件所有数据", "<script>%s SELECT %s FROM %s %s %s %s\n</script>"),
    SELECT_OBJS("selectObjs", "查询满足条件所有数据", "<script>%s SELECT %s FROM %s %s %s %s\n</script>");

    private final String method;
    private final String desc;
    private final String sql;

    private CSSqlMethod(String method, String desc, String sql) {
        this.method = method;
        this.desc = desc;
        this.sql = sql;
    }

    public String getMethod() {
        return this.method;
    }

    public String getDesc() {
        return this.desc;
    }

    public String getSql() {
        return this.sql;
    }
}
