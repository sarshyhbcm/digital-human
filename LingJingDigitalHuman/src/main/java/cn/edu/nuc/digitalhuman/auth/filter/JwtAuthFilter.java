package cn.edu.nuc.digitalhuman.auth.filter;

import cn.edu.nuc.digitalhuman.common.utils.JwtUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

/**
 * JWT 认证过滤器（安检门）
 *
 * 每个HTTP请求进来都会先过这道安检：
 * - 白名单路径（登录/注册/景点/路线等）：放行（不用token也能访问）
 * - 其他请求：检查请求头 Authorization 里有没有有效的token
 *
 * 没有token或token无效 → 直接返回401，不进Controller
 * token有效 → 把用户ID放到请求属性里，放行
 */
@Component
@Order(1)
@RequiredArgsConstructor
public class JwtAuthFilter implements Filter {

    private final JwtUtils jwtUtils;

    private static final Set<String> WHITE_LIST = Set.of(
            "/api/auth/login",
            "/api/auth/register"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String path = req.getRequestURI();

        // 白名单放行：登录注册、景点列表、路线推荐、数字人配置、静态资源等公开接口
        if (WHITE_LIST.contains(path)
                || path.startsWith("/api/attractions")
                || path.startsWith("/api/routes")
                || path.startsWith("/api/digital-human")
                || path.startsWith("/api/qrcodes")
                || path.startsWith("/api/hot-searches")
                || path.startsWith("/api/banners")
                || path.startsWith("/uploads")) {
            chain.doFilter(request, response);
            return;
        }

        String token = req.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            resp.setContentType("application/json;charset=utf-8");
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"code\":401,\"message\":\"未登录或token已过期\",\"data\":null}");
            return;
        }

        token = token.substring(7);
        if (!jwtUtils.validateToken(token)) {
            resp.setContentType("application/json;charset=utf-8");
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"code\":401,\"message\":\"token无效或已过期\",\"data\":null}");
            return;
        }

        // 将用户ID放入请求属性，方便Controller获取
        Long userId = jwtUtils.getUserId(token);
        String role = jwtUtils.getRole(token);
        req.setAttribute("userId", userId);
        req.setAttribute("role", role);

        // 管理后台接口需要 admin 角色
        if (path.startsWith("/api/admin") && !"admin".equals(role)) {
            resp.setContentType("application/json;charset=utf-8");
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"code\":403,\"message\":\"权限不足，需要管理员权限\",\"data\":null}");
            return;
        }

        chain.doFilter(request, response);
    }
}
