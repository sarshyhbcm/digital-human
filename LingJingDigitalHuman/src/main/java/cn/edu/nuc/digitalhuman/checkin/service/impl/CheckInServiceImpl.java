package cn.edu.nuc.digitalhuman.checkin.service.impl;

import cn.edu.nuc.digitalhuman.achievement.service.AchievementService;
import cn.edu.nuc.digitalhuman.attraction.entity.Attraction;
import cn.edu.nuc.digitalhuman.attraction.mapper.AttractionMapper;
import cn.edu.nuc.digitalhuman.checkin.entity.CheckIn;
import cn.edu.nuc.digitalhuman.checkin.mapper.CheckInMapper;
import cn.edu.nuc.digitalhuman.checkin.service.CheckInService;
import cn.edu.nuc.digitalhuman.common.exception.ServiceException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class CheckInServiceImpl implements CheckInService {

    private final CheckInMapper checkInMapper;
    private final AttractionMapper attractionMapper;
    private final AchievementService achievementService;

    @Override
    public CheckIn checkIn(Long userId, Long attractionId, String type, BigDecimal latitude, BigDecimal longitude) {
        Attraction attraction = attractionMapper.selectById(attractionId);
        if (attraction == null) {
            throw new ServiceException(404, "景点不存在");
        }

        Long count = checkInMapper.selectCount(
                new LambdaQueryWrapper<CheckIn>()
                        .eq(CheckIn::getUserId, userId)
                        .eq(CheckIn::getAttractionId, attractionId));
        if (count > 0) {
            throw new ServiceException(400, "已打卡过该景点");
        }

        CheckIn checkIn = new CheckIn();
        checkIn.setUserId(userId);
        checkIn.setAttractionId(attractionId);
        checkIn.setCheckInType(type != null ? type : "gps");
        if (latitude != null) checkIn.setLatitude(latitude);
        if (longitude != null) checkIn.setLongitude(longitude);
        checkInMapper.insert(checkIn);
        achievementService.checkAndUnlock(userId);
        return checkIn;
    }

    @Override
    public List<CheckIn> getUserCheckIns(Long userId) {
        return checkInMapper.selectList(
                new LambdaQueryWrapper<CheckIn>()
                        .eq(CheckIn::getUserId, userId)
                        .orderByDesc(CheckIn::getCreatedAt));
    }

    @Override
    public boolean hasCheckedIn(Long userId, Long attractionId) {
        Long count = checkInMapper.selectCount(
                new LambdaQueryWrapper<CheckIn>()
                        .eq(CheckIn::getUserId, userId)
                        .eq(CheckIn::getAttractionId, attractionId));
        return count > 0;
    }

    @Override
    public Map<String, Object> getCheckInStatus(Long userId, Long attractionId) {
        boolean checkedIn = hasCheckedIn(userId, attractionId);
        Map<String, Object> result = new HashMap<>();
        result.put("checkedIn", checkedIn);
        return result;
    }

    @Override
    public long getUserCheckInCount(Long userId) {
        return checkInMapper.selectCount(
                new LambdaQueryWrapper<CheckIn>().eq(CheckIn::getUserId, userId));
    }
}
