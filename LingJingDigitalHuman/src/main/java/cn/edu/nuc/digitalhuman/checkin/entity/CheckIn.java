package cn.edu.nuc.digitalhuman.checkin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("check_in")
public class CheckIn {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long attractionId;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String checkInType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
