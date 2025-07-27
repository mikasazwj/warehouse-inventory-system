package com.warehouse.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 分页响应DTO
 * 
 * @author Warehouse Team
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {

    /**
     * 数据列表
     */
    private List<T> content;

    /**
     * 当前页码（从0开始）
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer size;

    /**
     * 总元素数
     */
    private Long totalElements;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 是否第一页
     */
    private Boolean first;

    /**
     * 是否最后一页
     */
    private Boolean last;

    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    /**
     * 当前页元素数量
     */
    private Integer numberOfElements;

    /**
     * 是否为空
     */
    private Boolean empty;

    // 构造函数
    public PageResponse() {
    }

    public PageResponse(List<T> content, Integer page, Integer size, Long totalElements, Integer totalPages) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.numberOfElements = content != null ? content.size() : 0;
        this.empty = numberOfElements == 0;
        this.first = page == 0;
        this.last = page >= totalPages - 1;
        this.hasPrevious = page > 0;
        this.hasNext = page < totalPages - 1;
    }

    // 静态工厂方法

    /**
     * 从Spring Data的Page对象创建PageResponse
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        PageResponse<T> response = new PageResponse<>();
        response.content = page.getContent();
        response.page = page.getNumber();
        response.size = page.getSize();
        response.totalElements = page.getTotalElements();
        response.totalPages = page.getTotalPages();
        response.numberOfElements = page.getNumberOfElements();
        response.empty = page.isEmpty();
        response.first = page.isFirst();
        response.last = page.isLast();
        response.hasPrevious = page.hasPrevious();
        response.hasNext = page.hasNext();
        return response;
    }

    /**
     * 创建空的分页响应
     */
    public static <T> PageResponse<T> empty() {
        return new PageResponse<>(List.of(), 0, 0, 0L, 0);
    }

    /**
     * 创建单页响应
     */
    public static <T> PageResponse<T> single(List<T> content) {
        return new PageResponse<>(content, 0, content.size(), (long) content.size(), 1);
    }

    // Getters and Setters
    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
        this.numberOfElements = content != null ? content.size() : 0;
        this.empty = numberOfElements == 0;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
        updateFlags();
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
        updateFlags();
    }

    public Boolean getFirst() {
        return first;
    }

    public void setFirst(Boolean first) {
        this.first = first;
    }

    public Boolean getLast() {
        return last;
    }

    public void setLast(Boolean last) {
        this.last = last;
    }

    public Boolean getHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(Boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    public Boolean getHasNext() {
        return hasNext;
    }

    public void setHasNext(Boolean hasNext) {
        this.hasNext = hasNext;
    }

    public Integer getNumberOfElements() {
        return numberOfElements;
    }

    public void setNumberOfElements(Integer numberOfElements) {
        this.numberOfElements = numberOfElements;
    }

    public Boolean getEmpty() {
        return empty;
    }

    public void setEmpty(Boolean empty) {
        this.empty = empty;
    }

    // 私有方法
    private void updateFlags() {
        if (page != null && totalPages != null) {
            this.first = page == 0;
            this.last = page >= totalPages - 1;
            this.hasPrevious = page > 0;
            this.hasNext = page < totalPages - 1;
        }
    }

    @Override
    public String toString() {
        return "PageResponse{" +
                "page=" + page +
                ", size=" + size +
                ", totalElements=" + totalElements +
                ", totalPages=" + totalPages +
                ", numberOfElements=" + numberOfElements +
                ", empty=" + empty +
                ", first=" + first +
                ", last=" + last +
                '}';
    }
}
