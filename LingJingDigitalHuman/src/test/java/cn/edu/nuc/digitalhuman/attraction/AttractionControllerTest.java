package cn.edu.nuc.digitalhuman.attraction;

import cn.edu.nuc.digitalhuman.attraction.controller.AttractionController;
import cn.edu.nuc.digitalhuman.attraction.entity.Attraction;
import cn.edu.nuc.digitalhuman.attraction.service.AttractionService;
import cn.edu.nuc.digitalhuman.common.utils.JwtUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AttractionController 单元测试
 *
 * 测试景点查询接口：
 * 1. GET /api/attractions — 列表查询（分页+区域筛选）
 * 2. GET /api/attractions/{id} — 详情查询
 *
 * 注意：@WebMvcTest 只会加载 Controller 层，
 * JwtAuthFilter 会尝试注入 JwtUtils，所以要用 @MockitoBean 提供 mock。
 */
@WebMvcTest(AttractionController.class)
@Import(cn.edu.nuc.digitalhuman.auth.filter.JwtAuthFilter.class)
public class AttractionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttractionService attractionService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @Test
    void testList() throws Exception {
        when(attractionService.page(any(), isNull())).thenReturn(new Page<>(1, 10));

        mockMvc.perform(get("/api/attractions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testListWithArea() throws Exception {
        when(attractionService.page(any(), eq("灵山胜境"))).thenReturn(new Page<>(1, 10));

        mockMvc.perform(get("/api/attractions").param("area", "灵山胜境"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testDetail() throws Exception {
        Attraction mockAttraction = new Attraction();
        mockAttraction.setId(1L);
        mockAttraction.setName("灵山大佛");
        mockAttraction.setArea("灵山胜境");

        when(attractionService.getById(1L)).thenReturn(mockAttraction);

        mockMvc.perform(get("/api/attractions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("灵山大佛"))
                .andExpect(jsonPath("$.data.area").value("灵山胜境"));
    }

    @Test
    void testDetailNotFound() throws Exception {
        when(attractionService.getById(999L)).thenReturn(null);

        mockMvc.perform(get("/api/attractions/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
