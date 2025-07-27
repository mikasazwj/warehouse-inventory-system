package com.warehouse.enums;

/**
 * 盘点类型枚举
 */
public enum StocktakeType {
    FULL("全盘"),
    PARTIAL("抽盘"),
    CYCLE("循环盘点"),
    DYNAMIC("动态盘点");

    private final String description;

    StocktakeType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
