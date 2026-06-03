package hospital.util;

import hospital.util.DBUtil;

import java.sql.Connection;
import java.sql.SQLException;

public class TestDB {
    public static void main(String[] args) {
        try {
            Connection conn = DBUtil.getConnection();
            System.out.println("数据库连接成功！");
            System.out.println(conn);

            conn.close();
        } catch (Exception e) {
            System.out.println("数据库连接失败！");
            e.printStackTrace();
        }
    }
}
