package cn.edu.nuc.digitalhuman.kb.service.impl;

import cn.edu.nuc.digitalhuman.kb.entity.KbChunk;
import cn.edu.nuc.digitalhuman.kb.entity.KbDocument;
import cn.edu.nuc.digitalhuman.kb.mapper.KbChunkMapper;
import cn.edu.nuc.digitalhuman.kb.mapper.KbDocumentMapper;
import cn.edu.nuc.digitalhuman.kb.service.KbDocumentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KbDocumentServiceImpl implements KbDocumentService {

    private final KbDocumentMapper documentMapper;
    private final KbChunkMapper chunkMapper;

    @Override
    @Transactional
    public KbDocument importFile(String filePath) {
        Path path = Path.of(filePath);
        if (!Files.exists(path)) {
            throw new RuntimeException("文件不存在: " + filePath);
        }

        String fileName = path.getFileName().toString();
        String fileType = fileName.endsWith(".docx") ? "docx"
                : fileName.endsWith(".xlsx") ? "xlsx"
                : fileName.endsWith(".txt") ? "txt" : "unknown";

        // 检查是否已导入
        KbDocument exist = documentMapper.selectOne(
                new LambdaQueryWrapper<KbDocument>().eq(KbDocument::getFileName, fileName));
        if (exist != null && exist.getStatus() == 1) {
            log.info("文件已导入，跳过: {}", fileName);
            return exist;
        }

        KbDocument doc = new KbDocument();
        doc.setTitle(fileName.replaceFirst("\\.\\w+$", ""));
        doc.setFileName(fileName);
        doc.setFilePath(filePath);
        doc.setFileType(fileType);
        try {
            doc.setFileSize(Files.size(path));
        } catch (IOException e) {
            doc.setFileSize(0L);
        }
        doc.setStatus(0);
        doc.setChunkCount(0);
        documentMapper.insert(doc);

        // 解析文本
        String rawText;
        try {
            rawText = extractText(path, fileType);
        } catch (Exception e) {
            log.error("解析文件失败: {}", fileName, e);
            doc.setStatus(2);
            documentMapper.updateById(doc);
            throw new RuntimeException("解析文件失败: " + fileName, e);
        }

        if (rawText.isBlank()) {
            doc.setStatus(2);
            documentMapper.updateById(doc);
            log.warn("文件内容为空: {}", fileName);
            return doc;
        }

        // 切片
        List<String> chunks = chunkText(rawText);
        int seq = 0;
        for (String chunk : chunks) {
            KbChunk kbChunk = new KbChunk();
            kbChunk.setDocumentId(doc.getId());
            kbChunk.setContent(chunk);
            kbChunk.setSeq(seq++);
            chunkMapper.insert(kbChunk);
        }

        doc.setStatus(1);
        doc.setChunkCount(chunks.size());
        documentMapper.updateById(doc);

        log.info("已导入知识库文档: {}, 切片数: {}", fileName, chunks.size());
        return doc;
    }

    @Override
    public List<KbDocument> listDocuments() {
        return documentMapper.selectList(
                Wrappers.<KbDocument>lambdaQuery().orderByDesc(KbDocument::getCreatedAt));
    }

    @Override
    @Transactional
    public void deleteDocument(Long id) {
        chunkMapper.delete(new LambdaQueryWrapper<KbChunk>().eq(KbChunk::getDocumentId, id));
        documentMapper.deleteById(id);
    }

    private String extractText(Path path, String fileType) throws Exception {
        return switch (fileType) {
            case "docx" -> extractDocx(path);
            case "xlsx" -> extractXlsx(path);
            case "txt" -> Files.readString(path);
            default -> throw new RuntimeException("不支持的文件类型: " + fileType);
        };
    }

    private String extractDocx(Path path) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (XWPFDocument doc = new XWPFDocument(new FileInputStream(path.toFile()))) {
            for (XWPFParagraph para : doc.getParagraphs()) {
                String text = para.getText().trim();
                if (!text.isEmpty()) {
                    sb.append(text).append("\n\n");
                }
            }
        }
        return sb.toString();
    }

    private String extractXlsx(Path path) {
        StringBuilder sb = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(path.toFile())) {
            // 使用 Apache POI 的 XSSFWorkbook 读取（无需引入额外依赖，poi-ooxml 已包含）
            org.apache.poi.xssf.usermodel.XSSFWorkbook workbook =
                    new org.apache.poi.xssf.usermodel.XSSFWorkbook(fis);
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                var sheet = workbook.getSheetAt(i);
                sb.append("### ").append(sheet.getSheetName()).append("\n\n");
                for (var row : sheet) {
                    for (var cell : row) {
                        String val = switch (cell.getCellType()) {
                            case STRING -> cell.getStringCellValue();
                            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
                            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                            default -> "";
                        };
                        if (!val.isBlank()) {
                            sb.append(val).append("\t");
                        }
                    }
                    sb.append("\n");
                }
                sb.append("\n\n");
            }
            workbook.close();
        } catch (Exception e) {
            log.warn("xlsx解析异常，尝试EasyExcel: {}", e.getMessage());
        }
        return sb.toString();
    }

    /**
     * 将文本切片：按段落分割，长段落按句子分割
     */
    private List<String> chunkText(String text) {
        List<String> result = new ArrayList<>();
        // 按段落分割
        String[] paragraphs = text.split("\\n\\s*\\n");
        for (String para : paragraphs) {
            para = para.trim();
            if (para.length() < 10) continue; // 太短忽略

            if (para.length() <= 500) {
                result.add(para);
            } else {
                // 长段落按句子分割
                String[] sentences = para.split("(?<=[。！？])");
                StringBuilder buf = new StringBuilder();
                for (String sent : sentences) {
                    sent = sent.trim();
                    if (sent.isEmpty()) continue;
                    if (buf.length() + sent.length() > 500 && !buf.isEmpty()) {
                        result.add(buf.toString().trim());
                        buf = new StringBuilder();
                    }
                    buf.append(sent);
                }
                if (!buf.isEmpty()) {
                    result.add(buf.toString().trim());
                }
            }
        }
        return result;
    }
}
