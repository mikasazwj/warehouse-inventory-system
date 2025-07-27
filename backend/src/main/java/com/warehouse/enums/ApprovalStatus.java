package com.warehouse.enums;

/**
 * 审批状态枚举
 * 
 * @author Warehouse Team
 */
public enum ApprovalStatus {

    /**
     * 草稿 - 刚创建，尚未提交审批
     */
    DRAFT("草稿", "刚创建，尚未提交审批"),

    /**
     * 待审批 - 已提交，等待班长审批
     */
    PENDING("待审批", "已提交，等待班长审批"),

    /**
     * 班长已审批 - 班长审批通过，等待队长审批
     */
    SQUAD_APPROVED("班长已审批", "班长审批通过，等待队长审批"),

    /**
     * 队长已审批 - 队长审批通过，等待库房管理员审批
     */
    TEAM_APPROVED("队长已审批", "队长审批通过，等待库房管理员审批"),

    /**
     * 审批中 - 正在审批流程中
     */
    IN_PROGRESS("审批中", "正在审批流程中"),

    /**
     * 已通过 - 所有审批通过，可以执行
     */
    APPROVED("已通过", "所有审批通过，可以执行"),

    /**
     * 已执行 - 业务操作已完成
     */
    EXECUTED("已执行", "业务操作已完成"),

    /**
     * 已拒绝 - 审批被拒绝
     */
    REJECTED("已拒绝", "审批被拒绝"),
    
    /**
     * 已撤销 - 申请人主动撤销
     */
    CANCELLED("已撤销", "申请人主动撤销"),
    
    /**
     * 已过期 - 超过审批时限
     */
    EXPIRED("已过期", "超过审批时限");

    private final String displayName;
    private final String description;

    ApprovalStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 判断是否可以审批
     */
    public boolean canApprove() {
        return this == PENDING || this == SQUAD_APPROVED || this == TEAM_APPROVED || this == IN_PROGRESS;
    }

    /**
     * 判断是否已完成（通过或拒绝）
     */
    public boolean isCompleted() {
        return this == APPROVED || this == EXECUTED || this == REJECTED || this == CANCELLED || this == EXPIRED;
    }

    /**
     * 判断是否可以执行业务操作
     */
    public boolean canExecute() {
        return this == APPROVED;
    }

    /**
     * 判断是否可以撤销
     */
    public boolean canCancel() {
        return this == DRAFT || this == PENDING || this == SQUAD_APPROVED || this == TEAM_APPROVED || this == IN_PROGRESS;
    }

    /**
     * 获取下一个审批状态
     */
    public ApprovalStatus getNextApprovalStatus() {
        switch (this) {
            case DRAFT:
                return PENDING;
            case PENDING:
                return SQUAD_APPROVED;
            case SQUAD_APPROVED:
                return TEAM_APPROVED;
            case TEAM_APPROVED:
                return APPROVED;
            default:
                return this;
        }
    }

    /**
     * 判断当前用户角色是否可以审批此状态
     */
    public boolean canApproveByRole(UserRole role) {
        switch (this) {
            case PENDING:
                return role == UserRole.SQUAD_LEADER || role == UserRole.ROLE_ADMIN;
            case SQUAD_APPROVED:
                return role == UserRole.TEAM_LEADER || role == UserRole.ROLE_ADMIN;
            case TEAM_APPROVED:
                return role == UserRole.WAREHOUSE_ADMIN || role == UserRole.ROLE_ADMIN;
            default:
                return false;
        }
    }
}
