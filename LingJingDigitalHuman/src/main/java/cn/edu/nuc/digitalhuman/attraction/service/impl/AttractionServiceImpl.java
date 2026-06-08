package cn.edu.nuc.digitalhuman.attraction.service.impl;

import cn.edu.nuc.digitalhuman.attraction.entity.Attraction;
import cn.edu.nuc.digitalhuman.attraction.mapper.AttractionMapper;
import cn.edu.nuc.digitalhuman.attraction.service.AttractionService;
import cn.edu.nuc.digitalhuman.common.config.UploadConfig;
import cn.edu.nuc.digitalhuman.common.exception.ServiceException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 景点业务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AttractionServiceImpl implements AttractionService {

    private final AttractionMapper attractionMapper;
    private final UploadConfig uploadConfig;
    private final ObjectMapper objectMapper;

    @Override
    public IPage<Attraction> page(Page<Attraction> page, String area) {
        LambdaQueryWrapper<Attraction> wrapper = new LambdaQueryWrapper<Attraction>()
                .eq(area != null && !area.isEmpty(), Attraction::getArea, area)
                .eq(Attraction::getStatus, 1)
                .orderByAsc(Attraction::getSortOrder);
        return attractionMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Attraction> adminPage(Page<Attraction> page, String area, String keyword) {
        LambdaQueryWrapper<Attraction> wrapper = new LambdaQueryWrapper<Attraction>()
                .eq(area != null && !area.isEmpty(), Attraction::getArea, area)
                .like(keyword != null && !keyword.isEmpty(), Attraction::getName, keyword)
                .orderByAsc(Attraction::getSortOrder);
        return attractionMapper.selectPage(page, wrapper);
    }

    @Override
    public Attraction getById(Long id) {
        Attraction attraction = attractionMapper.selectById(id);
        if (attraction == null) {
            throw new ServiceException(404, "景点不存在");
        }
        return attraction;
    }

    @Override
    public Attraction create(Attraction attraction) {
        if (attraction.getSortOrder() == null) {
            attraction.setSortOrder(0);
        }
        if (attraction.getStatus() == null) {
            attraction.setStatus(1);
        }
        attractionMapper.insert(attraction);
        return attraction;
    }

    @Override
    public Attraction update(Attraction attraction) {
        getById(attraction.getId());
        attractionMapper.updateById(attraction);
        return attraction;
    }

    @Override
    public void delete(Long id) {
        getById(id);
        attractionMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadCover(Long id, MultipartFile file) {
        Attraction attraction = getById(id);
        String url = uploadConfig.saveFile(file, "attractions");
        attraction.setCoverImage(url);
        attractionMapper.updateById(attraction);
        log.info("景点封面上传成功: id={}, url={}", id, url);
        return url;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadImages(Long id, MultipartFile[] files) {
        Attraction attraction = getById(id);

        List<String> imageList = parseImages(attraction.getImages());

        for (MultipartFile file : files) {
            String url = uploadConfig.saveFile(file, "attractions");
            imageList.add(url);
        }

        try {
            attraction.setImages(objectMapper.writeValueAsString(imageList));
        } catch (JsonProcessingException e) {
            throw new ServiceException(500, "图片数据序列化失败");
        }
        attractionMapper.updateById(attraction);
        log.info("景点图片上传成功: id={}, 新增{}张", id, files.length);
        return attraction.getImages();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Attraction removeImage(Long id, String imageUrl) {
        Attraction attraction = getById(id);

        List<String> imageList = parseImages(attraction.getImages());
        if (!imageList.remove(imageUrl)) {
            throw new ServiceException(400, "图片不存在");
        }

        try {
            attraction.setImages(objectMapper.writeValueAsString(imageList));
        } catch (JsonProcessingException e) {
            throw new ServiceException(500, "图片数据序列化失败");
        }
        attractionMapper.updateById(attraction);
        return attraction;
    }

    private List<String> parseImages(String imagesJson) {
        if (imagesJson == null || imagesJson.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.warn("解析景点图片JSON失败: {}", imagesJson, e);
            return new ArrayList<>();
        }
    }
}
