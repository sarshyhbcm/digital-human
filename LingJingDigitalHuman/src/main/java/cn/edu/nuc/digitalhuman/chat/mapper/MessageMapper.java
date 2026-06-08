package cn.edu.nuc.digitalhuman.chat.mapper;

import cn.edu.nuc.digitalhuman.chat.entity.Message;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
