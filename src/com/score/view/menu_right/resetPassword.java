package com.score.view.menu_right;

import com.score.dao.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.score.util.DBUtil;

/**
 * 密码修改窗口（适配PersonalInfoWindow风格 + 仅验证数据库原密码）
 */
public class resetPassword extends JDialog {
    // 当前登录用户名（从User对象提取）
    private final String currentUsername;

    // 构造函数：适配PersonalInfoWindow的调用方式（接收User对象）
    public resetPassword(JFrame parent, User loginUser) {
        super(parent, "修改密码", true);
        // 从登录用户对象获取用户名，避免直接传字符串的耦合
        this.currentUsername = loginUser.getUsername() != null ? loginUser.getUsername().trim() : "";
        initUI();
    }

    // 初始化UI（与PersonalInfoWindow统一风格）
    private void initUI() {
        // 窗口基础配置（与个人信息窗口尺寸比例协调）
        setSize(400, 300);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // ========== 核心组件（统一字体风格） ==========
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));

        // 统一字体（与PersonalInfoWindow保持一致）
        Font labelFont = new Font("宋体", Font.PLAIN, 16);
        Font fieldFont = new Font("宋体", Font.PLAIN, 16);

        // 原密码输入行
        JLabel oldPwdLabel = new JLabel("原密码：");
        oldPwdLabel.setFont(labelFont);
        JPasswordField oldPwdField = new JPasswordField(20);
        oldPwdField.setFont(fieldFont);

        // 新密码输入行
        JLabel newPwdLabel = new JLabel("新密码：");
        newPwdLabel.setFont(labelFont);
        JPasswordField newPwdField = new JPasswordField(20);
        newPwdField.setFont(fieldFont);

        // 确认新密码输入行
        JLabel confirmPwdLabel = new JLabel("确认新密码：");
        confirmPwdLabel.setFont(labelFont);
        JPasswordField confirmPwdField = new JPasswordField(20);
        confirmPwdField.setFont(fieldFont);

        // 添加组件到面板
        panel.add(oldPwdLabel);
        panel.add(oldPwdField);
        panel.add(newPwdLabel);
        panel.add(newPwdField);
        panel.add(confirmPwdLabel);
        panel.add(confirmPwdField);

        // 底部按钮面板（统一按钮风格）
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton confirmBtn = new JButton("确认修改");
        confirmBtn.setFont(labelFont);
        JButton cancelBtn = new JButton("取消");
        cancelBtn.setFont(labelFont);
        btnPanel.add(confirmBtn);
        btnPanel.add(cancelBtn);

        // 组装窗口
        add(panel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        // ========== 核心逻辑：仅验证数据库原密码 + 更新 ==========
        confirmBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 1. 获取输入密码并去空格
                String oldPwd = new String(oldPwdField.getPassword()).trim();
                String newPwd = new String(newPwdField.getPassword()).trim();
                String confirmPwd = new String(confirmPwdField.getPassword()).trim();

                // 2. 基础输入校验（更友好的提示）
                if (oldPwd.isEmpty()) {
                    JOptionPane.showMessageDialog(resetPassword.this,
                            "原密码不能为空！", "提示", JOptionPane.WARNING_MESSAGE);
                    oldPwdField.requestFocus(); // 聚焦到原密码框
                    return;
                }
                if (newPwd.isEmpty()) {
                    JOptionPane.showMessageDialog(resetPassword.this,
                            "新密码不能为空！", "提示", JOptionPane.WARNING_MESSAGE);
                    newPwdField.requestFocus();
                    return;
                }
                if (!newPwd.equals(confirmPwd)) {
                    JOptionPane.showMessageDialog(resetPassword.this,
                            "两次输入的新密码不一致！", "错误", JOptionPane.ERROR_MESSAGE);
                    confirmPwdField.setText(""); // 清空确认密码框
                    confirmPwdField.requestFocus();
                    return;
                }
                // 新增：密码长度校验（增强安全性）
                if (newPwd.length() < 6) {
                    JOptionPane.showMessageDialog(resetPassword.this,
                            "新密码长度不能少于6位！", "提示", JOptionPane.WARNING_MESSAGE);
                    newPwdField.setText("");
                    confirmPwdField.setText("");
                    newPwdField.requestFocus();
                    return;
                }

                // 3. 核心：仅验证数据库中的原密码
                if (!checkOldPwdFromDB(oldPwd)) {
                    JOptionPane.showMessageDialog(resetPassword.this,
                            "原密码错误！", "错误", JOptionPane.ERROR_MESSAGE);
                    oldPwdField.setText("");
                    oldPwdField.requestFocus();
                    return;
                }

                // 4. 仅更新数据库密码
                if (updatePwdToDB(newPwd)) {
                    JOptionPane.showMessageDialog(resetPassword.this,
                            "密码修改成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    dispose(); // 关闭密码修改窗口
                } else {
                    JOptionPane.showMessageDialog(resetPassword.this,
                            "密码修改失败！", "失败", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 取消按钮逻辑
        cancelBtn.addActionListener(e -> dispose());
    }

    /**
     * 核心方法：仅从数据库验证原密码
     * @param inputOldPwd 用户输入的原密码
     * @return 匹配返回true，否则false
     */
    private boolean checkOldPwdFromDB(String inputOldPwd) {
        // 防空：用户名为空直接返回false
        if (currentUsername.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String sql = "SELECT password FROM user_data WHERE username = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, currentUsername); // 传入去空格后的用户名

            rs = pstmt.executeQuery();
            if (rs.next()) {
                // 仅对比数据库存储的密码（精准匹配）
                String dbPwd = rs.getString("password");
                return inputOldPwd.equals(dbPwd);
            } else {
                JOptionPane.showMessageDialog(this, "用户名不存在！", "错误", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "原密码验证失败：" + ex.getMessage(), "数据库错误", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            // 调用DBUtil统一关闭资源（与项目规范一致）
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /**
     * 核心方法：仅更新数据库中的密码
     * @param newPwd 新密码
     * @return 更新成功返回true
     */
    private boolean updatePwdToDB(String newPwd) {
        if (currentUsername.isEmpty()) {
            return false;
        }

        String sql = "UPDATE user_data SET password = ? WHERE username = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newPwd);
            pstmt.setString(2, currentUsername);

            // 仅判断是否有数据行被更新
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "密码更新失败：" + ex.getMessage(), "数据库错误", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            DBUtil.close(conn, pstmt);
        }
    }
}