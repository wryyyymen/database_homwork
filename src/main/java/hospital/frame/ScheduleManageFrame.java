package hospital.frame;

import hospital.util.DBUtil;
import hospital.util.JdbcUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;

/**
 * 排班管理界面
 *
 * 功能：
 * 1. 查询医生排班
 * 2. 添加排班
 * 3. 修改排班
 * 4. 删除排班
 * 5. 点击表格行后，将数据回显到输入框
 */
public class ScheduleManageFrame extends JFrame {

    // JTable 表格组件，用来显示排班列表
    private JTable table;

    // 表格数据模型，用来管理表头和表格数据
    private DefaultTableModel tableModel;

    // 排班编号输入框，数据库自动生成，不允许手动修改
    private JTextField scheduleIdField;

    // 医生下拉框
    private JComboBox<DoctorItem> doctorBox;

    // 出诊日期输入框，格式：yyyy-MM-dd
    private JTextField workDateField;

    // 时间段下拉框：上午 / 下午 / 晚上
    private JComboBox<String> timePeriodBox;

    // 总号源输入框
    private JTextField totalNumField;

    // 剩余号源输入框
    private JTextField remainNumField;

    public ScheduleManageFrame() {
        // 设置窗口标题
        setTitle("医院门诊挂号系统 - 排班管理");

        // 设置窗口大小
        setSize(1000, 560);

        // 窗口居中显示
        setLocationRelativeTo(null);

        // 关闭当前窗口时，不退出整个程序
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 初始化界面
        initView();

        // 加载医生下拉框数据
        loadDoctorData();

        // 加载排班表格数据
        loadScheduleData();

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
        JLabel titleLabel = new JLabel("排班管理", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        // =========================
        // 2. 中间表格
        // =========================
        String[] columns = {
                "排班编号",
                "医生编号",
                "医生姓名",
                "科室",
                "职称",
                "出诊日期",
                "时间段",
                "总号源",
                "剩余号源"
        };

        // 创建表格模型
        // 重写 isCellEditable，让表格不能直接编辑
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // 创建 JTable
        table = new JTable(tableModel);

        // 设置表格行高
        table.setRowHeight(28);

        // 把表格放入滚动面板
        JScrollPane scrollPane = new JScrollPane(table);

        // 把滚动面板放到窗口中间
        add(scrollPane, BorderLayout.CENTER);

        // =========================
        // 3. 底部输入区和按钮区
        // =========================
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // 输入区：2 行 6 列
        JPanel inputPanel = new JPanel(new GridLayout(2, 6, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // 创建输入组件
        scheduleIdField = new JTextField();
        doctorBox = new JComboBox<>();
        workDateField = new JTextField();
        timePeriodBox = new JComboBox<>();
        totalNumField = new JTextField();
        remainNumField = new JTextField();

        // 排班编号由数据库自动生成，不允许手动修改
        scheduleIdField.setEditable(false);

        // 时间段下拉框选项
        timePeriodBox.addItem("上午");
        timePeriodBox.addItem("下午");
        timePeriodBox.addItem("晚上");

        // 第一行：标签
        inputPanel.add(new JLabel("排班编号"));
        inputPanel.add(new JLabel("医生"));
        inputPanel.add(new JLabel("出诊日期"));
        inputPanel.add(new JLabel("时间段"));
        inputPanel.add(new JLabel("总号源"));
        inputPanel.add(new JLabel("剩余号源"));

        // 第二行：输入框 / 下拉框
        inputPanel.add(scheduleIdField);
        inputPanel.add(doctorBox);
        inputPanel.add(workDateField);
        inputPanel.add(timePeriodBox);
        inputPanel.add(totalNumField);
        inputPanel.add(remainNumField);

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

        // 点击表格行时，将该行数据填入输入框
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillInputFromSelectedRow();
            }
        });

        // 添加排班
        addButton.addActionListener(e -> addSchedule());

        // 修改排班
        updateButton.addActionListener(e -> updateSchedule());

        // 删除排班
        deleteButton.addActionListener(e -> deleteSchedule());

        // 刷新排班列表
        refreshButton.addActionListener(e -> {
            loadDoctorData();
            loadScheduleData();
        });

        // 清空输入框
        clearButton.addActionListener(e -> clearInput());

        // 关闭窗口
        closeButton.addActionListener(e -> dispose());
    }

