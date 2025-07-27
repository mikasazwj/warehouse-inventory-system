package com.warehouse.enums;

/**
 * 单据状态枚举
 */
public enum OrderStatus {
    PENDING("待审批"),
    APPROVED("已审批"),
    REJECTED("已拒绝"),
    IN_PROGRESS("进行中"),
    COMPLETED("已完成"),
    CANCELLED("已取消");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
