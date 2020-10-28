package com.circustar.mvcenhance.common.response;

public class PageInfo<T> {
    public PageInfo() {
    }

    public PageInfo(Long total, Long size, Long current) {
        this.total = total;
        this.size = size;
        this.current = current;
    }
    private Long total;

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getCurrent() {
        return current;
    }

    public void setCurrent(Long current) {
        this.current = current;
    }

    private Long size;
    private Long current;
}
