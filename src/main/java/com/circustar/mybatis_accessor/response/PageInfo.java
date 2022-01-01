package com.circustar.mybatis_accessor.response;

import java.util.List;

public class PageInfo<T> {
    private List<T> records;
    private Long total;
    private Long size;
    private Long current;

    public PageInfo() {
        total = 0L;
        size = 0L;
        current = 0L;
    }

    public PageInfo(Long total, Long size, Long current) {
        this(total, size, current, null);
    }

    public PageInfo(Long total, Long size, Long current, List<T> records) {
        this.total = total;
        this.size = size;
        this.current = current;
        this.records = records;
    }

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

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }
}
