package com.score.view.login_register;

import com.score.dao.User;
import com.score.entity.UserDao;
import com.score.entity.UserDaoImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 忘记密码/重置密码界面（最终版）
 * 功能：用户名+邮箱+手机号三重身份验证 + 新密码校验 + 密码重置同步到数据库
 * 修复点：GridLayout布局下文本框未添加到面板的问题、重复事件绑定问题
 */
public class ForgetPwdFrame extends JDialog {
    // 界面组件：用户名、邮箱、手机号输入框
    private JTextField usernameField, emailField, telField;
    // 界面组件：新密码、确认新密码输入框（密码框，输入内容隐藏）
    private JPasswordField newPwdField, confirmPwdField;
    // 数据访问层对象：用于操作数据库
    private UserDao userDao = new UserDaoImpl();
    // 身份验证标记：true=验证通过，false=未通过
    private boolean isAuthSuccess = false;

    /**
     * 构造方法：初始化弹窗（模态对话框，阻塞其他窗口操作）
     */
    public ForgetPwdFrame() {
        // 参数说明：父窗口(null=无父窗口)、窗口标题、是否模态
        super((Frame) null, "忘记密码", true);
        // 初始化窗口基础属性（大小、位置、布局）
        initFrame();
        // 初始化所有界面组件（标签、输入框、按钮）并绑定事件
        initComponents();
    }

    /**
     * 初始化窗口基础属性
     */
    private void initFrame() {
        // 设置窗口大小：宽480px，高550px（适配3个输入框+密码重置区域）
        setSize(480, 550);
        // 窗口居中显示（相对于屏幕）
        setLocationRelativeTo(null);
        // 禁止调整窗口大小（避免布局错乱）
        setResizable(false);
        // 设置整体布局：BorderLayout（北、中、南），组件间距10px
        setLayout(new BorderLayout(10, 10));
    }

