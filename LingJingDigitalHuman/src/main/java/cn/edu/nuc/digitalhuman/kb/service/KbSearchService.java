package cn.edu.nuc.digitalhuman.kb.service;

import cn.edu.nuc.digitalhuman.kb.entity.KbChunk;

import java.util.List;

public interface KbSearchService {
    List<KbChunk> search(String query, int limit);
}
