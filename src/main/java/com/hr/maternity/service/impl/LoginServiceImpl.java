package com.hr.maternity.service.impl;

import com.hr.maternity.dto.LoginRequest;
import com.hr.maternity.dto.LoginResponse;
import com.hr.maternity.entity.Role;
import com.hr.maternity.entity.User;
import com.hr.maternity.entity.UserRole;
import com.hr.maternity.ldap.LdapAuthResult;
import com.hr.maternity.ldap.LdapAuthService;
import com.hr.maternity.ldap.LdapUserInfo;
import com.hr.maternity.repository.RoleRepository;
import com.hr.maternity.repository.UserRepository;
import com.hr.maternity.repository.UserRoleRepository;
import com.hr.maternity.service.JwtTokenService;
import com.hr.maternity.service.LoginService;
import com.hr.maternity.util.RSAUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private static final String ROLE_HR_ADMIN = "HR_Admin";
    private static final String ROLE_EMPLOYEE = "Employee";
    private static final String NORMALIZED_ROLE_HR_ADMIN = "HR_ADMIN";
    private static final String NORMALIZED_ROLE_EMPLOYEE = "EMPLOYEE";
    private static final int MIN_PASSWORD_LENGTH = 6;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final JwtTokenService jwtTokenService;
    private final LdapAuthService ldapAuthService;
    private final RSAUtil rsaUtil;

    @Value("${login.ldap.enabled:false}")
    private boolean ldapEnabled;

    @Value("${encryption.rsa-enabled:false}")
    private boolean rsaEnabled;

    @Value("${user.role.hr-department:CHN E2P Human Resources}")
    private String hrDepartment;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("开始用户登录验证，用户名: {}", loginRequest.getUsername());

        if (rsaEnabled) {
            String decryptedPassword = rsaUtil.decryptLogin(loginRequest);
            loginRequest.setPassword(decryptedPassword);
            log.debug("RSA密码解密成功，用户名: {}", loginRequest.getUsername());
        }

        LdapAuthResult ldapResult = authenticateUser(loginRequest);
        User user = loadAndValidateUser(loginRequest.getUsername(), ldapResult);
        LoginResponse loginResponse = buildLoginResponse(user);

        log.info("用户登录成功，用户名: {}, 用户ID: {}", loginRequest.getUsername(), user.getId());
        return loginResponse;
    }

    @Override
    public void logout(String token) {
        log.info("开始用户登出");
        
        User user = jwtTokenService.getUserFromToken(token);
        jwtTokenService.revokeAllUserTokens(user.getId());
        
        log.info("用户登出成功，用户ID: {}", user.getId());
    }

    @Override
    public LoginResponse.TokenInfo refreshToken(String refreshToken) {
        log.info("开始刷新令牌");
        return jwtTokenService.refreshToken(refreshToken);
    }

    @Override
    public boolean validateUserCredentials(String username, String password) {
        log.debug("验证用户凭据（Mock实现），用户名: {}", username);

        if (!isUserExists(username)) {
            return false;
        }

        log.info("用户凭据验证成功（Mock），用户名: {}", username);
        return true;
    }

    /**
     * 构建用户信息
     */
    private LoginResponse.UserInfo buildUserInfo(User user) {
        // 提取用户角色信息
        List<LoginResponse.RoleInfo> roles = user.getUserRoles().stream()
                .map(UserRole::getRole)
                .map(this::buildRoleInfo)
                .collect(Collectors.toList());

        return LoginResponse.UserInfo.builder()
                .userId(user.getId())
                .lanId(user.getLanId())
                .userName(user.getUserName())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }

    /**
     * 构建角色信息
     */
    private LoginResponse.RoleInfo buildRoleInfo(Role role) {
        return LoginResponse.RoleInfo.builder()
                .roleId(role.getId())
                .roleName(role.getName())
                .normalizedName(role.getNormalizedName())
                .build();
    }

    /**
     * 认证用户（LDAP 或 Mock）
     */
    private LdapAuthResult authenticateUser(LoginRequest loginRequest) {
        if (ldapEnabled) {
            return authenticateWithLdap(loginRequest);
        } else {
            authenticateWithMock(loginRequest);
            return null;
        }
    }

    /**
     * 使用 LDAP 认证
     */
    private LdapAuthResult authenticateWithLdap(LoginRequest loginRequest) {
        log.info("LDAP认证已开启，开始通过LDAP验证用户，用户名: {}", loginRequest.getUsername());
        LdapAuthResult result = ldapAuthService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());
        if (!result.isSuccess()) {
            throw new RuntimeException("LDAP认证失败");
        }
        return result;
    }

    /**
     * 使用 Mock 认证
     */
    private void authenticateWithMock(LoginRequest loginRequest) {
        if (!isUserExists(loginRequest.getUsername())) {
            throw new RuntimeException("用户不存在");
        }
        log.info("Mock认证通过，用户名: {}", loginRequest.getUsername());
    }

    /**
     * 加载并验证用户
     */
    private User loadAndValidateUser(String username, LdapAuthResult ldapResult) {
        User user;
        
        if (ldapEnabled && ldapResult != null) {
            user = findOrCreateUserFromLdap(username, ldapResult);
        } else {
            user = userRepository.findByLanIdWithRoles(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
        }

        validateUserActive(user);
        return user;
    }

    /**
     * 从LDAP查找或创建用户
     */
    private User findOrCreateUserFromLdap(String lanId, LdapAuthResult ldapResult) {
        return userRepository.findByLanIdWithRoles(lanId)
                .map(existingUser -> updateUserFromLdap(existingUser, ldapResult))
                .orElseGet(() -> createUserFromLdap(lanId, ldapResult));
    }

    /**
     * 从LDAP创建新用户
     */
    private User createUserFromLdap(String lanId, LdapAuthResult ldapResult) {
        log.info("创建新用户，LAN ID: {}", lanId);
        
        LdapUserInfo ldapInfo = ldapResult.getUserInfo();
        String displayName = extractDisplayName(ldapInfo, lanId);
        String email = extractEmail(ldapInfo);
        
        User newUser = User.builder()
                .id(UUID.randomUUID().toString())
                .lanId(lanId)
                .userName(displayName)
                .normalizedUserName(displayName.toUpperCase())
                .email(email)
                .normalizedEmail(normalizeEmail(email))
                .displayName(displayName)
                .isActive(true)
                .build();
        
        newUser = userRepository.save(newUser);
        assignRoleBasedOnDepartment(newUser, ldapInfo);
        
        log.info("用户创建成功，用户ID: {}, LAN ID: {}", newUser.getId(), lanId);
        return userRepository.findByLanIdWithRoles(lanId)
                .orElseThrow(() -> new RuntimeException("创建用户后查询失败"));
    }

    /**
     * 更新用户信息（从LDAP）
     */
    private User updateUserFromLdap(User user, LdapAuthResult ldapResult) {
        LdapUserInfo ldapInfo = ldapResult.getUserInfo();
        if (ldapInfo == null) {
            return user;
        }

        boolean changed = false;

        if (shouldUpdateDisplayName(ldapInfo, user)) {
            user.setDisplayName(ldapInfo.getDisplayName());
            changed = true;
        }

        if (shouldUpdateEmail(ldapInfo, user)) {
            user.setEmail(ldapInfo.getEmail());
            user.setNormalizedEmail(normalizeEmail(ldapInfo.getEmail()));
            changed = true;
        }

        if (changed) {
            userRepository.save(user);
            log.info("已更新用户信息，LAN ID: {}", user.getLanId());
        }

        return user;
    }

    /**
     * 根据部门分配角色
     */
    private void assignRoleBasedOnDepartment(User user, LdapUserInfo ldapInfo) {
        String department = extractDepartment(ldapInfo);
        boolean isHrDepartment = isHrDepartment(department);
        
        String roleName = isHrDepartment ? ROLE_HR_ADMIN : ROLE_EMPLOYEE;
        String normalizedRoleName = isHrDepartment ? NORMALIZED_ROLE_HR_ADMIN : NORMALIZED_ROLE_EMPLOYEE;
        
        log.info("用户部门: {}, 分配角色: {}", department != null ? department : "未知", roleName);
        
        Role role = roleRepository.findByNormalizedName(normalizedRoleName)
                .orElseThrow(() -> new RuntimeException("角色 " + roleName + " 不存在"));
        
        UserRole userRole = UserRole.builder()
                .userId(user.getId())
                .roleId(role.getId())
                .build();
        
        userRoleRepository.save(userRole);
        log.info("已为用户分配角色 {}，用户ID: {}", roleName, user.getId());
    }

    /**
     * 验证用户是否激活
     */
    private void validateUserActive(User user) {
        if (!user.getIsActive()) {
            throw new RuntimeException("用户账户已被禁用");
        }
    }

    /**
     * 构建登录响应
     */
    private LoginResponse buildLoginResponse(User user) {
        LoginResponse.TokenInfo tokenInfo = jwtTokenService.generateAndSaveToken(user);
        LoginResponse.UserInfo userInfo = buildUserInfo(user);

        return LoginResponse.builder()
                .tokenInfo(tokenInfo)
                .userInfo(userInfo)
                .build();
    }

    /**
     * 检查用户是否存在
     */
    private boolean isUserExists(String username) {
        boolean exists = userRepository.findByLanIdAndIsActiveTrue(username).isPresent();
        if (!exists) {
            log.warn("用户不存在: {}", username);
        }
        return exists;
    }


    /**
     * 提取显示名称
     */
    private String extractDisplayName(LdapUserInfo ldapInfo, String defaultValue) {
        return Optional.ofNullable(ldapInfo)
                .map(LdapUserInfo::getDisplayName)
                .filter(StringUtils::hasText)
                .orElse(defaultValue);
    }

    /**
     * 提取邮箱
     */
    private String extractEmail(LdapUserInfo ldapInfo) {
        return Optional.ofNullable(ldapInfo)
                .map(LdapUserInfo::getEmail)
                .filter(StringUtils::hasText)
                .orElse(null);
    }

    /**
     * 提取部门
     */
    private String extractDepartment(LdapUserInfo ldapInfo) {
        return Optional.ofNullable(ldapInfo)
                .map(LdapUserInfo::getDepartment)
                .filter(StringUtils::hasText)
                .orElse(null);
    }

    /**
     * 规范化邮箱
     */
    private String normalizeEmail(String email) {
        return StringUtils.hasText(email) ? email.toUpperCase() : null;
    }

    /**
     * 判断是否为HR部门
     */
    private boolean isHrDepartment(String department) {
        return StringUtils.hasText(department) && department.equals(hrDepartment);
    }

    /**
     * 判断是否需要更新显示名称
     */
    private boolean shouldUpdateDisplayName(LdapUserInfo ldapInfo, User user) {
        return StringUtils.hasText(ldapInfo.getDisplayName())
                && !ldapInfo.getDisplayName().equals(user.getDisplayName());
    }

    /**
     * 判断是否需要更新邮箱
     */
    private boolean shouldUpdateEmail(LdapUserInfo ldapInfo, User user) {
        return StringUtils.hasText(ldapInfo.getEmail())
                && !ldapInfo.getEmail().equals(user.getEmail());
    }

}
