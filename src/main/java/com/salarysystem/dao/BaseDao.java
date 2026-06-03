package com.salarysystem.dao;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public interface BaseDao {
    private DataSource getDataSource() {
        DataSource dataSource = null;
        try {
            Context context = new InitialContext();
            dataSource = (DataSource) context.lookup("java:comp/env/jdbc/SalarySystem");
        } catch (NamingException ne) {
            System.out.println("异常:" + ne);
        }
        return dataSource;
    }

    public default Connection getConnection() throws SQLException {
        DataSource dataSource = getDataSource();
        if (dataSource == null) {
            throw new SQLException("无法获取数据源：JNDI 查找 java:comp/env/jdbc/SalarySystem 失败，请检查 context.xml 配置");
        }
        try {
            return dataSource.getConnection();
        } catch (SQLException sqle) {
            System.out.println("获取数据库连接异常:" + sqle.getMessage());
            throw sqle;
        }
    }
}
