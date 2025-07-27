package com.warehouse.repository;

import com.warehouse.entity.User;
import com.warehouse.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问接口
 * 
 * @author Warehouse Team
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据用户名查找用户（忽略删除状态）
     */
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.deleted = false")
    Optional<User> findByUsernameAndNotDeleted(@Param("username") String username);

    /**
     * 根据用户名查找用户（预加载仓库信息）
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.warehouses WHERE u.username = :username AND u.deleted = false")
    Optional<User> findByUsernameWithWarehouses(@Param("username") String username);

    /**
     * 根据用户名查找用户（未删除）
     */
    Optional<User> findByUsernameAndDeletedFalse(String username);



    /**
     * 查找所有未删除的用户
     */
    List<User> findByDeletedFalse();

    /**
     * 根据启用状态统计用户数量（未删除）
     */
    long countByEnabledAndDeletedFalse(Boolean enabled);



    /**
     * 根据角色查找用户
     */
    List<User> findByRoleAndDeletedFalse(UserRole role);

    /**
     * 根据启用状态查找用户
     */
    List<User> findByEnabledAndDeletedFalse(Boolean enabled);

    /**
     * 根据角色和启用状态查找用户
     */
    List<User> findByRoleAndEnabledAndDeletedFalse(UserRole role, Boolean enabled);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsernameAndDeletedFalse(String username);



    /**
     * 检查用户名是否存在（排除指定ID）
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND u.id != :id AND u.deleted = false")
    boolean existsByUsernameAndIdNotAndDeletedFalse(@Param("username") String username, @Param("id") Long id);



    /**
     * 根据仓库ID查找有权限的用户
     */
    @Query("SELECT u FROM User u JOIN u.warehouses w WHERE w.id = :warehouseId AND u.deleted = false AND u.enabled = true")
    List<User> findByWarehouseId(@Param("warehouseId") Long warehouseId);

    /**
     * 根据仓库ID和角色查找用户
     */
    @Query("SELECT u FROM User u JOIN u.warehouses w WHERE w.id = :warehouseId AND u.role = :role AND u.deleted = false AND u.enabled = true")
    List<User> findByWarehouseIdAndRole(@Param("warehouseId") Long warehouseId, @Param("role") UserRole role);

    /**
     * 查找所有管理员用户
     */
    @Query("SELECT u FROM User u WHERE u.role IN ('ROLE_ADMIN', 'WAREHOUSE_ADMIN') AND u.deleted = false AND u.enabled = true")
    List<User> findAllAdmins();

    /**
     * 分页查询用户（支持关键字搜索）
     */
    @Query("SELECT u FROM User u WHERE u.deleted = false AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "u.username LIKE %:keyword% OR u.realName LIKE %:keyword%)")
    Page<User> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 分页查询用户（支持多条件筛选）
     */
    @Query("SELECT u FROM User u WHERE u.deleted = false AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "u.username LIKE %:keyword% OR u.realName LIKE %:keyword%) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:enabled IS NULL OR u.enabled = :enabled)")
    Page<User> findByFilters(@Param("keyword") String keyword,
                           @Param("role") UserRole role,
                           @Param("enabled") Boolean enabled,
                           Pageable pageable);

    /**
     * 分页查询用户（支持角色和关键字搜索）
     */
    @Query("SELECT u FROM User u WHERE u.deleted = false AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "u.username LIKE %:keyword% OR u.realName LIKE %:keyword%)")
    Page<User> findByRoleAndKeyword(@Param("role") UserRole role, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 分页查询用户（支持多条件筛选和角色限制）
     */
    @Query("SELECT u FROM User u WHERE u.deleted = false AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "u.username LIKE %:keyword% OR u.realName LIKE %:keyword%) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:enabled IS NULL OR u.enabled = :enabled) AND " +
           "u.role IN :allowedRoles")
    Page<User> findByFiltersWithRoleRestriction(@Param("keyword") String keyword,
                                              @Param("role") UserRole role,
                                              @Param("enabled") Boolean enabled,
                                              @Param("allowedRoles") List<UserRole> allowedRoles,
                                              Pageable pageable);

    /**
     * 统计用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.deleted = false")
    long countNotDeleted();

    /**
     * 根据角色统计用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.deleted = false")
    long countByRoleAndNotDeleted(@Param("role") UserRole role);

    /**
     * 统计启用的用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true AND u.deleted = false")
    long countEnabledAndNotDeleted();

    /**
     * 查找最近登录的用户
     */
    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.lastLoginTime IS NOT NULL ORDER BY u.lastLoginTime DESC")
    List<User> findRecentLoginUsers(Pageable pageable);

    /**
     * 查找从未登录的用户
     */
    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.lastLoginTime IS NULL")
    List<User> findNeverLoginUsers();

    /**
     * 根据创建时间范围查找用户
     */
    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.createdTime BETWEEN :startTime AND :endTime")
    List<User> findByCreatedTimeBetween(@Param("startTime") java.time.LocalDateTime startTime,
                                       @Param("endTime") java.time.LocalDateTime endTime);

    /**
     * 查找所有未删除的用户（按创建时间倒序）
     */
    List<User> findByDeletedFalseOrderByCreatedTimeDesc();

    // ===== 为Service层添加兼容方法 =====



    /**
     * 查找所有用户 (兼容方法)
     */
    default List<User> findAll() {
        return findByDeletedFalse();
    }

    /**
     * 根据角色查找用户 (兼容方法)
     */
    default List<User> findByRole(UserRole role) {
        return findByRoleAndDeletedFalse(role);
    }

    /**
     * 根据状态查找用户 (兼容方法)
     */
    default List<User> findByEnabled(boolean enabled) {
        return findByEnabledAndDeletedFalse(enabled);
    }

    /**
     * 查找启用的用户 (兼容方法)
     */
    default List<User> findEnabledUsers() {
        return findByEnabledAndDeletedFalse(true);
    }

    /**
     * 查找禁用的用户 (兼容方法)
     */
    default List<User> findDisabledUsers() {
        return findByEnabledAndDeletedFalse(false);
    }

    /**
     * 根据关键字搜索用户 (兼容方法)
     */
    default List<User> findByKeywordList(String keyword) {
        return findByKeyword(keyword, org.springframework.data.domain.Pageable.unpaged()).getContent();
    }

    /**
     * 检查用户名是否存在 (兼容方法)
     */
    default boolean existsByUsername(String username) {
        return findByUsernameAndDeletedFalse(username).isPresent();
    }



    /**
     * 统计所有用户数量 (兼容方法)
     */
    default long countAll() {
        return countNotDeleted();
    }

    /**
     * 统计启用用户数量 (兼容方法)
     */
    default long countEnabled() {
        return countByEnabledAndDeletedFalse(true);
    }

    /**
     * 统计禁用用户数量 (兼容方法)
     */
    default long countDisabled() {
        return countByEnabledAndDeletedFalse(false);
    }

    /**
     * 修复用户账号状态 - 将所有用户设置为未锁定状态
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET account_non_locked = 1, account_non_expired = 1, credentials_non_expired = 1, enabled = 1 WHERE deleted = 0", nativeQuery = true)
    int updateUserAccountStatus();
}
