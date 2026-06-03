package hospital.frame;

import hospital.util.DBUtil;
import hospital.util.JdbcUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 医生管理界面
 *
 * 功能：
 * 1. 查询医生信息
 * 2. 添加医生
 * 3. 修改医生
 * 4. 删除医生
 * 5. 点击表格行后，把医生信息回显到输入框
 */
public class DoctorManageFrame extends JFrame {

    // 表格组件，用来显示医生列表
    private JTable table;

    // 表格数据模型，用来管理表头和表格数据
    private DefaultTableModel tableModel;

    // 医生编号输入框，数据库自动生成，不允许手动修改
    private JTextField doctorIdField;

    // 医生姓名输入框
    private JTextField nameField;

    // 性别下拉框
    private JComboBox<String> genderBox;

    // 职称输入框
    private JTextField titleField;

    // 科室下拉框
    private JComboBox<DeptItem> deptBox;

    // 挂号费输入框
    private JTextField feeField;

    public DoctorManageFrame() {
        // 设置窗口标题
        setTitle("医院门诊挂号系统 - 医生管理");

        // 设置窗口大小
        setSize(950, 550);

        // 窗口居中显示
        setLocationRelativeTo(null);

        // 关闭当前窗口时，不退出整个程序
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 初始化界面
        initView();

        // 加载科室下拉框数据
        loadDepartmentData();

        // 加载医生表格数据
        loadDoctorData();

        // 显示窗口
        setVisible(true);
    }

