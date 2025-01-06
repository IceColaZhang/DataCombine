package com.jxdinfo.hussar.example.dao;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface DataSyncMapper {

    // 查询需要同步的表信息
    @Select("SELECT id, table_name, row_id, last_update_time,is_delete_id FROM t_base_data_sync")
    List<Map<String, Object>> getTablesToSync();

    // 更新同步状态
    @Update("UPDATE t_base_data_sync SET row_id = #{rowId}, last_update_time = NOW(), msg = #{msg} WHERE id = #{id}")
    void updateSyncStatus(@Param("id") Long id, @Param("rowId") Long rowId, @Param("msg") String msg);
}


