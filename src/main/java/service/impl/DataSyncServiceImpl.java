package com.jxdinfo.hussar.example.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.jxdinfo.hussar.example.dao.CommonMapper;
import com.jxdinfo.hussar.example.dao.DataSyncMapper;
import com.jxdinfo.hussar.example.service.DataSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataSyncServiceImpl implements DataSyncService {

    @Autowired
    private CommonMapper commonMapper;

    @Autowired
    private DataSyncMapper dataSyncMapper;

    @Autowired
    private DataSyncService dataSyncService;

    @DS("slave") // 从源数据库读取数据
    public List<Map<String, Object>> fetchPageData(String tableName, Long lastRowId, int pageSize) {
        // 按主键分页查询
        String conditions = " id > " + lastRowId + " ORDER BY id LIMIT " + pageSize;
        return commonMapper.executeSelect(tableName, conditions);
    }

    @DS("base") // 插入目标数据库
    public void insertData(String tableName, List<List<Object>> insertDataList, List<String> mappingFields) {
        commonMapper.insertData(tableName, insertDataList, mappingFields);
    }

    public void syncAllTables() {
        // 1. 查询需要同步的表
        List<Map<String, Object>> tablesToSync = dataSyncMapper.getTablesToSync();

        for (Map<String, Object> tableInfo : tablesToSync) {
            Long id = (Long) tableInfo.get("id");
            String tableName = (String) tableInfo.get("table_name");
            Long lastRowId = tableInfo.get("row_id") == null ? 0L : (Long) tableInfo.get("row_id");
            String msg = "";
            long startTime = System.currentTimeMillis();  // 记录开始时间
            int totalRows = 0; // 统计同步的条数
            String isDeleteId = (String) tableInfo.get("is_delete_id");

            try {
                boolean hasMoreData = true;

                while (hasMoreData) {
                    // 2. 按主键分页查询数据
                    List<Map<String, Object>> dataList = dataSyncService.fetchPageData(tableName, lastRowId, 1000);

                    if (dataList.isEmpty()) {
                        hasMoreData = false;
                        continue;
                    }

                    // 保留最后一条记录的原始 ID，用于更新 lastRowId
                    Long lastOriginalRowId = (Long) dataList.get(dataList.size() - 1).get("id");

                    // 将需要删除主键的表的 "id" 字段设置为 null
                    if ("1".equals(isDeleteId)) {
                        for (Map<String, Object> data : dataList) {
                            data.put("id", null);
                        }
                    }

                    // 3. 解析数据并准备插入
                    // 3.1. 确定字段顺序
                    Set<String> allKeys = new LinkedHashSet<>();
                    for (Map<String, Object> data : dataList) {
                        allKeys.addAll(data.keySet());
                    }
                    List<String> tableColumnKey = new ArrayList<>(allKeys);

                    // 3.2. 按字段顺序准备插入数据
                    List<List<Object>> insertDataList = new ArrayList<>();
                    for (Map<String, Object> data : dataList) {
                        List<Object> insertData = new ArrayList<>();
                        for (String fieldName : tableColumnKey) {
                            insertData.add(data.getOrDefault(fieldName, null)); // 使用 null 替代 ""
                        }
                        insertDataList.add(insertData);
                    }

                    // 4. 插入目标库
                    dataSyncService.insertData(tableName, insertDataList, tableColumnKey);

                    // 更新 lastRowId 为最新值
                    lastRowId = lastOriginalRowId;

                    // 统计同步的条数
                    totalRows =  totalRows + dataList.size();
                }

                // 计算同步时间
                long endTime = System.currentTimeMillis();  // 记录结束时间
                long duration = endTime - startTime;  // 计算同步时间

                // 将毫秒转换为秒
                long durationInSeconds = duration / 1000;  // 毫秒转换为秒

                msg = String.format("同步成功，耗时：%d秒，同步了%d条数据", durationInSeconds, totalRows);

            } catch (Exception e) {
                msg = "同步失败：" + e.getMessage();
                if (msg.getBytes().length > 65535) {
                    msg = new String(msg.getBytes(), 0, 65535); // 截取最多 65535 字节
                }

            }

            // 5. 更新同步状态，包括同步时间和同步条数
            dataSyncMapper.updateSyncStatus(id, lastRowId, msg);
        }
    }
}


