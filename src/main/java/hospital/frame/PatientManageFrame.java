package hospital.frame;

import hospital.util.DBUtil;
import hospital.util.JdbcUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 患者管理界面
 *
 * 功能：
 * 1. 查询患者信息
 * 2. 添加患者
 * 3. 修改患者
 * 4. 删除患者
 * 5. 点击表格行后，将数据回显到输入框
 */
public class PatientManageFrame extends JFrame {

    // 表格组件，用来显示患者列表
    private JTable table;

    // 表格数据模型，用来管理表头和表格数据
    private DefaultTableModel tableModel;

    // 患者编号输入框
    // 患者编号是数据库自动生成的，所以这里不允许用户手动修改
    private JTextField patientIdField;

    // 患者姓名输入框
    private JTextField nameField;

    // 性别输入框
    private JTextField genderField;

    // 年龄输入框
    private JTextField ageField;

    // 手机号输入框
    private JTextField phoneField;

    // 身份证号输入框
    private JTextField idCardField;

    public PatientManageFrame() {
        // 设置窗口标题
        setTitle("医院门诊挂号系统 - 患者管理");

        // 设置窗口大小
        setSize(900, 550);

        // 窗口居中显示
        setLocationRelativeTo(null);

        // 关闭当前窗口时，不退出整个程序
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 初始化界面
        initView();

        // 加载患者数据
        loadPatientData();

        // 显示窗口
        setVisible(true);
    }

    /**
     * 初始化界面
     */
    private void initView() {
        // 整个窗口使用 BorderLayout 布局
        setLayout(new BorderLayout());

        // =========================
        // 1. 顶部标题
        // =========================
        JLabel titleLabel = new JLabel("患者管理", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        // =========================
        // 2. 中间表格
        // =========================
        String[] columns = {
                "患者编号",
                "姓名",
                "性别",
                "年龄",
                "手机号",
                "身份证号",
                "创建时间"
        };

        // 创建表格模型
        tableModel = new DefaultTableModel(columns, 0);

        // 创建表格
        table = new JTable(tableModel);

        // 设置表格行高
        table.setRowHeight(28);

        // 把表格放入滚动面板
        JScrollPane scrollPane = new JScrollPane(table);

        // 把滚动面板放在窗口中间
        add(scrollPane, BorderLayout.CENTER);

        // =========================
        // 3. 底部输入区和按钮区
        // =========================
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // 输入区面板，使用 GridLayout
        JPanel inputPanel = new JPanel(new GridLayout(2, 6, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // 创建输入框
        patientIdField = new JTextField();
        nameField = new JTextField();
        genderField = new JTextField();
        ageField = new JTextField();
        phoneField = new JTextField();
        idCardField = new JTextField();

        // 患者编号由数据库自动生成，不允许手动编辑
        patientIdField.setEditable(false);

        // 第一行：标签
        inputPanel.add(new JLabel("患者编号"));
        inputPanel.add(new JLabel("姓名"));
        inputPanel.add(new JLabel("性别"));
        inputPanel.add(new JLabel("年龄"));
        inputPanel.add(new JLabel("手机号"));
        inputPanel.add(new JLabel("身份证号"));

        // 第二行：输入框
        inputPanel.add(patientIdField);
        inputPanel.add(nameField);
        inputPanel.add(genderField);
        inputPanel.add(ageField);
        inputPanel.add(phoneField);
        inputPanel.add(idCardField);

        // 按钮区
        JPanel buttonPanel = new JPanel();

        JButton addButton = new JButton("添加");
        JButton updateButton = new JButton("修改");
        JButton deleteButton = new JButton("删除");
        JButton refreshButton = new JButton("刷新");
        JButton clearButton = new JButton("清空");
        JButton closeButton = new JButton("关闭");

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(closeButton);

        // 把输入区和按钮区放到底部面板
        bottomPanel.add(inputPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        // 把底部面板加入窗口下方
        add(bottomPanel, BorderLayout.SOUTH);

        // =========================
        // 4. 绑定事件
        // =========================

        // 点击表格某一行时，把该行数据填入输入框
        table.getSelectionModel().addListSelectionListener(e -> {
            // 这个判断可以避免事件重复触发
            if (!e.getValueIsAdjusting()) {
                fillInputFromSelectedRow();
            }
        });

        // 添加患者
        addButton.addActionListener(e -> addPatient());

        // 修改患者
        updateButton.addActionListener(e -> updatePatient());

        // 删除患者
        deleteButton.addActionListener(e -> deletePatient());

        // 刷新列表
        refreshButton.addActionListener(e -> loadPatientData());

        // 清空输入框
        clearButton.addActionListener(e -> clearInput());

        // 关闭窗口
        closeButton.addActionListener(e -> dispose());
    }

    /**
     * 加载患者数据到 JTable
     */
    private void loadPatientData() {
        // 清空表格原有数据
        tableModel.setRowCount(0);

        String sql = """
                SELECT
                    patient_id,
                    name,
                    gender,
                    age,
                    phone,
                    id_card,
                    create_time
                FROM patient
                ORDER BY patient_id
                """;

        try (
                // 获取数据库连接
                Connection conn = DBUtil.getConnection();

                // 创建 PreparedStatement
                PreparedStatement ps = conn.prepareStatement(sql);

                // 执行查询
                ResultSet rs = ps.executeQuery()
        ) {
            // 遍历查询结果
            while (rs.next()) {
                Object[] row = {
                        rs.getInt("patient_id"),
                        rs.getString("name"),
                        rs.getString("gender"),
                        rs.getInt("age"),
                        rs.getString("phone"),
                        rs.getString("id_card"),
                        rs.getTimestamp("create_time")
                };

                // 把一行患者数据添加到表格中
                tableModel.addRow(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载患者数据失败：" + e.getMessage());
        }
    }

    /**
     * 添加患者
     */
    private void addPatient() {
        // 获取输入框内容
        String name = nameField.getText().trim();
        String gender = genderField.getText().trim();
        String ageText = ageField.getText().trim();
        String phone = phoneField.getText().trim();
        String idCard = idCardField.getText().trim();

        // 简单校验：姓名不能为空
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "患者姓名不能为空");
            return;
        }

        // 简单校验：年龄不能为空
        if (ageText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "年龄不能为空");
            return;
        }

        int age;

        try {
            age = Integer.parseInt(ageText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "年龄必须是数字");
            return;
        }

        String sql = """
                INSERT INTO patient(name, gender, age, phone, id_card)
                VALUES (?, ?, ?, ?, ?)
                """;

        int rows = JdbcUtil.update(sql, name, gender, age, phone, idCard);

        if (rows > 0) {
            JOptionPane.showMessageDialog(this, "添加患者成功");

            // 重新加载表格数据
            loadPatientData();

            // 清空输入框
            clearInput();
        } else {
            JOptionPane.showMessageDialog(this, "添加患者失败");
        }
    }

    /**
     * 修改患者
     */
    private void updatePatient() {
        String patientIdText = patientIdField.getText().trim();

        if (patientIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先选择要修改的患者");
            return;
        }

        String name = nameField.getText().trim();
        String gender = genderField.getText().trim();
        String ageText = ageField.getText().trim();
        String phone = phoneField.getText().trim();
        String idCard = idCardField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "患者姓名不能为空");
            return;
        }

        if (ageText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "年龄不能为空");
            return;
        }

        int patientId;
        int age;

        try {
            patientId = Integer.parseInt(patientIdText);
            age = Integer.parseInt(ageText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "患者编号或年龄格式错误");
            return;
        }

        String sql = """
                UPDATE patient
                SET name = ?,
                    gender = ?,
                    age = ?,
                    phone = ?,
                    id_card = ?
                WHERE patient_id = ?
                """;

        int rows = JdbcUtil.update(sql, name, gender, age, phone, idCard, patientId);

        if (rows > 0) {
            JOptionPane.showMessageDialog(this, "修改患者成功");
            loadPatientData();
            clearInput();
        } else {
            JOptionPane.showMessageDialog(this, "修改患者失败");
        }
    }

