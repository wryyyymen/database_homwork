package hospital.util;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class JdbcUtil {
    //update解决增，删，改功能
    public  static  int update(String sql,Object... params){
        try(
                Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ){
            //把参数填入 SQL 的 ? 里面。
            for(int i = 0;i < params.length;i++){
                // JDBC 参数下标从 1 开始
                // Java 数组下标从 0 开始
                ps.setObject(i+1,params[i]);
            }
            // 执行 INSERT / UPDATE / DELETE
            // 返回受影响的行数
            return  ps.executeUpdate();
            //对于增删改 SQL 来说，executeUpdate() 返回的是受影响的行数。
        }catch (Exception e){
            // 打印错误信息
            e.printStackTrace();;
            return  0;
        }
    }
    /**
     * 这个方法不会自己关闭 Connection。
     * 因为事务需要多个 SQL 共用同一个 conn。
     *
     * 用于：
     * 1. 预约挂号
     * 2. 转账
     * 3. 多条 SQL 必须一起成功/一起失败的操作
     */
    public  static  int update(Connection conn,String sql,Object... params) throws  Exception{
            try(
                    // 使用外部传进来的 conn 创建 PreparedStatement
                    PreparedStatement ps = conn.prepareStatement(sql)
                    ){
                for (int i = 0;i<params.length;i++){
                    ps.setObject(i + 1,params[i] );
                }

                //执行增删改sql
                return  ps.executeUpdate();
            }
    }
}
