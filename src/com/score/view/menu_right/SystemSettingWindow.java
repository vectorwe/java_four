package com.score.view.menu_right;

import com.score.view.ScoreSystemMainFrame;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * 系统设置窗口（GridBagLayout修复版）
 * 修复点：解决组件不显示问题，完善布局参数配置
 */
public class SystemSettingWindow extends JDialog {
    private final ScoreSystemMainFrame mainFrame;
    private Color currentBgColor;
    private Font currentFont;

    // 组件成员变量
    private final JLabel lblBgColor = new JLabel("背景颜色：");
    private final JPanel panelColorPreview = new JPanel();
    private final JButton btnChooseColor = new JButton("选择颜色");
    private final JLabel lblFontStyle = new JLabel("字体样式：");
    private final JComboBox<String> cmbFontStyle;
    private final JLabel lblFontSize = new JLabel("字体大小：");
    private final JComboBox<Integer> cmbFontSize;
    private final JButton btnConfirm = new JButton("确认应用");
    private final JButton btnReset = new JButton("恢复默认");
    private final JButton btnCancel = new JButton("取消");

    public SystemSettingWindow(ScoreSystemMainFrame parent) {
        super(parent, "系统设置", true);
        this.mainFrame = parent;
        this.currentBgColor = parent.getContentPane().getBackground();
        this.currentFont = parent.getFont();

        // 初始化下拉框
        String[] fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        this.cmbFontStyle = new JComboBox<>(fontNames);
        Integer[] fontSizes = {12, 14, 16, 18, 20, 22, 24};
        this.cmbFontSize = new JComboBox<>(fontSizes);

        // 初始化流程
        initWindowConfig();
        initComponentStyles();
        initGridBagLayout(); // 修复后的布局
        initEvents();
        initDefaultValues();
    }

    /**
     * 初始化窗口基础配置
     */
    private void initWindowConfig() {
        setSize(500, 400);
        setLocationRelativeTo(mainFrame);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // 主面板（必须先初始化，否则getContentPane()为空）
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        this.setContentPane(mainPanel);
    }

    /**
     * 初始化组件样式
     */
    private void initComponentStyles() {
        // 颜色预览面板
        panelColorPreview.setPreferredSize(new Dimension(100, 30));
        panelColorPreview.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        // 下拉框
        cmbFontStyle.setPreferredSize(new Dimension(200, 30));
        cmbFontSize.setPreferredSize(new Dimension(80, 30));
        // 按钮
        Dimension btnSize = new Dimension(100, 30);
        btnChooseColor.setPreferredSize(btnSize);
        btnConfirm.setPreferredSize(btnSize);
        btnReset.setPreferredSize(btnSize);
        btnCancel.setPreferredSize(btnSize);
    }

