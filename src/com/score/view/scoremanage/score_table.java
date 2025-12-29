package com.score.view.scoremanage;

import com.score.service.ImportService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class score_table extends JPanel {
    private final ImportService importService = new ImportService();
    private JTextField tableNameField;
    private JTextField headersField;
    private JTextField requiredHeadersField;
    private JFileChooser fileChooser;
    private File selectedFile;

    public score_table() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 输入面板
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        inputPanel.add(new JLabel("表单名称："));
        tableNameField = new JTextField();
        inputPanel.add(tableNameField);

        inputPanel.add(new JLabel("表头（逗号分隔）："));
        headersField = new JTextField();
        inputPanel.add(headersField);

        inputPanel.add(new JLabel("必填表头（逗号分隔）："));
        requiredHeadersField = new JTextField();
        inputPanel.add(requiredHeadersField);

        inputPanel.add(new JLabel("选择Excel："));
        JButton chooseBtn = new JButton("选择文件");
        chooseBtn.addActionListener(this::chooseFile);
        inputPanel.add(chooseBtn);

        // 按钮面板
        JPanel btnPanel = new JPanel();
        JButton createBtn = new JButton("创建表单");
        createBtn.addActionListener(this::createTable);
        JButton importBtn = new JButton("导入数据");
        importBtn.addActionListener(this::importData);
        btnPanel.add(createBtn);
        btnPanel.add(importBtn);

        add(inputPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        fileChooser = new JFileChooser();
    }

    // 选择Excel文件
    private void chooseFile(ActionEvent e) {
        int res = fileChooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            JOptionPane.showMessageDialog(this, "已选文件：" + selectedFile.getName());
        }
    }

    // 创建数据库表单
    private void createTable(ActionEvent e) {
        String tableName = tableNameField.getText().trim();
        String headersStr = headersField.getText().trim();
        if (tableName.isEmpty() || headersStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "表单名称/表头不能为空！");
            return;
        }

        List<String> headers = new ArrayList<>();
        for (String h : headersStr.split(",")) {
            String trimH = h.trim();
            if (!trimH.isEmpty()) headers.add(trimH);
        }

        try {
            importService.createFormTable(tableName, headers);
            JOptionPane.showMessageDialog(this, "表单创建成功！");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "创建失败：" + ex.getMessage());
        }
    }

    // 导入Excel数据
    private void importData(ActionEvent e) {
        String tableName = tableNameField.getText().trim();
        String requiredStr = requiredHeadersField.getText().trim();
        if (tableName.isEmpty() || requiredStr.isEmpty() || selectedFile == null) {
            JOptionPane.showMessageDialog(this, "请填写完整信息并选择文件！");
            return;
        }

        Set<String> requiredHeaders = new HashSet<>();
        for (String h : requiredStr.split(",")) {
            String trimH = h.trim();
            if (!trimH.isEmpty()) requiredHeaders.add(trimH);
        }

        try {
            importService.importExcelData(selectedFile, tableName, requiredHeaders);
            JOptionPane.showMessageDialog(this, "数据导入成功！");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "导入失败：" + ex.getMessage());
        }
    }
}