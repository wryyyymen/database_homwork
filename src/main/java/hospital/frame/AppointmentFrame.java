package hospital.frame;

import hospital.util.DBUtil;
import hospital.util.JdbcUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 预约挂号界面
 *
 * 功能：
 * 1. 输入患者编号并查询患者信息
 * 2. 查询可预约排班
 * 3. 选择排班
 * 4. 点击预约挂号
 * 5. 判断患者是否存在
 * 6. 判断是否重复预约
 * 7. 扣减排班剩余号源
 * 8. 插入挂号记录
 */
public class AppointmentFrame extends JFrame {

    // JTable 是 Java Swing 里的表格组件，用来显示排班列表
    private JTable table;

    // DefaultTableModel 是表格的数据模型
    // 负责保存表头、行数据、添加行、清空数据等
    private DefaultTableModel tableModel;

    // 患者编号输入框
    private JTextField patientIdField;

    // 显示患者信息的标签
    private JLabel patientInfoLabel;

    public AppointmentFrame() {
        setTitle("医院门诊挂号系统 - 预约挂号");
        setSize(950, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 初始化界面
        initView();

        // 加载可预约排班
        loadScheduleData();

        setVisible(true);
    }

    /**
     * 初始化界面
     */
    private void initView() {
        setLayout(new BorderLayout());

        // =========================
        // 1. 顶部区域：标题 + 患者查询
        // =========================

        JPanel topPanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("预约挂号", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        topPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel patientPanel = new JPanel();

        patientPanel.add(new JLabel("患者编号："));

        patientIdField = new JTextField(10);
        patientPanel.add(patientIdField);

        JButton searchPatientButton = new JButton("查询患者");
        patientPanel.add(searchPatientButton);

        patientInfoLabel = new JLabel("未选择患者");
        patientPanel.add(patientInfoLabel);

        topPanel.add(patientPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // 点击“查询患者”按钮
        searchPatientButton.addActionListener(e -> searchPatient());

        // =========================
        // 2. 中间区域：排班表格
        // =========================

        String[] columns = {
                "排班编号",
                "科室",
                "医生",
                "职称",
                "出诊日期",
                "时间段",
                "挂号费",
                "总号源",
                "剩余号源"
        };

        // 创建表格模型，并禁止用户直接编辑表格内容
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(28);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // =========================
        // 3. 底部区域：按钮
        // =========================

        JPanel bottomPanel = new JPanel();

        JButton refreshButton = new JButton("刷新排班");
        JButton appointmentButton = new JButton("预约挂号");
        JButton clearButton = new JButton("清空");
        JButton closeButton = new JButton("关闭");

        bottomPanel.add(refreshButton);
        bottomPanel.add(appointmentButton);
        bottomPanel.add(clearButton);
        bottomPanel.add(closeButton);

        add(bottomPanel, BorderLayout.SOUTH);

        // 点击刷新按钮，重新加载排班
        refreshButton.addActionListener(e -> loadScheduleData());

        // 点击预约按钮，执行预约
        appointmentButton.addActionListener(e -> makeAppointment());

        // 点击清空按钮
        clearButton.addActionListener(e -> clearInput());

        // 点击关闭按钮
        closeButton.addActionListener(e -> dispose());
    }

    /**
     * 查询患者信息
     */
    private void searchPatient() {
        String patientIdText = patientIdField.getText().trim();

        if (patientIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入患者编号");
            return;
        }

        int patientId;

        try {
            patientId = Integer.parseInt(patientIdText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "患者编号必须是数字");
            return;
        }

        String sql = """
                SELECT patient_id, name, gender, phone
                FROM patient
                WHERE patient_id = ?
                """;

        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, patientId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    String gender = rs.getString("gender");
                    String phone = rs.getString("phone");

                    patientInfoLabel.setText(
                            "患者：" + name + "，性别：" + gender + "，电话：" + phone
                    );
                } else {
                    patientInfoLabel.setText("未找到该患者");
                    JOptionPane.showMessageDialog(this, "患者不存在，请先添加患者信息");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "查询患者失败：" + e.getMessage());
        }
    }

    /**
     * 查询可预约排班信息，并显示到 JTable
     */
    private void loadScheduleData() {
        // 清空表格原有数据
        tableModel.setRowCount(0);

        String sql = """
                SELECT
                    s.schedule_id,
                    dept.dept_name,
                    d.name AS doctor_name,
                    d.title,
                    s.work_date,
                    s.time_period,
                    d.fee,
                    s.total_num,
                    s.remain_num
                FROM schedule s
                JOIN doctor d ON s.doctor_id = d.doctor_id
                JOIN department dept ON d.dept_id = dept.dept_id
                WHERE s.remain_num > 0
                ORDER BY s.work_date, s.time_period
                """;

        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Object[] row = {
                        rs.getInt("schedule_id"),
                        rs.getString("dept_name"),
                        rs.getString("doctor_name"),
                        rs.getString("title"),
                        rs.getDate("work_date"),
                        rs.getString("time_period"),
                        rs.getBigDecimal("fee"),
                        rs.getInt("total_num"),
                        rs.getInt("remain_num")
                };

                tableModel.addRow(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载排班数据失败：" + e.getMessage());
        }
    }

    /**
     * 预约挂号
     */
    private void makeAppointment() {
        // =========================
        // 1. 获取患者编号
        // =========================

        String patientIdText = patientIdField.getText().trim();

        if (patientIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先输入患者编号");
            return;
        }

        int patientId;

        try {
            patientId = Integer.parseInt(patientIdText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "患者编号必须是数字");
            return;
        }

        // =========================
        // 2. 获取选中的排班
        // =========================

        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择一条排班");
            return;
        }

        int scheduleId = (int) tableModel.getValueAt(selectedRow, 0);
        Object fee = tableModel.getValueAt(selectedRow, 6);

        // =========================
        // 3. 弹出确认框
        // =========================

        int result = JOptionPane.showConfirmDialog(
                this,
                "确定为该患者预约当前排班吗？",
                "确认预约",
                JOptionPane.YES_NO_OPTION
        );

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        Connection conn = null;

        try {
            // 获取数据库连接
            conn = DBUtil.getConnection();

            // 开启事务
            // 因为预约要同时执行“扣减号源”和“插入挂号记录”
            conn.setAutoCommit(false);

            // =========================
            // 4. 检查患者是否存在
            // =========================

            String checkPatientSql = """
                    SELECT COUNT(*)
                    FROM patient
                    WHERE patient_id = ?
                    """;

            try (PreparedStatement checkPatientPs = conn.prepareStatement(checkPatientSql)) {
                checkPatientPs.setInt(1, patientId);

                try (ResultSet rs = checkPatientPs.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt(1);

                        if (count == 0) {
                            conn.rollback();
                            JOptionPane.showMessageDialog(this, "预约失败：患者不存在，请先添加患者信息");
                            return;
                        }
                    }
                }
            }

            // =========================
            // 5. 检查是否重复预约
            // =========================

            String checkRegSql = """
                    SELECT COUNT(*)
                    FROM registration
                    WHERE patient_id = ?
                      AND schedule_id = ?
                    """;

            try (PreparedStatement checkRegPs = conn.prepareStatement(checkRegSql)) {
                checkRegPs.setInt(1, patientId);
                checkRegPs.setInt(2, scheduleId);

                try (ResultSet rs = checkRegPs.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt(1);

                        if (count > 0) {
                            conn.rollback();
                            JOptionPane.showMessageDialog(this, "预约失败：该患者已经预约过这个排班");
                            return;
                        }
                    }
                }
            }

