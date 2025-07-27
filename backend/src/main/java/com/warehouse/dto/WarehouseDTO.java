package com.warehouse.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.DecimalMin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 仓库DTO
 * 
 * @author Warehouse Team
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WarehouseDTO {

    private Long id;

    @NotBlank(message = "仓库编码不能为空")
    @Size(max = 50, message = "仓库编码长度不能超过50个字符")
    private String code;

    @NotBlank(message = "仓库名称不能为空")
    @Size(max = 100, message = "仓库名称长度不能超过100个字符")
    private String name;

    @Size(max = 500, message = "仓库地址长度不能超过500个字符")
    private String address;

    @Size(max = 50, message = "联系人长度不能超过50个字符")
    private String contactPerson;

    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    private String contactPhone;



    @DecimalMin(value = "0.0", message = "仓库面积不能为负数")
    private Double area;

    @DecimalMin(value = "0.0", message = "仓库容量不能为负数")
    private Double capacity;

    private Boolean enabled;

    private String remark;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    private String createdBy;

    private String updatedBy;

    /**
     * 仓库管理员列表
     */
    private List<UserDTO> managers;

    /**
     * 库存统计信息
     */
    private InventoryStatistics inventoryStatistics;

    // 构造函数
    public WarehouseDTO() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }



    public Double getArea() {
        return area;
    }

    public void setArea(Double area) {
        this.area = area;
    }

    public Double getCapacity() {
        return capacity;
    }

    public void setCapacity(Double capacity) {
        this.capacity = capacity;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public List<UserDTO> getManagers() {
        return managers;
    }

    public void setManagers(List<UserDTO> managers) {
        this.managers = managers;
    }

    public InventoryStatistics getInventoryStatistics() {
        return inventoryStatistics;
    }

    public void setInventoryStatistics(InventoryStatistics inventoryStatistics) {
        this.inventoryStatistics = inventoryStatistics;
    }

    // 业务方法

    /**
     * 获取状态显示文本
     */
    public String getStatusText() {
        return enabled != null && enabled ? "启用" : "禁用";
    }

    /**
     * 获取容量利用率
     */
    public Double getCapacityUtilization() {
        if (capacity == null || capacity <= 0 || inventoryStatistics == null) {
            return 0.0;
        }
        return inventoryStatistics.getTotalVolume() / capacity * 100;
    }

    @Override
    public String toString() {
        return "WarehouseDTO{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", enabled=" + enabled +
                '}';
    }

    /**
     * 库存统计信息
     */
    public static class InventoryStatistics {
        private Long totalItems;
        private Double totalQuantity;
        private Double totalValue;
        private Double totalVolume;
        private Long lowStockItems;
        private Long nearExpiryItems;

        // Constructors
        public InventoryStatistics() {}

        public InventoryStatistics(Long totalItems, Double totalQuantity, Double totalValue, 
                                 Double totalVolume, Long lowStockItems, Long nearExpiryItems) {
            this.totalItems = totalItems;
            this.totalQuantity = totalQuantity;
            this.totalValue = totalValue;
            this.totalVolume = totalVolume;
            this.lowStockItems = lowStockItems;
            this.nearExpiryItems = nearExpiryItems;
        }

        // Getters and Setters
        public Long getTotalItems() { return totalItems; }
        public void setTotalItems(Long totalItems) { this.totalItems = totalItems; }
        public Double getTotalQuantity() { return totalQuantity; }
        public void setTotalQuantity(Double totalQuantity) { this.totalQuantity = totalQuantity; }
        public Double getTotalValue() { return totalValue; }
        public void setTotalValue(Double totalValue) { this.totalValue = totalValue; }
        public Double getTotalVolume() { return totalVolume; }
        public void setTotalVolume(Double totalVolume) { this.totalVolume = totalVolume; }
        public Long getLowStockItems() { return lowStockItems; }
        public void setLowStockItems(Long lowStockItems) { this.lowStockItems = lowStockItems; }
        public Long getNearExpiryItems() { return nearExpiryItems; }
        public void setNearExpiryItems(Long nearExpiryItems) { this.nearExpiryItems = nearExpiryItems; }
    }

    /**
     * 仓库创建请求DTO
     */
    public static class CreateRequest {
        @NotBlank(message = "仓库编码不能为空")
        @Size(max = 50, message = "仓库编码长度不能超过50个字符")
        private String code;

        @NotBlank(message = "仓库名称不能为空")
        @Size(max = 100, message = "仓库名称长度不能超过100个字符")
        private String name;

        private String address;
        private String contactPerson;
        private String contactPhone;

        private Double area;
        private Double capacity;
        private String remark;
        private List<Long> managerIds;

        // Getters and Setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getContactPerson() { return contactPerson; }
        public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
        public String getContactPhone() { return contactPhone; }
        public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

        public Double getArea() { return area; }
        public void setArea(Double area) { this.area = area; }
        public Double getCapacity() { return capacity; }
        public void setCapacity(Double capacity) { this.capacity = capacity; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
        public List<Long> getManagerIds() { return managerIds; }
        public void setManagerIds(List<Long> managerIds) { this.managerIds = managerIds; }
    }

    /**
     * 仓库更新请求DTO
     */
    public static class UpdateRequest {
        @NotBlank(message = "仓库名称不能为空")
        @Size(max = 100, message = "仓库名称长度不能超过100个字符")
        private String name;

        private String address;
        private String contactPerson;
        private String contactPhone;

        private Double area;
        private Double capacity;
        private Boolean enabled;
        private String remark;
        private List<Long> managerIds;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getContactPerson() { return contactPerson; }
        public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
        public String getContactPhone() { return contactPhone; }
        public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

        public Double getArea() { return area; }
        public void setArea(Double area) { this.area = area; }
        public Double getCapacity() { return capacity; }
        public void setCapacity(Double capacity) { this.capacity = capacity; }
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
        public List<Long> getManagerIds() { return managerIds; }
        public void setManagerIds(List<Long> managerIds) { this.managerIds = managerIds; }
    }
}
