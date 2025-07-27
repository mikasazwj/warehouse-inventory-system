package com.warehouse.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.warehouse.enums.UserRole;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户DTO
 * 
 * @author Warehouse Team
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {

    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;

    private String email;

    private String phone;

    private UserRole role;

    private Boolean enabled;

    private Boolean accountNonExpired;

    private Boolean accountNonLocked;

    private Boolean credentialsNonExpired;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    private Integer loginCount;

    private String avatarUrl;

    private String remark;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    private String createdBy;

    private String updatedBy;

    /**
     * 用户可访问的仓库列表
     */
    private List<WarehouseDTO> warehouses;

    // 构造函数
    public UserDTO() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public List<WarehouseDTO> getWarehouses() {
        return warehouses;
    }

    public void setWarehouses(List<WarehouseDTO> warehouses) {
        this.warehouses = warehouses;
    }

    // 业务方法

    /**
     * 获取角色显示名称
     */
    public String getRoleDisplayName() {
        return role != null ? role.getDisplayName() : null;
    }

    /**
     * 判断是否为管理员
     */
    public boolean isAdmin() {
        return role != null && role.isAdmin();
    }

    /**
     * 判断是否为仓库管理员
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
     * 获取状态显示文本
     */
    public String getStatusText() {
        if (enabled == null || !enabled) {
            return "已禁用";
        }
        if (accountNonLocked == null || !accountNonLocked) {
            return "已锁定";
        }
        if (accountNonExpired == null || !accountNonExpired) {
            return "已过期";
        }
        if (credentialsNonExpired == null || !credentialsNonExpired) {
            return "密码过期";
        }
        return "正常";
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", realName='" + realName + '\'' +
                ", role=" + role +
                ", enabled=" + enabled +
                '}';
    }

    /**
     * 用户创建请求DTO
     */
    public static class CreateRequest {
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
        private String username;

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, message = "密码长度不能少于6个字符")
        private String password;

        @NotBlank(message = "真实姓名不能为空")
        @Size(max = 50, message = "真实姓名长度不能超过50个字符")
        private String realName;


        private UserRole role = UserRole.ROLE_USER;
        private String remark;
        private List<Long> warehouseIds;

        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }

        public UserRole getRole() { return role; }
        public void setRole(UserRole role) { this.role = role; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
        public List<Long> getWarehouseIds() { return warehouseIds; }
        public void setWarehouseIds(List<Long> warehouseIds) { this.warehouseIds = warehouseIds; }
    }

    /**
     * 用户更新请求DTO
     */
    public static class UpdateRequest {
        @NotBlank(message = "真实姓名不能为空")
        @Size(max = 50, message = "真实姓名长度不能超过50个字符")
        private String realName;


        private UserRole role;
        private Boolean enabled;
        private String remark;
        private List<Long> warehouseIds;

        // Getters and Setters
        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }

        public UserRole getRole() { return role; }
        public void setRole(UserRole role) { this.role = role; }
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
        public List<Long> getWarehouseIds() { return warehouseIds; }
        public void setWarehouseIds(List<Long> warehouseIds) { this.warehouseIds = warehouseIds; }
    }

    /**
     * 个人资料更新请求DTO
     */
    public static class UpdateProfileRequest {
        @Size(max = 50, message = "真实姓名长度不能超过50个字符")
        private String realName;

        @Size(max = 100, message = "邮箱长度不能超过100个字符")
        private String email;

        @Size(max = 20, message = "手机号长度不能超过20个字符")
        private String phone;

        // Getters and Setters
        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    /**
     * 密码修改请求DTO
     */
    public static class ChangePasswordRequest {
        @NotBlank(message = "原密码不能为空")
        private String oldPassword;

        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, message = "新密码长度不能少于6个字符")
        private String newPassword;

        @NotBlank(message = "确认密码不能为空")
        private String confirmPassword;

        // Getters and Setters
        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
        public String getConfirmPassword() { return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    }
}
