package com.score.view.menu_right;

import com.score.util.DBUtil;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 反馈窗口工具类（提取用户id、name，分表插入对应反馈表）
 */
public class FeedbackWindow {

    /**
     * 打开反馈提交窗口
     * @param parent 父窗口
     * @param username 当前登录用户名（用于查询用户id、name）
     */
    public static void openFeedbackWindow(JFrame parent, String username) {
        // 1. 创建模态对话框
        JDialog feedbackDialog = new JDialog(parent, "向Microsoft提供反馈", true);
        feedbackDialog.setSize(600, 400);
        feedbackDialog.setLocationRelativeTo(parent);
        feedbackDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        feedbackDialog.setLayout(new BorderLayout(10, 10));
        feedbackDialog.setResizable(false);


        // 2. 反馈分类选择
        JPanel categoryPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        JRadioButton rbSupplement = new JRadioButton("♥ 进行补充");
        JRadioButton rbReport = new JRadioButton("📋 报告问题");
        JRadioButton rbSuggest = new JRadioButton("💡 提出建议");
        ButtonGroup categoryGroup = new ButtonGroup();
        categoryGroup.add(rbSupplement);
        categoryGroup.add(rbReport);
        categoryGroup.add(rbSuggest);
        rbSupplement.setSelected(true);
        categoryPanel.add(rbSupplement);
        categoryPanel.add(rbReport);
        categoryPanel.add(rbSuggest);


        // 3. 反馈内容输入
        JPanel contentPanel = new JPanel(new BorderLayout(5, 5));
        JTextArea contentArea = new JTextArea();
        contentArea.setLineWrap(true);
        contentArea.setRows(8);
        contentArea.setBorder(BorderFactory.createTitledBorder("反馈内容（请勿包含隐私信息）"));
        contentPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);


        // 4. 提交/后退按钮
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton btnBack = new JButton("后退");
        JButton btnSubmit = new JButton("提交");
        btnPanel.add(btnBack);
        btnPanel.add(btnSubmit);


        // 5. 组装窗口
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(categoryPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        feedbackDialog.add(mainPanel);


        // 6. 按钮事件
        btnBack.addActionListener(e -> feedbackDialog.dispose());

        btnSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // （1）获取用户输入
                String content = contentArea.getText().trim();
                if (content.isEmpty()) {
                    JOptionPane.showMessageDialog(feedbackDialog, "请输入反馈内容！", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }


                // （2）先查询当前用户的id和name（从user_data表）
                UserInfo userInfo = getUserInfoByUsername(username);
                if (userInfo == null) {
                    JOptionPane.showMessageDialog(feedbackDialog, "获取用户信息失败！", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }


                // （3）根据选择的分类，确定要插入的表和SQL（完全匹配表名/字段名）
                String sql = "";
                if (rbSupplement.isSelected()) {
                    // 修正：表名add_supplement，字段名add_supplement（下划线格式，无空格）
                    sql = "INSERT INTO add_supplement (id, name, add_supplement) VALUES (?, ?, ?)";
                } else if (rbReport.isSelected()) {
                    // 表名report_problem，字段名report_problem
                    sql = "INSERT INTO report_problem (id, name, report_problem) VALUES (?, ?, ?)";
                } else if (rbSuggest.isSelected()) {
                    // 表名put_suggestions，字段名put_suggestions
                    sql = "INSERT INTO put_suggestions (id, name, put_suggestions) VALUES (?, ?, ?)";
                } else {
                    JOptionPane.showMessageDialog(feedbackDialog, "请选择反馈分类！", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }


                // （4）执行插入操作（分表插入）
                Connection conn = null;
                PreparedStatement pstmt = null;
                try {
                    conn = DBUtil.getConnection();
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, userInfo.getId());      // 填充用户id
                    pstmt.setString(2, userInfo.getName()); // 填充用户名
                    pstmt.setString(3, content);            // 填充反馈内容
                    pstmt.executeUpdate();

                    JOptionPane.showMessageDialog(feedbackDialog, "反馈提交成功！", "提示", JOptionPane.INFORMATION_MESSAGE);
                    feedbackDialog.dispose();
                } catch (RuntimeException ex) {
                    JOptionPane.showMessageDialog(feedbackDialog, "反馈提交失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(feedbackDialog, "反馈提交失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    DBUtil.close(conn, pstmt);
                }
            }
        });

        feedbackDialog.setVisible(true);
    }


    /**
     * 内部工具类：封装用户id和name
     */
    private static class UserInfo {
        private int id;
        private String name;

        public UserInfo(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }


    /**
     * 根据username查询用户的id和name（从user_data表）
     */
    private static UserInfo getUserInfoByUsername(String username) {
        String sql = "SELECT id, name FROM user_data WHERE username = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("id");
                String userName = rs.getString("name");
                return new UserInfo(userId, userName);
            } else {
                JOptionPane.showMessageDialog(null, "当前用户不存在！", "错误", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "查询用户信息失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            return null;
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }


    // 重载方法（兼容无username的情况）
    public static void openFeedbackWindow(JFrame parent) {
        openFeedbackWindow(parent, "未知用户");
    }
}