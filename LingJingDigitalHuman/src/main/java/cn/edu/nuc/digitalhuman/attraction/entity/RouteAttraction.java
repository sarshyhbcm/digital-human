package cn.edu.nuc.digitalhuman.attraction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 路线-景点关联实体
 *
 * 多对多关联表，记录每条路线包含哪些景点及其顺序。
 */
@Data
@TableName("route_attraction")
public class RouteAttraction {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 路线ID */
    private Long routeId;

    /** 景点ID */
    private Long attractionId;

    /** 在路线中的顺序 */
    private Integer sortOrder;
}
