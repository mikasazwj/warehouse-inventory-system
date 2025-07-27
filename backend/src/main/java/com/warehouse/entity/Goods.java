package com.warehouse.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * 货物实体类
 * 
 * @author Warehouse Team
 */
@Entity
@Table(name = "goods", indexes = {
    @Index(name = "idx_goods_code", columnList = "code", unique = true),
    @Index(name = "idx_goods_name", columnList = "name"),
    @Index(name = "idx_goods_category", columnList = "category_id"),

    @Index(name = "idx_goods_barcode", columnList = "barcode")
})
public class Goods extends BaseEntity {

    @NotBlank(message = "货物编码不能为空")
    @Size(max = 50, message = "货物编码长度不能超过50个字符")
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @NotBlank(message = "货物名称不能为空")
    @Size(max = 200, message = "货物名称长度不能超过200个字符")
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "short_name", length = 100)
    private String shortName;

    @Column(name = "english_name", length = 200)
    private String englishName;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "specification", length = 500)
    private String specification;

    @Column(name = "brand", length = 100)
    private String brand;

    @Column(name = "barcode", length = 50)
    private String barcode;

    @NotNull(message = "货物分类不能为空")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private GoodsCategory category;



    @NotBlank(message = "计量单位不能为空")
    @Size(max = 20, message = "计量单位长度不能超过20个字符")
    @Column(name = "unit", nullable = false, length = 20)
    private String unit;

    @DecimalMin(value = "0.0", message = "重量不能为负数")
    @Column(name = "weight", precision = 10, scale = 3)
    private BigDecimal weight;

    @DecimalMin(value = "0.0", message = "体积不能为负数")
    @Column(name = "volume", precision = 10, scale = 3)
    private BigDecimal volume;



    @DecimalMin(value = "0.0", message = "最低库存不能为负数")
    @Column(name = "min_stock", precision = 15, scale = 2)
    private BigDecimal minStock = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "最高库存不能为负数")
    @Column(name = "max_stock", precision = 15, scale = 2)
    private BigDecimal maxStock;

    @DecimalMin(value = "0.0", message = "安全库存不能为负数")
    @Column(name = "safety_stock", precision = 15, scale = 2)
    private BigDecimal safetyStock = BigDecimal.ZERO;

    @Column(name = "shelf_life_days")
    private Integer shelfLifeDays;

    @Column(name = "storage_conditions", length = 500)
    private String storageConditions;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "remark", length = 1000)
    private String remark;

    // Constructors
    public Goods() {
    }

    public Goods(String code, String name, GoodsCategory category, String unit) {
        this.code = code;
        this.name = name;
        this.category = category;
        this.unit = unit;
    }

    // Getters and Setters
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

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public GoodsCategory getCategory() {
        return category;
    }

    public void setCategory(GoodsCategory category) {
        this.category = category;
    }



    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }



    public BigDecimal getMinStock() {
        return minStock;
    }

    public void setMinStock(BigDecimal minStock) {
        this.minStock = minStock;
    }

    public BigDecimal getMaxStock() {
        return maxStock;
    }

    public void setMaxStock(BigDecimal maxStock) {
        this.maxStock = maxStock;
    }

    public BigDecimal getSafetyStock() {
        return safetyStock;
    }

    public void setSafetyStock(BigDecimal safetyStock) {
        this.safetyStock = safetyStock;
    }

    public Integer getShelfLifeDays() {
        return shelfLifeDays;
    }

    public void setShelfLifeDays(Integer shelfLifeDays) {
        this.shelfLifeDays = shelfLifeDays;
    }

    public String getStorageConditions() {
        return storageConditions;
    }

    public void setStorageConditions(String storageConditions) {
        this.storageConditions = storageConditions;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    // Business methods
    
    /**
     * 获取完整的货物名称（包含型号和规格）
     */
    public String getFullName() {
        StringBuilder fullName = new StringBuilder(name);
        if (model != null && !model.trim().isEmpty()) {
            fullName.append(" ").append(model);
        }
        if (specification != null && !specification.trim().isEmpty()) {
            fullName.append(" (").append(specification).append(")");
        }
        return fullName.toString();
    }

    /**
     * 判断是否有保质期
     */
    public boolean hasShelfLife() {
        return shelfLifeDays != null && shelfLifeDays > 0;
    }

    /**
     * 判断是否设置了库存预警
     */
    public boolean hasStockWarning() {
        return minStock != null && minStock.compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public String toString() {
        return "Goods{" +
                "id=" + getId() +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", model='" + model + '\'' +
                ", unit='" + unit + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
