package com.score.dao;

/**
 * 用户实体类（对应数据库中的用户表）
 */
public class User {
    private int id;
    private String name;
    private String sex;
    private String title;
    private String tel;
    private String email;
    private String username;
    private String password;

    public User() {}

    public User(int id, String name, String sex, String title, String tel, String email, String username, String password) {
        this.id = id;
        this.name = name;
        this.sex = sex;
        this.title = title;
        this.tel = tel;
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // 修复：方法名改为getId()（符合驼峰规范）
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // 其他Getter/Setter方法保持不变
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTel() { return tel; }
    public void setTel(String tel) { this.tel = tel; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                ", title='" + title + '\'' +
                ", tel='" + tel + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}