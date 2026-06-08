package cn.edu.nuc.digitalhuman.checkin.controller;

import cn.edu.nuc.digitalhuman.checkin.dto.CheckInDto;
import cn.edu.nuc.digitalhuman.checkin.entity.CheckIn;
import cn.edu.nuc.digitalhuman.checkin.service.CheckInService;
import cn.edu.nuc.digitalhuman.common.result.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checkins")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping
    public R<CheckIn> checkIn(@Valid @RequestBody CheckInDto dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        CheckIn result = checkInService.checkIn(userId, dto.getAttractionId(),
                dto.getCheckInType(), dto.getLatitude(), dto.getLongitude());
        return R.success(result);
    }

    @GetMapping("/my")
    public R<List<CheckIn>> myCheckIns(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return R.success(checkInService.getUserCheckIns(userId));
    }

    @GetMapping("/status/{attractionId}")
    public R<Map<String, Object>> status(@PathVariable Long attractionId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return R.success(checkInService.getCheckInStatus(userId, attractionId));
    }

    @GetMapping("/count")
    public R<Map<String, Long>> count(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        long count = checkInService.getUserCheckInCount(userId);
        Map<String, Long> result = new HashMap<>();
        result.put("count", count);
        return R.success(result);
    }
}
