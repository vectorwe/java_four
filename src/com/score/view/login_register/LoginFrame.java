package com.score.view.login_register;

import com.score.dao.User;
import com.score.entity.UserDao;
import com.score.entity.UserDaoImpl;
import com.score.view.ScoreSystemMainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 登录界面（适配user_data表+增强验证逻辑）
 * 核心功能：
 * 1. 用户名/密码输入验证（关联数据库user_data表）
 * 2. 登录成功跳转主界面（传递登录用户信息）
 * 3. 跳转注册/忘记密码界面
 */
public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private UserDao userDao = new UserDaoImpl();

    public LoginFrame() {
        initFrame();
        initComponents();
    }

    private void initFrame() {
        setTitle("学生管理系统 - 登录");
        setSize(450, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));
    }

    private void initComponents() {
        // 标题面板
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("学生管理系统");
        titleLabel.setFont(new Font("宋体", Font.BOLD, 24));
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        // 表单面板
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(3, 2, 10, 25));
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 60, 10, 60));

        // 用户名输入项
        JLabel usernameLabel = new JLabel("用户名：");
        usernameLabel.setFont(new Font("宋体", Font.PLAIN, 16));
        usernameField = new JTextField() {{
            setFont(new Font("宋体", Font.PLAIN, 16));
            setToolTipText("请输入注册时的用户名");
        }};

        // 密码输入项
        JLabel passwordLabel = new JLabel("密  码：");
        passwordLabel.setFont(new Font("宋体", Font.PLAIN, 16));
        passwordField = new JPasswordField() {{
            setFont(new Font("宋体", Font.PLAIN, 16));
            setToolTipText("请输入注册时的密码");
        }};

        // 忘记密码按钮
        JLabel emptyLabel = new JLabel();
        JButton forgetPwdBtn = new JButton("忘记密码？") {{
            setFont(new Font("宋体", Font.PLAIN, 14));
            setBorderPainted(false);
            setBackground(Color.WHITE);
            setForeground(Color.BLUE);
        }};

        // 添加组件到表单面板
        formPanel.add(usernameLabel);
        formPanel.add(usernameField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);
        formPanel.add(emptyLabel);
        formPanel.add(forgetPwdBtn);
        add(formPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        JButton loginBtn = new JButton("登录") {{
            setFont(new Font("宋体", Font.PLAIN, 16));
            setPreferredSize(new Dimension(120, 40));
        }};
        JButton registerBtn = new JButton("注册账号") {{
            setFont(new Font("宋体", Font.PLAIN, 16));
            setPreferredSize(new Dimension(120, 40));
        }};
        btnPanel.add(loginBtn);
        btnPanel.add(registerBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // ====================== 关键修改：登录按钮事件 ======================
        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // 1. 获取输入并清洗（重点：彻底去除所有空格，包括全角空格）
                    String username = usernameField.getText().trim()
                            .replaceAll("\\s+", "") // 去除所有空白字符（空格、制表符等）
                            .replaceAll("　", ""); // 去除全角空格
                    char[] passwordChars = passwordField.getPassword(); // 先获取char数组
                    String password = new String(passwordChars).trim()
                            .replaceAll("\\s+", "")
                            .replaceAll("　", "");

                    // 2. 非空校验（更严格）
                    if (username.isEmpty() || password.isEmpty()) {
                        JOptionPane.showMessageDialog(
                                LoginFrame.this,
                                "用户名和密码不能为空！",
                                "提示",
                                JOptionPane.WARNING_MESSAGE
                        );
                        // 清空密码框（char数组置空，更安全）
                        passwordField.setText("");
                        return;
                    }

                    // 3. 调试：打印输入的用户名密码（方便排查，上线时删除）
                    System.out.println("尝试登录 - 用户名：" + username + "，密码：" + password);

                    // 4. 数据库验证
                    User loginUser = userDao.login(username, password);

                    // 5. 结果处理
                    if (loginUser != null) {
                        JOptionPane.showMessageDialog(
                                LoginFrame.this,
                                "登录成功！欢迎你，" + loginUser.getName(),
                                "成功",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        dispose();
                        // 确保主界面类存在，若不存在可先注释或创建空的ScoreSystemMainFrame
                        new ScoreSystemMainFrame(loginUser).setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(
                                LoginFrame.this,
                                "用户名或密码错误！\n请检查：\n1. 用户名密码是否正确\n2. 是否有多余空格",
                                "错误",
                                JOptionPane.ERROR_MESSAGE
                        );
                        // 清空输入框
                        usernameField.setText("");
                        passwordField.setText("");
                        // 聚焦用户名输入框，提升体验
                        usernameField.requestFocus();
                    }
                } catch (Exception ex) {
                    // 关键修改：捕获所有异常并提示，方便定位问题
                    JOptionPane.showMessageDialog(
                            LoginFrame.this,
                            "登录出错：" + ex.getMessage(),
                            "系统错误",
                            JOptionPane.ERROR_MESSAGE
                    );
                    ex.printStackTrace(); // 打印异常栈，方便调试
                }
            }
        });

        // 注册按钮事件
        registerBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new RegisterFrame().setVisible(true);
            }
        });

        // 忘记密码按钮事件
        forgetPwdBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ForgetPwdFrame().setVisible(true);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}