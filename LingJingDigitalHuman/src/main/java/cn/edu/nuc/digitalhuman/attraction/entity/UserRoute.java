package cn.edu.nuc.digitalhuman.attraction.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_route")
public class UserRoute {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 路线名称 */
    private String name;

    /** 排序后的景点ID列表(JSON数组) */
    private String attractionIds;

    /** 总路程(米) */
    private Integer totalDistanceMeters;

    /** 预计耗时(分钟) */
    private Integer estimatedMinutes;

    /** 景点数量 */
    private Integer attractionCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
