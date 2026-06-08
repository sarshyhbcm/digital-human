package cn.edu.nuc.digitalhuman.auth;

import cn.edu.nuc.digitalhuman.auth.dto.LoginDto;
import cn.edu.nuc.digitalhuman.auth.dto.RegisterDto;
import cn.edu.nuc.digitalhuman.auth.entity.SysUser;
import cn.edu.nuc.digitalhuman.auth.mapper.SysUserMapper;
import cn.edu.nuc.digitalhuman.auth.service.impl.UserServiceImpl;
import cn.edu.nuc.digitalhuman.common.exception.ServiceException;
import cn.edu.nuc.digitalhuman.common.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private JwtUtils jwtUtils;

    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserServiceImpl(sysUserMapper, passwordEncoder, jwtUtils);
    }

    @Test
    @DisplayName("注册：用户名不存在时注册成功")
    void register_NewUsername_ShouldSucceed() {
        RegisterDto dto = new RegisterDto();
        dto.setUsername("newuser");
        dto.setPassword("123456");

        when(sysUserMapper.selectCount(any())).thenReturn(0L);

        userService.register(dto);

        verify(sysUserMapper, times(1)).insert(any(SysUser.class));
    }

    @Test
    @DisplayName("注册：用户名已存在时抛出异常")
    void register_ExistingUsername_ShouldThrowException() {
        RegisterDto dto = new RegisterDto();
        dto.setUsername("existing");
        dto.setPassword("123456");

        when(sysUserMapper.selectCount(any())).thenReturn(1L);

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userService.register(dto);
        });

        assertEquals("用户名已存在", exception.getMessage());
        verify(sysUserMapper, never()).insert(any(SysUser.class));
    }

    @Test
    @DisplayName("登录：用户名密码正确时返回token")
    void login_CorrectCredentials_ShouldReturnToken() {
        LoginDto dto = new LoginDto();
        dto.setUsername("testuser");
        dto.setPassword("123456");

        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRole("user");
        when(sysUserMapper.selectOne(any())).thenReturn(user);

        when(jwtUtils.generateToken(1L, "testuser", "user")).thenReturn("mock-jwt-token");

        Map<String, String> result = userService.login(dto);

        assertEquals("mock-jwt-token", result.get("token"));
        assertEquals("user", result.get("role"));
    }

    @Test
    @DisplayName("登录：用户不存在时抛出异常")
    void login_UserNotFound_ShouldThrowException() {
        LoginDto dto = new LoginDto();
        dto.setUsername("nonexistent");
        dto.setPassword("123456");

        when(sysUserMapper.selectOne(any())).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userService.login(dto);
        });

        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("登录：密码错误时抛出异常")
    void login_WrongPassword_ShouldThrowException() {
        LoginDto dto = new LoginDto();
        dto.setUsername("testuser");
        dto.setPassword("wrongpassword");

        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("123456"));
        when(sysUserMapper.selectOne(any())).thenReturn(user);

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userService.login(dto);
        });

        assertEquals("用户名或密码错误", exception.getMessage());
    }
}
