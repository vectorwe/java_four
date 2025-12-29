package com.score.service;

import com.alibaba.excel.EasyExcel;
import com.score.dao.ImportDao;
import com.score.util.ExcelDataListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImportService {
    private final ImportDao importDao = new ImportDao();

    /**
     * 步骤1：创建数据库表单
     */
    public void createFormTable(String tableName, List<String> headers) {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("表单名称不能为空！");
        }
        if (headers == null || headers.isEmpty()) {
            throw new IllegalArgumentException("表头列表不能为空！");
        }

        try {
            importDao.createTable(tableName, headers);
        } catch (Exception e) {
            throw new RuntimeException("创建表失败：" + e.getMessage());
        }
    }

    /**
     * 步骤2：导入Excel数据
     */
    public void importExcelData(File file, String tableName, Set<String> requiredHeaders) {
        if (file == null || !file.exists()) {
            throw new RuntimeException("Excel文件不存在！");
        }
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new RuntimeException("目标表名不能为空！");
        }
        if (requiredHeaders == null || requiredHeaders.isEmpty()) {
            throw new RuntimeException("必填表头不能为空！");
        }

        try (InputStream inputStream = new FileInputStream(file)) {
            // 校验文件格式
            String fileName = file.getName().toLowerCase();
            if (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls")) {
                throw new RuntimeException("请选择Excel文件（.xlsx/.xls）！");
            }

            // 解析Excel+校验表头
            ExcelDataListener<Map<Integer, String>> listener = new ExcelDataListener<>(requiredHeaders);
            EasyExcel.read(inputStream, listener)
                    .sheet()
                    .headRowNumber(1)
                    .doRead();

            if (!listener.isHeaderValid()) {
                throw new RuntimeException("表头缺失！需要包含：" + requiredHeaders);
            }

            // 批量插入数据
            importDao.batchInsert(tableName, listener.getActualHeaders(), listener.getDataList());
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败：" + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("导入失败：" + e.getMessage());
        }
    }
}