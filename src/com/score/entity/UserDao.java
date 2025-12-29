package com.score.entity;

import com.score.dao.User;
import java.util.List;

/**
 * 用户数据访问层接口
 * 定义用户的所有数据库操作规范
 */
public interface UserDao {
    /**
     * 用户登录验证
     * @param username 用户名
     * @param password 密码
     * @retAurn 验证成功返回User对象，失败返回null
     */
    User login(String username, String password);

    /**
     * 新增用户（注册）
     * @param user 用户对象
     * @return 成功返回true，失败返回false（用户名重复/参数错误）
     */
    boolean addUser(User user);

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 存在返回User对象，不存在返回null
     */
    User getUserByUsername(String username);

    /**
     * 修改用户信息
     * @param user 包含新信息的用户对象（username作为唯一标识）
     * @return 成功返回true，失败返回false
     */
    boolean updateUser(User user);

    /**
     * 根据用户名删除用户
     * @param username 用户名
     * @return 成功返回true，失败返回false
     */
    boolean deleteUser(String username);

    /**
     * 查询所有用户
     * @return 用户列表（无数据返回空列表）
     */
    List<User> listAllUsers();

    /**
     * 根据用户名+邮箱查询用户（忘记密码验证）
     * @param username 用户名
     * @param email 邮箱
     * @return 匹配返回User对象，否则返回null
     */
    User getUserByUsernameAndEmail(String username, String email);

    /**
     * 根据用户名+邮箱+手机号查询用户（三重验证）
     * @param username 用户名
     * @param email 邮箱
     * @param tel 手机号
     * @return 匹配返回User对象，否则返回null
     */
    User getUserByUsernameEmailTel(String username, String email, String tel);

    /**
     * 重置密码
     * @param username 用户名
     * @param newPassword 新密码
     * @return 成功返回true，失败返回false
     */
    boolean resetPassword(String username, String newPassword);
}