    /**
     * 加载医生数据到下拉框
     *
     * 排班表 schedule 中有 doctor_id 外键，
     * 所以添加排班时必须选择一个已经存在的医生。
     */
    private void loadDoctorData() {
        // 清空原来的医生选项
        doctorBox.removeAllItems();

        String sql = """
                SELECT
                    d.doctor_id,
                    d.name,
                    dept.dept_name
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
                int doctorId = rs.getInt("doctor_id");
                String doctorName = rs.getString("name");
                String deptName = rs.getString("dept_name");

                // 把医生编号、医生姓名、科室名封装成 DoctorItem
                doctorBox.addItem(new DoctorItem(doctorId, doctorName, deptName));
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载医生数据失败：" + e.getMessage());
        }
    }

    /**
     * 加载排班数据到表格
     */
    private void loadScheduleData() {
        // 清空表格旧数据
        tableModel.setRowCount(0);

        String sql = """
                SELECT
                    s.schedule_id,
                    d.doctor_id,
                    d.name AS doctor_name,
                    dept.dept_name,
                    d.title,
                    s.work_date,
                    s.time_period,
                    s.total_num,
                    s.remain_num
                FROM schedule s
                JOIN doctor d ON s.doctor_id = d.doctor_id
                JOIN department dept ON d.dept_id = dept.dept_id
                ORDER BY s.work_date, s.time_period, s.schedule_id
                """;

        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Object[] row = {
                        rs.getInt("schedule_id"),
                        rs.getInt("doctor_id"),
                        rs.getString("doctor_name"),
                        rs.getString("dept_name"),
                        rs.getString("title"),
                        rs.getDate("work_date"),
                        rs.getString("time_period"),
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
     * 添加排班
     */
    private void addSchedule() {
        // 获取选中的医生
        DoctorItem doctorItem = (DoctorItem) doctorBox.getSelectedItem();

        // 获取输入框内容
        String workDateText = workDateField.getText().trim();
        String timePeriod = (String) timePeriodBox.getSelectedItem();
        String totalNumText = totalNumField.getText().trim();
        String remainNumText = remainNumField.getText().trim();

        // 校验医生
        if (doctorItem == null) {
            JOptionPane.showMessageDialog(this, "请先添加医生，再添加排班");
            return;
        }

        // 校验日期
        if (workDateText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "出诊日期不能为空，格式例如：2026-06-01");
            return;
        }

        // 校验总号源
        if (totalNumText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "总号源不能为空");
            return;
        }

        // 校验剩余号源
        if (remainNumText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "剩余号源不能为空");
            return;
        }

        Date workDate;
        int totalNum;
        int remainNum;

        try {
            // Date.valueOf 要求格式必须是 yyyy-MM-dd
            workDate = Date.valueOf(workDateText);

            // 把总号源转换为整数
            totalNum = Integer.parseInt(totalNumText);

            // 把剩余号源转换为整数
            remainNum = Integer.parseInt(remainNumText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "日期或号源格式错误。日期格式应为：2026-06-01");
            return;
        }

        // 校验号源不能为负数
        if (totalNum < 0 || remainNum < 0) {
            JOptionPane.showMessageDialog(this, "号源数量不能小于 0");
            return;
        }

        // 剩余号源不能大于总号源
        if (remainNum > totalNum) {
            JOptionPane.showMessageDialog(this, "剩余号源不能大于总号源");
            return;
        }

        String sql = """
                INSERT INTO schedule(doctor_id, work_date, time_period, total_num, remain_num)
                VALUES (?, ?, ?, ?, ?)
                """;

        int rows = JdbcUtil.update(
                sql,
                doctorItem.getDoctorId(),
                workDate,
                timePeriod,
                totalNum,
                remainNum
        );

        if (rows > 0) {
            JOptionPane.showMessageDialog(this, "添加排班成功");
            loadScheduleData();
            clearInput();
        } else {
            JOptionPane.showMessageDialog(this, "添加排班失败，可能是该医生当天该时段已有排班");
        }
    }

    /**
     * 修改排班
     */
    private void updateSchedule() {
        String scheduleIdText = scheduleIdField.getText().trim();

        // 必须先选中一条排班才能修改
        if (scheduleIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先选择要修改的排班");
            return;
        }

        DoctorItem doctorItem = (DoctorItem) doctorBox.getSelectedItem();

        String workDateText = workDateField.getText().trim();
        String timePeriod = (String) timePeriodBox.getSelectedItem();
        String totalNumText = totalNumField.getText().trim();
        String remainNumText = remainNumField.getText().trim();

        if (doctorItem == null) {
            JOptionPane.showMessageDialog(this, "医生不能为空");
            return;
        }

        if (workDateText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "出诊日期不能为空");
            return;
        }

        if (totalNumText.isEmpty() || remainNumText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "总号源和剩余号源不能为空");
            return;
        }

        int scheduleId;
        Date workDate;
        int totalNum;
        int remainNum;

        try {
            scheduleId = Integer.parseInt(scheduleIdText);
            workDate = Date.valueOf(workDateText);
            totalNum = Integer.parseInt(totalNumText);
            remainNum = Integer.parseInt(remainNumText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "排班编号、日期或号源格式错误");
            return;
        }

        if (totalNum < 0 || remainNum < 0) {
            JOptionPane.showMessageDialog(this, "号源数量不能小于 0");
            return;
        }

        if (remainNum > totalNum) {
            JOptionPane.showMessageDialog(this, "剩余号源不能大于总号源");
            return;
        }

        String sql = """
                UPDATE schedule
                SET doctor_id = ?,
                    work_date = ?,
                    time_period = ?,
                    total_num = ?,
                    remain_num = ?
                WHERE schedule_id = ?
                """;

        int rows = JdbcUtil.update(
                sql,
                doctorItem.getDoctorId(),
                workDate,
                timePeriod,
                totalNum,
                remainNum,
                scheduleId
        );

        if (rows > 0) {
            JOptionPane.showMessageDialog(this, "修改排班成功");
            loadScheduleData();
            clearInput();
        } else {
            JOptionPane.showMessageDialog(this, "修改排班失败");
        }
    }

    /**
     * 删除排班
     */
    private void deleteSchedule() {
        String scheduleIdText = scheduleIdField.getText().trim();

        if (scheduleIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的排班");
            return;
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                "确定要删除该排班吗？",
                "确认删除",
                JOptionPane.YES_NO_OPTION
        );

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        int scheduleId;

        try {
            scheduleId = Integer.parseInt(scheduleIdText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "排班编号格式错误");
            return;
        }

        String sql = "DELETE FROM schedule WHERE schedule_id = ?";

        int rows = JdbcUtil.update(sql, scheduleId);

        if (rows > 0) {
            JOptionPane.showMessageDialog(this, "删除排班成功");
            loadScheduleData();
            clearInput();
        } else {
            JOptionPane.showMessageDialog(this, "删除排班失败");
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

        Object scheduleId = tableModel.getValueAt(selectedRow, 0);
        Object doctorId = tableModel.getValueAt(selectedRow, 1);
        Object workDate = tableModel.getValueAt(selectedRow, 5);
        Object timePeriod = tableModel.getValueAt(selectedRow, 6);
        Object totalNum = tableModel.getValueAt(selectedRow, 7);
        Object remainNum = tableModel.getValueAt(selectedRow, 8);

        scheduleIdField.setText(scheduleId == null ? "" : scheduleId.toString());
        workDateField.setText(workDate == null ? "" : workDate.toString());
        totalNumField.setText(totalNum == null ? "" : totalNum.toString());
        remainNumField.setText(remainNum == null ? "" : remainNum.toString());

        if (timePeriod != null) {
            timePeriodBox.setSelectedItem(timePeriod.toString());
        }

        if (doctorId != null) {
            selectDoctorById(Integer.parseInt(doctorId.toString()));
        }
    }

    /**
     * 根据医生编号，在医生下拉框中选中对应医生
     */
    private void selectDoctorById(int doctorId) {
        for (int i = 0; i < doctorBox.getItemCount(); i++) {
            DoctorItem item = doctorBox.getItemAt(i);

            if (item.getDoctorId() == doctorId) {
                doctorBox.setSelectedIndex(i);
                return;
            }
        }
    }

    /**
     * 清空输入框
     */
    private void clearInput() {
        scheduleIdField.setText("");
        workDateField.setText("");
        totalNumField.setText("");
        remainNumField.setText("");

        if (doctorBox.getItemCount() > 0) {
            doctorBox.setSelectedIndex(0);
        }

        if (timePeriodBox.getItemCount() > 0) {
            timePeriodBox.setSelectedIndex(0);
        }

        table.clearSelection();
    }

    /**
     * 医生下拉框使用的内部类
     *
     * JComboBox 显示对象时会调用 toString()
     * 所以 toString() 返回医生姓名和科室名称。
     *
     * 但真正插入 schedule 表时，需要的是 doctor_id。
     */
    private static class DoctorItem {
        private int doctorId;
        private String doctorName;
        private String deptName;

        public DoctorItem(int doctorId, String doctorName, String deptName) {
            this.doctorId = doctorId;
            this.doctorName = doctorName;
            this.deptName = deptName;
        }

        public int getDoctorId() {
            return doctorId;
        }

        public String getDoctorName() {
            return doctorName;
        }

        public String getDeptName() {
            return deptName;
        }

        @Override
        public String toString() {
            return doctorName + " - " + deptName;
        }
    }
}