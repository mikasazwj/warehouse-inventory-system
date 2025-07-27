package com.warehouse.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

/**
 * 仓库实体类
 * 
 * @author Warehouse Team
 */
@Entity
@Table(name = "warehouses", indexes = {
    @Index(name = "idx_warehouse_code", columnList = "code", unique = true),
    @Index(name = "idx_warehouse_name", columnList = "name")
})
public class Warehouse extends BaseEntity {

    @NotBlank(message = "仓库编码不能为空")
    @Size(max = 50, message = "仓库编码长度不能超过50个字符")
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @NotBlank(message = "仓库名称不能为空")
    @Size(max = 100, message = "仓库名称长度不能超过100个字符")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "contact_person", length = 50)
    private String contactPerson;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;



    @Column(name = "area")
    private Double area;

    @Column(name = "capacity")
    private Double capacity;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 仓库管理员
     * 一个仓库可以有多个管理员
     */
    @ManyToMany(mappedBy = "warehouses", fetch = FetchType.LAZY)
    private Set<User> managers = new HashSet<>();

    // Constructors
    public Warehouse() {
    }

    public Warehouse(String code, String name) {
        this.code = code;
        this.name = name;
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

    public Set<User> getManagers() {
        return managers;
    }

    public void setManagers(Set<User> managers) {
        this.managers = managers;
    }

    // Business methods
    
    /**
     * 添加管理员
     */
    public void addManager(User user) {
        this.managers.add(user);
        // 避免循环引用，不直接调用user.getWarehouses().add(this)
    }

    /**
     * 移除管理员
     */
    public void removeManager(User user) {
        this.managers.remove(user);
        // 避免循环引用，不直接调用user.getWarehouses().remove(this)
    }

    /**
     * 判断用户是否为该仓库的管理员
     */
    public boolean isManager(User user) {
        return managers.contains(user);
    }

    @Override
    public String toString() {
        return "Warehouse{" +
                "id=" + getId() +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
