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
import java.sql.SQLException;

/**
 * 缴费管理界面
 *
 * 当前 payment 表字段：
 * payment_id, reg_id, amount, pay_type, pay_status, pay_time
 */
public class PaymentManageFrame extends JFrame {

    // 挂号记录表格
    private JTable registrationTable;

    // 挂号记录表格模型
    private DefaultTableModel registrationTableModel;

    // 缴费记录表格
    private JTable paymentTable;

    // 缴费记录表格模型
    private DefaultTableModel paymentTableModel;

    // 挂号编号输入框
    private JTextField regIdField;

    // 患者姓名输入框
    private JTextField patientNameField;

    // 科室输入框
    private JTextField deptNameField;

    // 医生姓名输入框
    private JTextField doctorNameField;

    // 缴费金额输入框
    private JTextField amountField;

    // 支付方式下拉框
    private JComboBox<String> payTypeBox;

    public PaymentManageFrame() {
        setTitle("医院门诊挂号系统 - 缴费管理");
        setSize(1050, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initView();

        loadRegistrationData();
        loadPaymentData();

        setVisible(true);
    }

    /**
     * 初始化界面
     */
    private void initView() {
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("缴费管理", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        // =========================
        // 中间区域：挂号记录表 + 缴费记录表
        // =========================
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));

        String[] registrationColumns = {
                "挂号编号",
                "患者姓名",
                "科室",
                "医生",
                "出诊日期",
                "时间段",
                "挂号费",
                "挂号状态"
        };

        registrationTableModel = new DefaultTableModel(registrationColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        registrationTable = new JTable(registrationTableModel);
        registrationTable.setRowHeight(26);

        JScrollPane registrationScrollPane = new JScrollPane(registrationTable);
        registrationScrollPane.setBorder(BorderFactory.createTitledBorder("挂号记录"));

        centerPanel.add(registrationScrollPane);

        String[] paymentColumns = {
                "缴费编号",
                "挂号编号",
                "患者姓名",
                "缴费金额",
                "支付方式",
                "缴费状态",
                "缴费时间"
        };

        paymentTableModel = new DefaultTableModel(paymentColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        paymentTable = new JTable(paymentTableModel);
        paymentTable.setRowHeight(26);

        JScrollPane paymentScrollPane = new JScrollPane(paymentTable);
        paymentScrollPane.setBorder(BorderFactory.createTitledBorder("缴费记录"));

        centerPanel.add(paymentScrollPane);

        add(centerPanel, BorderLayout.CENTER);

        // =========================
        // 底部输入区和按钮区
        // =========================
        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(2, 6, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        regIdField = new JTextField();
        patientNameField = new JTextField();
        deptNameField = new JTextField();
        doctorNameField = new JTextField();
        amountField = new JTextField();
        payTypeBox = new JComboBox<>();

        payTypeBox.addItem("现金");
        payTypeBox.addItem("微信");
        payTypeBox.addItem("支付宝");
        payTypeBox.addItem("银行卡");

        // 这些字段从表格回显，不让用户手动修改
        regIdField.setEditable(false);
        patientNameField.setEditable(false);
        deptNameField.setEditable(false);
        doctorNameField.setEditable(false);

        inputPanel.add(new JLabel("挂号编号"));
        inputPanel.add(new JLabel("患者姓名"));
        inputPanel.add(new JLabel("科室"));
        inputPanel.add(new JLabel("医生"));
        inputPanel.add(new JLabel("缴费金额"));
        inputPanel.add(new JLabel("支付方式"));

        inputPanel.add(regIdField);
        inputPanel.add(patientNameField);
        inputPanel.add(deptNameField);
        inputPanel.add(doctorNameField);
        inputPanel.add(amountField);
        inputPanel.add(payTypeBox);

        JPanel buttonPanel = new JPanel();

        JButton payButton = new JButton("缴费");
        JButton refreshButton = new JButton("刷新");
        JButton clearButton = new JButton("清空");
        JButton closeButton = new JButton("关闭");

        buttonPanel.add(payButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(closeButton);

        bottomPanel.add(inputPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        // 点击挂号记录表格某一行，把信息回显到输入框
        registrationTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillInputFromSelectedRegistration();
            }
        });

        payButton.addActionListener(e -> pay());

        refreshButton.addActionListener(e -> {
            loadRegistrationData();
            loadPaymentData();
        });

        clearButton.addActionListener(e -> clearInput());

        closeButton.addActionListener(e -> dispose());
    }

    /**
     * 加载挂号记录
     */
    private void loadRegistrationData() {
        registrationTableModel.setRowCount(0);

        String sql = """
                SELECT
                    r.reg_id,
                    p.name AS patient_name,
                    dept.dept_name,
                    d.name AS doctor_name,
                    s.work_date,
                    s.time_period,
                    r.fee,
                    r.status
                FROM registration r
                JOIN patient p ON r.patient_id = p.patient_id
                JOIN schedule s ON r.schedule_id = s.schedule_id
                JOIN doctor d ON s.doctor_id = d.doctor_id
                JOIN department dept ON d.dept_id = dept.dept_id
                ORDER BY r.reg_id DESC
                """;

        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Object[] row = {
                        rs.getInt("reg_id"),
                        rs.getString("patient_name"),
                        rs.getString("dept_name"),
                        rs.getString("doctor_name"),
                        rs.getDate("work_date"),
                        rs.getString("time_period"),
                        rs.getBigDecimal("fee"),
                        rs.getString("status")
                };

                registrationTableModel.addRow(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载挂号记录失败：" + e.getMessage());
        }
    }

    /**
     * 加载缴费记录
     *
     * payment 表字段：
     * payment_id, reg_id, amount, pay_type, pay_status, pay_time
     */
    private void loadPaymentData() {
        paymentTableModel.setRowCount(0);

        String sql = """
                SELECT
                    pay.payment_id,
                    pay.reg_id,
                    p.name AS patient_name,
                    pay.amount,
                    pay.pay_type,
                    pay.pay_status,
                    pay.pay_time
                FROM payment pay
                JOIN registration r ON pay.reg_id = r.reg_id
                JOIN patient p ON r.patient_id = p.patient_id
                ORDER BY pay.payment_id DESC
                """;

        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Object[] row = {
                        rs.getInt("payment_id"),
                        rs.getInt("reg_id"),
                        rs.getString("patient_name"),
                        rs.getBigDecimal("amount"),
                        rs.getString("pay_type"),
                        rs.getString("pay_status"),
                        rs.getTimestamp("pay_time")
                };

                paymentTableModel.addRow(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载缴费记录失败：" + e.getMessage());
        }
    }

    /**
     * 点击挂号记录表格后，把选中的挂号信息填入输入框
     */
    private void fillInputFromSelectedRegistration() {
        int selectedRow = registrationTable.getSelectedRow();

        if (selectedRow == -1) {
            return;
        }

        Object regId = registrationTableModel.getValueAt(selectedRow, 0);
        Object patientName = registrationTableModel.getValueAt(selectedRow, 1);
        Object deptName = registrationTableModel.getValueAt(selectedRow, 2);
        Object doctorName = registrationTableModel.getValueAt(selectedRow, 3);
        Object fee = registrationTableModel.getValueAt(selectedRow, 6);

        regIdField.setText(regId == null ? "" : regId.toString());
        patientNameField.setText(patientName == null ? "" : patientName.toString());
        deptNameField.setText(deptName == null ? "" : deptName.toString());
        doctorNameField.setText(doctorName == null ? "" : doctorName.toString());
        amountField.setText(fee == null ? "" : fee.toString());
    }

    /**
     * 缴费方法
     *
     * 执行两件事：
     * 1. payment 表新增缴费记录
     * 2. registration 表状态改为“已缴费”
     *
     * 这两步放在同一个事务中。
     */
    private void pay() {
        String regIdText = regIdField.getText().trim();
        String amountText = amountField.getText().trim();
        String payType = (String) payTypeBox.getSelectedItem();

        if (regIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先选择一条挂号记录");
            return;
        }

        if (amountText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "缴费金额不能为空");
            return;
        }

        int regId;
        BigDecimal amount;

        try {
            regId = Integer.parseInt(regIdText);
            amount = new BigDecimal(amountText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "挂号编号或缴费金额格式错误");
            return;
        }

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            JOptionPane.showMessageDialog(this, "缴费金额不能小于 0");
            return;
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                "确定完成缴费吗？",
                "确认缴费",
                JOptionPane.YES_NO_OPTION
        );

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        Connection conn = null;

        try {
            conn = DBUtil.getConnection();

            // 开启事务
            conn.setAutoCommit(false);

            // 1. 检查挂号记录是否存在
            String checkRegistrationSql = """
                    SELECT COUNT(*)
                    FROM registration
                    WHERE reg_id = ?
                    """;

            try (PreparedStatement checkRegPs = conn.prepareStatement(checkRegistrationSql)) {
                checkRegPs.setInt(1, regId);

                try (ResultSet rs = checkRegPs.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt(1);

                        if (count == 0) {
                            conn.rollback();
                            JOptionPane.showMessageDialog(this, "缴费失败：挂号记录不存在");
                            return;
                        }
                    }
                }
            }

            // 2. 检查是否已经缴费
            String checkPaymentSql = """
                    SELECT COUNT(*)
                    FROM payment
                    WHERE reg_id = ?
                    """;

            try (PreparedStatement checkPayPs = conn.prepareStatement(checkPaymentSql)) {
                checkPayPs.setInt(1, regId);

                try (ResultSet rs = checkPayPs.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt(1);

                        if (count > 0) {
                            conn.rollback();
                            JOptionPane.showMessageDialog(this, "该挂号记录已经缴费，不能重复缴费");
                            return;
                        }
                    }
                }
            }

