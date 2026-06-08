package cn.edu.nuc.digitalhuman.attraction;

import cn.edu.nuc.digitalhuman.attraction.controller.TourRouteController;
import cn.edu.nuc.digitalhuman.attraction.entity.Attraction;
import cn.edu.nuc.digitalhuman.attraction.entity.TourRoute;
import cn.edu.nuc.digitalhuman.attraction.service.TourRouteService;
import cn.edu.nuc.digitalhuman.common.utils.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TourRouteController 单元测试
 *
 * 测试路线查询接口：
 * 1. GET /api/routes — 路线列表
 * 2. GET /api/routes/{id} — 路线详情（含景点列表）
 */
@WebMvcTest(TourRouteController.class)
@Import(cn.edu.nuc.digitalhuman.auth.filter.JwtAuthFilter.class)
public class TourRouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TourRouteService tourRouteService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @Test
    void testList() throws Exception {
        TourRoute route = new TourRoute();
        route.setId(1L);
        route.setName("历史文化爱好者路线");

        when(tourRouteService.listAll()).thenReturn(List.of(route));

        mockMvc.perform(get("/api/routes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].name").value("历史文化爱好者路线"));
    }

    @Test
    void testDetail() throws Exception {
        TourRoute route = new TourRoute();
        route.setId(1L);
        route.setName("历史文化爱好者路线");

        Attraction attraction = new Attraction();
        attraction.setId(1L);
        attraction.setName("灵山大佛");

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("route", route);
        detail.put("attractions", List.of(attraction));

        when(tourRouteService.getDetail(1L)).thenReturn(detail);

        mockMvc.perform(get("/api/routes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.route.name").value("历史文化爱好者路线"))
                .andExpect(jsonPath("$.data.attractions[0].name").value("灵山大佛"));
    }
}
