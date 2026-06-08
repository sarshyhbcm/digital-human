package cn.edu.nuc.digitalhuman.attraction.mapper;

import cn.edu.nuc.digitalhuman.attraction.entity.Attraction;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 景点 Mapper
 *
 * MyBatis-Plus 的 BaseMapper 已经提供了基础的 CRUD 方法，
 * 包括 selectPage（分页查询）、selectById（按ID查）等。
 * 如果后面需要复杂的联表查询，在这里加 @Select 注解即可。
 */
@Mapper
public interface AttractionMapper extends BaseMapper<Attraction> {
}
