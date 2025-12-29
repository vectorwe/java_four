package com.score.view.scoremanage;

import com.score.util.DBUtil;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 成绩单个导入面板（支持自定义表格名称、列名，数据存入数据库）
 */
public class ScoreImportPanel extends JPanel {
    // 组件常量
    private static final String DEFAULT_TABLE_PREFIX = "score_import_";
    private static final List<String> MYSQL_KEYWORDS = Arrays.asList(
            "user", "table", "select", "insert", "update", "delete", "where",
            "from", "join", "on", "group", "by", "order", "limit", "having"
    );
    private static final String DEFAULT_COLUMN_PREFIX = "column_";
    private static final int BATCH_ADD_ROWS = 5;

    // 核心组件
    private DefaultTableModel importTableModel;
    private JTable importTable;
    private final CardLayout parentCardLayout;
    private final JPanel parentCardPanel;
    private final List<String> columnNames = new ArrayList<>();
    private String tableName;
    private boolean hasUnsavedChanges = false;

    // 按钮组件
    private JButton addRowBtn;
    private JButton saveBtn;
    private JButton batchAddRowBtn;


    public ScoreImportPanel(CardLayout cardLayout, JPanel cardPanel) {
        this.parentCardLayout = cardLayout;
        this.parentCardPanel = cardPanel;
        initTableName();
        initPanel();
        initTableStatus();
        registerShortcuts();
    }


