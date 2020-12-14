package io.koschicken.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class InitDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitDatabase.class);

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
        } catch (ClassNotFoundException | SQLException ignore) {
            LOGGER.info("初次运行，即将新建数据库。");
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
            if (version == -1) {
                statement.executeUpdate("create table 'version'('version' integer)");
                statement.executeUpdate("insert into version values (" + NEW_VERSION + ")");
                statement.executeUpdate("create table pic (pid integer primary key, last_send_time datetime)");
                statement.executeUpdate("create table scores (qq varchar(15) primary key, nickname varchar(50), sign_flag boolean default false, live_flag boolean default false, score integer default 0, roll_count integer default 3)");
                statement.executeUpdate("create table qq_group (id integer not null constraint group_pk primary key autoincrement, qq varchar(15) not null, group_code varchar(15) not null)");
                statement.executeUpdate("create table live (id integer not null constraint live_pk primary key autoincrement, qq varchar(15) not null, bili_uid varchar(15) not null)");
                statement.executeUpdate("create table lucky (id integer not null constraint lucky_pk primary key autoincrement, qq integer, date datetime, coin integer)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
