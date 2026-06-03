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
 * 科室管理界面
 *
 * 功能：
 * 1. 查询科室信息
 * 2. 添加科室
 * 3. 修改科室
 * 4. 删除科室
 * 5. 点击表格行后，将数据回显到输入框
 */
public class DepartmentManageFrame extends JFrame {

    // JTable 是 Swing 的表格组件，用来显示科室列表
    private JTable table;

    // DefaultTableModel 是表格的数据模型，用来管理表头和表格数据
    private DefaultTableModel tableModel;

    // 科室编号输入框
    // 科室编号由数据库自动生成，所以不允许用户手动修改
    private JTextField deptIdField;

    // 科室名称输入框
    private JTextField deptNameField;

    // 科室位置输入框
    private JTextField locationField;

    /**
     * 构造方法
     *
     * 当执行 new DepartmentManageFrame() 时，
     * 就会创建并显示科室管理窗口。
     */
    public DepartmentManageFrame() {
        // 设置窗口标题
        setTitle("医院门诊挂号系统 - 科室管理");

        // 设置窗口大小
        setSize(750, 500);

        // 设置窗口居中显示
        setLocationRelativeTo(null);

        // 关闭当前窗口时，不退出整个程序
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 初始化界面组件
        initView();

        // 从数据库加载科室数据
        loadDepartmentData();

        // 显示窗口
        setVisible(true);
    }

