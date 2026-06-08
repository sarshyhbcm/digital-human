package cn.edu.nuc.digitalhuman.hotsearch.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("hot_search")
public class HotSearch {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String question;

    private String heatLabel;

    private Integer sortOrder;

    private Boolean enabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
