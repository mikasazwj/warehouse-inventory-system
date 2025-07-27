package com.warehouse.enums;

/**
 * 用户角色枚举
 * 
 * @author Warehouse Team
 */
public enum UserRole {
    
    /**
     * 超级管理员 - 系统最高权限，管理所有仓库、用户、配置
     */
    ROLE_ADMIN("超级管理员", "系统最高权限，管理所有仓库、用户、配置"),

    /**
     * 库房管理员 - 管理分配的仓库，负责日常业务
     */
    WAREHOUSE_ADMIN("库房管理员", "管理分配的仓库，负责日常业务"),

    /**
     * 队长 - 负责审批班长审批过的单据
     */
    TEAM_LEADER("队长", "负责审批班长审批过的单据"),

    /**
     * 班长 - 负责初级审批
     */
    SQUAD_LEADER("班长", "负责初级审批"),

    /**
     * 普通用户 - 查看和申请部分业务，权限受限
     */
    ROLE_USER("普通用户", "查看和申请部分业务，权限受限");

    private final String displayName;
    private final String description;

    UserRole(String displayName, String description) {
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
     * 判断是否为管理员角色
     */
    public boolean isAdmin() {
        return this == ROLE_ADMIN;
    }

    /**
     * 判断是否为库房管理员角色
     */
    public boolean isWarehouseAdmin() {
        return this == WAREHOUSE_ADMIN;
    }

    /**
     * 判断是否为队长角色
     */
    public boolean isTeamLeader() {
        return this == TEAM_LEADER;
    }

    /**
     * 判断是否为班长角色
     */
    public boolean isSquadLeader() {
        return this == SQUAD_LEADER;
    }

    /**
     * 判断是否有管理权限
     */
    public boolean hasManagePermission() {
        return this == ROLE_ADMIN || this == WAREHOUSE_ADMIN;
    }

    /**
     * 判断是否有审批权限
     */
    public boolean hasApprovalPermission() {
        return this == ROLE_ADMIN || this == WAREHOUSE_ADMIN || this == TEAM_LEADER || this == SQUAD_LEADER;
    }
}