    /** 初始化表格名称（含重复/关键字检测） */
    private void initTableName() {
        while (true) {
            String inputName = JOptionPane.showInputDialog(
                    this,
                    "请输入表格名称（仅支持字母/数字/下划线，避免MySQL关键字）：\n示例：math_score_2025",
                    "创建表格",
                    JOptionPane.PLAIN_MESSAGE
            );

            if (inputName == null) {
                this.tableName = DEFAULT_TABLE_PREFIX + System.currentTimeMillis();
                JOptionPane.showMessageDialog(this, "已使用默认表名：" + this.tableName, "提示", JOptionPane.INFORMATION_MESSAGE);
                break;
            }

            inputName = inputName.trim();
            if (inputName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "表格名称不能为空，请重新输入！", "提示", JOptionPane.WARNING_MESSAGE);
                continue;
            }

            String filteredTableName = inputName.replaceAll("[^a-zA-Z0-9_]", "_");
            // 关键字检测
            if (MYSQL_KEYWORDS.contains(filteredTableName.toLowerCase())) {
                String recommendedName = filteredTableName + "_score_" + System.currentTimeMillis();
                JOptionPane.showMessageDialog(this, "表名\"" + filteredTableName + "\"是MySQL关键字，请修改！\n推荐：" + recommendedName, "提示", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            // 表名重复检测
            if (DBUtil.isTableExists(filteredTableName)) {
                int choice = JOptionPane.showConfirmDialog(this, "表名\"" + filteredTableName + "\"已存在！\n1. 追加时间戳后缀\n2. 重新输入", "表名重复", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    filteredTableName += "_" + System.currentTimeMillis();
                } else {
                    continue;
                }
            }

            this.tableName = filteredTableName;
            break;
        }
    }


    /** 初始化面板布局 */
    private void initPanel() {
        this.setLayout(new BorderLayout(10, 10));
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.add(createTopPanel(), BorderLayout.NORTH);
        this.add(createTableScrollPane(), BorderLayout.CENTER);
        this.add(createStatusPanel(), BorderLayout.SOUTH);
    }


    /** 创建顶部面板（表名+功能按钮） */
    private JPanel createTopPanel() {
        // 表名标签
        JLabel tableNameLabel = new JLabel("当前数据库表名：" + tableName);
        tableNameLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        tableNameLabel.setForeground(new Color(0, 80, 160));

        // 列操作面板
        JPanel colOperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        colOperPanel.setBorder(BorderFactory.createTitledBorder("列操作"));
        colOperPanel.add(createButton("添加列", e -> addTableColumn(), KeyEvent.VK_C));
        colOperPanel.add(createButton("修改表头", e -> editSelectedHeader(), KeyEvent.VK_E));
        colOperPanel.add(createButton("删除列", e -> deleteSelectedColumn(), KeyEvent.VK_D));

        // 行操作面板
        JPanel rowOperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        rowOperPanel.setBorder(BorderFactory.createTitledBorder("行操作"));
        addRowBtn = createButton("添加单行", e -> addDataRow(), KeyEvent.VK_A);
        batchAddRowBtn = createButton("批量添加5行", e -> batchAddDataRows(), KeyEvent.VK_B);
        rowOperPanel.add(addRowBtn);
        rowOperPanel.add(batchAddRowBtn);
        rowOperPanel.add(createButton("删除选中行", e -> deleteSelectedRow(), KeyEvent.VK_R));

        // 数据操作面板
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        actionPanel.setBorder(BorderFactory.createTitledBorder("数据操作"));
        saveBtn = createButton("保存到数据库", e -> saveToDatabase(), KeyEvent.VK_S);
        saveBtn.setBackground(new Color(0, 150, 0));
        saveBtn.setForeground(Color.WHITE);
        actionPanel.add(saveBtn);
        actionPanel.add(createButton("清空数据行", e -> clearDataRows(), KeyEvent.VK_L));
        actionPanel.add(createButton("返回上一级", e -> backToDefaultPanel(), KeyEvent.VK_ESCAPE));

        // 合并按钮面板
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        btnPanel.add(colOperPanel);
        btnPanel.add(rowOperPanel);
        btnPanel.add(actionPanel);

        // 顶部总面板
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));
        topPanel.add(tableNameLabel, BorderLayout.WEST);
        topPanel.add(btnPanel, BorderLayout.CENTER);
        return topPanel;
    }


    /** 创建状态提示面板 */
    private JPanel createStatusPanel() {
        JLabel statusLabel = new JLabel("状态：未修改 | 列数：0 | 数据行数：0");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // 实时更新状态
        new javax.swing.Timer(500, e -> {
            String changeStatus = hasUnsavedChanges ? "⚠️ 有未保存修改" : "未修改";
            int colCount = importTableModel.getColumnCount();
            int rowCount = Math.max(0, importTableModel.getRowCount() - 1);
            statusLabel.setText(String.format("状态：%s | 列数：%d | 数据行数：%d", changeStatus, colCount, rowCount));
        }).start();

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.setBackground(Color.LIGHT_GRAY);
        return statusPanel;
    }


    /** 创建通用按钮（带快捷键） */
    private JButton createButton(String text, java.awt.event.ActionListener listener, int keyCode) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        button.addActionListener(listener);
        button.setPreferredSize(new Dimension(100, 28));
        if (keyCode != 0) button.setMnemonic(keyCode);
        return button;
    }


    /** 创建表格滚动面板 */
    private JScrollPane createTableScrollPane() {
        initTableModel();
        initTableComponent();
        JScrollPane scrollPane = new JScrollPane(importTable);
        scrollPane.setBorder(BorderFactory.createEtchedBorder());
        scrollPane.setPreferredSize(new Dimension(800, 400));
        return scrollPane;
    }


    /** 初始化表格模型（同步列名+监听修改） */
    private void initTableModel() {
        importTableModel = new DefaultTableModel(1, 0) {
            @Override
            public String getColumnName(int column) {
                // 同步列名列表
                if (column < columnNames.size()) {
                    String colName = columnNames.get(column).trim();
                    return colName.isEmpty() ? DEFAULT_COLUMN_PREFIX + (column + 1) : colName;
                }
                return DEFAULT_COLUMN_PREFIX + (column + 1);
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return row != 0 || (row == 0 && column < columnNames.size());
            }
        };

        // 监听表格修改
        importTableModel.addTableModelListener(e -> {
            hasUnsavedChanges = true;
            int col = e.getColumn();
            if (col == -1) return;

            // 列名配置行（固定第0行）的修改校验
            String oldName = columnNames.size() > col ? columnNames.get(col) : "";
            Object newValueObj = importTableModel.getValueAt(0, col);
            String newName = newValueObj == null ? "" : newValueObj.toString().trim();

            if (validateColumnName(col, newName)) {
                // 更新列名列表
                while (columnNames.size() <= col) columnNames.add("");
                columnNames.set(col, newName);
                importTable.getTableHeader().repaint();
                updateButtonStatus();
            } else {
                importTableModel.setValueAt(oldName, 0, col);
                hasUnsavedChanges = false;
            }
        });
    }


    /** 初始化表格组件（样式+交互） */
    private void initTableComponent() {
        importTable = new JTable(importTableModel);
        importTable.setRowHeight(0, 35);
        importTable.setRowHeight(30);
        importTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        importTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        importTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        importTable.setShowGrid(true);
        importTable.setGridColor(Color.LIGHT_GRAY);

        // 列名配置行样式
        importTable.setValueAt("⚠️ 列名配置行（点击修改列名）", 0, 0);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (row == 0) {
                    comp.setBackground(new Color(255, 240, 240));
                    comp.setForeground(new Color(180, 0, 0));
                    ((JLabel) comp).setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    comp.setBackground(table.getBackground());
                    comp.setForeground(table.getForeground());
                    ((JLabel) comp).setHorizontalAlignment(SwingConstants.LEFT);
                }
                return comp;
            }
        };
        for (int i = 0; i < importTable.getColumnCount(); i++) {
            importTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        // 表头点击编辑列名
        importTable.getTableHeader().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int col = importTable.columnAtPoint(e.getPoint());
                if (col != -1) editHeaderByColumn(col);
            }
        });
    }


    /** 注册全局快捷键 */
    private void registerShortcuts() {
        // ESC返回
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "back");
        this.getActionMap().put("back", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                backToDefaultPanel();
            }
        });

        // Ctrl+S保存
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), "save");
        this.getActionMap().put("save", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (saveBtn.isEnabled()) saveToDatabase();
            }
        });
    }


    /** 初始化按钮状态 */
    private void initTableStatus() {
        updateButtonStatus();
    }


    /** 更新按钮状态（根据表格状态动态启用/禁用） */
    private void updateButtonStatus() {
        boolean hasColumn = importTableModel.getColumnCount() > 0;
        boolean hasData = importTableModel.getRowCount() > 1;
        boolean hasSelectedRow = importTable.getSelectedRow() != -1 && importTable.getSelectedRow() != 0;
        boolean hasSelectedCol = importTable.getSelectedColumn() != -1;

        addRowBtn.setEnabled(hasColumn);
        batchAddRowBtn.setEnabled(hasColumn);
        saveBtn.setEnabled(hasColumn && hasData && hasUnsavedChanges);
        importTable.getActionMap().get("delete").setEnabled(hasSelectedRow);

        // 列操作按钮状态
        for (Component comp : this.getComponents()) {
            if (comp instanceof JPanel) {
                for (Component subComp : ((JPanel) comp).getComponents()) {
                    if (subComp instanceof JButton) {
                        JButton btn = (JButton) subComp;
                        if (btn.getText().contains("删除列") || btn.getText().contains("修改表头")) {
                            btn.setEnabled(hasSelectedCol);
                        }
                    }
                }
            }
        }
    }


    /** 校验列名合法性 */
    private boolean validateColumnName(int col, String newName) {
        if (newName.isEmpty()) return true;

        // 过滤特殊字符
        String filteredNewName = newName.replaceAll("[^a-zA-Z0-9_]", "");
        if (filteredNewName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "列名\"" + newName + "\"过滤后为空，请包含字母/数字/下划线！", "提示", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // 过滤后列名重复校验
        List<String> filteredColumnNames = new ArrayList<>();
        for (String c : columnNames) filteredColumnNames.add(c.replaceAll("[^a-zA-Z0-9_]", ""));
        if (filteredColumnNames.contains(filteredNewName) && filteredColumnNames.indexOf(filteredNewName) != col) {
            JOptionPane.showMessageDialog(this, "列名\"" + newName + "\"过滤后与已有列名重复，请修改！", "提示", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // 原始列名重复校验
        if (columnNames.contains(newName) && columnNames.indexOf(newName) != col) {
            JOptionPane.showMessageDialog(this, "列名\"" + newName + "\"已存在，请修改！", "提示", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // MySQL关键字校验
        if (MYSQL_KEYWORDS.contains(filteredNewName.toLowerCase())) {
            JOptionPane.showMessageDialog(this, "列名\"" + newName + "\"是MySQL关键字，请修改！", "提示", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }


    /** 修改选中列的表头 */
    private void editSelectedHeader() {
        int selectedCol = importTable.getSelectedColumn();
        if (selectedCol == -1) {
            JOptionPane.showMessageDialog(this, "请先选中要修改的列！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        editHeaderByColumn(selectedCol);
    }


    /** 编辑指定列的表头 */
    private void editHeaderByColumn(int col) {
        String currentName = columnNames.size() > col ? columnNames.get(col) : DEFAULT_COLUMN_PREFIX + (col + 1);
        JTextField inputField = new JTextField(currentName);
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.add(new JLabel("请输入新的列名（仅支持字母/数字/下划线）："), BorderLayout.NORTH);
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(new JLabel("示例：student_id / math_score"), BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(this, inputPanel, "修改列名", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String newName = inputField.getText().trim();
        if (newName.isEmpty()) {
            newName = DEFAULT_COLUMN_PREFIX + (col + 1);
            JOptionPane.showMessageDialog(this, "列名不能为空，已使用默认名称：" + newName, "提示", JOptionPane.INFORMATION_MESSAGE);
        }

        if (validateColumnName(col, newName)) {
            while (columnNames.size() <= col) columnNames.add("");
            columnNames.set(col, newName);
            importTableModel.setValueAt(newName, 0, col);
            importTable.getTableHeader().repaint();
            hasUnsavedChanges = true;
            updateButtonStatus();
            JOptionPane.showMessageDialog(this, "列名已修改为：" + newName, "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }


    /** 删除选中列 */
    private void deleteSelectedColumn() {
        int selectedCol = importTable.getSelectedColumn();
        if (selectedCol == -1) {
            JOptionPane.showMessageDialog(this, "请先选中要删除的列！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String colName = columnNames.size() > selectedCol ? columnNames.get(selectedCol) : "第" + (selectedCol + 1) + "列";
        int confirm = JOptionPane.showConfirmDialog(this, String.format("确定要删除【%s】列吗？\n删除后该列所有数据将被永久清除！", colName), "删除确认", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            importTable.removeColumn(importTable.getColumnModel().getColumn(selectedCol));
            if (selectedCol < columnNames.size()) columnNames.remove(selectedCol);
            hasUnsavedChanges = true;
            importTable.doLayout();
            updateButtonStatus();
            JOptionPane.showMessageDialog(this, "已成功删除【" + colName + "】列", "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }


    /** 添加列 */
    /** 添加列 */
    private void addTableColumn() {
        // 初始化默认列名
        String defaultColName = DEFAULT_COLUMN_PREFIX + (columnNames.size() + 1);

        // 改用更兼容的输入对话框写法，避免类型不兼容问题
        JTextField inputField = new JTextField(defaultColName);
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.add(new JLabel("请输入列名（仅支持字母/数字/下划线）："), BorderLayout.NORTH);
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(new JLabel("示例：chinese_score / student_name"), BorderLayout.SOUTH);

        // 显示自定义输入对话框
        int result = JOptionPane.showConfirmDialog(
                this,
                inputPanel,
                "添加列",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        // 用户取消操作
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        // 获取并处理输入值
        String columnName = inputField.getText().trim();
        if (columnName.isEmpty()) {
            columnName = defaultColName;
            JOptionPane.showMessageDialog(this,
                    "列名不能为空，已使用默认名称：" + columnName,
                    "提示", JOptionPane.INFORMATION_MESSAGE);
        }

        // 列名校验
        if (!validateColumnName(columnNames.size(), columnName)) {
            return;
        }

        // 添加列到表格模型
        int newColIndex = importTableModel.getColumnCount();
        importTableModel.addColumn(columnName);
        importTableModel.setValueAt(columnName, 0, newColIndex);
        columnNames.add(columnName);
        hasUnsavedChanges = true;
        importTable.doLayout();
        updateButtonStatus();

        JOptionPane.showMessageDialog(this,
                "已成功添加列：" + columnName,
                "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    /** 添加单行数据 */
    private void addDataRow() {
        if (importTableModel.getColumnCount() == 0) {
            JOptionPane.showMessageDialog(this, "请先添加列后再添加数据行！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Object[] emptyRow = new Object[importTableModel.getColumnCount()];
        Arrays.fill(emptyRow, "");
        importTableModel.addRow(emptyRow);
        hasUnsavedChanges = true;
        importTable.scrollRectToVisible(importTable.getCellRect(importTableModel.getRowCount()-1, 0, true));
        importTable.setRowSelectionInterval(importTableModel.getRowCount()-1, importTableModel.getRowCount()-1);
        importTable.setColumnSelectionInterval(0, 0);
        updateButtonStatus();
    }


    /** 批量添加5行数据 */
    private void batchAddDataRows() {
        if (importTableModel.getColumnCount() == 0) {
            JOptionPane.showMessageDialog(this, "请先添加列后再添加数据行！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        for (int i = 0; i < BATCH_ADD_ROWS; i++) {
            Object[] emptyRow = new Object[importTableModel.getColumnCount()];
            Arrays.fill(emptyRow, "");
            importTableModel.addRow(emptyRow);
        }
        hasUnsavedChanges = true;
        importTable.scrollRectToVisible(importTable.getCellRect(importTableModel.getRowCount()-1, 0, true));
        updateButtonStatus();
        JOptionPane.showMessageDialog(this, "已批量添加" + BATCH_ADD_ROWS + "行数据", "提示", JOptionPane.INFORMATION_MESSAGE);
    }


    /** 删除选中行 */
    private void deleteSelectedRow() {
        int selectedRow = importTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选中要删除的行！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedRow == 0) {
            JOptionPane.showMessageDialog(this, "列名配置行不可删除！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "确定要删除选中的数据行吗？删除后数据将被永久清除！", "删除确认", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            importTableModel.removeRow(selectedRow);
            hasUnsavedChanges = true;
            updateButtonStatus();
            JOptionPane.showMessageDialog(this, "已成功删除选中的数据行", "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }


    /** 清空所有数据行（保留列名配置行） */
    private void clearDataRows() {
        if (importTableModel.getRowCount() <= 1) {
            JOptionPane.showMessageDialog(this, "当前无数据行可清空！", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "确定要清空所有数据行吗？列名配置行不会被删除！", "清空确认", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            while (importTableModel.getRowCount() > 1) importTableModel.removeRow(1);
            hasUnsavedChanges = true;
            updateButtonStatus();
            JOptionPane.showMessageDialog(this, "已清空所有数据行", "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }


    /** 保存数据到数据库 */
    private void saveToDatabase() {
        // 基础校验
        if (columnNames.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先添加列后再保存！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (importTableModel.getRowCount() <= 1) {
            JOptionPane.showMessageDialog(this, "请先添加数据行后再保存！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 列名最终校验
        List<String> validColumnNames = new ArrayList<>();
        for (String col : columnNames) {
            String trimCol = col.trim();
            String filteredCol = trimCol.replaceAll("[^a-zA-Z0-9_]", "");
            if (filteredCol.isEmpty()) filteredCol = DEFAULT_COLUMN_PREFIX + (validColumnNames.size() + 1);
            validColumnNames.add(filteredCol);
        }

        // 最终确认
        int confirm = JOptionPane.showConfirmDialog(this, String.format("即将保存数据到数据库：\n表名：%s\n列数：%d\n数据行数：%d\n\n确定继续吗？", tableName, validColumnNames.size(), importTableModel.getRowCount() - 1), "保存确认", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        // 后台保存（避免UI卡顿）
        new SwingWorker<Boolean, Integer>() {
            private JDialog progressDialog;
            private JProgressBar progressBar;

            @Override
            protected Boolean doInBackground() {
                // 显示进度对话框
                SwingUtilities.invokeLater(() -> {
                    progressDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(ScoreImportPanel.this), "保存中...", true);
                    progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                    progressDialog.setSize(300, 100);
                    progressDialog.setLocationRelativeTo(ScoreImportPanel.this);
                    progressDialog.setLayout(new BorderLayout(10, 10));
                    progressBar = new JProgressBar(0, 100);
                    progressBar.setStringPainted(true);
                    progressDialog.add(new JLabel("正在保存数据，请稍候..."), BorderLayout.NORTH);
                    progressDialog.add(progressBar, BorderLayout.CENTER);
                    progressDialog.setVisible(true);
                });

                try {
                    // 步骤1：创建表（20%）
                    publish(20);
                    if (!DBUtil.createCustomTable(tableName, validColumnNames)) {
                        throw new RuntimeException("创建数据表失败");
                    }

                    // 步骤2：准备数据（50%）
                    publish(50);
                    List<Object[]> dataRows = new ArrayList<>();
                    for (int row = 1; row < importTableModel.getRowCount(); row++) {
                        Object[] rowData = new Object[validColumnNames.size()];
                        for (int col = 0; col < validColumnNames.size(); col++) {
                            Object value = importTableModel.getValueAt(row, col);
                            rowData[col] = value == null ? "" : value;
                        }
                        dataRows.add(rowData);
                        publish(50 + (row * 40) / (importTableModel.getRowCount() - 1));
                    }

                    // 步骤3：批量插入（100%）
                    publish(100);
                    int insertCount = DBUtil.batchInsertData(tableName, validColumnNames, dataRows);
                    return insertCount > 0;
                } catch (Exception e) {
                    System.err.println("保存数据失败：" + e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException("保存失败：" + e.getMessage());
                } finally {
                    SwingUtilities.invokeLater(() -> {
                        if (progressDialog != null) progressDialog.dispose();
                    });
                }
            }

            @Override
            protected void process(List<Integer> chunks) {
                if (progressBar != null && !chunks.isEmpty()) {
                    progressBar.setValue(chunks.get(chunks.size() - 1));
                }
            }

            @Override
            protected void done() {
                try {
                    Boolean result = get();
                    if (result) {
                        hasUnsavedChanges = false;
                        JOptionPane.showMessageDialog(ScoreImportPanel.this, String.format("数据保存成功！\n表名：%s\n共插入 %d 行数据", tableName, importTableModel.getRowCount() - 1), "成功", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(ScoreImportPanel.this, "保存成功，但无有效数据可插入！", "提示", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ScoreImportPanel.this, "保存数据失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
                updateButtonStatus();
            }
        }.execute();
    }


    /** 返回上一级面板 */
    private void backToDefaultPanel() {
        if (hasUnsavedChanges) {
            int choice = JOptionPane.showConfirmDialog(this, "当前有未保存的修改，确定要返回吗？未保存的数据将丢失！", "确认返回", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) return;
        }
        parentCardLayout.show(parentCardPanel, "default");
    }


    // ========== 测试入口（解决“类未使用”提示） ==========
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("成绩导入面板测试");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 600);
            CardLayout cardLayout = new CardLayout();
            JPanel cardPanel = new JPanel(cardLayout);
            ScoreImportPanel importPanel = new ScoreImportPanel(cardLayout, cardPanel);
            cardPanel.add(importPanel, "import");
            frame.add(cardPanel);
            frame.setVisible(true);
            cardLayout.show(cardPanel, "import");
        });
    }
}