    /**
     * 初始化界面
     */
    private void initView() {
        // 整体窗口使用 BorderLayout 布局
        setLayout(new BorderLayout());

        // =========================
        // 1. 顶部标题
        // =========================
        JLabel titleLabel = new JLabel("医生管理", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        // =========================
        // 2. 中间表格
        // =========================
        String[] columns = {
                "医生编号",
                "医生姓名",
                "性别",
                "职称",
                "科室编号",
                "科室名称",
                "挂号费"
        };

        // 创建表格模型，重写 isCellEditable，让表格不能直接编辑
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // 创建表格
        table = new JTable(tableModel);

        // 设置表格行高
        table.setRowHeight(28);

        // 把表格放入滚动面板，数据多时可以滚动
        JScrollPane scrollPane = new JScrollPane(table);

        // 把滚动面板放到窗口中间
        add(scrollPane, BorderLayout.CENTER);

        // =========================
        // 3. 底部输入区和按钮区
        // =========================
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // 输入区，2行6列
        JPanel inputPanel = new JPanel(new GridLayout(2, 6, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // 创建输入组件
        doctorIdField = new JTextField();
        nameField = new JTextField();
        genderBox = new JComboBox<>();
        titleField = new JTextField();
        deptBox = new JComboBox<>();
        feeField = new JTextField();

        // 医生编号由数据库自动生成，不允许手动输入
        doctorIdField.setEditable(false);

        // 性别下拉框选项
        genderBox.addItem("男");
        genderBox.addItem("女");

        // 第一行：标签
        inputPanel.add(new JLabel("医生编号"));
        inputPanel.add(new JLabel("医生姓名"));
        inputPanel.add(new JLabel("性别"));
        inputPanel.add(new JLabel("职称"));
        inputPanel.add(new JLabel("所属科室"));
        inputPanel.add(new JLabel("挂号费"));

        // 第二行：输入框/下拉框
        inputPanel.add(doctorIdField);
        inputPanel.add(nameField);
        inputPanel.add(genderBox);
        inputPanel.add(titleField);
        inputPanel.add(deptBox);
        inputPanel.add(feeField);

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

        // 把输入区和按钮区加入底部面板
        bottomPanel.add(inputPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        // 把底部面板加入窗口下方
        add(bottomPanel, BorderLayout.SOUTH);

        // =========================
        // 4. 绑定事件
        // =========================

        // 点击表格行时，把该行医生数据填入输入框
        table.getSelectionModel().addListSelectionListener(e -> {
            // 避免事件重复触发
            if (!e.getValueIsAdjusting()) {
                fillInputFromSelectedRow();
            }
        });

        // 添加医生
        addButton.addActionListener(e -> addDoctor());

        // 修改医生
        updateButton.addActionListener(e -> updateDoctor());

        // 删除医生
        deleteButton.addActionListener(e -> deleteDoctor());

        // 刷新医生列表
        refreshButton.addActionListener(e -> loadDoctorData());

        // 清空输入框
        clearButton.addActionListener(e -> clearInput());

        // 关闭窗口
        closeButton.addActionListener(e -> dispose());
    }

    /**
     * 加载科室数据到下拉框
     *
     * 医生表 doctor 中有 dept_id 外键，
     * 所以添加医生时必须选择一个已存在的科室。
     */
    private void loadDepartmentData() {
        // 清空原来的科室选项
        deptBox.removeAllItems();

        String sql = """
                SELECT dept_id, dept_name
                FROM department
                ORDER BY dept_id
                """;

        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                int deptId = rs.getInt("dept_id");
                String deptName = rs.getString("dept_name");

                // 把科室编号和科室名称封装成 DeptItem 放进下拉框
                deptBox.addItem(new DeptItem(deptId, deptName));
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载科室数据失败：" + e.getMessage());
        }
    }

    /**
     * 加载医生数据到表格
     */
    private void loadDoctorData() {
        // 清空表格旧数据
        tableModel.setRowCount(0);

        String sql = """
                SELECT
                    d.doctor_id,
                    d.name,
                    d.gender,
                    d.title,
                    d.dept_id,
                    dept.dept_name,
                    d.fee
                FROM doctor d
                JOIN department dept ON d.dept_id = dept.dept_id
                ORDER BY d.doctor_id
                """;

        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Object[] row = {
                        rs.getInt("doctor_id"),
                        rs.getString("name"),
                        rs.getString("gender"),
                        rs.getString("title"),
                        rs.getInt("dept_id"),
                        rs.getString("dept_name"),
                        rs.getBigDecimal("fee")
                };

                tableModel.addRow(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载医生数据失败：" + e.getMessage());
        }
    }

    /**
     * 添加医生
     */
    private void addDoctor() {
        // 获取输入框内容
        String name = nameField.getText().trim();
        String gender = (String) genderBox.getSelectedItem();
        String title = titleField.getText().trim();
        String feeText = feeField.getText().trim();

        // 获取选中的科室
        DeptItem deptItem = (DeptItem) deptBox.getSelectedItem();

        // 校验医生姓名
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "医生姓名不能为空");
            return;
        }

        // 校验科室
        if (deptItem == null) {
            JOptionPane.showMessageDialog(this, "请先添加科室，再添加医生");
            return;
        }

        // 校验挂号费
        if (feeText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "挂号费不能为空");
            return;
        }

        BigDecimal fee;

        try {
            fee = new BigDecimal(feeText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "挂号费必须是数字，例如 30 或 30.00");
            return;
        }

        String sql = """
                INSERT INTO doctor(name, gender, title, dept_id, fee)
                VALUES (?, ?, ?, ?, ?)
                """;

        int rows = JdbcUtil.update(sql, name, gender, title, deptItem.getDeptId(), fee);

        if (rows > 0) {
            JOptionPane.showMessageDialog(this, "添加医生成功");
            loadDoctorData();
            clearInput();
        } else {
            JOptionPane.showMessageDialog(this, "添加医生失败");
        }
    }