            // 3. 插入缴费记录
            // NOW() 表示使用数据库当前时间作为 pay_time
            String insertPaymentSql = """
                    INSERT INTO payment(reg_id, amount, pay_type, pay_status, pay_time)
                    VALUES (?, ?, ?, '已缴费', NOW())
                    """;

            int insertRows = JdbcUtil.update(conn, insertPaymentSql, regId, amount, payType);

            if (insertRows == 0) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "缴费失败：缴费记录保存失败");
                return;
            }

            // 4. 修改挂号记录状态
            String updateRegistrationSql = """
                    UPDATE registration
                    SET status = '已缴费'
                    WHERE reg_id = ?
                    """;

            int updateRows = JdbcUtil.update(conn, updateRegistrationSql, regId);

            if (updateRows == 0) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "缴费失败：挂号状态更新失败");
                return;
            }

            // 5. 提交事务
            conn.commit();

            JOptionPane.showMessageDialog(this, "缴费成功");

            loadRegistrationData();
            loadPaymentData();
            clearInput();

        } catch (Exception e) {
            e.printStackTrace();

            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            JOptionPane.showMessageDialog(this, "缴费失败：" + e.getMessage());

        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 清空输入框和表格选择状态
     */
    private void clearInput() {
        regIdField.setText("");
        patientNameField.setText("");
        deptNameField.setText("");
        doctorNameField.setText("");
        amountField.setText("");

        if (payTypeBox.getItemCount() > 0) {
            payTypeBox.setSelectedIndex(0);
        }

        registrationTable.clearSelection();
        paymentTable.clearSelection();
    }
}