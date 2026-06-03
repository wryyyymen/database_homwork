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
 * 就诊记录管理界面
 *
 * medical_record 表字段：
 * record_id, reg_id, symptom, diagnosis, prescription, advice, visit_time
 *
 * registration 表字段：
 * reg_id, patient_id, schedule_id, reg_time, status, fee
 */
public class MedicalRecordFrame extends JFrame {

    // 就诊记录表格
    private JTable recordTable;

    // 就诊记录表格模型
    private DefaultTableModel recordTableModel;

    // 挂号记录表格
    private JTable registrationTable;

    // 挂号记录表格模型
    private DefaultTableModel registrationTableModel;

    // 记录编号输入框
    private JTextField recordIdField;

    // 挂号编号输入框
    private JTextField regIdField;

    // 患者姓名输入框
    private JTextField patientNameField;

    // 医生姓名输入框
    private JTextField doctorNameField;

    // 科室输入框
    private JTextField deptNameField;

    // 症状输入区
    private JTextArea symptomArea;

    // 诊断输入区
    private JTextArea diagnosisArea;

    // 处方输入区
    private JTextArea prescriptionArea;

    // 医嘱输入区
    private JTextArea adviceArea;

    public MedicalRecordFrame() {
        setTitle("医院门诊挂号系统 - 就诊记录管理");
        setSize(1150, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initView();

        loadRegistrationData();
        loadMedicalRecordData();

        setVisible(true);
    }

    /**
     * 初始化界面
     */
    private void initView() {
        setLayout(new BorderLayout());

        // =========================
        // 1. 顶部标题
        // =========================
        JLabel titleLabel = new JLabel("就诊记录管理", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        // =========================
        // 2. 中间区域：上面挂号记录，下面就诊记录
        // =========================
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));

        // 挂号记录表头
        String[] registrationColumns = {
                "挂号编号",
                "患者姓名",
                "科室",
                "医生",
                "出诊日期",
                "时间段",
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

        // 就诊记录表头
        String[] recordColumns = {
                "记录编号",
                "挂号编号",
                "患者姓名",
                "科室",
                "医生",
                "症状",
                "诊断",
                "处方",
                "医嘱",
                "就诊时间"
        };

        recordTableModel = new DefaultTableModel(recordColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        recordTable = new JTable(recordTableModel);
        recordTable.setRowHeight(26);

        JScrollPane recordScrollPane = new JScrollPane(recordTable);
        recordScrollPane.setBorder(BorderFactory.createTitledBorder("就诊记录"));

        centerPanel.add(recordScrollPane);

        add(centerPanel, BorderLayout.CENTER);

        // =========================
        // 3. 底部输入区
        // =========================
        JPanel bottomPanel = new JPanel(new BorderLayout());
        // 增加底部输入区整体高度
        bottomPanel.setPreferredSize(new Dimension(1150, 260));
        // 第一部分：基础信息
        JPanel basicPanel = new JPanel(new GridLayout(2, 5, 10, 10));
        basicPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));

        recordIdField = new JTextField();
        regIdField = new JTextField();
        patientNameField = new JTextField();
        deptNameField = new JTextField();
        doctorNameField = new JTextField();

        // 这些字段由表格选择后自动回显，不允许手动输入
        recordIdField.setEditable(false);
        regIdField.setEditable(false);
        patientNameField.setEditable(false);
        deptNameField.setEditable(false);
        doctorNameField.setEditable(false);

        basicPanel.add(new JLabel("记录编号"));
        basicPanel.add(new JLabel("挂号编号"));
        basicPanel.add(new JLabel("患者姓名"));
        basicPanel.add(new JLabel("科室"));
        basicPanel.add(new JLabel("医生"));

        basicPanel.add(recordIdField);
        basicPanel.add(regIdField);
        basicPanel.add(patientNameField);
        basicPanel.add(deptNameField);
        basicPanel.add(doctorNameField);

        // 第二部分：文本输入
        JPanel textPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        textPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 10, 20));

        symptomArea = new JTextArea(5, 20);
        diagnosisArea = new JTextArea(5, 20);
        prescriptionArea = new JTextArea(5, 20);
        adviceArea = new JTextArea(5, 20);

        symptomArea.setLineWrap(true);
        diagnosisArea.setLineWrap(true);
        prescriptionArea.setLineWrap(true);
        adviceArea.setLineWrap(true);

        textPanel.add(new JLabel("症状"));
        textPanel.add(new JLabel("诊断"));
        textPanel.add(new JLabel("处方"));
        textPanel.add(new JLabel("医嘱"));

        textPanel.add(new JScrollPane(symptomArea));
        textPanel.add(new JScrollPane(diagnosisArea));
        textPanel.add(new JScrollPane(prescriptionArea));
        textPanel.add(new JScrollPane(adviceArea));

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

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(basicPanel, BorderLayout.NORTH);
        inputPanel.add(textPanel, BorderLayout.CENTER);

        bottomPanel.add(inputPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        // =========================
        // 4. 绑定事件
        // =========================

        // 点击挂号记录后，把挂号信息回显到输入框
        registrationTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillInputFromSelectedRegistration();
            }
        });

        // 点击就诊记录后，把就诊记录回显到输入框
        recordTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillInputFromSelectedRecord();
            }
        });

        addButton.addActionListener(e -> addMedicalRecord());

        updateButton.addActionListener(e -> updateMedicalRecord());

        deleteButton.addActionListener(e -> deleteMedicalRecord());

        refreshButton.addActionListener(e -> {
            loadRegistrationData();
            loadMedicalRecordData();
        });

        clearButton.addActionListener(e -> clearInput());

        closeButton.addActionListener(e -> dispose());
    }

    /**
     * 加载挂号记录
     * <p>
     * 这里建议只显示“已缴费”的挂号记录，
     * 因为一般缴费后才进入就诊。
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
                    r.status
                FROM registration r
                JOIN patient p ON r.patient_id = p.patient_id
                JOIN schedule s ON r.schedule_id = s.schedule_id
                JOIN doctor d ON s.doctor_id = d.doctor_id
                JOIN department dept ON d.dept_id = dept.dept_id
                WHERE r.status = '已缴费'
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
     * 加载就诊记录
     */
    private void loadMedicalRecordData() {
        recordTableModel.setRowCount(0);

        String sql = """
                SELECT
                    mr.record_id,
                    mr.reg_id,
                    p.name AS patient_name,
                    dept.dept_name,
                    d.name AS doctor_name,
                    mr.symptom,
                    mr.diagnosis,
                    mr.prescription,
                    mr.advice,
                    mr.visit_time
                FROM medical_record mr
                JOIN registration r ON mr.reg_id = r.reg_id
                JOIN patient p ON r.patient_id = p.patient_id
                JOIN schedule s ON r.schedule_id = s.schedule_id
                JOIN doctor d ON s.doctor_id = d.doctor_id
                JOIN department dept ON d.dept_id = dept.dept_id
                ORDER BY mr.record_id DESC
                """;

        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Object[] row = {
                        rs.getInt("record_id"),
                        rs.getInt("reg_id"),
                        rs.getString("patient_name"),
                        rs.getString("dept_name"),
                        rs.getString("doctor_name"),
                        rs.getString("symptom"),
                        rs.getString("diagnosis"),
                        rs.getString("prescription"),
                        rs.getString("advice"),
                        rs.getTimestamp("visit_time")
                };

                recordTableModel.addRow(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载就诊记录失败：" + e.getMessage());
        }
    }

    /**
     * 点击挂号记录后，回显挂号信息
     */
    private void fillInputFromSelectedRegistration() {
        // 获取挂号记录表格中当前选中的行号
        int selectedRow = registrationTable.getSelectedRow();

        // 如果 selectedRow == -1，说明用户没有选中任何一行
        if (selectedRow == -1) {
            return;
        }

        // 从挂号记录表格模型中取出当前选中行的数据
        Object regId = registrationTableModel.getValueAt(selectedRow, 0);
        Object patientName = registrationTableModel.getValueAt(selectedRow, 1);
        Object deptName = registrationTableModel.getValueAt(selectedRow, 2);
        Object doctorName = registrationTableModel.getValueAt(selectedRow, 3);

        // 选择挂号记录时，说明准备新增一条就诊记录
        // 所以记录编号 record_id 应该清空
        recordIdField.setText("");

        // 把挂号编号、患者姓名、科室、医生回显到输入框
        regIdField.setText(regId == null ? "" : regId.toString());
        patientNameField.setText(patientName == null ? "" : patientName.toString());
        deptNameField.setText(deptName == null ? "" : deptName.toString());
        doctorNameField.setText(doctorName == null ? "" : doctorName.toString());

        // 清空症状、诊断、处方、医嘱输入框
        // 因为选择挂号记录后，一般是准备新增就诊记录
        symptomArea.setText("");
        diagnosisArea.setText("");
        prescriptionArea.setText("");
        adviceArea.setText("");

        // 清除就诊记录表格的选中状态
        // 避免同时选中“挂号记录”和“已有就诊记录”造成混淆
        recordTable.clearSelection();
    }

    /**
     * 点击就诊记录后，回显病历信息。
     */
    private void fillInputFromSelectedRecord() {
        int selectedRow = recordTable.getSelectedRow();

        if (selectedRow == -1) {
            return;
        }

        Object recordId = recordTableModel.getValueAt(selectedRow, 0);
        Object regId = recordTableModel.getValueAt(selectedRow, 1);
        Object patientName = recordTableModel.getValueAt(selectedRow, 2);
        Object deptName = recordTableModel.getValueAt(selectedRow, 3);
        Object doctorName = recordTableModel.getValueAt(selectedRow, 4);
        Object symptom = recordTableModel.getValueAt(selectedRow, 5);
        Object diagnosis = recordTableModel.getValueAt(selectedRow, 6);
        Object prescription = recordTableModel.getValueAt(selectedRow, 7);
        Object advice = recordTableModel.getValueAt(selectedRow, 8);

        recordIdField.setText(recordId == null ? "" : recordId.toString());
        regIdField.setText(regId == null ? "" : regId.toString());
        patientNameField.setText(patientName == null ? "" : patientName.toString());
        deptNameField.setText(deptName == null ? "" : deptName.toString());
        doctorNameField.setText(doctorName == null ? "" : doctorName.toString());
        symptomArea.setText(symptom == null ? "" : symptom.toString());
        diagnosisArea.setText(diagnosis == null ? "" : diagnosis.toString());
        prescriptionArea.setText(prescription == null ? "" : prescription.toString());
        adviceArea.setText(advice == null ? "" : advice.toString());

        registrationTable.clearSelection();
    }

    /**
     * 新增就诊记录，并将对应挂号状态改为“已就诊”。
     */
    private void addMedicalRecord() {
        String recordIdText = recordIdField.getText().trim();
        String regIdText = regIdField.getText().trim();
        String symptom = symptomArea.getText().trim();
        String diagnosis = diagnosisArea.getText().trim();
        String prescription = prescriptionArea.getText().trim();
        String advice = adviceArea.getText().trim();

        if (!recordIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "当前已选中就诊记录，新增前请先选择一条已缴费挂号记录或清空输入框");
            return;
        }

        if (regIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先选择一条已缴费挂号记录");
            return;
        }

        if (symptom.isEmpty()) {
            JOptionPane.showMessageDialog(this, "症状不能为空");
            return;
        }

        if (diagnosis.isEmpty()) {
            JOptionPane.showMessageDialog(this, "诊断不能为空");
            return;
        }

        int regId;

        try {
            regId = Integer.parseInt(regIdText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "挂号编号格式错误");
            return;
        }

        Connection conn = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            String checkRegistrationSql = """
                    SELECT status
                    FROM registration
                    WHERE reg_id = ?
                    """;

            try (PreparedStatement ps = conn.prepareStatement(checkRegistrationSql)) {
                ps.setInt(1, regId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        JOptionPane.showMessageDialog(this, "新增失败：挂号记录不存在");
                        return;
                    }

                    String status = rs.getString("status");

                    if (!"已缴费".equals(status)) {
                        conn.rollback();
                        JOptionPane.showMessageDialog(this, "新增失败：只有已缴费的挂号记录才能就诊，当前状态为：" + status);
                        return;
                    }
                }
            }

            String checkRecordSql = """
                    SELECT COUNT(*)
                    FROM medical_record
                    WHERE reg_id = ?
                    """;

            try (PreparedStatement ps = conn.prepareStatement(checkRecordSql)) {
                ps.setInt(1, regId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        conn.rollback();
                        JOptionPane.showMessageDialog(this, "新增失败：该挂号记录已有就诊记录");
                        return;
                    }
                }
            }

            String insertSql = """
                    INSERT INTO medical_record(reg_id, symptom, diagnosis, prescription, advice, visit_time)
                    VALUES (?, ?, ?, ?, ?, NOW())
                    """;

            int insertRows = JdbcUtil.update(conn, insertSql, regId, symptom, diagnosis, prescription, advice);

            if (insertRows == 0) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "新增就诊记录失败");
                return;
            }

            String updateRegistrationSql = """
                    UPDATE registration
                    SET status = '已就诊'
                    WHERE reg_id = ?
                    """;

            int updateRows = JdbcUtil.update(conn, updateRegistrationSql, regId);

            if (updateRows == 0) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "新增失败：挂号状态更新失败");
                return;
            }

            conn.commit();

            JOptionPane.showMessageDialog(this, "新增就诊记录成功");
            loadRegistrationData();
            loadMedicalRecordData();
            clearInput();

        } catch (Exception e) {
            e.printStackTrace();
            rollbackQuietly(conn);
            JOptionPane.showMessageDialog(this, "新增就诊记录失败：" + e.getMessage());

        } finally {
            closeConnection(conn);
        }
    }

    /**
     * 修改就诊记录。
     */
    private void updateMedicalRecord() {
        String recordIdText = recordIdField.getText().trim();
        String symptom = symptomArea.getText().trim();
        String diagnosis = diagnosisArea.getText().trim();
        String prescription = prescriptionArea.getText().trim();
        String advice = adviceArea.getText().trim();

        if (recordIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先选择要修改的就诊记录");
            return;
        }

        if (symptom.isEmpty()) {
            JOptionPane.showMessageDialog(this, "症状不能为空");
            return;
        }

        if (diagnosis.isEmpty()) {
            JOptionPane.showMessageDialog(this, "诊断不能为空");
            return;
        }

        int recordId;

        try {
            recordId = Integer.parseInt(recordIdText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "记录编号格式错误");
            return;
        }

        String sql = """
                UPDATE medical_record
                SET symptom = ?,
                    diagnosis = ?,
                    prescription = ?,
                    advice = ?
                WHERE record_id = ?
                """;

        int rows = JdbcUtil.update(sql, symptom, diagnosis, prescription, advice, recordId);

        if (rows > 0) {
            JOptionPane.showMessageDialog(this, "修改就诊记录成功");
            loadMedicalRecordData();
            clearInput();
        } else {
            JOptionPane.showMessageDialog(this, "修改就诊记录失败");
        }
    }

    /**
     * 删除就诊记录，并将对应挂号状态恢复为“已缴费”。
     */
    private void deleteMedicalRecord() {
        String recordIdText = recordIdField.getText().trim();
        String regIdText = regIdField.getText().trim();

        if (recordIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的就诊记录");
            return;
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                "确定要删除该就诊记录吗？",
                "确认删除",
                JOptionPane.YES_NO_OPTION
        );

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        int recordId;
        int regId;

        try {
            recordId = Integer.parseInt(recordIdText);
            regId = Integer.parseInt(regIdText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "记录编号或挂号编号格式错误");
            return;
        }

        Connection conn = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            String deleteSql = """
                    DELETE FROM medical_record
                    WHERE record_id = ?
                    """;

            int deleteRows = JdbcUtil.update(conn, deleteSql, recordId);

            if (deleteRows == 0) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "删除就诊记录失败");
                return;
            }

            String updateRegistrationSql = """
                    UPDATE registration
                    SET status = '已缴费'
                    WHERE reg_id = ?
                    """;

            int updateRows = JdbcUtil.update(conn, updateRegistrationSql, regId);

            if (updateRows == 0) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "删除失败：挂号状态恢复失败");
                return;
            }

            conn.commit();

            JOptionPane.showMessageDialog(this, "删除就诊记录成功");
            loadRegistrationData();
            loadMedicalRecordData();
            clearInput();

        } catch (Exception e) {
            e.printStackTrace();
            rollbackQuietly(conn);
            JOptionPane.showMessageDialog(this, "删除就诊记录失败：" + e.getMessage());

        } finally {
            closeConnection(conn);
        }
    }

    /**
     * 清空输入框和表格选择状态。
     */
    private void clearInput() {
        recordIdField.setText("");
        regIdField.setText("");
        patientNameField.setText("");
        deptNameField.setText("");
        doctorNameField.setText("");
        symptomArea.setText("");
        diagnosisArea.setText("");
        prescriptionArea.setText("");
        adviceArea.setText("");

        registrationTable.clearSelection();
        recordTable.clearSelection();
    }

    private void rollbackQuietly(Connection conn) {
        try {
            if (conn != null) {
                conn.rollback();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void closeConnection(Connection conn) {
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
