package cn.edu.nuc.digitalhuman.auth.service.impl;

import cn.edu.nuc.digitalhuman.auth.dto.LoginDto;
import cn.edu.nuc.digitalhuman.auth.dto.RegisterDto;
import cn.edu.nuc.digitalhuman.auth.dto.UpdateProfileDto;
import cn.edu.nuc.digitalhuman.auth.entity.SysUser;
import cn.edu.nuc.digitalhuman.auth.mapper.SysUserMapper;
import cn.edu.nuc.digitalhuman.auth.service.UserService;
import cn.edu.nuc.digitalhuman.common.exception.ServiceException;
import cn.edu.nuc.digitalhuman.common.utils.JwtUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Value("${upload.path}")
    private String uploadPath;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDto dto) {
        Long count = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new ServiceException(400, "用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setRole("user");
        sysUserMapper.insert(user);
    }

    @Override
    public Map<String, String> login(LoginDto dto) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (user == null) {
            throw new ServiceException(400, "用户名或密码错误");
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new ServiceException(400, "用户名或密码错误");
        }
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        result.put("role", user.getRole());
        result.put("username", user.getUsername());
        return result;
    }

    @Override
    public SysUser getById(Long id) {
        return sysUserMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, UpdateProfileDto dto) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException(404, "用户不存在");
        }
        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        sysUserMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadAvatar(Long userId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new ServiceException(400, "文件不能为空");
        }

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String fileName = "avatar_" + userId + "_" + UUID.randomUUID().toString().replace("-", "") + ext;

        try {
            Path avatarDir = Paths.get(uploadPath, "avatars");
            Files.createDirectories(avatarDir);
            Files.write(avatarDir.resolve(fileName), file.getBytes());
            String avatarUrl = "/uploads/avatars/" + fileName;

            // 更新数据库
            SysUser user = sysUserMapper.selectById(userId);
            if (user != null) {
                user.setAvatar(avatarUrl);
                sysUserMapper.updateById(user);
            }

            log.info("头像上传成功: userId={}, url={}", userId, avatarUrl);
            return avatarUrl;
        } catch (IOException e) {
            log.error("头像上传失败", e);
            throw new ServiceException(500, "头像上传失败");
        }
    }
}
