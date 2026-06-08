package cn.edu.nuc.digitalhuman.admin.controller;

import cn.edu.nuc.digitalhuman.attraction.entity.Attraction;
import cn.edu.nuc.digitalhuman.attraction.mapper.AttractionMapper;
import cn.edu.nuc.digitalhuman.common.result.R;
import cn.edu.nuc.digitalhuman.common.utils.QrCodeUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/qrcodes")
@RequiredArgsConstructor
public class QrCodeAdminController {

    private final AttractionMapper attractionMapper;

    @GetMapping
    public R<List<Attraction>> list() {
        List<Attraction> list = attractionMapper.selectList(
                new LambdaQueryWrapper<Attraction>()
                        .eq(Attraction::getStatus, 1)
                        .orderByAsc(Attraction::getSortOrder));
        return R.success(list);
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String qrCode = body.get("qrCode");
        Attraction attraction = attractionMapper.selectById(id);
        if (attraction == null) {
            return R.notFound("景点不存在");
        }
        attraction.setQrCode(qrCode);
        attractionMapper.updateById(attraction);
        return R.success();
    }

    @PostMapping("/{id}/generate")
    public R<Map<String, Object>> generate(@PathVariable Long id) {
        Attraction attraction = attractionMapper.selectById(id);
        if (attraction == null) {
            return R.notFound("景点不存在");
        }

        String qrContent = "attraction:" + id;
        attraction.setQrCode(qrContent);
        attractionMapper.updateById(attraction);

        Map<String, Object> result = Map.of(
                "id", id,
                "qrCode", qrContent
        );
        return R.success(result);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> downloadImage(@PathVariable Long id) {
        Attraction attraction = attractionMapper.selectById(id);
        if (attraction == null || attraction.getQrCode() == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] png = QrCodeUtils.generatePng(attraction.getQrCode());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + attraction.getName() + "_二维码.png\"")
                .contentType(MediaType.IMAGE_PNG)
                .body(png);
    }
}
