package com.warehouse.enums;

/**
 * 业务类型枚举
 * 
 * @author Warehouse Team
 */
public enum BusinessType {
    
    // 入库类型
    PURCHASE_IN("采购入库", "采购货物入库"),
    RETURN_IN("归还入库", "归还货物入库"),
    TRANSFER_IN("调拨入库", "从其他仓库调拨入库"),
    INVENTORY_GAIN("盘盈入库", "盘点发现多余货物入库"),
    OTHER_IN("其他入库", "其他原因入库"),

    // 出库类型
    SALE_OUT("领用出库", "领用货物出库"),
    TRANSFER_OUT("调拨出库", "调拨到其他仓库出库"),
    INVENTORY_LOSS("盘亏出库", "盘点发现缺失货物出库"),
    DAMAGE_OUT("借用出库", "借用货物出库"),
    OTHER_OUT("其他出库", "其他原因出库"),
    
    // 调拨类型
    WAREHOUSE_TRANSFER("仓库调拨", "仓库间货物调拨"),
    
    // 盘点类型
    REGULAR_CHECK("定期盘点", "定期库存盘点"),
    SPOT_CHECK("抽查盘点", "随机抽查盘点"),
    ANNUAL_CHECK("年度盘点", "年度全面盘点");

    private final String displayName;
    private final String description;

    BusinessType(String displayName, String description) {
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
     * 判断是否为入库类型
     */
    public boolean isInbound() {
        return this == PURCHASE_IN || this == RETURN_IN || 
               this == TRANSFER_IN || this == INVENTORY_GAIN || this == OTHER_IN;
    }

    /**
     * 判断是否为出库类型
     */
    public boolean isOutbound() {
        return this == SALE_OUT || this == TRANSFER_OUT || 
               this == INVENTORY_LOSS || this == DAMAGE_OUT || this == OTHER_OUT;
    }

    /**
     * 判断是否为调拨类型
     */
    public boolean isTransfer() {
        return this == WAREHOUSE_TRANSFER;
    }

    /**
     * 判断是否为盘点类型
     */
    public boolean isInventoryCheck() {
        return this == REGULAR_CHECK || this == SPOT_CHECK || this == ANNUAL_CHECK;
    }
}
