# DataCombine

DataCombine 是一个用于实现跨库数据合并的工具，能够从源数据库中读取数据，合并后同步到目标数据库，并记录同步过程中的信息。

## 功能简介
- 支持多数据源配置（主数据库、源端数据库、目标数据库）。
- 提供数据同步进度记录（包括同步条数、耗时、错误信息等）。
- 自动记录同步的更新时间。

## 安装与运行

### 1. 克隆项目

https://github.com/IceColaZhang/DataCombine.git

### 2. 配置 application.yml
#### 在项目目录下的 src/main/resources/application.yml 中配置数据库信息。
以下是数据源的具体配置说明：

（1）主数据库 (master)：用于记录同步的中间数据，配置如下：

	-- 表名：
	
	t_base_data_sync

	-- 字段说明：

	row_id：业务表的主键。
	
	table_name:业务表名。

	msg：同步信息，包含同步条数、报错信息及同步耗时。
	
	is_delete_id：标识为1时，删除源端表主键（适用于目标端表主键递增的情况）

	last_update_time：记录最新的同步时间。
	
	-- 建表语句：
		CREATE TABLE `t_base_data_sync` (
 		 `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  		`row_id` bigint(20) DEFAULT NULL COMMENT '业务表主键',
  		`table_name` varchar(255) DEFAULT NULL COMMENT '业务表名',
  		`last_update_time` datetime DEFAULT NULL COMMENT '最近更新时间',
  		`msg` text COMMENT '错误日志',
  		`is_delete_id` varchar(1) DEFAULT NULL COMMENT '是否删除主键',
  		PRIMARY KEY (`id`) USING BTREE
		) ENGINE=InnoDB AUTO_INCREMENT=140 DEFAULT CHARSET=utf8mb4 COMMENT='数据同步中间表';

（2）源端数据库 (slave)：用于存储需要同步的原始数据。

（3）目标端数据库 (base)：用于存储同步后的数据。

示例配置（yml）：

	spring:
  	datasource:
    master:
      url: jdbc:mysql://<master-db-host>:3306/master
      username: <master-username>
      password: <master-password>
      driver-class-name: com.mysql.cj.jdbc.Driver

    slave:
      url: jdbc:mysql://<slave-db-host>:3306/slave
      username: <slave-username>
      password: <slave-password>
      driver-class-name: com.mysql.cj.jdbc.Driver

    base:
      url: jdbc:mysql://<base-db-host>:3306/base
      username: <base-username>
      password: <base-password>
      driver-class-name: com.mysql.cj.jdbc.Driver

## 使用说明
1.向中间表（t_base_data_sync）中初始数据，tabl_name为需要同步的表名，row_id统一更新为0,若需要在同步时删除源端表主键，则将is_delete_id字段标识为1

2.配置无误后，调用控制层DataSyncController中的接口syncData（/dataSync/syncData）

## 注意事项
1、请确保 application.yml 文件中配置的数据库信息正确无误。

2、源端和目标端数据库中的表结构需保持一致，否则可能导致同步失败。

3、日志文件中会记录详细的错误信息，请根据日志排查问题。
