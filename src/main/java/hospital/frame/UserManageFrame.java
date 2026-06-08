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
 * 用户管理界面
 *
 * 只有 super_admin 超级管理员可以进入
 *
 * 功能：
 * 1. 查询管理员用户
 * 2. 添加管理员用户
 * 3. 修改管理员用户
 * 4. 删除管理员用户
 * 5. 点击表格行后，将数据显示到输入框
 */
public class UserManageFrame extends JFrame {

    // 当前登录管理员编号
    private int currentAdminId;

    // 当前登录管理员姓名
    private String currentRealName;

    // 当前登录管理员角色
    private String currentRole;

    // 表格
    private JTable table;

    // 表格模型
    private DefaultTableModel tableModel;

    // 管理员编号输入框
    private JTextField adminIdField;

    // 账号输入框
    private JTextField usernameField;

    // 密码输入框
    private JTextField passwordField;

    // 真实姓名输入框
    private JTextField realNameField;

    // 角色下拉框
    private JComboBox<String> roleBox;

    // 状态下拉框
    private JComboBox<String> statusBox;

    /**
     * 构造方法
     */
    public UserManageFrame(int currentAdminId, String currentRealName, String currentRole) {
        this.currentAdminId = currentAdminId;
        this.currentRealName = currentRealName;
        this.currentRole = currentRole;

        setTitle("医院门诊挂号系统 - 用户管理");

        setSize(900, 550);

        setLocationRelativeTo(null);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initView();

        loadUserData();

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
        JLabel titleLabel = new JLabel("用户管理 - 当前用户：" + currentRealName, SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // =========================
        // 2. 中间表格
        // =========================
        String[] columns = {
                "管理员编号",
                "账号",
                "密码",
                "真实姓名",
                "角色",
                "状态",
                "创建时间"
        };

        tableModel = new DefaultTableModel(columns, 0);

        table = new JTable(tableModel);

        table.setRowHeight(28);

        JScrollPane scrollPane = new JScrollPane(table);

        add(scrollPane, BorderLayout.CENTER);

        // =========================
        // 3. 底部输入区
        // =========================
        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(2, 6, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        adminIdField = new JTextField();
        usernameField = new JTextField();
        passwordField = new JTextField();
        realNameField = new JTextField();

        roleBox = new JComboBox<>(new String[]{"admin", "super_admin"});
        statusBox = new JComboBox<>(new String[]{"1-启用", "0-禁用"});

        // 管理员编号是数据库自动生成的，不允许手动修改
        adminIdField.setEditable(false);

        // 第一行：标签
        inputPanel.add(new JLabel("管理员编号"));
        inputPanel.add(new JLabel("账号"));
        inputPanel.add(new JLabel("密码"));
        inputPanel.add(new JLabel("真实姓名"));
        inputPanel.add(new JLabel("角色"));
        inputPanel.add(new JLabel("状态"));

        // 第二行：输入框
        inputPanel.add(adminIdField);
        inputPanel.add(usernameField);
        inputPanel.add(passwordField);
        inputPanel.add(realNameField);
        inputPanel.add(roleBox);
        inputPanel.add(statusBox);

        // =========================
        // 4. 按钮区
        // =========================
        JPanel buttonPanel = new JPanel();

        JButton addButton = new JButton("添加");
        JButton updateButton = new JButton("修改");
        JButton deleteButton = new JButton("删除");
        JButton refreshButton = new JButton("刷新");
        JButton clearButton = new JButton("清空");
        JButton mainButton = new JButton("进入主界面");
        JButton closeButton = new JButton("关闭");

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(mainButton);
        buttonPanel.add(closeButton);

        bottomPanel.add(inputPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        // =========================
        // 5. 事件绑定
        // =========================

        // 点击表格行，回显数据
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillInputFromSelectedRow();
            }
        });

        // 添加用户
        addButton.addActionListener(e -> addUser());

        // 修改用户
        updateButton.addActionListener(e -> updateUser());

        // 删除用户
        deleteButton.addActionListener(e -> deleteUser());

        // 刷新表格
        refreshButton.addActionListener(e -> loadUserData());

        // 清空输入框
        clearButton.addActionListener(e -> clearInput());

        // 进入主界面
        mainButton.addActionListener(e -> new MainFrame());

        // 关闭当前窗口
        closeButton.addActionListener(e -> dispose());
    }

