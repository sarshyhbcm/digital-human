package cn.edu.nuc.digitalhuman.achievement.controller;

import cn.edu.nuc.digitalhuman.achievement.entity.Achievement;
import cn.edu.nuc.digitalhuman.achievement.service.AchievementService;
import cn.edu.nuc.digitalhuman.common.result.R;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    @GetMapping
    public R<List<Achievement>> list() {
        return R.success(achievementService.listAll());
    }

    @GetMapping("/my")
    public R<List<Map<String, Object>>> myAchievements(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return R.success(achievementService.getUserAchievements(userId));
    }

    @PutMapping("/equip/{achievementId}")
    public R<Void> equip(@PathVariable Long achievementId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        achievementService.equipAchievement(userId, achievementId);
        return R.success();
    }

    @DeleteMapping("/equip")
    public R<Void> unequip(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        achievementService.unequipAchievement(userId);
        return R.success();
    }
}
