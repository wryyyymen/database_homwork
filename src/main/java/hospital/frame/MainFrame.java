package hospital.frame;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    public  MainFrame(){
        setTitle("医院门诊挂号系统 - 主界面");
        setSize(700, 450);

        // 设置窗口居中显示
        setLocationRelativeTo(null);

        // 关闭窗口时退出整个程序
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 初始化界面
        initView();

        // 显示窗口
        setVisible(true);
    }

    /**
     * 初始化主菜单界面
     */
    private void initView(){
        setLayout(new BorderLayout());
        //设置的文本标签组件
        JLabel titleLabel = new JLabel("医院门诊挂号系统", SwingConstants.CENTER);
       // "微软雅黑"：字体名称, Font.BOLD：加粗26：字体大小
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 26));
        //把标题标签添加到窗口上方
        add(titleLabel, BorderLayout.NORTH);
        /*
         * 创建菜单面板
         * JPanel 是一个容器，可以放按钮、标签等组件。
         * 4 行 2 列
         * 20：每列之间的水平间距
         * 20：每行之间的垂直间距
         * 所以这里会形成一个 4 行 2 列的按钮区域。
         */
        JPanel menuPanel = new JPanel(new GridLayout(4, 2, 20, 20));
        /*
         * 设置菜单面板的内边距
         * EmptyBorder(40, 80, 40, 80)
         * 上边距 40 左边距 80 下边距 40 右边距 80
         * 这样按钮不会紧贴窗口边缘，界面更好看。
         */
        menuPanel.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));

        /*
         * 创建各个功能按钮
         * JButton 是 Swing 中的按钮组件。
         */
        JButton patientButton = new JButton("患者管理");
        JButton deptButton = new JButton("科室管理");
        JButton doctorButton = new JButton("医生管理");
        JButton scheduleButton = new JButton("排班管理");
        JButton appointmentButton = new JButton("预约挂号");
        JButton paymentButton = new JButton("缴费管理");
        JButton recordButton = new JButton("就诊记录");
        JButton exitButton = new JButton("退出系统");

        /*
         * 把按钮添加到菜单面板中
         * 因为 menuPanel 使用的是 GridLayout(4, 2)，
         * 所以按钮会自动按照 4 行 2 列排列。
         */
        menuPanel.add(patientButton);
        menuPanel.add(deptButton);
        menuPanel.add(doctorButton);
        menuPanel.add(scheduleButton);
        menuPanel.add(appointmentButton);
        menuPanel.add(paymentButton);
        menuPanel.add(recordButton);
        menuPanel.add(exitButton);

        //把菜单面板添加到窗口中间
        add(menuPanel,BorderLayout.CENTER);

        /*
         * 给“患者管理”按钮绑定点击事件
         * e -> 是 Lambda 表达式，是 Java 简化事件监听写法。
         */
        patientButton.addActionListener(e -> {
            new PatientManageFrame();
        });

        /*
         * 给“科室管理”按钮绑定点击事件
         */
        deptButton.addActionListener(e -> {
            new DepartmentManageFrame();
        });

        /*
         * 给“医生管理”按钮绑定点击事件
         */
        doctorButton.addActionListener(e -> {
            new DoctorManageFrame();
        });

        /*
         * 给“排班管理”按钮绑定点击事件
         */
        scheduleButton.addActionListener(e -> {
            new ScheduleManageFrame();
        });

        /*
         * 给“预约挂号”按钮绑定点击事件
         */
        appointmentButton.addActionListener(e -> {
            /*
             * 创建预约挂号窗口
             *
             * 这句执行后，会打开 AppointmentFrame 窗口。
             */
            new AppointmentFrame();
        });

        /*
         * 给“缴费管理”按钮绑定点击事件
         */
        paymentButton.addActionListener(e -> {
            new PaymentManageFrame();
        });

        /*
         * 给“就诊记录”按钮绑定点击事件
         */
        recordButton.addActionListener(e -> {
            new MedicalRecordFrame();
        });

        /*
         * 给“退出系统”按钮绑定点击事件
         *
         * System.exit(0) 表示正常退出整个 Java 程序。
         */
        exitButton.addActionListener(e -> {
            System.exit(0);
        });



    }
}
