package com.jxdinfo.hussar.example.dao;


import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface CommonMapper {

    // 插入通用方法
    void insertData(@Param("tableName") String tableName,  @Param("insertDates")List<List<Object>> insertDates,@Param("mappingFields") List<String> mappingFields);

    //查询通用方法
    @Select("SELECT * FROM ${tableName} ${conditions}")
    List<Map<String, Object>> executeSelect(@Param("tableName") String tableName, @Param("conditions") String conditions);
}