    /**
     * 加载管理员用户数据
     */
    private void loadUserData() {
        tableModel.setRowCount(0);

        String sql = """
                SELECT
                    admin_id,
                    username,
                    password,
                    real_name,
                    role,
                    status,
                    create_time
                FROM admin_user
                ORDER BY admin_id
                """;

        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Object[] row = {
                        rs.getInt("admin_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("real_name"),
                        rs.getString("role"),
                        rs.getInt("status"),
                        rs.getTimestamp("create_time")
                };

                tableModel.addRow(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载用户数据失败：" + e.getMessage());
        }
    }

    /**
     * 添加管理员用户
     */
    private void addUser() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String realName = realNameField.getText().trim();
        String role = roleBox.getSelectedItem().toString();
        int status = getSelectedStatus();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "账号不能为空");
            return;
        }

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "密码不能为空");
            return;
        }

        String sql = """
                INSERT INTO admin_user(username, password, real_name, role, status)
                VALUES (?, ?, ?, ?, ?)
                """;

        try {
            int rows = JdbcUtil.update(sql, username, password, realName, role, status);

            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "添加用户成功");
                loadUserData();
                clearInput();
            } else {
                JOptionPane.showMessageDialog(this, "添加用户失败");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "添加用户失败：" + e.getMessage());
        }
    }

    /**
     * 修改管理员用户
     */
    private void updateUser() {
        String adminIdText = adminIdField.getText().trim();

        if (adminIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先选择要修改的用户");
            return;
        }

        int adminId;

        try {
            adminId = Integer.parseInt(adminIdText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "管理员编号格式错误");
            return;
        }

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String realName = realNameField.getText().trim();
        String role = roleBox.getSelectedItem().toString();
        int status = getSelectedStatus();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "账号不能为空");
            return;
        }

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "密码不能为空");
            return;
        }

        // 防止超级管理员把自己禁用
        if (adminId == currentAdminId && status == 0) {
            JOptionPane.showMessageDialog(this, "不能禁用当前登录账号");
            return;
        }

        // 防止超级管理员把自己改成普通管理员
        if (adminId == currentAdminId && !"super_admin".equals(role)) {
            JOptionPane.showMessageDialog(this, "不能将当前登录账号改为普通管理员");
            return;
        }

        String sql = """
                UPDATE admin_user
                SET username = ?,
                    password = ?,
                    real_name = ?,
                    role = ?,
                    status = ?
                WHERE admin_id = ?
                """;

        try {
            int rows = JdbcUtil.update(sql, username, password, realName, role, status, adminId);

            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "修改用户成功");
                loadUserData();
                clearInput();
            } else {
                JOptionPane.showMessageDialog(this, "修改用户失败");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "修改用户失败：" + e.getMessage());
        }
    }

    /**
     * 删除管理员用户
     */
    private void deleteUser() {
        String adminIdText = adminIdField.getText().trim();

        if (adminIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的用户");
            return;
        }

        int adminId;

        try {
            adminId = Integer.parseInt(adminIdText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "管理员编号格式错误");
            return;
        }

        // 防止删除当前登录账号
        if (adminId == currentAdminId) {
            JOptionPane.showMessageDialog(this, "不能删除当前登录账号");
            return;
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                "确定要删除该用户吗？",
                "确认删除",
                JOptionPane.YES_NO_OPTION
        );

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = """
                DELETE FROM admin_user
                WHERE admin_id = ?
                """;

        try {
            int rows = JdbcUtil.update(sql, adminId);

            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "删除用户成功");
                loadUserData();
                clearInput();
            } else {
                JOptionPane.showMessageDialog(this, "删除用户失败");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "删除用户失败：" + e.getMessage());
        }
    }

    /**
     * 点击表格行后，把数据回显到输入框
     */
    private void fillInputFromSelectedRow() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            return;
        }

        Object adminId = tableModel.getValueAt(selectedRow, 0);
        Object username = tableModel.getValueAt(selectedRow, 1);
        Object password = tableModel.getValueAt(selectedRow, 2);
        Object realName = tableModel.getValueAt(selectedRow, 3);
        Object role = tableModel.getValueAt(selectedRow, 4);
        Object status = tableModel.getValueAt(selectedRow, 5);

        adminIdField.setText(adminId == null ? "" : adminId.toString());
        usernameField.setText(username == null ? "" : username.toString());
        passwordField.setText(password == null ? "" : password.toString());
        realNameField.setText(realName == null ? "" : realName.toString());

        if (role != null) {
            roleBox.setSelectedItem(role.toString());
        }

        if (status != null && "0".equals(status.toString())) {
            statusBox.setSelectedItem("0-禁用");
        } else {
            statusBox.setSelectedItem("1-启用");
        }
    }

    /**
     * 清空输入框
     */
    private void clearInput() {
        adminIdField.setText("");
        usernameField.setText("");
        passwordField.setText("");
        realNameField.setText("");
        roleBox.setSelectedItem("admin");
        statusBox.setSelectedItem("1-启用");

        table.clearSelection();
    }

    /**
     * 获取状态下拉框的值
     *
     * 1-启用 -> 1
     * 0-禁用 -> 0
     */
    private int getSelectedStatus() {
        String statusText = statusBox.getSelectedItem().toString();

        if (statusText.startsWith("0")) {
            return 0;
        }

        return 1;
    }
}