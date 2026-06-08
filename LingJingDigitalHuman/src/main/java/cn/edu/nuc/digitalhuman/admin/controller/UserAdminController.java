package cn.edu.nuc.digitalhuman.admin.controller;

import cn.edu.nuc.digitalhuman.auth.entity.SysUser;
import cn.edu.nuc.digitalhuman.auth.mapper.SysUserMapper;
import cn.edu.nuc.digitalhuman.common.result.R;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final SysUserMapper sysUserMapper;

    @GetMapping
    public R<Map<String, Object>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword) {
        var query = new LambdaQueryWrapper<SysUser>();
        if (keyword != null && !keyword.isBlank()) {
            query.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getNickname, keyword)
                    .or().like(SysUser::getPhone, keyword);
        }

        // 总数
        long total = sysUserMapper.selectCount(query);

        // 分页查询
        query.orderByAsc(SysUser::getId);
        long offset = (long) (page - 1) * pageSize;
        query.last("LIMIT " + pageSize + " OFFSET " + offset);
        List<SysUser> records = sysUserMapper.selectList(query);

        // 隐藏密码
        records.forEach(u -> u.setPassword(null));

        // 管理员排最前，其余按 ID 升序
        records.sort((a, b) -> {
            if ("admin".equals(a.getRole()) && !"admin".equals(b.getRole())) return -1;
            if (!"admin".equals(a.getRole()) && "admin".equals(b.getRole())) return 1;
            return a.getId().compareTo(b.getId());
        });

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return R.success(result);
    }

    @PutMapping("/{id}/role")
    public R<String> updateRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            return R.notFound("用户不存在");
        }
        String role = body.get("role");
        if (!"user".equals(role) && !"admin".equals(role)) {
            return R.badRequest("角色值无效（user/admin）");
        }
        user.setRole(role);
        sysUserMapper.updateById(user);
        return R.success("更新成功");
    }
}
