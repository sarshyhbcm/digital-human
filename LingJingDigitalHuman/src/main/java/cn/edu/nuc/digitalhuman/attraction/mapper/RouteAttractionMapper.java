package cn.edu.nuc.digitalhuman.attraction.mapper;

import cn.edu.nuc.digitalhuman.attraction.entity.RouteAttraction;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 路线-景点关联 Mapper
 */
@Mapper
public interface RouteAttractionMapper extends BaseMapper<RouteAttraction> {
}
