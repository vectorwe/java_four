package com.score.dao;

import com.score.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 数据库操作层：建表+批量插入
 */
public class ImportDao {
    /**
     * 创建动态表（根据表头）
     */
    public void createTable(String tableName, List<String> headers) throws SQLException {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");
        for (int i = 0; i < headers.size(); i++) {
            String field = headers.get(i);
            sql.append("`").append(field).append("` VARCHAR(255)");
            if (i < headers.size() - 1) sql.append(", ");
        }
        sql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            pstmt.executeUpdate();
        }
    }

    /**
     * 批量插入Excel数据
     */
    public void batchInsert(String tableName, List<String> headers, List<Map<Integer, String>> dataList) throws SQLException {
        if (dataList.isEmpty()) throw new SQLException("无有效数据！");

        // 拼接插入SQL
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        for (int i = 0; i < headers.size(); i++) {
            sql.append("`").append(headers.get(i)).append("`");
            if (i < headers.size() - 1) sql.append(", ");
        }
        sql.append(") VALUES (");
        for (int i = 0; i < headers.size(); i++) {
            sql.append("?");
            if (i < headers.size() - 1) sql.append(", ");
        }
        sql.append(")");

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            conn.setAutoCommit(false);
            for (Map<Integer, String> row : dataList) {
                for (int i = 0; i < headers.size(); i++) {
                    pstmt.setString(i + 1, row.getOrDefault(i, ""));
                }
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();
        }
    }
}