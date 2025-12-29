package com.score.dao;

/**
 * 学生实体类：数据载体，仅封装属性和get/set
 * 对应可视化界面中输入的学号、姓名、性别、电话、家庭住址数据
 */
public class Student {
    // 修正：删除成绩字段，新增性别、电话、家庭住址字段
    private String id;         // 学号（唯一标识）
    private String name;       // 姓名
    private String gender;     // 性别
    private String phone;      // 电话
    private String address;    // 家庭住址

    // 无参构造器（框架/反射必备）
    public Student() {
    }

    // 全参构造器（方便快速创建对象）
    public Student(String id, String name, String gender, String phone, String address) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.phone = phone;
        this.address = address;
    }

    // Getter/Setter：供其他层获取/修改数据（对应新增字段）
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    // 修正：toString方法适配新字段，便于打印/导出数据
    @Override
    public String toString() {
        return id + "," + name + "," + gender + "," + phone + "," + address;
    }
}