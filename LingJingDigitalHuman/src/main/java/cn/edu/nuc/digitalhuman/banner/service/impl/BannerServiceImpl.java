package cn.edu.nuc.digitalhuman.banner.service.impl;

import cn.edu.nuc.digitalhuman.banner.entity.Banner;
import cn.edu.nuc.digitalhuman.banner.mapper.BannerMapper;
import cn.edu.nuc.digitalhuman.banner.service.BannerService;
import cn.edu.nuc.digitalhuman.common.exception.ServiceException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BannerServiceImpl implements BannerService {

    private final BannerMapper bannerMapper;

    @Override
    public List<Banner> listEnabled() {
        return bannerMapper.selectList(new LambdaQueryWrapper<Banner>()
                .eq(Banner::getStatus, 1)
                .orderByAsc(Banner::getSortOrder));
    }

    @Override
    public Banner getById(Long id) {
        Banner banner = bannerMapper.selectById(id);
        if (banner == null) {
            throw new ServiceException(404, "轮播图不存在");
        }
        return banner;
    }

    @Override
    public Banner create(Banner banner) {
        if (banner.getSortOrder() == null) {
            banner.setSortOrder(0);
        }
        if (banner.getStatus() == null) {
            banner.setStatus(1);
        }
        bannerMapper.insert(banner);
        return banner;
    }

    @Override
    public Banner update(Banner banner) {
        getById(banner.getId());
        bannerMapper.updateById(banner);
        return banner;
    }

    @Override
    public void delete(Long id) {
        getById(id);
        bannerMapper.deleteById(id);
    }
}
