package io.koschicken.database;

import java.sql.*;

public class InitDatabase {
    private static final int NEW_VERSION = 7;

    public void initDB() {
        int version = -1;
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:root.db");
            preparedStatement = conn.prepareStatement("select version from version");
            ResultSet set = preparedStatement.executeQuery();
            while (set.next()) {
                version = set.getInt("version");
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            createDatabase(version, conn);
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    public void createDatabase(int version, Connection conn) {
        try (Statement statement = conn.createStatement()) {
            if (version == -1) {//没有数据库版本标识，重新建立数据库
                statement.executeUpdate("create table scores(qq integer primary key, nickname varchar(50), sign_flag boolean default false, score integer(8) default 0, group_code text)");
                statement.executeUpdate("create table 'version'('version' integer)");
                statement.executeUpdate("insert into version values (" + NEW_VERSION + ")");
                statement.executeUpdate("create table pic(pid integer primary key, last_send_time datetime)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
