package cn.edu.nuc.digitalhuman.common.config;

import cn.edu.nuc.digitalhuman.auth.entity.SysUser;
import cn.edu.nuc.digitalhuman.auth.mapper.SysUserMapper;
import cn.edu.nuc.digitalhuman.checkin.entity.CheckIn;
import cn.edu.nuc.digitalhuman.checkin.mapper.CheckInMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class DemoDataInitializer implements CommandLineRunner {

    private static final int ATTRACTION_COUNT = 22;
    private static final int USER_COUNT = 100;

    private final SysUserMapper sysUserMapper;
    private final CheckInMapper checkInMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 检查是否已初始化
        SysUser existing = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, "user002"));
        if (existing != null) {
            log.info("演示用户已存在，跳过初始化");
            return;
        }

        String encodedPassword = passwordEncoder.encode("123456");
        List<Long> userIds = new ArrayList<>();
        List<List<Long>> userCheckIns = new ArrayList<>();

        for (int i = 2; i <= USER_COUNT + 1; i++) {
            String username = String.format("user%03d", i);

            SysUser user = new SysUser();
            user.setUsername(username);
            user.setPassword(encodedPassword);
            user.setNickname("游客" + username.substring(4));
            user.setRole("user");
            sysUserMapper.insert(user);
            userIds.add(user.getId());

            int checkInCount = ThreadLocalRandom.current().nextInt(3, 16);
            List<Long> attractionIds = new ArrayList<>();
            for (long a = 1; a <= ATTRACTION_COUNT; a++) {
                attractionIds.add(a);
            }
            Collections.shuffle(attractionIds, ThreadLocalRandom.current());
            userCheckIns.add(new ArrayList<>(attractionIds.subList(0, Math.min(checkInCount, ATTRACTION_COUNT))));
        }

        int totalCheckIns = 0;
        for (int idx = 0; idx < userIds.size(); idx++) {
            Long userId = userIds.get(idx);
            for (Long attractionId : userCheckIns.get(idx)) {
                try {
                    CheckIn checkIn = new CheckIn();
                    checkIn.setUserId(userId);
                    checkIn.setAttractionId(attractionId);
                    checkIn.setCheckInType("gps");
                    checkInMapper.insert(checkIn);
                    totalCheckIns++;
                } catch (Exception e) {
                    log.warn("打卡记录插入失败: userId={}, attractionId={}", userId, attractionId);
                }
            }
        }

        log.info("演示数据初始化完成: {} 个用户, {} 条打卡记录", USER_COUNT, totalCheckIns);
    }
}
