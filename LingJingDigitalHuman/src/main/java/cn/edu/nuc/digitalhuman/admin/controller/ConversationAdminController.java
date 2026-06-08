package cn.edu.nuc.digitalhuman.admin.controller;

import cn.edu.nuc.digitalhuman.chat.entity.Conversation;
import cn.edu.nuc.digitalhuman.chat.entity.Message;
import cn.edu.nuc.digitalhuman.chat.mapper.ConversationMapper;
import cn.edu.nuc.digitalhuman.chat.mapper.MessageMapper;
import cn.edu.nuc.digitalhuman.common.result.R;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/conversations")
@RequiredArgsConstructor
public class ConversationAdminController {

    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;

    @GetMapping
    public R<Map<String, Object>> listConversations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword) {
        var query = new LambdaQueryWrapper<Conversation>();
        if (keyword != null && !keyword.isBlank()) {
            query.like(Conversation::getTitle, keyword);
        }
        query.orderByDesc(Conversation::getUpdatedAt);
        var pageResult = conversationMapper.selectPage(new Page<>(page, pageSize), query);

        Map<String, Object> result = new HashMap<>();
        result.put("records", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", pageResult.getCurrent());
        result.put("pageSize", pageResult.getSize());
        return R.success(result);
    }

    @GetMapping("/{id}/messages")
    public R<Map<String, Object>> getMessages(@PathVariable Long id) {
        var conversation = conversationMapper.selectById(id);
        if (conversation == null) {
            return R.notFound("对话不存在");
        }
        var messages = messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, id)
                        .orderByAsc(Message::getCreatedAt));
        Map<String, Object> result = new HashMap<>();
        result.put("conversation", conversation);
        result.put("messages", messages);
        return R.success(result);
    }
}
