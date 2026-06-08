package cn.edu.nuc.digitalhuman.kb;

import cn.edu.nuc.digitalhuman.kb.service.KbDocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

@Slf4j
@Component
public class KbStartupImporter implements CommandLineRunner {

    private final KbDocumentService documentService;

    @Value("${kb.docs-path}")
    private String docsPath;

    public KbStartupImporter(KbDocumentService documentService) {
        this.documentService = documentService;
    }

    @Override
    public void run(String... args) {
        Path dir = Path.of(docsPath);
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            log.warn("知识库目录不存在: {}", docsPath);
            return;
        }

        log.info("开始导入知识库文档: {}", docsPath);

        try (Stream<Path> files = Files.list(dir)) {
            files.filter(f -> {
                String name = f.getFileName().toString().toLowerCase();
                return name.endsWith(".docx") || name.endsWith(".txt");
            }).sorted(Comparator.comparing(p -> p.getFileName().toString()))
              .forEach(this::importFile);
        } catch (IOException e) {
            log.error("扫描知识库目录失败", e);
        }
    }

    private void importFile(Path file) {
        try {
            documentService.importFile(file.toAbsolutePath().toString());
        } catch (Exception e) {
            log.warn("导入知识库文件失败: {} - {}", file.getFileName(), e.getMessage());
        }
    }
}