    /**
     * 初始化界面
     */
    private void initView() {
        // 使用 BorderLayout 布局
        // NORTH：标题
        // CENTER：表格
        // SOUTH：输入框和按钮
        setLayout(new BorderLayout());

        // =========================
        // 1. 顶部标题
        // =========================

        // 创建标题标签
        JLabel titleLabel = new JLabel("科室管理", SwingConstants.CENTER);

        // 设置标题字体
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));

        // 把标题放到窗口上方
        add(titleLabel, BorderLayout.NORTH);

        // =========================
        // 2. 中间表格
        // =========================

        // 定义表格表头
        String[] columns = {
                "科室编号",
                "科室名称",
                "科室位置"
        };

        // 创建表格模型
        // 这里重写 isCellEditable，让表格不能直接编辑
        // 用户要修改数据，应该先点表格行，再在输入框中修改
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // 根据表格模型创建 JTable
        table = new JTable(tableModel);

        // 设置表格行高
        table.setRowHeight(28);

        // 把表格放入滚动面板中
        // 数据多的时候可以滚动查看
        JScrollPane scrollPane = new JScrollPane(table);

        // 把滚动面板放到窗口中间
        add(scrollPane, BorderLayout.CENTER);

        // =========================
        // 3. 底部输入区和按钮区
        // =========================

        // 底部总面板
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // 输入区面板
        // 2 行 3 列：
        // 第一行放标签
        // 第二行放输入框
        JPanel inputPanel = new JPanel(new GridLayout(2, 3, 10, 10));

        // 设置输入区内边距
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // 创建输入框
        deptIdField = new JTextField();
        deptNameField = new JTextField();
        locationField = new JTextField();

        // 科室编号由数据库自动生成，不允许用户手动输入或修改
        deptIdField.setEditable(false);

        // 第一行：标签
        inputPanel.add(new JLabel("科室编号"));
        inputPanel.add(new JLabel("科室名称"));
        inputPanel.add(new JLabel("科室位置"));

        // 第二行：输入框
        inputPanel.add(deptIdField);
        inputPanel.add(deptNameField);
        inputPanel.add(locationField);

        // 按钮区面板
        JPanel buttonPanel = new JPanel();

        // 创建按钮
        JButton addButton = new JButton("添加");
        JButton updateButton = new JButton("修改");
        JButton deleteButton = new JButton("删除");
        JButton refreshButton = new JButton("刷新");
        JButton clearButton = new JButton("清空");
        JButton closeButton = new JButton("关闭");

        // 把按钮加入按钮区
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(closeButton);

        // 把输入区和按钮区加入底部总面板
        bottomPanel.add(inputPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        // 把底部总面板放到窗口下方
        add(bottomPanel, BorderLayout.SOUTH);

        // =========================
        // 4. 绑定事件
        // =========================

        // 点击表格某一行时，把该行数据填入输入框
        table.getSelectionModel().addListSelectionListener(e -> {
            // 避免事件重复触发
            if (!e.getValueIsAdjusting()) {
                fillInputFromSelectedRow();
            }
        });

        // 点击“添加”按钮，执行添加科室
        addButton.addActionListener(e -> addDepartment());

        // 点击“修改”按钮，执行修改科室
        updateButton.addActionListener(e -> updateDepartment());

        // 点击“删除”按钮，执行删除科室
        deleteButton.addActionListener(e -> deleteDepartment());

        // 点击“刷新”按钮，重新加载科室数据
        refreshButton.addActionListener(e -> loadDepartmentData());

        // 点击“清空”按钮，清空输入框
        clearButton.addActionListener(e -> clearInput());

        // 点击“关闭”按钮，关闭当前窗口
        closeButton.addActionListener(e -> dispose());
    }

    /**
     * 从数据库查询科室数据，并显示到 JTable
     */
    private void loadDepartmentData() {
        // 清空表格中原来的数据
        tableModel.setRowCount(0);

        // 查询科室表
        String sql = """
                SELECT
                    dept_id,
                    dept_name,
                    location
                FROM department
                ORDER BY dept_id
                """;

        try (
                // 获取数据库连接
                Connection conn = DBUtil.getConnection();

                // 创建 PreparedStatement
                PreparedStatement ps = conn.prepareStatement(sql);

                // 执行查询，得到 ResultSet 结果集
                ResultSet rs = ps.executeQuery()
        ) {
            // 遍历查询结果
            while (rs.next()) {
                // 把当前这一行的数据放入 Object 数组
                Object[] row = {
                        rs.getInt("dept_id"),
                        rs.getString("dept_name"),
                        rs.getString("location")
                };

                // 把这一行数据添加到表格模型中
                tableModel.addRow(row);
            }

        } catch (Exception e) {
            // 打印错误信息，方便调试
            e.printStackTrace();

            // 弹窗提示
            JOptionPane.showMessageDialog(this, "加载科室数据失败：" + e.getMessage());
        }
    }

    /**
     * 添加科室
     */
    private void addDepartment() {
        // 获取输入框内容，并去掉前后空格
        String deptName = deptNameField.getText().trim();
        String location = locationField.getText().trim();

        // 科室名称不能为空
        if (deptName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "科室名称不能为空");
            return;
        }

        // 插入科室 SQL
        String sql = """
                INSERT INTO department(dept_name, location)
                VALUES (?, ?)
                """;

        // 调用 JdbcUtil.update 执行新增操作
        int rows = JdbcUtil.update(sql, deptName, location);

        // rows > 0 表示至少影响了一行，说明添加成功
        if (rows > 0) {
            JOptionPane.showMessageDialog(this, "添加科室成功");

            // 重新加载表格数据
            loadDepartmentData();

            // 清空输入框
            clearInput();
        } else {
            JOptionPane.showMessageDialog(this, "添加科室失败");
        }
    }

    /**
     * 修改科室
     */
    private void updateDepartment() {
        // 获取科室编号
        String deptIdText = deptIdField.getText().trim();

        // 如果科室编号为空，说明用户还没有选择表格中的某一行
        if (deptIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先选择要修改的科室");
            return;
        }

        // 获取输入框内容
        String deptName = deptNameField.getText().trim();
        String location = locationField.getText().trim();

        // 科室名称不能为空
        if (deptName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "科室名称不能为空");
            return;
        }

        int deptId;

        try {
            // 把字符串类型的科室编号转换成 int
            deptId = Integer.parseInt(deptIdText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "科室编号格式错误");
            return;
        }

        // 修改科室 SQL
        String sql = """
                UPDATE department
                SET dept_name = ?,
                    location = ?
                WHERE dept_id = ?
                """;

        // 调用 JdbcUtil.update 执行修改
        int rows = JdbcUtil.update(sql, deptName, location, deptId);

        if (rows > 0) {
            JOptionPane.showMessageDialog(this, "修改科室成功");

            // 重新加载表格
            loadDepartmentData();

            // 清空输入框
            clearInput();
        } else {
            JOptionPane.showMessageDialog(this, "修改科室失败");
        }
    }

    /**
     * 删除科室
     */
    private void deleteDepartment() {
        // 获取科室编号
        String deptIdText = deptIdField.getText().trim();

        // 如果为空，说明没有选中要删除的科室
        if (deptIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的科室");
            return;
        }

        // 删除前弹出确认框
        int result = JOptionPane.showConfirmDialog(
                this,
                "确定要删除该科室吗？",
                "确认删除",
                JOptionPane.YES_NO_OPTION
        );

        // 如果用户没有点击“是”，就取消删除
        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        int deptId;

        try {
            // 把科室编号转换为 int
            deptId = Integer.parseInt(deptIdText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "科室编号格式错误");
            return;
        }

        // 删除科室 SQL
        String sql = "DELETE FROM department WHERE dept_id = ?";

        // 调用通用增删改方法
        int rows = JdbcUtil.update(sql, deptId);

        if (rows > 0) {
            JOptionPane.showMessageDialog(this, "删除科室成功");

            // 重新加载表格
            loadDepartmentData();

            // 清空输入框
            clearInput();
        } else {
            JOptionPane.showMessageDialog(this, "删除科室失败");
        }
    }

    /**
     * 点击表格行后，把选中行的数据填入输入框
     */
    private void fillInputFromSelectedRow() {
        // 获取当前选中的行号
        int selectedRow = table.getSelectedRow();

        // 如果没有选中任何行，直接返回
        if (selectedRow == -1) {
            return;
        }

        // 从表格模型中取出当前行的数据
        Object deptId = tableModel.getValueAt(selectedRow, 0);
        Object deptName = tableModel.getValueAt(selectedRow, 1);
        Object location = tableModel.getValueAt(selectedRow, 2);

        // 把数据填入输入框
        deptIdField.setText(deptId == null ? "" : deptId.toString());
        deptNameField.setText(deptName == null ? "" : deptName.toString());
        locationField.setText(location == null ? "" : location.toString());
    }

    /**
     * 清空输入框
     */
    private void clearInput() {
        // 清空三个输入框
        deptIdField.setText("");
        deptNameField.setText("");
        locationField.setText("");

        // 清除表格选中状态
        table.clearSelection();
    }
}