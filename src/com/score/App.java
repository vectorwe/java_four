package com.score;

import com.score.view.login_register.LoginRegisterHomeFrame;
import javax.swing.*;

public class App {
    public static void main(String[] args) {
        // 启动整合展示页面
        SwingUtilities.invokeLater(() -> {
            LoginRegisterHomeFrame homeFrame = new LoginRegisterHomeFrame();
            homeFrame.setVisible(true);
        });
    }
}