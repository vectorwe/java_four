package com.score.view.menu_right;
import com.score.dao.User;
import com.score.entity.UserDao;
import com.score.entity.UserDaoImpl;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PersonalInfoWindow {

        // 数据访问对象（操作数据库）
        private static final UserDao userDao = new UserDaoImpl();

        /**
         * 打开个人信息窗口（支持修改）
         */
        public static void openPersonalWindow(JFrame parent, User loginUser) {
            if (loginUser == null) {
                JOptionPane.showMessageDialog(parent, "用户信息为空！请先登录", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 创建个人信息窗口
            JFrame personalFrame = new JFrame("个人信息");
            personalFrame.setSize(500, 400);
            personalFrame.setLocationRelativeTo(parent);
            personalFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            personalFrame.setLayout(new BorderLayout(10, 10));
            personalFrame.setResizable(false);

            // 构建信息面板（每行：标签 + 显示框 + 修改按钮）
            JPanel infoPanel = new JPanel(new GridLayout(5, 3, 10, 15)); // 5行3列（适配新增的email）
            infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // --------------------- 1. 用户名 ---------------------
            JLabel usernameLabel = new JLabel("用户名：");
            JTextField usernameField = new JTextField(loginUser.getUsername() != null ? loginUser.getUsername().trim() : "未设置");
            usernameField.setEditable(false); // 用户名通常不可修改
            JButton usernameBtn = new JButton("修改");
            usernameBtn.setEnabled(false); // 禁用按钮
            infoPanel.add(usernameLabel);
            infoPanel.add(usernameField);
            infoPanel.add(usernameBtn);

            // --------------------- 2. 角色 ---------------------
            JLabel roleLabel = new JLabel("角色：");
            JTextField roleField = new JTextField(loginUser.getTitle() != null ? loginUser.getTitle().trim() : "普通用户");
            roleField.setEditable(false);
            JButton roleBtn = new JButton("修改");
            roleBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    modifyField("角色", roleField, loginUser, "title");
                }
            });
            infoPanel.add(roleLabel);
            infoPanel.add(roleField);
            infoPanel.add(roleBtn);

            // --------------------- 3. 姓名 ---------------------
            JLabel nameLabel = new JLabel("姓名：");
            JTextField nameField = new JTextField(loginUser.getName() != null ? loginUser.getName().trim() : "未设置");
            nameField.setEditable(false);
            JButton nameBtn = new JButton("修改");
            nameBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    modifyField("姓名", nameField, loginUser, "name");
                }
            });
            infoPanel.add(nameLabel);
            infoPanel.add(nameField);
            infoPanel.add(nameBtn);

            // --------------------- 4. 联系方式 ---------------------
            JLabel telLabel = new JLabel("联系方式：");
            JTextField telField = new JTextField(loginUser.getTel() != null ? loginUser.getTel().trim() : "未绑定");
            telField.setEditable(false);
            JButton telBtn = new JButton("修改");
            telBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    modifyField("联系方式", telField, loginUser, "tel");
                }
            });
            infoPanel.add(telLabel);
            infoPanel.add(telField);
            infoPanel.add(telBtn);

            // --------------------- 5. 邮箱（新增，对应数据库email字段） ---------------------
            JLabel emailLabel = new JLabel("邮箱：");
            JTextField emailField = new JTextField(loginUser.getEmail() != null ? loginUser.getEmail().trim() : "未绑定");
            emailField.setEditable(false);
            JButton emailBtn = new JButton("修改");
            emailBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    modifyField("邮箱", emailField, loginUser, "email");
                }
            });
            infoPanel.add(emailLabel);
            infoPanel.add(emailField);
            infoPanel.add(emailBtn);

            // 组装窗口
            personalFrame.add(infoPanel, BorderLayout.CENTER);
            personalFrame.setVisible(true);
        }

        /**
         * 通用修改方法：弹窗修改字段 + 更新数据库
         * @param fieldName 字段名称（用于提示）
         * @param field 输入框
         * @param user 当前用户对象
         * @param dbField 数据库字段名（如"name"/"tel"）
         */
        private static void modifyField(String fieldName, JTextField field, User user, String dbField) {
            // 1. 弹窗让用户输入新值
            String newValue = JOptionPane.showInputDialog(
                    null,
                    "请输入新的" + fieldName + "：",
                    "修改" + fieldName,
                    JOptionPane.PLAIN_MESSAGE
            );

            // 2. 校验输入（非空）
            if (newValue == null || newValue.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, fieldName + "不能为空！", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            newValue = newValue.trim();

            // 3. 更新输入框显示
            field.setText(newValue);

            // 4. 更新用户对象和数据库
            try {
                // 更新用户对象对应的字段
                switch (dbField) {
                    case "title":
                        user.setTitle(newValue);
                        break;
                    case "name":
                        user.setName(newValue);
                        break;
                    case "tel":
                        user.setTel(newValue);
                        break;
                    case "email":
                        user.setEmail(newValue);
                        break;
                }
                // 调用DAO更新数据库（需确保UserDao有updateUser方法）
                userDao.updateUser(user);
                JOptionPane.showMessageDialog(null, fieldName + "修改成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, fieldName + "修改失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }

}
