package com.ocbc.ms.easy.care.service.impl;

import com.ocbc.ms.easy.care.config.LoginConfigurationProperties;
import com.ocbc.ms.easy.care.dto.LdapUserInfo;
import com.ocbc.ms.easy.care.dto.LoginRequest;
import com.ocbc.ms.easy.care.dto.LoginResponse;
import com.ocbc.ms.easy.care.entity.Role;
import com.ocbc.ms.easy.care.entity.User;
import com.ocbc.ms.easy.care.entity.UserRole;
import com.ocbc.ms.easy.care.mapper.LdapUserInfoAttributesMapper;
import com.ocbc.ms.easy.care.service.LdapService;
import lombok.RequiredArgsConstructor;
import com.ocbc.ms.easy.care.repository.RoleRepository;
import com.ocbc.ms.easy.care.repository.UserRepository;
import com.ocbc.ms.easy.care.repository.UserRoleRepository;
import com.ocbc.ms.easy.care.service.JwtTokenService;
import com.ocbc.ms.easy.care.service.LoginService;
import com.ocbc.ms.easy.care.util.RSAUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
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

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final JwtTokenService jwtTokenService;
    private final RSAUtil rsaUtil;
    private final LoginConfigurationProperties loginConfig;
    private final LdapService ldapService;

    @Value("${encryption.rsa-enabled:false}")
    private boolean rsaEnabled;

    @Value("${user.role.hr-department:CHN E2P Human Resources}")
    private String hrDepartment;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest loginRequest, boolean skipRsaDecryption) {
        log.info("开始用户登录验证，用户名: {}, Mock模式: {}", loginRequest.getUsername(), skipRsaDecryption);

        boolean needDecryption = rsaEnabled && !skipRsaDecryption;
        if (needDecryption) {
            String decryptedPassword = rsaUtil.decryptLogin(loginRequest);
            loginRequest.setPassword(decryptedPassword);
        } else if (skipRsaDecryption) {
            log.info("Mock模式登录，跳过RSA解密，用户名: {}", loginRequest.getUsername());
        }

        LdapUserInfo ldapUserInfo = null;
        if (loginConfig.getLdap().isEnabled()) {
            boolean isValidUser = ldapService.validateUserAndPassword(loginRequest.getUsername(), loginRequest.getPassword());
            if (!isValidUser) {
                throw new RuntimeException("LDAP认证失败：用户名或密码错误");
            }
            
            List<LdapUserInfo> ldapUserInfoList = ldapService.getUserInfo(
                loginRequest.getUsername(), 
                new LdapUserInfoAttributesMapper(true, ldapService)
            );
            
            if (!ldapUserInfoList.isEmpty()) {
                ldapUserInfo = ldapUserInfoList.getFirst();
            }
        }

        User user = loadAndValidateUser(loginRequest.getUsername(), ldapUserInfo);
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

        if (isUserExists(username)) {
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
     * 使用 Mock 认证
     */
    private void authenticateWithMock(LoginRequest loginRequest) {
        log.info("开始Mock认证，用户名: {}", loginRequest.getUsername());
        if (isUserExists(loginRequest.getUsername())) {
            throw new RuntimeException("用户不存在");
        }
        log.info("Mock认证通过，用户名: {}", loginRequest.getUsername());
    }

    /**
     * 加载并验证用户
     */
    private User loadAndValidateUser(String username, LdapUserInfo ldapUserInfo) {
        log.info("开始加载用户信息，用户名: {}", username);
        User user;
        
        if (loginConfig.getLdap().isEnabled() && ldapUserInfo != null) {
            user = findOrCreateUserFromLdap(username, ldapUserInfo);
        } else {
            user = userRepository.findByLanIdWithRoles(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
        }

        validateUserActive(user);
        log.info("用户信息加载成功，用户ID: {}", user.getId());
        return user;
    }

    /**
     * 从LDAP查找或创建用户
     */
    private User findOrCreateUserFromLdap(String lanId, LdapUserInfo ldapUserInfo) {
        return userRepository.findByLanIdWithRoles(lanId)
                .map(existingUser -> updateUserFromLdap(existingUser, ldapUserInfo))
                .orElseGet(() -> createUserFromLdap(lanId, ldapUserInfo));
    }

    /**
     * 从LDAP创建新用户
     */
    private User createUserFromLdap(String lanId, LdapUserInfo ldapUserInfo) {
        log.info("创建新用户，LAN ID: {}", lanId);
        
        String displayName = ldapUserInfo.getName() != null ? ldapUserInfo.getName() : lanId;
        String email = ldapUserInfo.getEmail();
        
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
        assignRoleBasedOnDepartment(newUser, ldapUserInfo.getDepartment());
        
        log.info("用户创建成功，用户ID: {}, LAN ID: {}", newUser.getId(), lanId);
        return userRepository.findByLanIdWithRoles(lanId)
                .orElseThrow(() -> new RuntimeException("创建用户后查询失败"));
    }

    /**
     * 更新用户信息（从LDAP）
     */
    private User updateUserFromLdap(User user, LdapUserInfo ldapUserInfo) {
        if (ldapUserInfo == null) {
            return user;
        }

        boolean changed = false;

        if (ldapUserInfo.getName() != null && !ldapUserInfo.getName().equals(user.getDisplayName())) {
            user.setDisplayName(ldapUserInfo.getName());
            changed = true;
        }

        if (ldapUserInfo.getEmail() != null && !ldapUserInfo.getEmail().equals(user.getEmail())) {
            user.setEmail(ldapUserInfo.getEmail());
            user.setNormalizedEmail(normalizeEmail(ldapUserInfo.getEmail()));
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
    private void assignRoleBasedOnDepartment(User user, String department) {
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
        return !exists;
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

}
