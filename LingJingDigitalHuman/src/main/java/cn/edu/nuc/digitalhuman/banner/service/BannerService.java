package cn.edu.nuc.digitalhuman.banner.service;

import cn.edu.nuc.digitalhuman.banner.entity.Banner;
import java.util.List;

public interface BannerService {

    List<Banner> listEnabled();

    Banner getById(Long id);

    Banner create(Banner banner);

    Banner update(Banner banner);

    void delete(Long id);
}
