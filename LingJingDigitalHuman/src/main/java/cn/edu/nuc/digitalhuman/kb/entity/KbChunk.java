package cn.edu.nuc.digitalhuman.kb.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kb_chunk")
public class KbChunk {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long documentId;

    private String content;

    private Integer seq;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