    /**
     * 删除患者
     */
    private void deletePatient() {
        String patientIdText = patientIdField.getText().trim();

        if (patientIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的患者");
            return;
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                "确定要删除该患者吗？",
                "确认删除",
                JOptionPane.YES_NO_OPTION
        );

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        int patientId;

        try {
            patientId = Integer.parseInt(patientIdText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "患者编号格式错误");
            return;
        }

        String sql = "DELETE FROM patient WHERE patient_id = ?";

        int rows = JdbcUtil.update(sql, patientId);

        if (rows > 0) {
            JOptionPane.showMessageDialog(this, "删除患者成功");
            loadPatientData();
            clearInput();
        } else {
            JOptionPane.showMessageDialog(this, "删除患者失败");
        }
    }

    /**
     * 点击表格行后，把选中行的数据填入输入框
     */
    private void fillInputFromSelectedRow() {
        int selectedRow = table.getSelectedRow();

        // 没有选中任何行
        if (selectedRow == -1) {
            return;
        }

        // 从表格中取出当前选中行的数据
        Object patientId = tableModel.getValueAt(selectedRow, 0);
        Object name = tableModel.getValueAt(selectedRow, 1);
        Object gender = tableModel.getValueAt(selectedRow, 2);
        Object age = tableModel.getValueAt(selectedRow, 3);
        Object phone = tableModel.getValueAt(selectedRow, 4);
        Object idCard = tableModel.getValueAt(selectedRow, 5);

        // 填入输入框
        patientIdField.setText(patientId == null ? "" : patientId.toString());
        nameField.setText(name == null ? "" : name.toString());
        genderField.setText(gender == null ? "" : gender.toString());
        ageField.setText(age == null ? "" : age.toString());
        phoneField.setText(phone == null ? "" : phone.toString());
        idCardField.setText(idCard == null ? "" : idCard.toString());
    }

    /**
     * 清空输入框
     */
    private void clearInput() {
        patientIdField.setText("");
        nameField.setText("");
        genderField.setText("");
        ageField.setText("");
        phoneField.setText("");
        idCardField.setText("");

        // 清除表格选中状态
        table.clearSelection();
    }
}