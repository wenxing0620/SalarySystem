package com.salarysystem.model;

import java.util.List;

public class PageResult<T> {
    private List<T> data;
    private int pageNo;
    private int pageSize;
    private long totalCount;

    public PageResult(List<T> data, int pageNo, int pageSize, long totalCount) {
        this.data = data;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public int getTotalPages() {
        return (int) ((totalCount + pageSize - 1) / pageSize);
    }

    public boolean hasNextPage() {
        return pageNo < getTotalPages();
    }

    public boolean hasPrevPage() {
        return pageNo > 1;
    }
}

