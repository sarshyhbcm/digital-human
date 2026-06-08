package cn.edu.nuc.digitalhuman.achievement.service;

import cn.edu.nuc.digitalhuman.achievement.entity.Achievement;

import java.util.List;
import java.util.Map;

public interface AchievementService {

    List<Achievement> listAll();

    List<Map<String, Object>> getUserAchievements(Long userId);

    void equipAchievement(Long userId, Long achievementId);

    void unequipAchievement(Long userId);

    void checkAndUnlock(Long userId);
}