    /**
     * 修改医生
     */
    private void updateDoctor() {
        String doctorIdText = doctorIdField.getText().trim();

        // 必须先选中一行医生，才能修改
        if (doctorIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先选择要修改的医生");
            return;
        }

        String name = nameField.getText().trim();
        String gender = (String) genderBox.getSelectedItem();
        String title = titleField.getText().trim();
        String feeText = feeField.getText().trim();

        DeptItem deptItem = (DeptItem) deptBox.getSelectedItem();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "医生姓名不能为空");
            return;
        }

        if (deptItem == null) {
            JOptionPane.showMessageDialog(this, "所属科室不能为空");
            return;
        }

        if (feeText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "挂号费不能为空");
            return;
        }

        int doctorId;
        BigDecimal fee;

        try {
            doctorId = Integer.parseInt(doctorIdText);
            fee = new BigDecimal(feeText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "医生编号或挂号费格式错误");
            return;
        }

        String sql = """
                UPDATE doctor
                SET name = ?,
                    gender = ?,
                    title = ?,
                    dept_id = ?,
                    fee = ?
                WHERE doctor_id = ?
                """;

        int rows = JdbcUtil.update(sql, name, gender, title, deptItem.getDeptId(), fee, doctorId);

        if (rows > 0) {
            JOptionPane.showMessageDialog(this, "修改医生成功");
            loadDoctorData();
            clearInput();
        } else {
            JOptionPane.showMessageDialog(this, "修改医生失败");
        }
    }

    /**
     * 删除医生
     */
    private void deleteDoctor() {
        String doctorIdText = doctorIdField.getText().trim();

        if (doctorIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的医生");
            return;
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                "确定要删除该医生吗？",
                "确认删除",
                JOptionPane.YES_NO_OPTION
        );

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        int doctorId;

        try {
            doctorId = Integer.parseInt(doctorIdText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "医生编号格式错误");
            return;
        }

        String sql = "DELETE FROM doctor WHERE doctor_id = ?";

        int rows = JdbcUtil.update(sql, doctorId);

        if (rows > 0) {
            JOptionPane.showMessageDialog(this, "删除医生成功");
            loadDoctorData();
            clearInput();
        } else {
            JOptionPane.showMessageDialog(this, "删除医生失败");
        }
    }

    /**
     * 点击表格行后，把选中行数据填入输入框
     */
    private void fillInputFromSelectedRow() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            return;
        }

        Object doctorId = tableModel.getValueAt(selectedRow, 0);
        Object name = tableModel.getValueAt(selectedRow, 1);
        Object gender = tableModel.getValueAt(selectedRow, 2);
        Object title = tableModel.getValueAt(selectedRow, 3);
        Object deptId = tableModel.getValueAt(selectedRow, 4);
        Object fee = tableModel.getValueAt(selectedRow, 6);

        doctorIdField.setText(doctorId == null ? "" : doctorId.toString());
        nameField.setText(name == null ? "" : name.toString());
        titleField.setText(title == null ? "" : title.toString());
        feeField.setText(fee == null ? "" : fee.toString());

        // 设置性别下拉框
        if (gender != null) {
            genderBox.setSelectedItem(gender.toString());
        }

        // 根据 dept_id 选中对应科室
        if (deptId != null) {
            selectDeptById(Integer.parseInt(deptId.toString()));
        }
    }

    /**
     * 根据科室编号，在下拉框中选中对应科室
     */
    private void selectDeptById(int deptId) {
        for (int i = 0; i < deptBox.getItemCount(); i++) {
            DeptItem item = deptBox.getItemAt(i);

            if (item.getDeptId() == deptId) {
                deptBox.setSelectedIndex(i);
                return;
            }
        }
    }

    /**
     * 清空输入框
     */
    private void clearInput() {
        doctorIdField.setText("");
        nameField.setText("");
        titleField.setText("");
        feeField.setText("");

        if (genderBox.getItemCount() > 0) {
            genderBox.setSelectedIndex(0);
        }

        if (deptBox.getItemCount() > 0) {
            deptBox.setSelectedIndex(0);
        }

        table.clearSelection();
    }

    /**
     * 科室下拉框使用的内部类
     *
     * 因为 JComboBox 需要显示科室名称，
     * 但插入 doctor 表时需要 dept_id，
     * 所以这里用 DeptItem 同时保存 dept_id 和 dept_name。
     */
    private static class DeptItem {
        private int deptId;
        private String deptName;

        public DeptItem(int deptId, String deptName) {
            this.deptId = deptId;
            this.deptName = deptName;
        }

        public int getDeptId() {
            return deptId;
        }

        public String getDeptName() {
            return deptName;
        }

        /**
         * JComboBox 显示对象时会自动调用 toString()
         * 所以这里返回科室名称，让下拉框显示中文科室名。
         */
        @Override
        public String toString() {
            return deptName;
        }
    }
}