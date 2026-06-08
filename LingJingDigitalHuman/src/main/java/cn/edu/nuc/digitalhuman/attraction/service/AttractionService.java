package cn.edu.nuc.digitalhuman.attraction.service;

import cn.edu.nuc.digitalhuman.attraction.entity.Attraction;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 景点业务接口
 */
public interface AttractionService {

    /**
     * 分页查询景点列表（游客端，仅 visible）
     */
    IPage<Attraction> page(Page<Attraction> page, String area);

    /**
     * 分页查询景点列表（管理端，含隐藏）
     */
    IPage<Attraction> adminPage(Page<Attraction> page, String area, String keyword);

    /**
     * 获取景点详情
     */
    Attraction getById(Long id);

    /**
     * 新增景点
     */
    Attraction create(Attraction attraction);

    /**
     * 更新景点
     */
    Attraction update(Attraction attraction);

    /**
     * 删除景点
     */
    void delete(Long id);

    /**
     * 上传封面图
     */
    String uploadCover(Long id, org.springframework.web.multipart.MultipartFile file);

    /**
     * 上传多张图片（追加到 images JSON 数组）
     */
    String uploadImages(Long id, org.springframework.web.multipart.MultipartFile[] files);

    /**
     * 删除指定图片
     */
    Attraction removeImage(Long id, String imageUrl);
}
