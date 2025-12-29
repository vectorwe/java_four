package com.score.util;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Excel解析监听器：负责表头校验 + 数据读取
 * 泛型T：Excel每行数据的类型（Map<String, Object> 或自定义实体类）
 */
public class ExcelDataListener<T> extends AnalysisEventListener<T> {
    // 存储读取到的Excel数据
    private final List<T> dataList = new ArrayList<>();
    // 必填表头（用于校验）
    private final Set<String> expectedHeaders;
    // 实际读取到的表头
    private List<String> actualHeaders;
    // 表头校验结果（true=通过，false=不通过）
    private boolean headerValid = false;

    // 构造方法：传入必填表头（可为null，null则不校验表头）
    public ExcelDataListener(Set<String> expectedHeaders) {
        this.expectedHeaders = expectedHeaders;
    }

    /**
     * 解析Excel表头（核心：校验必填表头）
     */
    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        // 将表头Map转换为有序列表（按Excel列顺序）
        actualHeaders = new ArrayList<>(headMap.values());

        // 表头校验逻辑：如果传入了必填表头，则校验是否包含所有必填项
        if (expectedHeaders != null && !expectedHeaders.isEmpty()) {
            headerValid = actualHeaders.containsAll(expectedHeaders);
        } else {
            // 未传入必填表头，默认校验通过
            headerValid = true;
        }
    }

    /**
     * 解析Excel每行数据（逐行读取）
     */
    @Override
    public void invoke(T data, AnalysisContext context) {
        // 仅当表头校验通过时，才存储数据
        if (headerValid) {
            dataList.add(data);
        }
    }

    /**
     * 所有数据解析完成后执行（可留空，也可添加收尾逻辑）
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        System.out.println("Excel解析完成！");
        System.out.println("实际表头：" + actualHeaders);
        System.out.println("表头校验结果：" + (headerValid ? "通过" : "不通过"));
        System.out.println("读取到的数据行数：" + dataList.size());
    }

    // ========== Getter方法（供外部调用） ==========
    public boolean isHeaderValid() {
        return headerValid;
    }

    public List<String> getActualHeaders() {
        return actualHeaders;
    }

    public List<T> getDataList() {
        return dataList;
    }
}