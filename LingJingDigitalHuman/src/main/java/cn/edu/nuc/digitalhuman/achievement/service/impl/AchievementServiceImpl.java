package cn.edu.nuc.digitalhuman.achievement.service.impl;

import cn.edu.nuc.digitalhuman.achievement.entity.Achievement;
import cn.edu.nuc.digitalhuman.achievement.entity.UserAchievement;
import cn.edu.nuc.digitalhuman.achievement.mapper.AchievementMapper;
import cn.edu.nuc.digitalhuman.achievement.mapper.UserAchievementMapper;
import cn.edu.nuc.digitalhuman.achievement.service.AchievementService;
import cn.edu.nuc.digitalhuman.attraction.entity.Attraction;
import cn.edu.nuc.digitalhuman.attraction.mapper.AttractionMapper;
import cn.edu.nuc.digitalhuman.auth.entity.SysUser;
import cn.edu.nuc.digitalhuman.auth.mapper.SysUserMapper;
import cn.edu.nuc.digitalhuman.checkin.entity.CheckIn;
import cn.edu.nuc.digitalhuman.checkin.mapper.CheckInMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AchievementServiceImpl implements AchievementService {

    private final AchievementMapper achievementMapper;
    private final UserAchievementMapper userAchievementMapper;
    private final CheckInMapper checkInMapper;
    private final AttractionMapper attractionMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public List<Achievement> listAll() {
        return achievementMapper.selectList(
                new LambdaQueryWrapper<Achievement>()
                        .orderByAsc(Achievement::getSortOrder));
    }

    @Override
    public List<Map<String, Object>> getUserAchievements(Long userId) {
        List<Achievement> all = listAll();
        Set<Long> unlockedIds = userAchievementMapper.selectList(
                new LambdaQueryWrapper<UserAchievement>()
                        .eq(UserAchievement::getUserId, userId))
                .stream()
                .map(UserAchievement::getAchievementId)
                .collect(Collectors.toSet());

        // 获取当前佩戴的徽章 ID
        SysUser user = sysUserMapper.selectById(userId);
        Long equippedId = user != null ? user.getEquippedBadgeId() : null;

        return all.stream().map(a -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", a.getId());
            item.put("name", a.getName());
            item.put("description", a.getDescription());
            item.put("icon", a.getIcon());
            item.put("conditionDesc", a.getConditionDesc());
            item.put("conditionType", a.getConditionType());
            item.put("conditionValue", a.getConditionValue());
            item.put("unlocked", unlockedIds.contains(a.getId()));
            item.put("equipped", unlockedIds.contains(a.getId()) && equippedId != null && equippedId.equals(a.getId()));
            return item;
        }).collect(Collectors.toList());
    }

    @Override
    public void equipAchievement(Long userId, Long achievementId) {
        // 确保已解锁
        Long count = userAchievementMapper.selectCount(
                new LambdaQueryWrapper<UserAchievement>()
                        .eq(UserAchievement::getUserId, userId)
                        .eq(UserAchievement::getAchievementId, achievementId));
        if (count == 0) {
            throw new RuntimeException("该成就尚未解锁");
        }
        SysUser user = new SysUser();
        user.setId(userId);
        user.setEquippedBadgeId(achievementId);
        sysUserMapper.updateById(user);
    }

    @Override
    public void unequipAchievement(Long userId) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setEquippedBadgeId(null);
        sysUserMapper.updateById(user);
    }

    @Override
    public void checkAndUnlock(Long userId) {
        List<Achievement> all = listAll();
        long checkInCount = checkInMapper.selectCount(
                new LambdaQueryWrapper<CheckIn>().eq(CheckIn::getUserId, userId));
        long totalAttractions = attractionMapper.selectCount(
                new LambdaQueryWrapper<Attraction>().eq(Attraction::getStatus, 1));

        Set<Long> unlockedIds = userAchievementMapper.selectList(
                new LambdaQueryWrapper<UserAchievement>()
                        .eq(UserAchievement::getUserId, userId))
                .stream()
                .map(UserAchievement::getAchievementId)
                .collect(Collectors.toSet());

        for (Achievement achievement : all) {
            if (unlockedIds.contains(achievement.getId())) continue;

            boolean meet = false;
            switch (achievement.getConditionType()) {
                case "checkin_count":
                    meet = checkInCount >= achievement.getConditionValue();
                    break;
                case "all_attractions":
                    meet = checkInCount >= totalAttractions;
                    break;
            }
            if (meet) {
                UserAchievement ua = new UserAchievement();
                ua.setUserId(userId);
                ua.setAchievementId(achievement.getId());
                userAchievementMapper.insert(ua);
            }
        }
    }
}