            // =========================
            // 6. 扣减剩余号源
            // =========================

            String updateScheduleSql = """
                    UPDATE schedule
                    SET remain_num = remain_num - 1
                    WHERE schedule_id = ?
                      AND remain_num > 0
                    """;

            int updateRows = JdbcUtil.update(conn, updateScheduleSql, scheduleId);

            if (updateRows == 0) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "预约失败：该排班已经没有剩余号源");
                return;
            }

            // =========================
            // 7. 插入挂号记录
            // =========================

            String insertRegSql = """
                    INSERT INTO registration(patient_id, schedule_id, status, fee)
                    VALUES (?, ?, '已挂号', ?)
                    """;

            int insertRows = JdbcUtil.update(conn, insertRegSql, patientId, scheduleId, fee);

            if (insertRows == 0) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "预约失败：挂号记录保存失败");
                return;
            }

            // =========================
            // 8. 提交事务
            // =========================

            conn.commit();

            JOptionPane.showMessageDialog(this, "预约成功");

            // 自动刷新排班表，更新剩余号源
            loadScheduleData();

        } catch (Exception e) {
            e.printStackTrace();

            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            JOptionPane.showMessageDialog(this, "预约失败：" + e.getMessage());

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
     * 清空输入框和选择状态
     */
    private void clearInput() {
        patientIdField.setText("");
        patientInfoLabel.setText("未选择患者");
        table.clearSelection();
    }
}