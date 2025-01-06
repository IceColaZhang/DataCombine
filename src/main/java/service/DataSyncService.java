package com.jxdinfo.hussar.example.service;

import com.baomidou.dynamic.datasource.annotation.DS;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DataSyncService {
    @DS("slave") // 使用 威海 数据源
    public List<Map<String, Object>> fetchPageData(String tableName, Long lastRowId, int pageSize);

    @DS("base") // 使用 科技园 数据源
    public void insertData(String tableName, List<List<Object>> insertDataList, List<String> mappingFields);

    void syncAllTables();
}
