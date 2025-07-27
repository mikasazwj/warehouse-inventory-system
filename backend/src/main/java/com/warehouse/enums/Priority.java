package com.warehouse.enums;

/**
 * 优先级枚举
 * 
 * @author Warehouse Team
 */
public enum Priority {
    
    /**
     * 普通
     */
    NORMAL("普通", "普通优先级"),
    
    /**
     * 紧急
     */
    URGENT("紧急", "紧急优先级"),
    
    /**
     * 特急
     */
    CRITICAL("特急", "特急优先级");

    private final String displayName;
    private final String description;

    Priority(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