    /**
     * 核心修复：GridBagLayout布局（关键！）
     */
    private void initGridBagLayout() {
        JPanel contentPane = (JPanel) getContentPane();
        GridBagLayout layout = new GridBagLayout();
        contentPane.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();

        // 全局布局参数（基础配置）
        gbc.fill = GridBagConstraints.HORIZONTAL; // 组件水平填充单元格
        gbc.insets = new Insets(8, 8, 8, 8); // 所有组件统一边距
        gbc.weightx = 0.0; // 默认不分配空余空间
        gbc.weighty = 0.0;

        // ========== 第一行：背景色设置 ==========
        // 标签：背景颜色 (0,0)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST; // 右对齐
        contentPane.add(lblBgColor, gbc);

        // 颜色预览面板 (1,0)
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST; // 左对齐
        contentPane.add(panelColorPreview, gbc);

        // 选择颜色按钮 (2,0)
        gbc.gridx = 2;
        contentPane.add(btnChooseColor, gbc);

        // 确认按钮 (3,0)
        gbc.gridx = 3;
        contentPane.add(btnConfirm, gbc);

        // ========== 第二行：字体样式设置 ==========
        // 标签：字体样式 (0,1)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        contentPane.add(lblFontStyle, gbc);

        // 字体样式下拉框 (1,1)
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        contentPane.add(cmbFontStyle, gbc);

        // 恢复默认按钮 (3,1)
        gbc.gridx = 3;
        contentPane.add(btnReset, gbc);

        // ========== 第三行：字体大小设置 ==========
        // 标签：字体大小 (0,2)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        contentPane.add(lblFontSize, gbc);

        // 字体大小下拉框 (1,2)
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        contentPane.add(cmbFontSize, gbc);

        // 取消按钮 (3,2)
        gbc.gridx = 3;
        contentPane.add(btnCancel, gbc);

        // ========== 关键：设置空余空间分配 ==========
        // 让第4列占据所有水平空余空间（按钮组右对齐）
        gbc.weightx = 1.0;
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.gridheight = 3; // 跨3行
        gbc.fill = GridBagConstraints.BOTH; // 水平+垂直填充
        contentPane.add(new JLabel(), gbc); // 占位组件

        // 设置行权重，让组件垂直居中
        gbc.weighty = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 5; // 跨5列
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(new JLabel(), gbc);
    }

    /**
     * 初始化事件监听
     */
    private void initEvents() {
        btnChooseColor.addActionListener(e -> chooseBgColor());
        cmbFontStyle.addActionListener(e -> updateFontStyle());
        cmbFontSize.addActionListener(e -> updateFontSize());
        btnConfirm.addActionListener(e -> applySettings());
        btnReset.addActionListener(e -> resetToDefault());
        btnCancel.addActionListener(e -> dispose());
    }

    /**
     * 初始化默认值
     */
    private void initDefaultValues() {
        panelColorPreview.setBackground(currentBgColor);
        cmbFontStyle.setSelectedItem(currentFont.getFamily());
        cmbFontSize.setSelectedItem(currentFont.getSize());
    }

    /**
     * 选择背景色
     */
    private void chooseBgColor() {
        Color newColor = JColorChooser.showDialog(this, "选择背景色", currentBgColor);
        if (newColor != null) {
            currentBgColor = newColor;
            panelColorPreview.setBackground(newColor);
        }
    }

    /**
     * 更新字体样式
     */
    private void updateFontStyle() {
        String selectedFont = (String) cmbFontStyle.getSelectedItem();
        if (selectedFont != null) {
            currentFont = new Font(selectedFont, currentFont.getStyle(), currentFont.getSize());
        }
    }

    /**
     * 更新字体大小
     */
    private void updateFontSize() {
        Integer selectedSize = (Integer) cmbFontSize.getSelectedItem();
        if (selectedSize != null) {
            currentFont = new Font(currentFont.getFamily(), currentFont.getStyle(), selectedSize);
        }
    }

    /**
     * 应用设置到主界面
     */
    private void applySettings() {
        try {
            mainFrame.getContentPane().setBackground(currentBgColor);
            updateAllComponentsFont(mainFrame.getContentPane());
            mainFrame.revalidate();
            mainFrame.repaint();
            JOptionPane.showMessageDialog(this, "设置已生效！", "成功", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "设置失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 递归更新所有组件字体
     */
    private void updateAllComponentsFont(Container container) {
        if (container == null) return;
        for (Component comp : container.getComponents()) {
            if (comp != null) {
                comp.setFont(currentFont);
                if (comp instanceof Container child) {
                    updateAllComponentsFont(child);
                }
            }
        }
    }

    /**
     * 恢复默认设置
     */
    private void resetToDefault() {
        currentBgColor = Color.WHITE;
        currentFont = new Font("宋体", Font.PLAIN, 16);
        panelColorPreview.setBackground(currentBgColor);
        cmbFontStyle.setSelectedItem("宋体");
        cmbFontSize.setSelectedItem(16);
        JOptionPane.showMessageDialog(this, "已恢复默认！", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
}