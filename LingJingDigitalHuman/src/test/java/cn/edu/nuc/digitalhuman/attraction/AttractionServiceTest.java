package cn.edu.nuc.digitalhuman.attraction;

import cn.edu.nuc.digitalhuman.attraction.entity.Attraction;
import cn.edu.nuc.digitalhuman.attraction.mapper.AttractionMapper;
import cn.edu.nuc.digitalhuman.attraction.service.impl.AttractionServiceImpl;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AttractionService 单元测试
 *
 * 测试景点查询业务逻辑：
 * 1. 分页查询（全部区域）
 * 2. 按区域筛选
 * 3. 按ID查详情
 */
@ExtendWith(MockitoExtension.class)
public class AttractionServiceTest {

    @Mock
    private AttractionMapper attractionMapper;

    @InjectMocks
    private AttractionServiceImpl attractionService;

    @SuppressWarnings("unchecked")
    @Test
    void testPageAll() {
        when(attractionMapper.selectPage(any(Page.class), any())).thenReturn(new Page<>(1, 10));

        IPage<Attraction> result = attractionService.page(new Page<>(1, 10), null);

        assertNotNull(result);
        verify(attractionMapper, times(1)).selectPage(any(Page.class), any());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testPageWithArea() {
        when(attractionMapper.selectPage(any(Page.class), any())).thenReturn(new Page<>(1, 10));

        IPage<Attraction> result = attractionService.page(new Page<>(1, 10), "灵山胜境");

        assertNotNull(result);
        verify(attractionMapper, times(1)).selectPage(any(Page.class), any());
    }

    @Test
    void testGetById() {
        Attraction mockAttraction = new Attraction();
        mockAttraction.setId(1L);
        mockAttraction.setName("灵山大佛");

        when(attractionMapper.selectById(1L)).thenReturn(mockAttraction);

        Attraction result = attractionService.getById(1L);

        assertEquals(1L, result.getId());
        assertEquals("灵山大佛", result.getName());
        verify(attractionMapper, times(1)).selectById(1L);
    }

    @Test
    void testGetByIdNotFound() {
        when(attractionMapper.selectById(999L)).thenReturn(null);

        Attraction result = attractionService.getById(999L);

        assertNull(result);
        verify(attractionMapper, times(1)).selectById(999L);
    }
}
