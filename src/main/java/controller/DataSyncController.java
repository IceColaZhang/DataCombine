package com.jxdinfo.hussar.example.controller;

import com.jxdinfo.hussar.example.service.DataSyncService;
import com.jxdinfo.hussar.example.service.impl.DataSyncServiceImpl;
import com.jxdinfo.hussar.platform.core.base.apiresult.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dataSync")
public class DataSyncController {

    @Autowired
    private DataSyncService dataSyncService;

    @GetMapping("/syncData")
    public ApiResponse<String> syncData() {
        try {
            dataSyncService.syncAllTables();
            return ApiResponse.success("数据同步任务已触发，请检查同步数据！");
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.fail("数据同步任务触发失败：" + e.getMessage());
        }
    }
}

