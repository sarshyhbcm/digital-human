package cn.edu.nuc.digitalhuman.attraction.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 推荐路线实体
 */
@Data
@TableName("tour_route")
public class TourRoute {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 路线名称 */
    private String name;

    /** 路线描述 */
    private String description;

    /** 建议时长(如"6小时") */
    private String duration;

    /** 封面图 */
    private String coverImage;

    /** 排序 */
    private Integer sortOrder;

    /** 1=显示 0=隐藏 */
    private Integer status;

    /** 标签(逗号分隔)，如"历史文化,人文" */
    private String tags;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
