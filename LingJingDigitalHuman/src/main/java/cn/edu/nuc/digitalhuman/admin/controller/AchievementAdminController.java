package cn.edu.nuc.digitalhuman.admin.controller;

import cn.edu.nuc.digitalhuman.achievement.entity.Achievement;
import cn.edu.nuc.digitalhuman.achievement.mapper.AchievementMapper;
import cn.edu.nuc.digitalhuman.common.result.R;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/achievements")
@RequiredArgsConstructor
public class AchievementAdminController {

    private final AchievementMapper achievementMapper;

    @GetMapping
    public R<List<Achievement>> listAchievements() {
        var list = achievementMapper.selectList(
                new LambdaQueryWrapper<Achievement>().orderByAsc(Achievement::getSortOrder));
        return R.success(list);
    }

    @PostMapping
    public R<String> createAchievement(@RequestBody Achievement achievement) {
        achievement.setId(null);
        achievementMapper.insert(achievement);
        return R.success("创建成功");
    }

    @PutMapping("/{id}")
    public R<String> updateAchievement(@PathVariable Long id, @RequestBody Achievement achievement) {
        var existing = achievementMapper.selectById(id);
        if (existing == null) {
            return R.notFound("成就不存在");
        }
        achievement.setId(id);
        achievementMapper.updateById(achievement);
        return R.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public R<String> deleteAchievement(@PathVariable Long id) {
        var existing = achievementMapper.selectById(id);
        if (existing == null) {
            return R.notFound("成就不存在");
        }
        achievementMapper.deleteById(id);
        return R.success("删除成功");
    }
}
