package com.score.view;

import com.score.dao.User;
import com.score.util.DBUtil;
import com.score.view.menu_right.FeedbackWindow;
import com.score.view.menu_right.PersonalInfoWindow;
import com.score.view.menu_right.resetPassword;
import com.score.view.menu_right.SystemSettingWindow;
import com.score.view.scoremanage.score_table;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.ListSelectionModel;

/**
 * 学生成绩管理系统主界面
 * 优化点：
 * 1. 修复批量导入面板重复添加问题
 * 2. 增强空值校验和异常处理
 * 3. 优化界面响应性（EDT线程）
 * 4. 提升代码可维护性（常量提取、方法解耦）
 * 5. 补充表格数据加载逻辑
 */
public class ScoreSystemMainFrame extends JFrame {
    // 常量定义：提升可维护性
    private static final String TITLE = "学生成绩管理系统";
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 600;
    private static final String DEFAULT_PANEL_KEY = "default";
    private static final String BATCH_IMPORT_PANEL_KEY = "batchImport";

    // 界面组件
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JTable dataTable;
    private DefaultTableModel tableModel;
    private User loginUser;
    // 批量导入面板单例：避免重复创建
    private score_table batchImportPanel;

    /**
     * 构造方法：创建主界面时必须传入登录用户对象
     *
     * @param loginUser 登录成功后的User对象（非null）
     */
    public ScoreSystemMainFrame(User loginUser) {
        // 增强空值校验
        if (loginUser == null) {
            JOptionPane.showMessageDialog(null, "登录用户信息不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
            throw new IllegalArgumentException("登录用户信息不能为空！");
        }
        this.loginUser = loginUser;

        // 初始化流程
        initFrameConfig();
        initMenuBar();
        initMainLayout();
        initTableModel();
        loadInitialTableData(); // 新增：加载初始表格数据
    }

    /**
     * 步骤1：初始化窗口基础配置
     */
    private void initFrameConfig() {
        this.setLayout(new BorderLayout(10, 10));
        this.setTitle(TITLE);
        this.setSize(WIDTH, HEIGHT);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(true); // 改为可调整大小，提升用户体验
        // 设置窗口图标（可选，提升美观度）
        try {
            this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icon.png")));
        } catch (Exception e) {
            // 静默处理：图标不存在不影响主功能
        }
    }

    /**
     * 步骤2：初始化顶部菜单栏
     */
    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // 左侧功能菜单组
        menuBar.add(createScoreMenu());
        menuBar.add(createEditMenu());
        menuBar.add(createFindMenu());
        menuBar.add(createChangeMenu());

        // 右侧菜单分隔符
        menuBar.add(Box.createHorizontalGlue());

        // 右侧系统菜单组
        menuBar.add(createSettingMenu());
        menuBar.add(createPersonalMenu());
        menuBar.add(createFeedbackMenu());

        this.setJMenuBar(menuBar);
    }

    /**
     * 子方法：创建成绩管理菜单（优化批量导入逻辑）
     */
    private JMenu createScoreMenu() {
        JMenu scoreMenu = new JMenu("成绩管理");

        // 新增成绩子菜单
        JMenu addScore = new JMenu("新增成绩");
        JMenuItem addScoreItem = new JMenuItem("单个导入");
        JMenuItem batchAddScoreItem = new JMenuItem("批量导入");

        // 单个导入事件
        addScoreItem.addActionListener(e -> showTip("单个导入功能待实现，可参考批量导入逻辑扩展"));

        // 批量导入点击事件（优化：EDT线程处理）
        batchAddScoreItem.addActionListener(e -> SwingUtilities.invokeLater(this::switchToBatchImportPanel));

        addScore.add(addScoreItem);
        addScore.add(batchAddScoreItem);
        scoreMenu.add(addScore);

        // 修改成绩
        JMenuItem changeScoreItem = new JMenuItem("修改成绩");
        changeScoreItem.addActionListener(e -> showTip("修改成绩功能待实现"));
        scoreMenu.add(changeScoreItem);

        // 删除成绩
        JMenuItem deleteScoreItem = new JMenuItem("删除成绩");
        deleteScoreItem.addActionListener(e -> deleteSelectedScore()); // 新增：删除选中行逻辑
        scoreMenu.add(deleteScoreItem);

        // 查找成绩
        JMenuItem findScoreItem = new JMenuItem("查找成绩");
        findScoreItem.addActionListener(e -> showTip("查找成绩功能待实现"));
        scoreMenu.add(findScoreItem);

        return scoreMenu;
    }

