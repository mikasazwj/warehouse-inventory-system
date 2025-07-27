package com.warehouse.controller;

import com.warehouse.dto.ApiResponse;
import com.warehouse.dto.PageResponse;
import com.warehouse.dto.UserDTO;
import com.warehouse.dto.WarehouseDTO;
import com.warehouse.entity.User;
import com.warehouse.enums.UserRole;
import com.warehouse.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.Optional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 用户控制器
 * 
 * @author Warehouse Team
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 创建用户
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<UserDTO> createUser(@Valid @RequestBody UserDTO.CreateRequest request) {
        UserDTO user = userService.createUser(request);
        return ApiResponse.success("用户创建成功", user);
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO.UpdateRequest request) {
        UserDTO user = userService.updateUser(id, request);
        return ApiResponse.success("用户更新成功", user);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success("用户删除成功");
    }

    /**
     * 根据ID查找用户
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<UserDTO> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(user -> ApiResponse.success(user))
                .orElse(ApiResponse.notFound("用户不存在"));
    }

    /**
     * 分页查询用户
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<PageResponse<UserDTO>> getUsers(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdTime") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PageResponse<UserDTO> users = userService.findByPageWithFilters(keyword, role, enabled, pageable);
        return ApiResponse.success(users);
    }

    /**
     * 根据角色分页查询用户
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<PageResponse<UserDTO>> getUsersByRole(
            @PathVariable UserRole role,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdTime"));
        PageResponse<UserDTO> users = userService.findByRoleAndPage(role, keyword, pageable);
        return ApiResponse.success(users);
    }

    /**
     * 根据仓库查找用户
     */
    @GetMapping("/warehouse/{warehouseId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<List<UserDTO>> getUsersByWarehouse(@PathVariable Long warehouseId) {
        List<UserDTO> users = userService.findByWarehouseId(warehouseId);
        return ApiResponse.success(users);
    }

    /**
     * 获取用户的仓库权限
     */
    @GetMapping("/{id}/warehouses")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<List<WarehouseDTO>> getUserWarehouses(@PathVariable Long id) {
        List<WarehouseDTO> warehouses = userService.getUserWarehouses(id);
        return ApiResponse.success(warehouses);
    }

    /**
     * 查找所有用户
     */
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.findAll();
        return ApiResponse.success(users);
    }

    /**
     * 查找所有管理员
     */
    @GetMapping("/admins")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<List<UserDTO>> getAllAdmins() {
        List<UserDTO> admins = userService.findAllAdmins();
        return ApiResponse.success(admins);
    }

    /**
     * 启用用户
     */
    @PutMapping("/{id}/enable")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<String> enableUser(@PathVariable Long id) {
        userService.enableUser(id);
        return ApiResponse.success("用户启用成功");
    }

    /**
     * 禁用用户
     */
    @PutMapping("/{id}/disable")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<String> disableUser(@PathVariable Long id) {
        userService.disableUser(id);
        return ApiResponse.success("用户禁用成功");
    }

    /**
     * 锁定用户
     */
    @PutMapping("/{id}/lock")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<String> lockUser(@PathVariable Long id) {
        userService.lockUser(id);
        return ApiResponse.success("用户锁定成功");
    }

    /**
     * 解锁用户
     */
    @PutMapping("/{id}/unlock")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<String> unlockUser(@PathVariable Long id) {
        userService.unlockUser(id);
        return ApiResponse.success("用户解锁成功");
    }

    /**
     * 重置密码
     */
    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<String> resetPassword(@PathVariable Long id, @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request.getNewPassword());
        return ApiResponse.success("密码重置成功");
    }

    /**
     * 修改密码
     */
    @PutMapping("/{id}/change-password")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or authentication.principal.id == #id")
    public ApiResponse<String> changePassword(@PathVariable Long id, @Valid @RequestBody UserDTO.ChangePasswordRequest request) {
        userService.changePassword(id, request);
        return ApiResponse.success("密码修改成功");
    }

    /**
     * 当前用户修改密码
     */
    @PutMapping("/current/change-password")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<String> changeCurrentUserPassword(@Valid @RequestBody UserDTO.ChangePasswordRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Optional<UserDTO> userOpt = userService.findByUsername(username);
            if (!userOpt.isPresent()) {
                return ApiResponse.error("用户不存在");
            }

            userService.changePassword(userOpt.get().getId(), request);
            return ApiResponse.success("密码修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("密码修改失败: " + e.getMessage());
        }
    }

    /**
     * 分配仓库权限
     */
    @PutMapping("/{id}/assign-warehouses")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<String> assignWarehouses(@PathVariable Long id, @RequestBody AssignWarehousesRequest request) {
        userService.assignWarehouses(id, request.getWarehouseIds());
        return ApiResponse.success("仓库权限分配成功");
    }

    /**
     * 移除仓库权限
     */
    @PutMapping("/{id}/remove-warehouses")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<String> removeWarehouses(@PathVariable Long id, @RequestBody RemoveWarehousesRequest request) {
        userService.removeWarehouses(id, request.getWarehouseIds());
        return ApiResponse.success("仓库权限移除成功");
    }

    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check-username")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<Boolean> checkUsername(@RequestParam String username, @RequestParam(required = false) Long excludeId) {
        boolean exists = excludeId != null ? 
                userService.existsByUsernameAndIdNot(username, excludeId) :
                userService.existsByUsername(username);
        return ApiResponse.success(exists);
    }



    /**
     * 获取用户统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<UserService.UserStatistics> getUserStatistics() {
        UserService.UserStatistics statistics = userService.getUserStatistics();
        return ApiResponse.success(statistics);
    }

    /**
     * 获取当前用户详细信息
     */
    @GetMapping("/current/profile")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Object>> getCurrentUserProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // 查找用户
            Optional<UserDTO> userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ApiResponse.notFound("用户不存在");
            }

            UserDTO user = userOpt.get();
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", user.getId());
            profile.put("username", user.getUsername());
            profile.put("realName", user.getRealName());
            profile.put("role", user.getRole());
            profile.put("enabled", user.getEnabled());
            profile.put("createdTime", user.getCreatedTime());
            profile.put("lastLoginTime", user.getLastLoginTime());
            profile.put("loginCount", user.getLoginCount());
            profile.put("avatarUrl", user.getAvatarUrl());

            return ApiResponse.success(profile);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("获取用户信息失败: " + e.getMessage());
        }
    }



    /**
     * 更新当前用户头像
     */
    @PostMapping("/current/avatar")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<String> updateCurrentUserAvatar(@RequestParam("avatar") MultipartFile file) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // 验证文件类型
            if (file.isEmpty()) {
                return ApiResponse.error("请选择要上传的文件");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ApiResponse.error("只能上传图片文件");
            }

            // 验证文件大小（限制为2MB）
            if (file.getSize() > 2 * 1024 * 1024) {
                return ApiResponse.error("文件大小不能超过2MB");
            }

            // 创建上传目录
            String uploadDir = "uploads/avatars/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
            String filename = username + "_" + System.currentTimeMillis() + extension;
            String filePath = uploadDir + filename;

            // 保存文件
            file.transferTo(new File(filePath));

            // 更新用户头像URL
            String avatarUrl = "/" + filePath;
            userService.updateUserAvatar(username, avatarUrl);

            return ApiResponse.success(avatarUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("头像更新失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户的操作日志
     */
    @GetMapping("/current/operation-logs")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Map<String, Object>>> getCurrentUserOperationLogs(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        try {
            // 获取当前用户ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // 暂时返回模拟数据，因为操作日志功能还未完全实现
            List<Map<String, Object>> logs = new ArrayList<>();
            Map<String, Object> log1 = new HashMap<>();
            log1.put("id", 1L);
            log1.put("action", "登录系统");
            log1.put("description", "用户登录系统");
            log1.put("ipAddress", "127.0.0.1");
            log1.put("userAgent", "Chrome/120.0.0.0");
            log1.put("createdTime", "2025-07-23 16:00:00");
            logs.add(log1);

            Map<String, Object> log2 = new HashMap<>();
            log2.put("id", 2L);
            log2.put("action", "查看仪表盘");
            log2.put("description", "访问系统仪表盘");
            log2.put("ipAddress", "127.0.0.1");
            log2.put("userAgent", "Chrome/120.0.0.0");
            log2.put("createdTime", "2025-07-23 15:00:00");
            logs.add(log2);

            return ApiResponse.success(logs);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("获取操作日志失败: " + e.getMessage());
        }
    }

    // 内部DTO类

    /**
     * 重置密码请求
     */
    public static class ResetPasswordRequest {
        private String newPassword;

        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    /**
     * 分配仓库请求
     */
    public static class AssignWarehousesRequest {
        private List<Long> warehouseIds;

        public List<Long> getWarehouseIds() { return warehouseIds; }
        public void setWarehouseIds(List<Long> warehouseIds) { this.warehouseIds = warehouseIds; }
    }

    /**
     * 移除仓库请求
     */
    public static class RemoveWarehousesRequest {
        private List<Long> warehouseIds;

        public List<Long> getWarehouseIds() { return warehouseIds; }
        public void setWarehouseIds(List<Long> warehouseIds) { this.warehouseIds = warehouseIds; }
    }
}
