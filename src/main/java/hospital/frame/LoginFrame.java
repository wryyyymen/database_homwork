package hospital.frame;

import hospital.util.DBUtil;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 管理员登录界面
 */
public class LoginFrame extends JFrame {

    // 账号输入框
    private JTextField usernameField;

    // 密码输入框
    private JPasswordField passwordField;

    // 登录按钮
    private JButton loginButton;

    // 重置按钮
    private JButton resetButton;

    /**
     * 构造方法
     * new LoginFrame() 时自动执行
     */
    public LoginFrame() {
        // 设置窗口标题
        setTitle("医院门诊挂号系统 - 登录");

        // 设置窗口大小
        setSize(420, 280);

        // 窗口居中显示
        setLocationRelativeTo(null);

        // 点击关闭按钮时结束程序
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 禁止改变窗口大小
        setResizable(false);

        // 初始化界面
        initView();

        // 显示窗口
        setVisible(true);
    }

    /**
     * 限制输入框最大输入长度
     */
    private static class LimitDocument extends PlainDocument {

        // 最大长度
        private final int maxLength;

        public LimitDocument(int maxLength) {
            this.maxLength = maxLength;
        }

        @Override
        public void insertString(int offset, String str, AttributeSet attr)
                throws BadLocationException {

            if (str == null) {
                return;
            }

            // 当前已有长度 + 新输入长度 <= 最大长度，才允许输入
            if (getLength() + str.length() <= maxLength) {
                super.insertString(offset, str, attr);
            }
        }
    }

    /**
     * 初始化界面组件
     */
    private void initView() {
        // 主面板，使用 BorderLayout 分为上中下区域
        JPanel mainPanel = new JPanel(new BorderLayout());

        // ===================== 标题区域 =====================

        JLabel titleLabel = new JLabel("医院门诊挂号系统", SwingConstants.CENTER);

        // 设置标题字体
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 22));

        // 设置标题上下边距
        titleLabel.setBorder(BorderFactory.createEmptyBorder(25, 0, 10, 0));

        // 把标题放到窗口上方
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // ===================== 表单区域 =====================

        JPanel formPanel = new JPanel();

        // 使用 GridBagLayout，方便精确控制输入框大小和间距
        formPanel.setLayout(new GridBagLayout());

        // 设置表单区域整体边距：上、左、下、右
        formPanel.setBorder(BorderFactory.createEmptyBorder(5, 50, 5, 50));

        // GridBagConstraints 用来控制组件位置、间距
        GridBagConstraints gbc = new GridBagConstraints();

        // 设置每个组件周围的间距：上、左、下、右
        // 左右设置得比较小，所以“账号”和输入框距离较近
        gbc.insets = new Insets(6, 2, 6, 2);

        // 组件居中
        gbc.anchor = GridBagConstraints.CENTER;

        // 标签字体：账号、密码
        Font labelFont = new Font("微软雅黑", Font.PLAIN, 17);

        // 输入框字体
        Font inputFont = new Font("微软雅黑", Font.PLAIN, 14);

        // ---------- 第一行：账号 ----------

        JLabel usernameLabel = new JLabel("账号：");
        usernameLabel.setFont(labelFont);

        // 第 0 列，第 0 行
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(usernameLabel, gbc);

        usernameField = new JTextField();
        usernameField.setFont(inputFont);

        // 限制账号最多输入 8 位
        usernameField.setDocument(new LimitDocument(8));

        // 设置输入框宽高：宽 180，高 24
        usernameField.setPreferredSize(new Dimension(140, 24));

        // 第 1 列，第 0 行
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(usernameField, gbc);

        // ---------- 第二行：密码 ----------

        JLabel passwordLabel = new JLabel("密码：");
        passwordLabel.setFont(labelFont);

        // 第 0 列，第 1 行
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField();
        passwordField.setFont(inputFont);

        // 限制密码最多输入 16 位
        passwordField.setDocument(new LimitDocument(16));

        // 设置密码框宽高：宽 180，高 24
        passwordField.setPreferredSize(new Dimension(140, 24));

        // 第 1 列，第 1 行
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(passwordField, gbc);

        // 把表单区域放到窗口中间
        mainPanel.add(formPanel, BorderLayout.CENTER);

        // ===================== 按钮区域 =====================

        JPanel buttonPanel = new JPanel(new FlowLayout());

        loginButton = new JButton("登录");
        resetButton = new JButton("重置");

        // 设置按钮字体
        loginButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        resetButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        // 设置按钮大小
        loginButton.setPreferredSize(new Dimension(80, 30));
        resetButton.setPreferredSize(new Dimension(80, 30));

        buttonPanel.add(loginButton);
        buttonPanel.add(resetButton);

        // 设置按钮区域下边距
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // 把按钮区域放到窗口下方
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // 把主面板加入窗口
        add(mainPanel);

        // ===================== 事件绑定 =====================

        // 点击登录按钮，执行 login 方法
        loginButton.addActionListener(e -> login());

        // 点击重置按钮，执行 reset 方法
        resetButton.addActionListener(e -> reset());

        // 在密码框中按回车，也执行登录
        passwordField.addActionListener(e -> login());
    }

    /**
     * 登录验证方法
     */
    private void login() {
        // 获取账号
        String username = usernameField.getText().trim();

        // 获取密码
        String password = new String(passwordField.getPassword()).trim();

        // 判断账号是否为空
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入账号！");
            usernameField.requestFocus();
            return;
        }

        // 判断密码是否为空
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入密码！");
            passwordField.requestFocus();
            return;
        }

        /*
         * 查询管理员表
         *
         * username/admin_id = ?  表示账号名或管理员编号必须匹配
         * password = ?  表示密码必须匹配
         * status = 1    表示账号必须是启用状态
         */
        String sql = """
                SELECT admin_id, username, real_name, role
                FROM admin_user
                WHERE (username = ? OR CAST(admin_id AS CHAR) = ?)
                  AND password = ?
                  AND status = 1
                """;

        try (
                // 获取数据库连接
                Connection conn = DBUtil.getConnection();

                // 创建 PreparedStatement
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            // 设置第一个 ?，也就是 username
            ps.setString(1, username);

            // 设置第二个 ?，也就是 admin_id
            ps.setString(2, username);

            // 设置第三个 ?，也就是 password
            ps.setString(3, password);

            // 执行查询
            try (ResultSet rs = ps.executeQuery()) {

                // 如果查到了数据，说明登录成功
                if (rs.next()) {
                    int adminId = rs.getInt("admin_id");
                    String realName = rs.getString("real_name");
                    String role = rs.getString("role");

                    JOptionPane.showMessageDialog(this, "登录成功，欢迎：" + realName);

                    // 关闭登录窗口
                    this.dispose();

                    // 如果是超级管理员，进入用户管理页面
                    if ("super_admin".equals(role)) {
                        new UserManageFrame(adminId, realName, role);
                    } else {
                        // 普通管理员进入主界面
                        new MainFrame();
                    }

                } else {
                    // 没有查到数据，说明账号密码错误或者账号被禁用
                    JOptionPane.showMessageDialog(this, "账号或密码错误，或账号已被禁用！");
                    passwordField.setText("");
                    passwordField.requestFocus();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "登录失败，数据库连接或查询异常！");
        }
    }

    /**
     * 重置输入框
     */
    private void reset() {
        usernameField.setText("");
        passwordField.setText("");
        usernameField.requestFocus();
    }
}