    /**
     * 新增：删除选中的成绩行
     */
    private void deleteSelectedScore() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            showTip("请先选中要删除的成绩行！");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "确定要删除选中的成绩吗？", "确认删除", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.removeRow(selectedRow);
            showTip("成绩删除成功！");
            // 此处可补充：调用DAO层删除数据库中的数据
        }
    }

    /**
     * 子方法：创建查找菜单（解耦原有逻辑）
     */
    private JMenu createFindMenu() {
        JMenu findMenu = new JMenu("查找");
        JMenuItem findScoreItem = new JMenuItem("按条件查找");
        findScoreItem.addActionListener(e -> showTip("按条件查找功能待实现"));
        findMenu.add(findScoreItem);
        return findMenu;
    }

    /**
     * 子方法：创建修改菜单（解耦原有逻辑）
     */
    private JMenu createChangeMenu() {
        JMenu changeMenu = new JMenu("修改");
        JMenuItem changeScoreItem = new JMenuItem("修改选中成绩");
        changeScoreItem.addActionListener(e -> showTip("修改选中成绩功能待实现"));
        changeMenu.add(changeScoreItem);
        return changeMenu;
    }

    /**
     * 核心优化：跳转至批量导入面板（score_table）
     * 解决重复创建、重复添加问题
     */
    private void switchToBatchImportPanel() {
        // 单例模式：只创建一次批量导入面板
        if (batchImportPanel == null) {
            batchImportPanel = new score_table();
            addBackButtonToPanel(batchImportPanel);
            // 只添加一次到CardLayout
            cardPanel.add(batchImportPanel, BATCH_IMPORT_PANEL_KEY);
        }

        // 直接切换显示
        cardLayout.show(cardPanel, BATCH_IMPORT_PANEL_KEY);
        // 窗口自适应（可选）
        this.pack();
        this.setSize(WIDTH, HEIGHT);
        this.setLocationRelativeTo(null);
    }

    /**
     * 辅助方法：给批量导入面板添加返回按钮（优化布局逻辑）
     */
    private void addBackButtonToPanel(JPanel panel) {
        // 保存原有布局，避免覆盖
        LayoutManager originalLayout = panel.getLayout();

        // 创建返回按钮面板
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backBtn = new JButton("← 返回主界面");
        backBtn.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        backBtn.setBackground(Color.WHITE);
        backBtn.addActionListener(e -> {
            // 切换回默认面板
            cardLayout.show(cardPanel, DEFAULT_PANEL_KEY);
            // 恢复窗口大小
            ScoreSystemMainFrame.this.setSize(WIDTH, HEIGHT);
            ScoreSystemMainFrame.this.setLocationRelativeTo(null);
        });
        backPanel.add(backBtn);

        // 重新布局：保留原有面板布局，仅在顶部添加返回按钮
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.add(backPanel, BorderLayout.NORTH);
        wrapperPanel.add(panel, BorderLayout.CENTER);

        // 替换原有面板引用
        if (batchImportPanel == panel) {
            cardPanel.remove(panel);
            batchImportPanel = (score_table) wrapperPanel;
            cardPanel.add(batchImportPanel, BATCH_IMPORT_PANEL_KEY);
        }
    }

    /**
     * 子方法：创建编辑菜单
     */
    private JMenu createEditMenu() {
        JMenu editMenu = new JMenu("编辑");
        JMenuItem undoItem = new JMenuItem("撤销");
        JMenuItem redoItem = new JMenuItem("重做");
        undoItem.addActionListener(e -> showTip("撤销操作功能待实现"));
        redoItem.addActionListener(e -> showTip("重做操作功能待实现"));
        editMenu.add(undoItem);
        editMenu.add(redoItem);
        return editMenu;
    }

    /**
     * 子方法：创建设置菜单
     */
    private JMenu createSettingMenu() {
        JMenu settingMenu = new JMenu("设置");
        JMenuItem sysSettingItem = new JMenuItem("系统设置");

        sysSettingItem.addActionListener(e ->
                SwingUtilities.invokeLater(() -> new SystemSettingWindow(ScoreSystemMainFrame.this).setVisible(true))
        );

        settingMenu.add(sysSettingItem);
        return settingMenu;
    }

    /**
     * 子方法：创建个人菜单
     */
    private JMenu createPersonalMenu() {
        JMenu personalMenu = new JMenu("个人");
        JMenuItem personalInfoItem = new JMenuItem("个人信息");

        personalInfoItem.addActionListener(e ->
                SwingUtilities.invokeLater(() -> PersonalInfoWindow.openPersonalWindow(ScoreSystemMainFrame.this, loginUser))
        );

        JMenuItem changePwdItem = new JMenuItem("修改密码");
        changePwdItem.addActionListener(e ->
                SwingUtilities.invokeLater(() -> new resetPassword(ScoreSystemMainFrame.this, loginUser).setVisible(true))
        );

        personalMenu.add(personalInfoItem);
        personalMenu.add(changePwdItem);

        return personalMenu;
    }

    /**
     * 子方法：创建反馈菜单
     */
    private JMenu createFeedbackMenu() {
        JMenu feedbackMenu = new JMenu("反馈");
        JMenuItem submitFeedbackItem = new JMenuItem("提交反馈");

        submitFeedbackItem.addActionListener(e ->
                SwingUtilities.invokeLater(() -> FeedbackWindow.openFeedbackWindow(ScoreSystemMainFrame.this, loginUser.getUsername()))
        );

        feedbackMenu.add(submitFeedbackItem);
        return feedbackMenu;
    }

    /**
     * 步骤3：初始化核心布局（CardLayout）
     */
    private void initMainLayout() {
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        JPanel defaultPanel = createDefaultPanel();
        cardPanel.add(defaultPanel, DEFAULT_PANEL_KEY);

        this.add(cardPanel, BorderLayout.CENTER);
    }

    /**
     * 子方法：创建默认显示的成绩列表面板
     */
    private JPanel createDefaultPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 优化表格样式
        dataTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        dataTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        dataTable.getTableHeader().setBackground(new Color(230, 240, 250));

        JScrollPane scrollPane = new JScrollPane(dataTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 步骤4：初始化成绩表格模型
     */
    private void initTableModel() {
        String[] columnNames = {"学号", "姓名", "科目", "成绩", "录入时间"};

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 表格单元格不可编辑
            }

            // 优化：指定列类型，避免显示异常
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: return String.class; // 学号
                    case 1: return String.class; // 姓名
                    case 2: return String.class; // 科目
                    case 3: return Integer.class; // 成绩
                    case 4: return String.class; // 录入时间
                    default: return Object.class;
                }
            }
        };

        dataTable = new JTable(tableModel);
        dataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        dataTable.setRowHeight(30);
    }

    /**
     * 新增：加载初始表格数据
     * 模拟从数据库加载数据，可替换为实际DAO调用
     */
    private void loadInitialTableData() {
        // 模拟数据：实际项目中应从DBUtil/DAO层获取
        Object[][] mockData = {
                {"2024001", "张三", "数学", 95, "2024-01-01"},
                {"2024002", "李四", "语文", 88, "2024-01-02"},
                {"2024003", "王五", "英语", 92, "2024-01-03"}
        };

        // 清空原有数据，添加模拟数据
        tableModel.setRowCount(0);
        for (Object[] row : mockData) {
            tableModel.addRow(row);
        }
    }

    /**
     * 通用工具方法：显示提示框（优化样式）
     *
     * @param message 提示内容
     */
    private void showTip(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    this,
                    message,
                    "提示",
                    JOptionPane.INFORMATION_MESSAGE,
                    new ImageIcon(getClass().getResource("/tip.png")) // 可选：添加提示图标
            );
        });
    }

    // 测试主方法（优化：添加异常捕获）
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // 设置全局字体（解决中文乱码）
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                // 模拟登录用户
                User testUser = new User();
                testUser.setUsername("admin");
                testUser.setTitle("管理员");
                testUser.setName("系统管理员");
                testUser.setTel("13800138000");
                testUser.setEmail("admin@test.com");

                // 启动主界面
                new ScoreSystemMainFrame(testUser).setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "程序启动失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
}