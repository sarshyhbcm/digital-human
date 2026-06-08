package cn.edu.nuc.digitalhuman.common.controller;

import cn.edu.nuc.digitalhuman.attraction.entity.Attraction;
import cn.edu.nuc.digitalhuman.attraction.mapper.AttractionMapper;
import cn.edu.nuc.digitalhuman.common.utils.QrCodeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/qrcodes")
@RequiredArgsConstructor
public class QrCodeImageController {

    private final AttractionMapper attractionMapper;

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> image(@PathVariable Long id) {
        Attraction attraction = attractionMapper.selectById(id);
        if (attraction == null || attraction.getQrCode() == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] png = QrCodeUtils.generatePng(attraction.getQrCode());

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(png);
    }
}
