package com.warehouse.service.impl;

import com.warehouse.dto.PageResponse;
import com.warehouse.dto.UserDTO;
import com.warehouse.dto.WarehouseDTO;
import com.warehouse.entity.User;
import com.warehouse.entity.Warehouse;
import com.warehouse.enums.UserRole;
import com.warehouse.exception.BusinessException;
import com.warehouse.exception.ResourceNotFoundException;
import com.warehouse.repository.UserRepository;
import com.warehouse.repository.WarehouseRepository;
import com.warehouse.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.warehouse.security.UserDetailsServiceImpl.UserPrincipal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 * 
 * @author Warehouse Team
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDTO createUser(UserDTO.CreateRequest request) {
        // 检查用户名是否存在
        if (userRepository.existsByUsernameAndDeletedFalse(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 获取当前登录用户并验证角色权限
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal currentUserPrincipal = (UserPrincipal) authentication.getPrincipal();

            // 如果当前用户是库房管理员，只能创建班长、队长、普通用户
            if (currentUserPrincipal.isWarehouseAdmin() && !currentUserPrincipal.isAdmin()) {
                UserRole requestRole = request.getRole();
                if (requestRole != UserRole.SQUAD_LEADER &&
                    requestRole != UserRole.TEAM_LEADER &&
                    requestRole != UserRole.ROLE_USER) {
                    throw new BusinessException("库房管理员只能创建班长、队长或普通用户");
                }
            }
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());

        user.setRole(request.getRole());
        user.setRemark(request.getRemark());

        // 分配仓库权限
        if (request.getWarehouseIds() != null && !request.getWarehouseIds().isEmpty()) {
            // 如果当前用户是库房管理员，验证仓库权限
            if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
                UserPrincipal currentUserPrincipal = (UserPrincipal) authentication.getPrincipal();
                if (currentUserPrincipal.isWarehouseAdmin() && !currentUserPrincipal.isAdmin()) {
                    List<Long> currentUserWarehouseIds = currentUserPrincipal.getWarehouseIds();
                    for (Long warehouseId : request.getWarehouseIds()) {
                        if (!currentUserWarehouseIds.contains(warehouseId)) {
                            throw new BusinessException("没有权限分配仓库ID为 " + warehouseId + " 的仓库");
                        }
                    }
                }
            }

            List<Warehouse> warehouses = warehouseRepository.findAllById(request.getWarehouseIds());
            user.setWarehouses(warehouses.stream().collect(Collectors.toSet()));
        }

        user = userRepository.save(user);
        return convertToDTO(user);
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO.UpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户", "id", id));

        // 验证当前用户是否有权限操作目标用户
        validateUserOperationPermission(user);

        // 特殊处理：内置admin用户的保护
        boolean isBuiltinAdmin = "admin".equals(user.getUsername()) && user.isAdmin();

        // 获取当前登录用户并验证权限
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal currentUserPrincipal = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            currentUserPrincipal = (UserPrincipal) authentication.getPrincipal();
        }

        // 更新用户信息
        if (request.getRealName() != null && !request.getRealName().trim().isEmpty()) {
            user.setRealName(request.getRealName());
        }

        // 内置admin用户不允许修改角色
        if (request.getRole() != null && !isBuiltinAdmin) {
            // 如果当前用户是库房管理员，验证角色权限
            if (currentUserPrincipal != null && currentUserPrincipal.isWarehouseAdmin() && !currentUserPrincipal.isAdmin()) {
                UserRole requestRole = request.getRole();
                if (requestRole != UserRole.SQUAD_LEADER &&
                    requestRole != UserRole.TEAM_LEADER &&
                    requestRole != UserRole.ROLE_USER) {
                    throw new BusinessException("库房管理员只能设置班长、队长或普通用户角色");
                }
            }
            user.setRole(request.getRole());
        }

        // 内置admin用户不允许禁用
        if (request.getEnabled() != null && !isBuiltinAdmin) {
            user.setEnabled(request.getEnabled());
        }

        // 更新备注
        user.setRemark(request.getRemark());

        // 更新仓库权限（内置admin不需要仓库权限限制）
        if (request.getWarehouseIds() != null && !isBuiltinAdmin) {
            // 如果当前用户是库房管理员，验证仓库权限
            if (currentUserPrincipal != null && currentUserPrincipal.isWarehouseAdmin() && !currentUserPrincipal.isAdmin()) {
                List<Long> currentUserWarehouseIds = currentUserPrincipal.getWarehouseIds();
                for (Long warehouseId : request.getWarehouseIds()) {
                    if (!currentUserWarehouseIds.contains(warehouseId)) {
                        throw new BusinessException("没有权限分配仓库ID为 " + warehouseId + " 的仓库");
                    }
                }
            }

            user.getWarehouses().clear();
            if (!request.getWarehouseIds().isEmpty()) {
                List<Warehouse> warehouses = warehouseRepository.findAllById(request.getWarehouseIds());
                user.setWarehouses(warehouses.stream().collect(Collectors.toSet()));
            }
        }

        user = userRepository.save(user);
        return convertToDTO(user);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户", "id", id));

        // 不能删除管理员账户
        if (user.isAdmin()) {
            throw new BusinessException("不能删除管理员账户");
        }

        // 验证当前用户权限
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal currentUserPrincipal = (UserPrincipal) authentication.getPrincipal();

            // 如果当前用户是库房管理员，只能删除班长、队长、普通用户
            if (currentUserPrincipal.isWarehouseAdmin() && !currentUserPrincipal.isAdmin()) {
                UserRole userRole = user.getRole();
                if (userRole != UserRole.SQUAD_LEADER &&
                    userRole != UserRole.TEAM_LEADER &&
                    userRole != UserRole.ROLE_USER) {
                    throw new BusinessException("库房管理员只能删除班长、队长或普通用户");
                }
            }
        }

        user.setDeleted(true);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> findById(Long id) {
        Optional<User> userOpt = userRepository.findById(id)
                .filter(user -> !user.getDeleted());

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // 验证当前用户是否有权限查看目标用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
                UserPrincipal currentUserPrincipal = (UserPrincipal) authentication.getPrincipal();

                // 如果当前用户是库房管理员，只能查看班长、队长、普通用户
                if (currentUserPrincipal.isWarehouseAdmin() && !currentUserPrincipal.isAdmin()) {
                    UserRole targetRole = user.getRole();
                    if (targetRole != UserRole.SQUAD_LEADER &&
                        targetRole != UserRole.TEAM_LEADER &&
                        targetRole != UserRole.ROLE_USER) {
                        return Optional.empty(); // 没有权限查看，返回空
                    }
                }
            }

            return Optional.of(convertToDTO(user));
        }

        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> findByUsername(String username) {
        return userRepository.findByUsernameAndNotDeleted(username)
                .map(this::convertToDTO);
    }



    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> findAll() {
        return userRepository.findByDeletedFalseOrderByCreatedTimeDesc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> findByRole(UserRole role) {
        return userRepository.findByRoleAndDeletedFalse(role)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> findByEnabled(Boolean enabled) {
        return userRepository.findByEnabledAndDeletedFalse(enabled)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserDTO> findByPage(String keyword, Pageable pageable) {
        Page<User> page = userRepository.findByKeyword(keyword, pageable);
        return PageResponse.of(page.map(this::convertToDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserDTO> findByPageWithFilters(String keyword, UserRole role, Boolean enabled, Pageable pageable) {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal currentUserPrincipal = (UserPrincipal) authentication.getPrincipal();

            // 如果当前用户是库房管理员，只能查看班长、队长、普通用户
            if (currentUserPrincipal.isWarehouseAdmin() && !currentUserPrincipal.isAdmin()) {
                // 如果指定了角色过滤，验证角色是否在允许范围内
                if (role != null) {
                    if (role != UserRole.SQUAD_LEADER &&
                        role != UserRole.TEAM_LEADER &&
                        role != UserRole.ROLE_USER) {
                        // 如果查询的角色不在允许范围内，返回空结果
                        return PageResponse.empty();
                    }
                }

                // 限制查询结果只包含允许的角色
                Page<User> page = userRepository.findByFiltersWithRoleRestriction(
                    keyword, role, enabled,
                    List.of(UserRole.SQUAD_LEADER, UserRole.TEAM_LEADER, UserRole.ROLE_USER),
                    pageable);
                return PageResponse.of(page.map(this::convertToDTO));
            }
        }

        // 超级管理员可以查看所有用户
        Page<User> page = userRepository.findByFilters(keyword, role, enabled, pageable);
        return PageResponse.of(page.map(this::convertToDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserDTO> findByRoleAndPage(UserRole role, String keyword, Pageable pageable) {
        Page<User> page = userRepository.findByRoleAndKeyword(role, keyword, pageable);
        return PageResponse.of(page.map(this::convertToDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> findByWarehouseId(Long warehouseId) {
        return userRepository.findByWarehouseId(warehouseId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseDTO> getUserWarehouses(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户", "id", userId));

        return user.getWarehouses().stream()
                .map(this::convertWarehouseToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> findByWarehouseIdAndRole(Long warehouseId, UserRole role) {
        return userRepository.findByWarehouseIdAndRole(warehouseId, role)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> findAllAdmins() {
        return userRepository.findAllAdmins()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void enableUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户", "id", id));

        // 验证当前用户权限
        validateUserOperationPermission(user);

        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    public void disableUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户", "id", id));

        // 不能禁用管理员账户
        if (user.isAdmin()) {
            throw new BusinessException("不能禁用管理员账户");
        }

        // 验证当前用户权限
        validateUserOperationPermission(user);

        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    public void lockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户", "id", id));
        
        // 不能锁定管理员账户
        if (user.isAdmin()) {
            throw new BusinessException("不能锁定管理员账户");
        }
        
        user.setAccountNonLocked(false);
        userRepository.save(user);
    }

    @Override
    public void unlockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户", "id", id));
        user.setAccountNonLocked(true);
        userRepository.save(user);
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户", "id", id));

        // 验证当前用户权限
        validateUserOperationPermission(user);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void changePassword(Long id, UserDTO.ChangePasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户", "id", id));

        // 验证原密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }

        // 验证新密码确认
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("新密码和确认密码不一致");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void assignWarehouses(Long userId, List<Long> warehouseIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户", "id", userId));

        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal currentUserPrincipal = (UserPrincipal) authentication.getPrincipal();

            // 如果当前用户是库房管理员，需要验证仓库权限
            if (currentUserPrincipal.isWarehouseAdmin() && !currentUserPrincipal.isAdmin()) {
                List<Long> currentUserWarehouseIds = currentUserPrincipal.getWarehouseIds();

                // 检查要分配的仓库是否都在当前用户的权限范围内
                if (warehouseIds != null && !warehouseIds.isEmpty()) {
                    for (Long warehouseId : warehouseIds) {
                        if (!currentUserWarehouseIds.contains(warehouseId)) {
                            throw new BusinessException("没有权限分配仓库ID为 " + warehouseId + " 的仓库");
                        }
                    }
                }
            }
        }

        // 清除用户原有的仓库权限
        user.getWarehouses().clear();

        // 分配新的仓库权限
        if (warehouseIds != null && !warehouseIds.isEmpty()) {
            List<Warehouse> warehouses = warehouseRepository.findAllById(warehouseIds);
            warehouses.forEach(user::addWarehouse);
        }

        userRepository.save(user);
    }

    @Override
    public void removeWarehouses(Long userId, List<Long> warehouseIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户", "id", userId));

        List<Warehouse> warehouses = warehouseRepository.findAllById(warehouseIds);
        warehouses.forEach(user::removeWarehouse);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsernameAndDeletedFalse(username);
    }



    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsernameAndIdNot(String username, Long id) {
        return userRepository.existsByUsernameAndIdNotAndDeletedFalse(username, id);
    }



    @Override
    @Transactional(readOnly = true)
    public boolean validatePassword(Long id, String password) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户", "id", id));
        return passwordEncoder.matches(password, user.getPassword());
    }

    @Override
    public void updateLastLoginInfo(Long id, String ipAddress) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户", "id", id));
        
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ipAddress);
        user.setLoginCount(user.getLoginCount() + 1);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserStatistics getUserStatistics() {
        long totalUsers = userRepository.countNotDeleted();
        long enabledUsers = userRepository.countEnabledAndNotDeleted();
        long adminUsers = userRepository.countByRoleAndNotDeleted(UserRole.ROLE_ADMIN);
        long warehouseAdminUsers = userRepository.countByRoleAndNotDeleted(UserRole.WAREHOUSE_ADMIN);
        long normalUsers = userRepository.countByRoleAndNotDeleted(UserRole.ROLE_USER);
        long neverLoginUsers = userRepository.findNeverLoginUsers().size();

        return new UserStatistics(totalUsers, enabledUsers, adminUsers, 
                                warehouseAdminUsers, normalUsers, neverLoginUsers);
    }

    /**
     * 验证用户操作权限
     */
    private void validateUserOperationPermission(User targetUser) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal currentUserPrincipal = (UserPrincipal) authentication.getPrincipal();

            // 如果当前用户是库房管理员，只能操作班长、队长、普通用户
            if (currentUserPrincipal.isWarehouseAdmin() && !currentUserPrincipal.isAdmin()) {
                UserRole targetRole = targetUser.getRole();
                if (targetRole != UserRole.SQUAD_LEADER &&
                    targetRole != UserRole.TEAM_LEADER &&
                    targetRole != UserRole.ROLE_USER) {
                    throw new BusinessException("库房管理员只能操作班长、队长或普通用户，不能操作其他库房管理员或超级管理员");
                }
            }
        }
    }

    /**
     * 转换为DTO
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);

        // 转换仓库信息
        if (user.getWarehouses() != null) {
            // 这里简化处理，实际项目中可能需要完整的WarehouseDTO
            // dto.setWarehouses(user.getWarehouses().stream()...);
        }

        return dto;
    }

    /**
     * 转换仓库为DTO
     */
    private WarehouseDTO convertWarehouseToDTO(Warehouse warehouse) {
        WarehouseDTO dto = new WarehouseDTO();
        BeanUtils.copyProperties(warehouse, dto);
        return dto;
    }

    @Override
    public void updateUserAvatar(String username, String avatarUrl) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户", "username", username));
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
    }

    @Override
    public UserDTO updateUserProfile(Long id, UserDTO.UpdateProfileRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户", "id", id));

        // 更新个人资料信息
        if (request.getRealName() != null) {
            user.setRealName(request.getRealName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }
}
