package cn.edu.nuc.digitalhuman.attraction.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 景点信息实体
 */
@Data
@TableName("attraction")
public class Attraction {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 景点名称 */
    private String name;

    /** 简要介绍 */
    private String description;

    /** 详细介绍 */
    private String detail;

    /** 所属区域(灵山胜境/拈花湾) */
    private String area;

    /** 纬度 */
    private java.math.BigDecimal latitude;

    /** 经度 */
    private java.math.BigDecimal longitude;

    /** 封面图URL */
    private String coverImage;

    /** 更多图片(JSON数组) */
    private String images;

    /** 开放时间 */
    private String openingHours;

    /** 建议游览时长 */
    private String duration;

    /** 标签(逗号分隔) */
    private String tags;

    /** 二维码URL */
    private String qrCode;

    /** 排序 */
    private Integer sortOrder;

    /** 状态 1=显示 0=隐藏 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
