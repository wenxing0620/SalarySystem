package com.salarysystem;

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
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
        } catch (SQLException sqle) {
            System.out.println("异常:" + sqle);
        }
        return conn;
    }
}
