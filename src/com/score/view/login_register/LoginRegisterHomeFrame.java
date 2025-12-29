package com.score.view.login_register;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 登录/注册/忘记密码的整合展示页面
 * 程序启动后第一个界面，点击按钮跳转到对应功能页面
 */
public class LoginRegisterHomeFrame extends JFrame {
    public LoginRegisterHomeFrame() {
        initFrame();
        initComponents();
    }

    // 窗口基础配置
    private void initFrame() {
        setTitle("学生管理系统 - 首页");
        setSize(600, 400);
        setLocationRelativeTo(null); // 居中显示
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout(20, 20));
    }

    // 初始化组件
    private void initComponents() {
        // 1. 标题面板
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("学生管理系统");
        titleLabel.setFont(new Font("宋体", Font.BOLD, 36));
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        // 2. 按钮面板（核心功能入口）
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new GridLayout(3, 1, 0, 20)); // 3行1列，垂直间距20
        btnPanel.setBorder(BorderFactory.createEmptyBorder(50, 150, 50, 150));

        // 登录按钮
        JButton loginBtn = new JButton("登录");
        loginBtn.setFont(new Font("宋体", Font.PLAIN, 24));
        loginBtn.setPreferredSize(new Dimension(300, 60));

        // 注册按钮
        JButton registerBtn = new JButton("注册");
        registerBtn.setFont(new Font("宋体", Font.PLAIN, 24));
        registerBtn.setPreferredSize(new Dimension(300, 60));

        // 忘记密码按钮
        JButton forgetPwdBtn = new JButton("忘记密码");
        forgetPwdBtn.setFont(new Font("宋体", Font.PLAIN, 24));
        forgetPwdBtn.setPreferredSize(new Dimension(300, 60));

        btnPanel.add(loginBtn);
        btnPanel.add(registerBtn);
        btnPanel.add(forgetPwdBtn);
        add(btnPanel, BorderLayout.CENTER);

        // ====================== 按钮事件绑定（跳转逻辑） ======================
        // 点击“登录”→ 打开登录窗口，关闭当前首页
        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // 关闭当前首页
                new LoginFrame().setVisible(true); // 打开登录窗口
            }
        });

        // 点击“注册”→ 打开注册窗口，关闭当前首页
        registerBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new RegisterFrame().setVisible(true);
            }
        });

        // 点击“忘记密码”→ 打开忘记密码窗口（模态，不关闭首页）
        forgetPwdBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ForgetPwdFrame().setVisible(true);
            }
        });
    }
}