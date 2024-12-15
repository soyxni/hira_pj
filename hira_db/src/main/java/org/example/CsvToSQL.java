package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
public class CsvToSQL {

    private static final Logger logger = LogManager.getLogger(CsvToSQL.class);

    public static void main(String[] args) {
        String jdbcURL = "jdbc:mysql://localhost:3306";
        String username = "root";
        String password = "password";

        String csvFilePath = "20241213_condition.csv";
        String databaseName = "nonpay_database";
        String tableName = "nonpay_table";

        Connection connection = null;
        BufferedReader br = null;

        try {
            connection = DriverManager.getConnection(jdbcURL, username, password);
            try (Statement statement = connection.createStatement()) {
                createDatabase(statement, databaseName);
            }

            // 데이터베이스 선택
            connection.setCatalog(databaseName);

            // 테이블 생성
            br = new BufferedReader(new FileReader(csvFilePath));
            createTable(connection, br, tableName);

            // 데이터 삽입
            br.close();
            br = new BufferedReader(new FileReader(csvFilePath));
            br.readLine(); // 헤더 스킵
            insertData(connection, br, tableName);

            logger.info("Data 처리 완료");

        } catch (Exception e) {
            logger.error("오류 발생", e);
        } finally {
            try {
                if (connection != null) connection.close();
                if (br != null) br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    //DB 생성
    private static void createDatabase(Statement statement, String databaseName) {
        String createDatabaseSQL = "CREATE DATABASE IF NOT EXISTS " + databaseName;
        try {
            statement.executeUpdate(createDatabaseSQL);
            logger.info("DB 생성 완료: {}", databaseName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTable(Connection connection, BufferedReader br, String tableName) {
        try (Statement statement = connection.createStatement()) {
            String headerLine = br.readLine();
            String[] columns = headerLine.split(",");
            StringBuilder createTableSQL = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");

            for (String column : columns) {
                createTableSQL.append(column.trim()).append(" VARCHAR(255),");
            }
            createTableSQL.deleteCharAt(createTableSQL.length() - 1); // 마지막 콤마 제거
            createTableSQL.append(")");

            logger.info("테이블 생성 쿼리: {}", createTableSQL.toString());
            statement.executeUpdate(createTableSQL.toString());
            logger.info("Table 생성 완료: {}", tableName);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void insertData(Connection connection, BufferedReader br, String tableName) {
        String insertSQLTemplate = "INSERT INTO " + tableName + " VALUES (";
        Statement statement = null;

        try {
            statement = connection.createStatement();
            String line;

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",", -1); // 빈 문자열도 포함하기 위해 limit를 -1로 설정
                StringBuilder insertSQL = new StringBuilder(insertSQLTemplate);

                for (String value : values) {
                    if (value.trim().isEmpty()) { // 비어있는 값 처리
                        insertSQL.append("NULL,");
                    } else {
                        insertSQL.append("'").append(value.trim().replace("'", "''")).append("',");
                    }
                }
                insertSQL.deleteCharAt(insertSQL.length() - 1); // 마지막 콤마 제거
                insertSQL.append(")");

                try {
                    statement.executeUpdate(insertSQL.toString());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            logger.info("Table에 데이터 삽입 완료: {}", tableName);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (br != null) br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
