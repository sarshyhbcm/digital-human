package cn.edu.nuc.digitalhuman.auth.controller;

import cn.edu.nuc.digitalhuman.auth.dto.LoginDto;
import cn.edu.nuc.digitalhuman.auth.dto.RegisterDto;
import cn.edu.nuc.digitalhuman.auth.dto.UpdateProfileDto;
import cn.edu.nuc.digitalhuman.auth.entity.SysUser;
import cn.edu.nuc.digitalhuman.auth.service.UserService;
import cn.edu.nuc.digitalhuman.common.result.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 认证控制器
 *
 * 处理用户的注册和登录请求。
 * 这两个接口是白名单，不需要token就能访问（不然没账号的人永远进不来）。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public R<String> register(@Valid @RequestBody RegisterDto dto) {
        userService.register(dto);
        return R.success("注册成功");
    }

    @PostMapping("/login")
    public R<Map<String, String>> login(@Valid @RequestBody LoginDto dto) {
        Map<String, String> result = userService.login(dto);
        return R.success(result);
    }

    @GetMapping("/me")
    public R<SysUser> me(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.error(401, "未登录");
        }
        SysUser user = userService.getById(userId);
        if (user == null) {
            return R.error(404, "用户不存在");
        }
        user.setPassword(null);
        return R.success(user);
    }

    @PutMapping("/profile")
    public R<String> updateProfile(@Valid @RequestBody UpdateProfileDto dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.error(401, "未登录");
        }
        userService.updateProfile(userId, dto);
        return R.success("更新成功");
    }

    @PostMapping("/avatar")
    public R<String> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.error(401, "未登录");
        }
        String url = userService.uploadAvatar(userId, file);
        return R.success(url);
    }
}
