package com.warehouse.entity;

import com.warehouse.enums.UserRole;
import jakarta.persistence.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户实体类
 * 
 * @author Warehouse Team
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_username", columnList = "username", unique = true),
    @Index(name = "idx_role", columnList = "role")
})
public class User extends BaseEntity {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6个字符")
    @Column(name = "password", nullable = false)
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    @Column(name = "real_name", nullable = false, length = 50)
    private String realName;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "openid", length = 100)
    private String openid;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private UserRole role = UserRole.ROLE_USER;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "account_non_expired", nullable = false)
    private Boolean accountNonExpired = true;

    @Column(name = "account_non_locked", nullable = false)
    private Boolean accountNonLocked = true;

    @Column(name = "credentials_non_expired", nullable = false)
    private Boolean credentialsNonExpired = true;

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    @Column(name = "last_login_ip", length = 50)
    private String lastLoginIp;

    @Column(name = "login_count")
    private Integer loginCount = 0;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 用户可访问的仓库
     * 超级管理员可以访问所有仓库，库房管理员只能访问分配的仓库
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_warehouses",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "warehouse_id")
    )
    private Set<Warehouse> warehouses = new HashSet<>();

    // Constructors
    public User() {
    }

    public User(String username, String password, String realName, UserRole role) {
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.role = role;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getAccountNonExpired() {
        return accountNonExpired;
    }

    public void setAccountNonExpired(Boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    public Boolean getAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(Boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public Boolean getCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    public void setCredentialsNonExpired(Boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public Integer getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(Integer loginCount) {
        this.loginCount = loginCount;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Set<Warehouse> getWarehouses() {
        return warehouses;
    }

    public void setWarehouses(Set<Warehouse> warehouses) {
        this.warehouses = warehouses;
    }

    // Business methods
    
    /**
     * 判断是否为超级管理员
     */
    public boolean isAdmin() {
        return role != null && role.isAdmin();
    }

    /**
     * 判断是否为库房管理员
     */
    public boolean isWarehouseAdmin() {
        return role != null && role.isWarehouseAdmin();
    }

    /**
     * 判断是否有管理权限
     */
    public boolean hasManagePermission() {
        return role != null && role.hasManagePermission();
    }

    /**
     * 判断是否可以访问指定仓库
     */
    public boolean canAccessWarehouse(Long warehouseId) {
        if (isAdmin()) {
            return true; // 超级管理员可以访问所有仓库
        }
        return warehouses.stream().anyMatch(w -> w.getId().equals(warehouseId));
    }

    /**
     * 添加可访问的仓库
     */
    public void addWarehouse(Warehouse warehouse) {
        this.warehouses.add(warehouse);
    }

    /**
     * 移除可访问的仓库
     */
    public void removeWarehouse(Warehouse warehouse) {
        this.warehouses.remove(warehouse);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + getId() +
                ", username='" + username + '\'' +
                ", realName='" + realName + '\'' +
                ", role=" + role +
                ", enabled=" + enabled +
                '}';
    }
}
