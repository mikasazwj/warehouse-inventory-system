package com.warehouse.service;

import com.warehouse.dto.PageResponse;
import com.warehouse.dto.UserDTO;
import com.warehouse.dto.WarehouseDTO;
import com.warehouse.entity.User;
import com.warehouse.enums.UserRole;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务接口
 * 
 * @author Warehouse Team
 */
public interface UserService {

    /**
     * 创建用户
     */
    UserDTO createUser(UserDTO.CreateRequest request);

    /**
     * 更新用户
     */
    UserDTO updateUser(Long id, UserDTO.UpdateRequest request);

    /**
     * 删除用户（软删除）
     */
    void deleteUser(Long id);

    /**
     * 根据ID查找用户
     */
    Optional<UserDTO> findById(Long id);

    /**
     * 根据用户名查找用户
     */
    Optional<UserDTO> findByUsername(String username);



    /**
     * 查找所有用户
     */
    List<UserDTO> findAll();

    /**
     * 根据角色查找用户
     */
    List<UserDTO> findByRole(UserRole role);

    /**
     * 根据启用状态查找用户
     */
    List<UserDTO> findByEnabled(Boolean enabled);

    /**
     * 分页查询用户
     */
    PageResponse<UserDTO> findByPage(String keyword, Pageable pageable);

    /**
     * 分页查询用户（带筛选条件）
     */
    PageResponse<UserDTO> findByPageWithFilters(String keyword, UserRole role, Boolean enabled, Pageable pageable);

    /**
     * 分页查询用户（按角色）
     */
    PageResponse<UserDTO> findByRoleAndPage(UserRole role, String keyword, Pageable pageable);

    /**
     * 根据仓库ID查找有权限的用户
     */
    List<UserDTO> findByWarehouseId(Long warehouseId);

    /**
     * 获取用户的仓库权限
     */
    List<WarehouseDTO> getUserWarehouses(Long userId);

    /**
     * 根据仓库ID和角色查找用户
     */
    List<UserDTO> findByWarehouseIdAndRole(Long warehouseId, UserRole role);

    /**
     * 查找所有管理员用户
     */
    List<UserDTO> findAllAdmins();

    /**
     * 启用用户
     */
    void enableUser(Long id);

    /**
     * 禁用用户
     */
    void disableUser(Long id);

    /**
     * 更新用户头像
     */
    void updateUserAvatar(String username, String avatarUrl);

    /**
     * 更新用户个人资料
     */
    UserDTO updateUserProfile(Long id, UserDTO.UpdateProfileRequest request);

    /**
     * 锁定用户
     */
    void lockUser(Long id);

    /**
     * 解锁用户
     */
    void unlockUser(Long id);

    /**
     * 重置密码
     */
    void resetPassword(Long id, String newPassword);

    /**
     * 修改密码
     */
    void changePassword(Long id, UserDTO.ChangePasswordRequest request);

    /**
     * 分配仓库权限
     */
    void assignWarehouses(Long userId, List<Long> warehouseIds);

    /**
     * 移除仓库权限
     */
    void removeWarehouses(Long userId, List<Long> warehouseIds);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);



    /**
     * 检查用户名是否存在（排除指定ID）
     */
    boolean existsByUsernameAndIdNot(String username, Long id);



    /**
     * 验证用户密码
     */
    boolean validatePassword(Long id, String password);

    /**
     * 更新最后登录信息
     */
    void updateLastLoginInfo(Long id, String ipAddress);

    /**
     * 获取用户统计信息
     */
    UserStatistics getUserStatistics();

    /**
     * 用户统计信息
     */
    class UserStatistics {
        private long totalUsers;
        private long enabledUsers;
        private long adminUsers;
        private long warehouseAdminUsers;
        private long normalUsers;
        private long neverLoginUsers;

        // Constructors
        public UserStatistics() {}

        public UserStatistics(long totalUsers, long enabledUsers, long adminUsers, 
                            long warehouseAdminUsers, long normalUsers, long neverLoginUsers) {
            this.totalUsers = totalUsers;
            this.enabledUsers = enabledUsers;
            this.adminUsers = adminUsers;
            this.warehouseAdminUsers = warehouseAdminUsers;
            this.normalUsers = normalUsers;
            this.neverLoginUsers = neverLoginUsers;
        }

        // Getters and Setters
        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
        public long getEnabledUsers() { return enabledUsers; }
        public void setEnabledUsers(long enabledUsers) { this.enabledUsers = enabledUsers; }
        public long getAdminUsers() { return adminUsers; }
        public void setAdminUsers(long adminUsers) { this.adminUsers = adminUsers; }
        public long getWarehouseAdminUsers() { return warehouseAdminUsers; }
        public void setWarehouseAdminUsers(long warehouseAdminUsers) { this.warehouseAdminUsers = warehouseAdminUsers; }
        public long getNormalUsers() { return normalUsers; }
        public void setNormalUsers(long normalUsers) { this.normalUsers = normalUsers; }
        public long getNeverLoginUsers() { return neverLoginUsers; }
        public void setNeverLoginUsers(long neverLoginUsers) { this.neverLoginUsers = neverLoginUsers; }
    }
}
