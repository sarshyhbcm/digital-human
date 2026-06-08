package cn.edu.nuc.digitalhuman.common.config;

import cn.edu.nuc.digitalhuman.common.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
@Slf4j
public class UploadConfig {

    @Value("${upload.path}")
    private String uploadPath;

    /**
     * 保存上传文件
     *
     * @param file   上传的文件
     * @param subDir 子目录名，如 "avatars", "attractions", "banners"
     * @return 可访问的 URL 路径
     */
    public String saveFile(MultipartFile file, String subDir) {
        if (file.isEmpty()) {
            throw new ServiceException(400, "文件不能为空");
        }

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String fileName = subDir + "_" + UUID.randomUUID().toString().replace("-", "") + ext;

        try {
            Path dir = Paths.get(uploadPath, subDir);
            Files.createDirectories(dir);
            Files.write(dir.resolve(fileName), file.getBytes());
            String url = "/uploads/" + subDir + "/" + fileName;
            log.info("文件上传成功: {} -> {}", originalName, url);
            return url;
        } catch (IOException e) {
            log.error("文件上传失败: {}", originalName, e);
            throw new ServiceException(500, "文件上传失败");
        }
    }
}