    /**
     * 初始化所有界面组件（核心方法）
     */
    private void initComponents() {
        // ====================== 1. 标题面板（北）======================
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("重置密码");
        // 设置标题字体：宋体、加粗、24号
        titleLabel.setFont(new Font("宋体", Font.BOLD, 24));
        titlePanel.add(titleLabel);
        // 将标题面板添加到窗口北部（顶部）
        add(titlePanel, BorderLayout.NORTH);

        // ====================== 2. 表单面板（中）======================
        // 主表单面板：垂直布局（BoxLayout.Y_AXIS）
        JPanel mainFormPanel = new JPanel();
        mainFormPanel.setLayout(new BoxLayout(mainFormPanel, BoxLayout.Y_AXIS));
        // 设置面板内边距：上10px、左50px、下10px、右50px（避免组件贴边）
        mainFormPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));

        // --------------------- 2.1 身份验证区域 ---------------------
        JPanel authPanel = new JPanel();
        // 布局：3行2列的网格布局，行间距10px，列间距15px（适配3个标签+3个输入框）
        authPanel.setLayout(new GridLayout(3, 2, 10, 15));
        // 面板边框：带标题的边框，提示用户验证规则
        authPanel.setBorder(BorderFactory.createTitledBorder("身份验证（用户名+邮箱+手机号）"));

        // 第1行：用户名标签 + 用户名输入框
        JLabel usernameLabel = new JLabel("用户名：");
        usernameLabel.setFont(new Font("宋体", Font.PLAIN, 16));
        authPanel.add(usernameLabel); // 标签 → 第1行第1列
        usernameField = new JTextField();
        usernameField.setFont(new Font("宋体", Font.PLAIN, 16));
        authPanel.add(usernameField); // 输入框 → 第1行第2列

        // 第2行：邮箱标签 + 邮箱输入框
        JLabel emailLabel = new JLabel("绑定邮箱：");
        emailLabel.setFont(new Font("宋体", Font.PLAIN, 16));
        authPanel.add(emailLabel); // 标签 → 第2行第1列
        emailField = new JTextField();
        emailField.setFont(new Font("宋体", Font.PLAIN, 16));
        authPanel.add(emailField); // 输入框 → 第2行第2列

        // 第3行：手机号标签 + 手机号输入框
        JLabel telLabel = new JLabel("绑定手机号：");
        telLabel.setFont(new Font("宋体", Font.PLAIN, 16));
        authPanel.add(telLabel); // 标签 → 第3行第1列
        telField = new JTextField();
        telField.setFont(new Font("宋体", Font.PLAIN, 16));
        // 鼠标悬浮提示：引导用户输入正确格式的手机号
        telField.setToolTipText("请输入11位纯数字手机号");
        authPanel.add(telField); // 输入框 → 第3行第2列

        // --------------------- 验证按钮 + 提示标签 ---------------------
        // 提示标签：显示验证结果（成功/失败），默认红色字体
        JLabel authTipLabel = new JLabel("");
        authTipLabel.setForeground(Color.RED);
        authTipLabel.setFont(new Font("宋体", Font.PLAIN, 14));

        // 验证按钮：触发身份验证逻辑
        JButton authBtn = new JButton("验证身份");
        authBtn.setFont(new Font("宋体", Font.PLAIN, 16));
        authBtn.setPreferredSize(new Dimension(120, 30)); // 按钮大小：宽120px，高30px

        // 按钮面板：居中显示按钮和提示标签
        JPanel authBtnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        authBtnPanel.add(authBtn);
        authBtnPanel.add(authTipLabel);

        // --------------------- 2.2 密码重置区域（初始隐藏） ---------------------
        JPanel pwdResetPanel = new JPanel();
        // 布局：2行2列（新密码+确认密码）
        pwdResetPanel.setLayout(new GridLayout(2, 2, 10, 20));
        pwdResetPanel.setBorder(BorderFactory.createTitledBorder("设置新密码"));
        pwdResetPanel.setVisible(false); // 初始隐藏，验证通过后显示

        // 新密码标签 + 新密码输入框
        JLabel newPwdLabel = new JLabel("新密码：");
        newPwdLabel.setFont(new Font("宋体", Font.PLAIN, 16));
        pwdResetPanel.add(newPwdLabel);
        newPwdField = new JPasswordField();
        newPwdField.setFont(new Font("宋体", Font.PLAIN, 16));
        pwdResetPanel.add(newPwdField);

        // 确认新密码标签 + 确认新密码输入框
        JLabel confirmPwdLabel = new JLabel("确认新密码：");
        confirmPwdLabel.setFont(new Font("宋体", Font.PLAIN, 16));
        pwdResetPanel.add(confirmPwdLabel);
        confirmPwdField = new JPasswordField();
        confirmPwdField.setFont(new Font("宋体", Font.PLAIN, 16));
        pwdResetPanel.add(confirmPwdField);

        // --------------------- 组装表单面板 ---------------------
        mainFormPanel.add(authPanel); // 添加身份验证区域
        mainFormPanel.add(authBtnPanel); // 添加验证按钮+提示标签
        mainFormPanel.add(Box.createVerticalStrut(15)); // 垂直间距15px
        mainFormPanel.add(pwdResetPanel); // 添加密码重置区域
        // 将主表单面板添加到窗口中部
        add(mainFormPanel, BorderLayout.CENTER);

        // ====================== 3. 底部按钮面板（南）======================
        JPanel btnPanel = new JPanel();
        // 布局：居中显示按钮，按钮间距30px，上下间距15px
        btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 15));

        // 确认重置按钮：初始禁用（验证通过后启用）
        JButton resetBtn = new JButton("确认重置");
        resetBtn.setFont(new Font("宋体", Font.PLAIN, 16));
        resetBtn.setPreferredSize(new Dimension(120, 40));
        resetBtn.setEnabled(false);

        // 取消按钮：关闭窗口
        JButton cancelBtn = new JButton("取消");
        cancelBtn.setFont(new Font("宋体", Font.PLAIN, 16));
        cancelBtn.setPreferredSize(new Dimension(120, 40));

        // 添加按钮到面板
        btnPanel.add(resetBtn);
        btnPanel.add(cancelBtn);
        // 将底部面板添加到窗口南部（底部）
        add(btnPanel, BorderLayout.SOUTH);

        // ====================== 核心事件绑定 ======================
        // --------------------- 1. 身份验证按钮事件 ---------------------
        authBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 1. 获取并清洗输入数据（去空格）
                String username = usernameField.getText().trim();
                String email = emailField.getText().trim();
                String tel = telField.getText().trim();

                // 2. 基础非空校验：三个字段都不能为空
                if (username.isEmpty() || email.isEmpty() || tel.isEmpty()) {
                    authTipLabel.setText("用户名、邮箱、手机号不能为空！");
                    return; // 终止方法，不执行后续逻辑
                }

                // 3. 手机号格式校验：必须是11位纯数字
                if (!tel.matches("^\\d{11}$")) {
                    authTipLabel.setText("手机号格式错误（请输入11位纯数字）！");
                    return;
                }

                // 4. 调用DAO的三重验证方法，查询数据库
                User user = userDao.getUserByUsernameEmailTel(username, email, tel);
                if (user != null) {
                    // 验证成功
                    isAuthSuccess = true;
                    authTipLabel.setForeground(Color.GREEN); // 字体改为绿色
                    authTipLabel.setText("验证成功！请设置新密码");
                    pwdResetPanel.setVisible(true); // 显示密码重置区域
                    resetBtn.setEnabled(true); // 启用确认重置按钮
                    // 锁定输入框：防止用户篡改验证信息
                    usernameField.setEditable(false);
                    emailField.setEditable(false);
                    telField.setEditable(false);
                } else {
                    // 验证失败
                    isAuthSuccess = false;
                    authTipLabel.setForeground(Color.RED); // 字体改为红色
                    authTipLabel.setText("用户名/邮箱/手机号不匹配！");
                    pwdResetPanel.setVisible(false); // 隐藏密码重置区域
                    resetBtn.setEnabled(false); // 禁用确认重置按钮
                }
            }
        });

        // --------------------- 2. 确认重置密码事件 ---------------------
        resetBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 双重校验：防止未验证直接重置
                if (!isAuthSuccess) {
                    JOptionPane.showMessageDialog(ForgetPwdFrame.this,
                            "请先完成身份验证！", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 1. 获取输入的密码（注意：JPasswordField需要转成String）
                String username = usernameField.getText().trim();
                String newPwd = new String(newPwdField.getPassword()).trim();
                String confirmPwd = new String(confirmPwdField.getPassword()).trim();

                // 2. 新密码非空校验
                if (newPwd.isEmpty()) {
                    JOptionPane.showMessageDialog(ForgetPwdFrame.this,
                            "新密码不能为空！", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 3. 新密码长度校验（至少6位）
                if (newPwd.length() < 6) {
                    JOptionPane.showMessageDialog(ForgetPwdFrame.this,
                            "密码长度不能少于6位！", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 4. 两次密码一致性校验
                if (!newPwd.equals(confirmPwd)) {
                    JOptionPane.showMessageDialog(ForgetPwdFrame.this,
                            "两次密码输入不一致！", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 5. 调用DAO重置密码（更新数据库）
                boolean isSuccess = userDao.resetPassword(username, newPwd);
                if (isSuccess) {
                    // 重置成功：提示用户，关闭窗口，跳转到登录界面
                    JOptionPane.showMessageDialog(ForgetPwdFrame.this,
                            "密码重置成功！新密码已同步到数据库", "成功", JOptionPane.INFORMATION_MESSAGE);
                    dispose(); // 关闭当前窗口
                    // 可选：打开登录界面（需确保LoginFrame类存在）
                    new LoginFrame().setVisible(true);
                } else {
                    // 重置失败：提示用户检查数据库
                    JOptionPane.showMessageDialog(ForgetPwdFrame.this,
                            "密码重置失败！请检查数据库连接或重试", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // --------------------- 3. 取消按钮事件 ---------------------
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // 直接关闭窗口，不执行任何操作
            }
        });
    }

    /**
     * 测试主方法：运行该类即可看到忘记密码界面
     * 实际项目中可从登录界面的“忘记密码”按钮调用该窗口
     */
    public static void main(String[] args) {
        // Swing组件需在事件调度线程中运行，避免线程安全问题
        SwingUtilities.invokeLater(() -> new ForgetPwdFrame().setVisible(true));
    }
}