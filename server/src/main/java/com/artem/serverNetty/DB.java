package com.artem.serverNetty;

import java.sql.*;

public class DB {
    private Connection connection;
    private Statement stmt;

    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:DB.db");
            stmt = connection.createStatement();
            System.out.printf("Поключились к бд");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean authCheck(String login, String pass){
        boolean authOk = false;
        //boolean authOk = true;
        try {
            System.out.printf(login + "  ::  " + pass);
            String sql = String.format("SELECT * FROM users where login = '%s' and pass = '%s'", login, pass);
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                authOk = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return authOk;
    }

    public void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
