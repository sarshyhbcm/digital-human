package cn.edu.nuc.digitalhuman.auth;

import cn.edu.nuc.digitalhuman.auth.controller.AuthController;
import cn.edu.nuc.digitalhuman.auth.dto.LoginDto;
import cn.edu.nuc.digitalhuman.auth.dto.RegisterDto;
import cn.edu.nuc.digitalhuman.auth.service.UserService;
import cn.edu.nuc.digitalhuman.common.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @Test
    @DisplayName("POST /api/auth/register：正常注册返回200")
    void register_ValidInput_ShouldReturn200() throws Exception {
        RegisterDto dto = new RegisterDto();
        dto.setUsername("newuser");
        dto.setPassword("123456");

        doNothing().when(userService).register(any(RegisterDto.class));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("注册成功"));
    }

    @Test
    @DisplayName("POST /api/auth/register：用户名为空返回400")
    void register_EmptyUsername_ShouldReturn400() throws Exception {
        RegisterDto dto = new RegisterDto();
        dto.setPassword("123456");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register：密码太短返回400")
    void register_ShortPassword_ShouldReturn400() throws Exception {
        RegisterDto dto = new RegisterDto();
        dto.setUsername("testuser");
        dto.setPassword("123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login：正常登录返回200和token")
    void login_ValidInput_ShouldReturn200() throws Exception {
        LoginDto dto = new LoginDto();
        dto.setUsername("testuser");
        dto.setPassword("123456");

        Map<String, String> mockResult = new HashMap<>();
        mockResult.put("token", "mock-token");
        mockResult.put("role", "user");
        when(userService.login(any(LoginDto.class))).thenReturn(mockResult);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("mock-token"));
    }

    @Test
    @DisplayName("POST /api/auth/login：用户名为空返回400")
    void login_EmptyUsername_ShouldReturn400() throws Exception {
        LoginDto dto = new LoginDto();
        dto.setPassword("123456");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login：密码为空返回400")
    void login_EmptyPassword_ShouldReturn400() throws Exception {
        LoginDto dto = new LoginDto();
        dto.setUsername("testuser");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}
