package com.warehouse.controller;

import com.warehouse.dto.ApiResponse;
import com.warehouse.dto.WarehouseDTO;
import com.warehouse.security.JwtTokenUtil;
import com.warehouse.security.UserDetailsServiceImpl.UserPrincipal;
import com.warehouse.service.WarehouseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证控制器
 * 
 * @author Warehouse Team
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private WarehouseService warehouseService;



    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            // 认证用户
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // 获取用户信息
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            
            // 生成token
            String accessToken = jwtTokenUtil.generateTokenWithUserInfo(
                userPrincipal.getUsername(),
                userPrincipal.getId(),
                userPrincipal.getRole(),
                userPrincipal.getWarehouseIds()
            );
            
            String refreshToken = jwtTokenUtil.generateRefreshToken(userPrincipal);

            // 构建响应
            LoginResponse response = new LoginResponse();
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setTokenType("Bearer");
            response.setExpiresIn(jwtTokenUtil.getTokenRemainingTime(accessToken));
            
            // 用户信息
            UserInfo userInfo = new UserInfo();
            userInfo.setId(userPrincipal.getId());
            userInfo.setUsername(userPrincipal.getUsername());
            userInfo.setRealName(userPrincipal.getRealName());
            userInfo.setRole(userPrincipal.getRole());
            userInfo.setWarehouseIds(userPrincipal.getWarehouseIds());
            userInfo.setIsAdmin(userPrincipal.isAdmin());
            userInfo.setIsWarehouseAdmin(userPrincipal.isWarehouseAdmin());
            userInfo.setHasManagePermission(userPrincipal.hasManagePermission());
            
            response.setUser(userInfo);

            return ApiResponse.success("登录成功", response);

        } catch (BadCredentialsException e) {
            return ApiResponse.error(401, "用户名或密码错误");
        } catch (Exception e) {
            return ApiResponse.error("登录失败: " + e.getMessage());
        }
    }

    /**
     * 刷新token
     */
    @PostMapping("/refresh")
    public ApiResponse<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        try {
            String refreshToken = request.getRefreshToken();
            
            if (!jwtTokenUtil.validateToken(refreshToken)) {
                return ApiResponse.error(401, "刷新token无效");
            }

            String username = jwtTokenUtil.getUsernameFromToken(refreshToken);
            String newAccessToken = jwtTokenUtil.refreshToken(refreshToken);
            
            RefreshResponse response = new RefreshResponse();
            response.setAccessToken(newAccessToken);
            response.setTokenType("Bearer");
            response.setExpiresIn(jwtTokenUtil.getTokenRemainingTime(newAccessToken));

            return ApiResponse.success("token刷新成功", response);

        } catch (Exception e) {
            return ApiResponse.error("token刷新失败: " + e.getMessage());
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ApiResponse<String> logout() {
        // 清除安全上下文
        SecurityContextHolder.clearContext();
        return ApiResponse.success("登出成功");
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ApiResponse<UserInfo> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof UserPrincipal)) {
                return ApiResponse.unauthorized("未登录");
            }

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            UserInfo userInfo = new UserInfo();
            userInfo.setId(userPrincipal.getId());
            userInfo.setUsername(userPrincipal.getUsername());
            userInfo.setRealName(userPrincipal.getRealName());
            userInfo.setRole(userPrincipal.getRole());
            userInfo.setWarehouseIds(userPrincipal.getWarehouseIds());
            userInfo.setIsAdmin(userPrincipal.isAdmin());
            userInfo.setIsWarehouseAdmin(userPrincipal.isWarehouseAdmin());
            userInfo.setHasManagePermission(userPrincipal.hasManagePermission());

            return ApiResponse.success(userInfo);

        } catch (Exception e) {
            return ApiResponse.error("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户权限
     */
    @GetMapping("/permissions")
    public ApiResponse<List<String>> getUserPermissions() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof UserPrincipal)) {
                return ApiResponse.unauthorized("未登录");
            }

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            // 根据用户角色返回权限列表
            List<String> permissions = new ArrayList<>();

            if (userPrincipal.isAdmin()) {
                permissions.addAll(Arrays.asList(
                    "user:read", "user:write", "user:delete",
                    "warehouse:read", "warehouse:write", "warehouse:delete",
                    "goods:read", "goods:write", "goods:delete",
                    "inventory:read", "inventory:write",
                    "inbound:read", "inbound:write", "inbound:approve",
                    "outbound:read", "outbound:write", "outbound:approve",
                    "transfer:read", "transfer:write", "transfer:approve",
                    "check:read", "check:write", "check:approve",
                    "report:read", "report:export",
                    "dashboard:read"
                ));
            } else if (userPrincipal.isWarehouseAdmin()) {
                permissions.addAll(Arrays.asList(
                    "warehouse:read", "warehouse:write",
                    "goods:read", "goods:write",
                    "inventory:read", "inventory:write",
                    "inbound:read", "inbound:write", "inbound:approve",
                    "outbound:read", "outbound:write", "outbound:approve",
                    "transfer:read", "transfer:write",
                    "check:read", "check:write",
                    "report:read",
                    "dashboard:read"
                ));
            } else if (userPrincipal.isTeamLeader()) {
                permissions.addAll(Arrays.asList(
                    "outbound:read", "outbound:write", "outbound:approve"
                ));
            } else if (userPrincipal.isSquadLeader()) {
                permissions.addAll(Arrays.asList(
                    "outbound:read", "outbound:write", "outbound:approve"
                ));
            } else {
                permissions.addAll(Arrays.asList(
                    "inventory:read",
                    "inbound:read", "inbound:write",
                    "outbound:read", "outbound:write",
                    "transfer:read", "transfer:write",
                    "check:read", "check:write",
                    "dashboard:read"
                ));
            }

            return ApiResponse.success(permissions);

        } catch (Exception e) {
            return ApiResponse.error("获取用户权限失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户仓库权限
     */
    @GetMapping("/warehouses")
    public ApiResponse<List<Map<String, Object>>> getUserWarehouses() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof UserPrincipal)) {
                return ApiResponse.unauthorized("未登录");
            }

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            List<Map<String, Object>> warehouses = new ArrayList<>();

            // 从数据库获取真实仓库数据
            List<WarehouseDTO> warehouseDTOs;
            if (userPrincipal.isAdmin()) {
                // 管理员可以访问所有仓库
                warehouseDTOs = warehouseService.findAll();
                for (WarehouseDTO warehouse : warehouseDTOs) {
                    warehouses.add(createWarehouseInfo(warehouse.getId(), warehouse.getName(), warehouse.getAddress(), true));
                }
            } else {
                // 其他用户先尝试获取分配给他们的仓库
                warehouseDTOs = warehouseService.findByUserId(userPrincipal.getId());

                // 如果用户没有分配仓库，则返回所有启用的仓库
                if (warehouseDTOs.isEmpty()) {
                    warehouseDTOs = warehouseService.findAllEnabled();
                }

                for (WarehouseDTO warehouse : warehouseDTOs) {
                    warehouses.add(createWarehouseInfo(warehouse.getId(), warehouse.getName(), warehouse.getAddress(), userPrincipal.isWarehouseAdmin()));
                }
            }

            return ApiResponse.success(warehouses);

        } catch (Exception e) {
            return ApiResponse.error("获取用户仓库权限失败: " + e.getMessage());
        }
    }

    private Map<String, Object> createWarehouseInfo(Long id, String name, String address, boolean canManage) {
        Map<String, Object> warehouse = new HashMap<>();
        warehouse.put("id", id);
        warehouse.put("name", name);
        warehouse.put("address", address);
        warehouse.put("canManage", canManage);
        return warehouse;
    }

    /**
     * 获取当前用户信息 (兼容前端)
     */
    @GetMapping("/user")
    public ApiResponse<UserInfo> getUser() {
        return getCurrentUser();
    }



    // DTO类

    /**
     * 登录请求
     */
    public static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;

        @NotBlank(message = "密码不能为空")
        private String password;

        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    /**
     * 登录响应
     */
    public static class LoginResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private Long expiresIn;
        private UserInfo user;

        // Getters and Setters
        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
        public String getTokenType() { return tokenType; }
        public void setTokenType(String tokenType) { this.tokenType = tokenType; }
        public Long getExpiresIn() { return expiresIn; }
        public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }
        public UserInfo getUser() { return user; }
        public void setUser(UserInfo user) { this.user = user; }
    }

    /**
     * 刷新请求
     */
    public static class RefreshRequest {
        @NotBlank(message = "刷新token不能为空")
        private String refreshToken;

        // Getters and Setters
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }

    /**
     * 刷新响应
     */
    public static class RefreshResponse {
        private String accessToken;
        private String tokenType;
        private Long expiresIn;

        // Getters and Setters
        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
        public String getTokenType() { return tokenType; }
        public void setTokenType(String tokenType) { this.tokenType = tokenType; }
        public Long getExpiresIn() { return expiresIn; }
        public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }
    }

    /**
     * 用户信息
     */
    public static class UserInfo {
        private Long id;
        private String username;
        private String realName;
        private String role;
        private java.util.List<Long> warehouseIds;
        private Boolean isAdmin;
        private Boolean isWarehouseAdmin;
        private Boolean hasManagePermission;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public java.util.List<Long> getWarehouseIds() { return warehouseIds; }
        public void setWarehouseIds(java.util.List<Long> warehouseIds) { this.warehouseIds = warehouseIds; }
        public Boolean getIsAdmin() { return isAdmin; }
        public void setIsAdmin(Boolean isAdmin) { this.isAdmin = isAdmin; }
        public Boolean getIsWarehouseAdmin() { return isWarehouseAdmin; }
        public void setIsWarehouseAdmin(Boolean isWarehouseAdmin) { this.isWarehouseAdmin = isWarehouseAdmin; }
        public Boolean getHasManagePermission() { return hasManagePermission; }
        public void setHasManagePermission(Boolean hasManagePermission) { this.hasManagePermission = hasManagePermission; }
    }
}
