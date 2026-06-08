package cn.edu.nuc.digitalhuman.auth;

import cn.edu.nuc.digitalhuman.common.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtils 单元测试
 *
 * 测试JWT令牌的生成、解析、验证功能
 * 不需要启动Spring Boot，直接new对象测试
 */
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        // 密钥由 JwtUtils 内部启动时随机生成，这里手动初始化
        jwtUtils = new JwtUtils(604800000L);
        jwtUtils.init();
    }

    /**
     * 测试：生成token应该返回一个不为空的字符串
     * JWT token由3部分组成（用.分隔）：header.payload.signature
     */
    @Test
    @DisplayName("生成token：应返回有效JWT格式字符串")
    void testGenerateToken_ShouldReturnValidToken() {
        String token = jwtUtils.generateToken(1L, "testuser", "user");
        assertNotNull(token);
        // JWT格式：由3段组成，用.分隔
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT token应由header.payload.signature三部分组成");
    }

    /**
     * 测试：从token中解析出用户ID
     * 生成token时传了userId=1，解析出来也应该是1
     */
    @Test
    @DisplayName("解析token：应能正确取出用户ID")
    void testGetUserId_ShouldReturnCorrectUserId() {
        String token = jwtUtils.generateToken(1L, "testuser", "user");
        Long userId = jwtUtils.getUserId(token);
        assertEquals(1L, userId);
    }

    /**
     * 测试：有效token验证通过
     */
    @Test
    @DisplayName("验证token：有效token应返回true")
    void testValidateToken_ValidToken_ShouldReturnTrue() {
        String token = jwtUtils.generateToken(1L, "testuser", "user");
        assertTrue(jwtUtils.validateToken(token));
    }

    /**
     * 测试：无效token（被篡改）验证不通过
     * 模拟一个脏数据：在token末尾加个"x"破坏签名
     */
    @Test
    @DisplayName("验证token：被篡改的token应返回false")
    void testValidateToken_TamperedToken_ShouldReturnFalse() {
        String token = jwtUtils.generateToken(1L, "testuser", "user");
        String tamperedToken = token + "x"; // 故意弄坏token
        assertFalse(jwtUtils.validateToken(tamperedToken));
    }

    /**
     * 测试：完全乱写的token验证不通过
     */
    @Test
    @DisplayName("验证token：乱写的字符串应返回false")
    void testValidateToken_GarbageString_ShouldReturnFalse() {
        assertFalse(jwtUtils.validateToken("this.is.a.garbage.token"));
    }

    /**
     * 测试：null token验证不通过
     */
    @Test
    @DisplayName("验证token：null应返回false")
    void testValidateToken_Null_ShouldReturnFalse() {
        assertFalse(jwtUtils.validateToken(null));
    }
}
