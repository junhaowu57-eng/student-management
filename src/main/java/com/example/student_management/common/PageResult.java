package com.example.student_management.common;

import java.util.List;

public class PageResult<T> {
    private List<T> records;
    private int total;
    private int page;
    private int size;
    private int totalPages;
    public PageResult(List<T> records,int total,int page,int size){
        this.records=records;
        this.total=total;
        this.page=page;
        this.size=size;
        this.totalPages=(total+size-1)/size;
    }
    public List<T> getRecords(){
        return records;
    }
    public int getTotal(){
        return total;
    }
    public int getPage(){
        return page;
    }
    public int getSize(){
        return size;
    }
    public int getTotalPages(){
        return totalPages;
    }
}
