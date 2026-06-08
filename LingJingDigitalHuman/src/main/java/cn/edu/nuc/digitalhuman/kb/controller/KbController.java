package cn.edu.nuc.digitalhuman.kb.controller;

import cn.edu.nuc.digitalhuman.kb.entity.KbChunk;
import cn.edu.nuc.digitalhuman.kb.entity.KbDocument;
import cn.edu.nuc.digitalhuman.kb.service.KbDocumentService;
import cn.edu.nuc.digitalhuman.kb.service.KbSearchService;
import cn.edu.nuc.digitalhuman.common.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kb")
@RequiredArgsConstructor
public class KbController {

    private final KbDocumentService documentService;
    private final KbSearchService searchService;

    @GetMapping("/documents")
    public R<List<KbDocument>> listDocuments() {
        return R.success(documentService.listDocuments());
    }

    @PostMapping("/import")
    public R<KbDocument> importDocument(@RequestBody Map<String, String> body) {
        String filePath = body.get("filePath");
        if (filePath == null || filePath.isBlank()) {
            return R.error(400, "filePath 不能为空");
        }
        try {
            KbDocument doc = documentService.importFile(filePath);
            return R.success(doc);
        } catch (Exception e) {
            return R.error("导入失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/documents/{id}")
    public R<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return R.success();
    }

    @GetMapping("/search")
    public R<List<KbChunk>> search(@RequestParam("q") String query,
                                   @RequestParam(value = "limit", defaultValue = "5") int limit) {
        return R.success(searchService.search(query, limit));
    }
}
