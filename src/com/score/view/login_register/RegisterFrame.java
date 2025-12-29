package com.score.view.login_register;

import com.score.dao.User;
import com.score.entity.UserDao;
import com.score.entity.UserDaoImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;

/**
 * 注册界面（最终适配数据库版：tel为11位字符串）
 */
public class RegisterFrame extends JFrame {
    private JTextField usernameField, nameField, sexField, telField, emailField;
    private JPasswordField passwordField, confirmPwdField;
    private UserDao userDao = new UserDaoImpl();

    public RegisterFrame() {
        initFrame();
        initComponents();
    }

    private void initFrame() {
        setTitle("学生管理系统 - 注册");
        setSize(600, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(new Color(242, 242, 242));
    }

    private void initComponents() {
        // 标题面板
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(242, 242, 242));
        JLabel titleLabel = new JLabel("用户注册");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 30));
        titleLabel.setForeground(new Color(51, 51, 51));
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        // 表单面板
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(4, 2, 25, 20));
        formPanel.setBackground(new Color(242, 242, 242));
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 70, 20, 70));

        Font labelFont = new Font("微软雅黑", Font.PLAIN, 16);
        Color labelColor = new Color(70, 70, 70);
        Font inputFont = new Font("微软雅黑", Font.PLAIN, 15);
        Insets inputInsets = new Insets(8, 12, 8, 12);

        // 用户名
        JLabel usernameLabel = new JLabel("用户名：");
        usernameLabel.setFont(labelFont);
        usernameLabel.setForeground(labelColor);
        usernameField = new JTextField();
        usernameField.setFont(inputFont);
        usernameField.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        usernameField.setMargin(inputInsets);
        usernameField.setToolTipText("请输入唯一用户名（不可重复）");
        formPanel.add(usernameLabel);
        formPanel.add(usernameField);

        // 姓名
        JLabel nameLabel = new JLabel("姓  名：");
        nameLabel.setFont(labelFont);
        nameLabel.setForeground(labelColor);
        nameField = new JTextField();
        nameField.setFont(inputFont);
        nameField.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        nameField.setMargin(inputInsets);
        formPanel.add(nameLabel);
        formPanel.add(nameField);

        // 性别
        JLabel sexLabel = new JLabel("性  别：");
        sexLabel.setFont(labelFont);
        sexLabel.setForeground(labelColor);
        sexField = new JTextField();
        sexField.setFont(inputFont);
        sexField.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        sexField.setMargin(inputInsets);
        sexField.setToolTipText("请输入“男”或“女”");
        formPanel.add(sexLabel);
        formPanel.add(sexField);

        // 电话（适配11位字符串）
        JLabel telLabel = new JLabel("电  话：");
        telLabel.setFont(labelFont);
        telLabel.setForeground(labelColor);
        telField = new JTextField();
        telField.setFont(inputFont);
        telField.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        telField.setMargin(inputInsets);
        telField.setToolTipText("请输入11位纯数字手机号"); // 明确提示11位
        formPanel.add(telLabel);
        formPanel.add(telField);

        // 邮箱
        JLabel emailLabel = new JLabel("邮  箱：");
        emailLabel.setFont(labelFont);
        emailLabel.setForeground(labelColor);
        emailField = new JTextField();
        emailField.setFont(inputFont);
        emailField.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        emailField.setMargin(inputInsets);
        emailField.setToolTipText("例：xxx@xxx.com");
        formPanel.add(emailLabel);
        formPanel.add(emailField);

        // 密码
        JLabel pwdLabel = new JLabel("密  码：");
        pwdLabel.setFont(labelFont);
        pwdLabel.setForeground(labelColor);
        passwordField = new JPasswordField();
        passwordField.setFont(inputFont);
        passwordField.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        passwordField.setMargin(inputInsets);
        formPanel.add(pwdLabel);
        formPanel.add(passwordField);

        // 确认密码
        JLabel confirmPwdLabel = new JLabel("确认密码：");
        confirmPwdLabel.setFont(labelFont);
        confirmPwdLabel.setForeground(labelColor);
        confirmPwdField = new JPasswordField();
        confirmPwdField.setFont(inputFont);
        confirmPwdField.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        confirmPwdField.setMargin(inputInsets);
        formPanel.add(confirmPwdLabel);
        formPanel.add(confirmPwdField);

        add(formPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(new Color(242, 242, 242));
        btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 15));

        JButton registerBtn = new JButton("注册");
        registerBtn.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        registerBtn.setPreferredSize(new Dimension(130, 45));
        registerBtn.setBackground(new Color(51, 153, 255));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setBorderPainted(false);
        registerBtn.setFocusPainted(false);
        registerBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                registerBtn.setBackground(new Color(30, 144, 255));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                registerBtn.setBackground(new Color(51, 153, 255));
            }
        });

        JButton backBtn = new JButton("返回登录");
        backBtn.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        backBtn.setPreferredSize(new Dimension(130, 45));
        backBtn.setBackground(new Color(204, 204, 204));
        backBtn.setForeground(new Color(51, 51, 51));
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                backBtn.setBackground(new Color(170, 170, 170));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                backBtn.setBackground(new Color(204, 204, 204));
            }
        });

        btnPanel.add(registerBtn);
        btnPanel.add(backBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // 注册按钮事件（核心：适配tel为11位字符串）
        registerBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 1. 获取并清洗输入数据
                String username = usernameField.getText().trim();
                String name = nameField.getText().trim();
                String sex = sexField.getText().trim();
                String telStr = telField.getText().trim();
                String email = emailField.getText().trim();
                String password = new String(passwordField.getPassword()).trim();
                String confirmPwd = new String(confirmPwdField.getPassword()).trim();

                try {
                    // 2. 基础非空校验
                    if (username.isEmpty() || name.isEmpty() || sex.isEmpty() || telStr.isEmpty() || email.isEmpty() || password.isEmpty()) {
                        throw new IllegalArgumentException("所有字段不能为空！");
                    }
                    if (!password.equals(confirmPwd)) {
                        throw new IllegalArgumentException("两次密码输入不一致！");
                    }

                    // 3. 手机号专属校验（核心修改：严格11位纯数字）
                    // 移除所有非数字字符（防止用户输入-、空格等）
                    String cleanTel = telStr.replaceAll("[^0-9]", "");
                    if (cleanTel.length() != 11) {
                        throw new IllegalArgumentException("手机号必须是11位纯数字！");
                    }

                    // 4. 其他格式校验
                    // 邮箱格式
                    if (!email.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
                        throw new IllegalArgumentException("邮箱格式不正确（例：xxx@xxx.com）！");
                    }
                    // 性别格式
                    if (!sex.equals("男") && !sex.equals("女")) {
                        throw new IllegalArgumentException("性别只能输入“男”或“女”！");
                    }

                    // 5. 封装User对象（tel直接存11位字符串）
                    User user = new User();
                    user.setUsername(username);
                    user.setName(name);
                    user.setSex(sex);
                    user.setTel(cleanTel); // 关键：tel为字符串类型，直接赋值
                    user.setEmail(email);
                    user.setPassword(password);
                    user.setTitle("普通用户");

                    // 6. 写入数据库
                    if (userDao.addUser(user)) {
                        JOptionPane.showMessageDialog(RegisterFrame.this,
                                "注册成功！数据已写入数据库", "成功", JOptionPane.INFORMATION_MESSAGE);
                        dispose(); // 关闭注册窗口
                        new LoginFrame().setVisible(true); // 跳转到登录界面
                    } else {
                        throw new SQLException("用户名已存在，注册失败");
                    }

                } catch (IllegalArgumentException ex) {
                    // 输入格式错误提示
                    JOptionPane.showMessageDialog(RegisterFrame.this,
                            ex.getMessage(), "输入错误", JOptionPane.WARNING_MESSAGE);
                } catch (SQLException ex) {
                    // 数据库错误提示
                    JOptionPane.showMessageDialog(RegisterFrame.this,
                            ex.getMessage(), "数据库错误", JOptionPane.ERROR_MESSAGE);
                    usernameField.setText(""); // 清空重复的用户名
                } catch (Exception ex) {
                    // 其他未知错误
                    JOptionPane.showMessageDialog(RegisterFrame.this,
                            "注册失败：" + ex.getMessage(), "系统错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 返回登录按钮事件
        backBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        });
    }

    // 主方法（测试用）
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegisterFrame().setVisible(true));
    }
}