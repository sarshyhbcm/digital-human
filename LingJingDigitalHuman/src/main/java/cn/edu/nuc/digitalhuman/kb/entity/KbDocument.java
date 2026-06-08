package cn.edu.nuc.digitalhuman.kb.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kb_document")
public class KbDocument {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String fileName;

    private String filePath;

    private String fileType;

    private Long fileSize;

    private Integer status; // 0=未解析 1=已解析 2=解析失败

    private Integer chunkCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
