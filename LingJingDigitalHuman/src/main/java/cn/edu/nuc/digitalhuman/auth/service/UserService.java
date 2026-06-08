package cn.edu.nuc.digitalhuman.auth.service;

import cn.edu.nuc.digitalhuman.auth.dto.LoginDto;
import cn.edu.nuc.digitalhuman.auth.dto.RegisterDto;
import cn.edu.nuc.digitalhuman.auth.dto.UpdateProfileDto;
import cn.edu.nuc.digitalhuman.auth.entity.SysUser;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户认证业务接口
 *
 * 定义注册、登录、查询用户、资料更新、头像上传等功能，
 * 具体实现在 UserServiceImpl 中。
 */
import java.util.Map;

public interface UserService {
    void register(RegisterDto dto);
    Map<String, String> login(LoginDto dto);
    SysUser getById(Long id);
    void updateProfile(Long userId, UpdateProfileDto dto);
    String uploadAvatar(Long userId, MultipartFile file);
}
