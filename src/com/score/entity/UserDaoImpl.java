package com.score.entity;

import com.score.dao.User;
import com.score.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDao接口的MySQL实现类（最终稳定版）
 * 修复点：
 * 1. 适配DBUtil的Statement资源关闭
 * 2. 优化异常处理，避免空指针
 * 3. 统一日志格式，便于调试
 */
public class UserDaoImpl implements UserDao {

    // ========== 1. 用户登录验证 ==========
    @Override
    public User login(String username, String password) {
        // 前置参数校验（防御性编程）
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            System.err.println("[UserDaoImpl] 登录失败：用户名/密码为空");
            return null;
        }

        String sql = "SELECT * FROM user_data WHERE username = ? AND password = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            // 严格去除首尾空格，避免输入空格导致验证失败
            pstmt.setString(1, username.trim());
            pstmt.setString(2, password.trim());

            System.out.println("[UserDaoImpl] 执行登录SQL：" + sql);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = buildUserFromResultSet(rs);
                // 补充密码字段（build方法未包含，仅登录场景需要）
                user.setPassword(rs.getString("password"));
                System.out.println("[UserDaoImpl] 登录成功：用户名=" + username);
                return user;
            } else {
                System.err.println("[UserDaoImpl] 登录失败：用户名/密码不匹配");
            }
        } catch (SQLException e) {
            System.err.println("[UserDaoImpl] 登录SQL异常：" + e.getMessage());
            // 打印异常栈（调试阶段保留，生产环境可移除）
            e.printStackTrace();
        } finally {
            // 调用DBUtil关闭资源，适配PreparedStatement
            DBUtil.close(conn, pstmt, rs);
        }
        return null;
    }

    // ========== 2. 新增用户（注册） ==========
    @Override
    public boolean addUser(User user) {
        // 前置校验：用户对象为空直接返回失败
        if (user == null) {
            System.err.println("[UserDaoImpl] 注册失败：用户对象为空");
            return false;
        }
        // 校验用户名是否重复
        if (getUserByUsername(user.getUsername()) != null) {
            System.err.println("[UserDaoImpl] 注册失败：用户名" + user.getUsername() + "已存在");
            return false;
        }

        String sql = "INSERT INTO user_data (username, password, title, name, sex, tel, email) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            // 赋值参数，空值兜底，避免SQL插入null导致异常
            pstmt.setString(1, user.getUsername() == null ? "" : user.getUsername().trim());
            pstmt.setString(2, user.getPassword() == null ? "" : user.getPassword().trim());
            pstmt.setString(3, user.getTitle() == null ? "普通用户" : user.getTitle().trim());
            pstmt.setString(4, user.getName() == null ? "" : user.getName().trim());
            pstmt.setString(5, user.getSex() == null ? "" : user.getSex().trim());
            pstmt.setString(6, user.getTel() == null ? "" : user.getTel().trim());
            pstmt.setString(7, user.getEmail() == null ? "" : user.getEmail().trim());

            System.out.println("[UserDaoImpl] 执行注册SQL：" + sql);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("[UserDaoImpl] 注册成功：用户名=" + user.getUsername());
                return true;
            } else {
                System.err.println("[UserDaoImpl] 注册失败：无数据插入");
            }
        } catch (SQLException e) {
            System.err.println("[UserDaoImpl] 注册SQL异常：" + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }

    // ========== 3. 根据用户名查询用户 ==========
    @Override
    public User getUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            System.err.println("[UserDaoImpl] 查询用户失败：用户名为空");
            return null;
        }

        String sql = "SELECT * FROM user_data WHERE username = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username.trim());

            rs = pstmt.executeQuery();
            if (rs.next()) {
                return buildUserFromResultSet(rs);
            } else {
                System.err.println("[UserDaoImpl] 查询用户失败：用户名" + username + "不存在");
            }
        } catch (SQLException e) {
            System.err.println("[UserDaoImpl] 查询用户SQL异常：" + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return null;
    }

    // ========== 4. 修改用户信息 ==========
    @Override
    public boolean updateUser(User user) {
        if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            System.err.println("[UserDaoImpl] 修改用户失败：用户对象/用户名为空");
            return false;
        }

        String sql = "UPDATE user_data SET title=?, name=?, sex=?, tel=?, email=? WHERE username=?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            // 参数赋值，空值兜底
            pstmt.setString(1, user.getTitle() == null ? "" : user.getTitle().trim());
            pstmt.setString(2, user.getName() == null ? "" : user.getName().trim());
            pstmt.setString(3, user.getSex() == null ? "" : user.getSex().trim());
            pstmt.setString(4, user.getTel() == null ? "" : user.getTel().trim());
            pstmt.setString(5, user.getEmail() == null ? "" : user.getEmail().trim());
            pstmt.setString(6, user.getUsername().trim());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("[UserDaoImpl] 修改用户成功：用户名=" + user.getUsername());
                return true;
            } else {
                System.err.println("[UserDaoImpl] 修改用户失败：用户名" + user.getUsername() + "不存在");
            }
        } catch (SQLException e) {
            System.err.println("[UserDaoImpl] 修改用户SQL异常：" + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }

    // ========== 5. 删除用户 ==========
    @Override
    public boolean deleteUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            System.err.println("[UserDaoImpl] 删除用户失败：用户名为空");
            return false;
        }

        String sql = "DELETE FROM user_data WHERE username = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username.trim());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("[UserDaoImpl] 删除用户成功：用户名=" + username);
                return true;
            } else {
                System.err.println("[UserDaoImpl] 删除用户失败：用户名" + username + "不存在");
            }
        } catch (SQLException e) {
            System.err.println("[UserDaoImpl] 删除用户SQL异常：" + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }

    // ========== 6. 查询所有用户 ==========
    @Override
    public List<User> listAllUsers() {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT * FROM user_data";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                userList.add(buildUserFromResultSet(rs));
            }
            System.out.println("[UserDaoImpl] 查询所有用户成功：共查询到" + userList.size() + "条数据");
        } catch (SQLException e) {
            System.err.println("[UserDaoImpl] 查询所有用户SQL异常：" + e.getMessage());
            e.printStackTrace();
        } finally {
            // 手动关闭Statement资源（DBUtil未适配，补充关闭逻辑）
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("[UserDaoImpl] 关闭Statement资源失败：" + e.getMessage());
            }
        }
        return userList;
    }

    // ========== 7. 用户名+邮箱验证（忘记密码） ==========
    @Override
    public User getUserByUsernameAndEmail(String username, String email) {
        if (username == null || username.trim().isEmpty() || email == null || email.trim().isEmpty()) {
            System.err.println("[UserDaoImpl] 验证失败：用户名/邮箱为空");
            return null;
        }

        String sql = "SELECT * FROM user_data WHERE username = ? AND email = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username.trim());
            pstmt.setString(2, email.trim());

            rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setSex(rs.getString("sex") == null ? "" : rs.getString("sex"));
                System.out.println("[UserDaoImpl] 用户名+邮箱验证成功：用户名=" + username);
                return user;
            } else {
                System.err.println("[UserDaoImpl] 验证失败：用户名/邮箱不匹配");
            }
        } catch (SQLException e) {
            System.err.println("[UserDaoImpl] 验证SQL异常：" + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return null;
    }

    // ========== 8. 三重验证（用户名+邮箱+手机号） ==========
    @Override
    public User getUserByUsernameEmailTel(String username, String email, String tel) {
        if (username == null || username.trim().isEmpty() || email == null || email.trim().isEmpty() || tel == null || tel.trim().isEmpty()) {
            System.err.println("[UserDaoImpl] 三重验证失败：参数为空");
            return null;
        }

        String sql = "SELECT * FROM user_data WHERE username = ? AND email = ? AND tel = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username.trim());
            pstmt.setString(2, email.trim());
            pstmt.setString(3, tel.trim());

            rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setUsername(rs.getString("username"));
                System.out.println("[UserDaoImpl] 三重验证成功：用户名=" + username);
                return user;
            } else {
                System.err.println("[UserDaoImpl] 三重验证失败：参数不匹配");
            }
        } catch (SQLException e) {
            System.err.println("[UserDaoImpl] 三重验证SQL异常：" + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return null;
    }

    // ========== 9. 重置密码 ==========
    @Override
    public boolean resetPassword(String username, String newPassword) {
        if (username == null || username.trim().isEmpty() || newPassword == null || newPassword.trim().isEmpty()) {
            System.err.println("[UserDaoImpl] 重置密码失败：参数为空");
            return false;
        }

        String sql = "UPDATE user_data SET password = ? WHERE username = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newPassword.trim());
            pstmt.setString(2, username.trim());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("[UserDaoImpl] 重置密码成功：用户名=" + username);
                return true;
            } else {
                System.err.println("[UserDaoImpl] 重置密码失败：用户名" + username + "不存在");
            }
        } catch (SQLException e) {
            System.err.println("[UserDaoImpl] 重置密码SQL异常：" + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return false;
    }

    // ========== 工具方法：从ResultSet构建User对象（复用逻辑） ==========
    private User buildUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        // 空值兜底，避免ResultSet.getXXX返回null导致空指针
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username") == null ? "" : rs.getString("username").trim());
        user.setTitle(rs.getString("title") == null ? "普通用户" : rs.getString("title").trim());
        user.setName(rs.getString("name") == null ? "" : rs.getString("name").trim());
        user.setSex(rs.getString("sex") == null ? "" : rs.getString("sex").trim());
        user.setTel(rs.getString("tel") == null ? "" : rs.getString("tel").trim());
        user.setEmail(rs.getString("email") == null ? "" : rs.getString("email").trim());
        return user;
    }
}