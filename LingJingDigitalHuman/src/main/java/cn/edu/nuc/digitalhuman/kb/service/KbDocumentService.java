package cn.edu.nuc.digitalhuman.kb.service;

import cn.edu.nuc.digitalhuman.kb.entity.KbDocument;

import java.util.List;

public interface KbDocumentService {
    KbDocument importFile(String filePath);

    List<KbDocument> listDocuments();

    void deleteDocument(Long id);
}
