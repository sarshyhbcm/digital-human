package cn.edu.nuc.digitalhuman.checkin.service;

import cn.edu.nuc.digitalhuman.checkin.entity.CheckIn;

import java.util.List;
import java.util.Map;

public interface CheckInService {

    CheckIn checkIn(Long userId, Long attractionId, String type, java.math.BigDecimal latitude, java.math.BigDecimal longitude);

    List<CheckIn> getUserCheckIns(Long userId);

    boolean hasCheckedIn(Long userId, Long attractionId);

    Map<String, Object> getCheckInStatus(Long userId, Long attractionId);

    long getUserCheckInCount(Long userId);
}
