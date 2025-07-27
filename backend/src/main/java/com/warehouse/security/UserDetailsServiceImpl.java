package com.warehouse.security;

import com.warehouse.entity.User;
import com.warehouse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 用户详情服务实现
 * 
 * @author Warehouse Team
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameWithWarehouses(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        return new UserPrincipal(user);
    }

    /**
     * 用户主体类
     */
    public static class UserPrincipal implements UserDetails {
        private final User user;

        public UserPrincipal(User user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            List<GrantedAuthority> authorities = new ArrayList<>();
            
            // 添加角色权限
            if (user.getRole() != null) {
                authorities.add(new SimpleGrantedAuthority(user.getRole().name()));
            }
            
            // 添加仓库权限
            if (user.getWarehouses() != null) {
                user.getWarehouses().forEach(warehouse -> {
                    authorities.add(new SimpleGrantedAuthority("WAREHOUSE_" + warehouse.getId()));
                });
            }
            
            return authorities;
        }

        @Override
        public String getPassword() {
            return user.getPassword();
        }

        @Override
        public String getUsername() {
            return user.getUsername();
        }

        @Override
        public boolean isAccountNonExpired() {
            return user.getAccountNonExpired() != null ? user.getAccountNonExpired() : true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return user.getAccountNonLocked() != null ? user.getAccountNonLocked() : true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return user.getCredentialsNonExpired() != null ? user.getCredentialsNonExpired() : true;
        }

        @Override
        public boolean isEnabled() {
            return user.getEnabled() != null ? user.getEnabled() : true;
        }

        // 获取用户实体
        public User getUser() {
            return user;
        }

        // 获取用户ID
        public Long getId() {
            return user.getId();
        }

        // 获取真实姓名
        public String getRealName() {
            return user.getRealName();
        }

        // 获取角色
        public String getRole() {
            return user.getRole() != null ? user.getRole().name() : null;
        }

        // 获取仓库ID列表
        public List<Long> getWarehouseIds() {
            if (user.getWarehouses() == null) {
                return new ArrayList<>();
            }
            return user.getWarehouses().stream()
                    .map(warehouse -> warehouse.getId())
                    .toList();
        }

        // 判断是否为管理员
        public boolean isAdmin() {
            return user.isAdmin();
        }

        // 判断是否为仓库管理员
        public boolean isWarehouseAdmin() {
            return user.isWarehouseAdmin();
        }

        // 判断是否有管理权限
        public boolean hasManagePermission() {
            return user.hasManagePermission();
        }

        // 判断是否可以访问指定仓库
        public boolean canAccessWarehouse(Long warehouseId) {
            return user.canAccessWarehouse(warehouseId);
        }

        // 判断是否为队长
        public boolean isTeamLeader() {
            return user.getRole() != null && user.getRole().isTeamLeader();
        }

        // 判断是否为班长
        public boolean isSquadLeader() {
            return user.getRole() != null && user.getRole().isSquadLeader();
        }

        // 判断是否有审批权限
        public boolean hasApprovalPermission() {
            return user.getRole() != null && user.getRole().hasApprovalPermission();
        }

        @Override
        public String toString() {
            return "UserPrincipal{" +
                    "id=" + user.getId() +
                    ", username='" + user.getUsername() + '\'' +
                    ", realName='" + user.getRealName() + '\'' +
                    ", role=" + user.getRole() +
                    ", enabled=" + user.getEnabled() +
                    '}';
        }
    }
}
