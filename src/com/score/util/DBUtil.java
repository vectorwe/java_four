package com.score.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库连接工具类（统一配置，适配所有场景）
 * 合并功能：
 * 1. 基础数据库连接/关闭资源
 * 2. 自定义建表（支持用户输入表名/列名，自动处理重复列名）
 * 3. 批量插入数据到自定义表
 * 4. 检查表名是否存在
 */
public class DBUtil {
    // ========== 核心配置（根据你的实际MySQL配置修改） ==========
    private static final String URL = "jdbc:mysql://localhost:3306/java";
    private static final String USER = "root";
    private static final String PASSWORD = "root"; // 你的MySQL密码

    // 加载MySQL驱动（仅执行一次）
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("[DBUtil] MySQL驱动加载成功");
        } catch (ClassNotFoundException e) {
            System.err.println("[DBUtil] MySQL驱动加载失败：" + e.getMessage());
            throw new RuntimeException("驱动加载失败，无法连接数据库", e);
        }
    }

    /**
     * 获取数据库连接（抛出运行时异常，上层无需捕获）
     */
    public static Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[DBUtil] 数据库连接成功：" + URL);
            return conn;
        } catch (SQLException e) {
            System.err.println("[DBUtil] 数据库连接失败：" + e.getMessage());
            // 优化：区分不同连接错误，给出更明确的提示
            String errorMsg = "数据库连接失败：";
            if (e.getMessage().contains("Access denied")) {
                errorMsg += "用户名/密码错误，请检查配置";
            } else if (e.getMessage().contains("Unknown database")) {
                errorMsg += "数据库不存在，请确认数据库名是否正确（当前配置：java）";
            } else {
                errorMsg += e.getMessage();
            }
            throw new RuntimeException(errorMsg, e);
        }
    }

    // ==================== 关闭资源方法（重载） ====================
    /**
     * 关闭数据库资源（重载1：支持PreparedStatement+ResultSet）
     */
    public static void close(Connection conn, PreparedStatement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
            System.out.println("[DBUtil] 资源关闭成功");
        } catch (SQLException e) {
            System.err.println("[DBUtil] 关闭资源失败：" + e.getMessage());
        }
    }

    /**
     * 关闭数据库资源（重载2：仅PreparedStatement）
     */
    public static void close(Connection conn, PreparedStatement stmt) {
        close(conn, stmt, null);
    }

    /**
     * 关闭数据库资源（重载3：支持Statement+ResultSet）
     */
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
            System.out.println("[DBUtil] Statement资源关闭成功");
        } catch (SQLException e) {
            System.err.println("[DBUtil] 关闭Statement资源失败：" + e.getMessage());
        }
    }

    // ==================== 新增：检查表名是否存在 ====================
    /**
     * 检查表名是否已存在
     * @param tableName 要检查的表名
     * @return 存在返回true，不存在返回false
     */
    public static boolean isTableExists(String tableName) {
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            // 查询当前数据库的所有表
            rs = conn.getMetaData().getTables(null, null, tableName, new String[]{"TABLE"});
            return rs.next(); // 存在则返回true
        } catch (SQLException e) {
            System.err.println("[DBUtil] 检查表名失败：" + e.getMessage());
            return false;
        } finally {
            close(conn, (Statement) null, rs);
        }
    }

    // ==================== 新增：自定义表操作方法 ====================
    /**
     * 创建自定义数据表（核心优化：自动处理重复列名）
     * @param tableName 表名（用户输入）
     * @param columnNames 列名列表（用户输入的表头）
     * @return 是否创建成功
     */
    public static boolean createCustomTable(String tableName, List<String> columnNames) {
        // 1. 表名安全过滤（仅保留字母、数字、下划线）
        String safeTableName = tableName.replaceAll("[^a-zA-Z0-9_]", "");
        if (safeTableName.isEmpty()) {
            System.err.println("[DBUtil] 表名过滤后为空，创建失败");
            return false;
        }

        // 2. 列名处理：安全过滤 + 自动去重（解决Duplicate column问题）
        List<String> uniqueColumns = new ArrayList<>();
        for (String col : columnNames) {
            // 列名安全过滤
            String safeCol = col.replaceAll("[^a-zA-Z0-9_]", "");
            // 空列名兜底
            if (safeCol.isEmpty()) {
                safeCol = "col_" + (uniqueColumns.size() + 1);
            }
            // 重复列名自动加序号（如 mah → mah_1 → mah_2）
            String finalCol = safeCol;
            int index = 1;
            while (uniqueColumns.contains(finalCol)) {
                finalCol = safeCol + "_" + index++;
            }
            uniqueColumns.add(finalCol);
        }

        // 3. 构建建表SQL（所有列默认VARCHAR(255)，新增主键id）
        StringBuilder createSql = new StringBuilder();
        createSql.append("CREATE TABLE IF NOT EXISTS ").append(safeTableName).append(" (");
        createSql.append("id INT AUTO_INCREMENT PRIMARY KEY, "); // 主键列，保证数据唯一性

        for (int i = 0; i < uniqueColumns.size(); i++) {
            String colName = uniqueColumns.get(i);
            createSql.append(colName).append(" VARCHAR(255)");
            if (i < uniqueColumns.size() - 1) {
                createSql.append(", ");
            }
        }
        createSql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

        // 4. 执行建表操作
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(createSql.toString());
            pstmt.executeUpdate();
            System.out.println("[DBUtil] 表创建成功：" + safeTableName);
            System.out.println("[DBUtil] 最终列名：" + uniqueColumns); // 打印最终列名，方便调试
            return true;
        } catch (Exception e) {
            System.err.println("[DBUtil] 创建表失败：" + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            close(conn, pstmt); // 关闭资源
        }
    }

    /**
     * 批量插入数据到自定义表
     * @param tableName 表名
     * @param columnNames 列名列表
     * @param dataRows 数据行（二维数组）
     * @return 插入成功的行数
     */
    public static int batchInsertData(String tableName, List<String> columnNames, List<Object[]> dataRows) {
        // 空数据校验
        if (dataRows.isEmpty() || columnNames.isEmpty()) {
            System.out.println("[DBUtil] 无有效数据/列名，无需插入");
            return 0;
        }

        // 安全过滤表名/列名（和建表逻辑保持一致）
        String safeTableName = tableName.replaceAll("[^a-zA-Z0-9_]", "");
        List<String> safeColumnNames = new ArrayList<>();
        for (String col : columnNames) {
            String safeCol = col.replaceAll("[^a-zA-Z0-9_]", "");
            if (!safeCol.isEmpty()) {
                safeColumnNames.add(safeCol);
            }
        }
        if (safeColumnNames.isEmpty()) {
            System.err.println("[DBUtil] 列名过滤后为空，插入失败");
            return 0;
        }

        // 构建插入SQL
        StringBuilder insertSql = new StringBuilder();
        insertSql.append("INSERT INTO ").append(safeTableName).append(" (");
        for (int i = 0; i < safeColumnNames.size(); i++) {
            insertSql.append(safeColumnNames.get(i));
            if (i < safeColumnNames.size() - 1) {
                insertSql.append(", ");
            }
        }
        insertSql.append(") VALUES (");
        for (int i = 0; i < safeColumnNames.size(); i++) {
            insertSql.append("?");
            if (i < safeColumnNames.size() - 1) {
                insertSql.append(", ");
            }
        }
        insertSql.append(")");

        // 执行批量插入
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // 开启事务，保证批量插入原子性
            pstmt = conn.prepareStatement(insertSql.toString());

            // 批量添加数据
            int count = 0;
            for (Object[] row : dataRows) {
                for (int i = 0; i < safeColumnNames.size(); i++) {
                    // 防止数组越界，空值兜底为空字符串
                    Object value = i < row.length ? row[i] : "";
                    pstmt.setObject(i + 1, value == null ? "" : value);
                }
                pstmt.addBatch();
                count++;
            }

            // 执行批量插入
            pstmt.executeBatch();
            conn.commit(); // 提交事务
            System.out.println("[DBUtil] 批量插入成功，共插入 " + count + " 行");
            return count;
        } catch (Exception e) {
            try {
                if (conn != null) conn.rollback(); // 失败回滚
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("[DBUtil] 批量插入失败：" + e.getMessage());
            e.printStackTrace();
            return 0;
        } finally {
            close(conn, pstmt); // 关闭资源
        }
    }
}