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
        Statement statement = null;

        try{
            // Q : try문 밖에다 선언했던 이유...
            connection = DriverManager.getConnection(jdbcURL, username, password);
            br = new BufferedReader(new FileReader(csvFilePath));

            statement = connection.createStatement();

            createDatabase(statement, databaseName);

            connection.setCatalog(databaseName);
            createTable(statement, br, tableName);

            br.close(); //닫고 다시 열어서 데이터 삽입
            br = new BufferedReader(new FileReader(csvFilePath));
            br.readLine();
            insertData(connection, br, tableName);

            logger.info("Data 처리 완료");
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                statement.close();
                connection.close();
                br.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    //DB 생성
    private static void createDatabase(Statement statement, String databaseName){ //throws Exception 권장X
        String createDatabaseSQL = "CREATE DATABASE IF NOT EXISTS " + databaseName;
        try {
            statement.executeUpdate(createDatabaseSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            try{
                statement.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        logger.info("DB 생성 {}", databaseName);
    }

    //테이블 생성
    private static void createTable(Statement statement, BufferedReader br, String tableName) {
        String headerLine = null;
        try {
            headerLine = br.readLine();
            String[] columns = headerLine.split(",");
            StringBuilder createTableSQL = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");

            for (String column : columns) {
                createTableSQL.append(column.trim()).append(" VARCHAR(255),"); // 모든 컬럼 varchar로 -> 숫자 반영 추가
            }
            createTableSQL.deleteCharAt(createTableSQL.length() - 1); // 제일 끝 콤마 제거
            createTableSQL.append(")");

            statement.executeUpdate(createTableSQL.toString());
            logger.info("Table 생성 {}", tableName);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (statement != null) statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void insertData(Connection connection, BufferedReader br, String tableName) {
        String insertSQLTemplate = "INSERT INTO " + tableName + " VALUES (";
        Statement statement = null;

        try {
            statement = connection.createStatement();
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                StringBuilder insertSQL = new StringBuilder(insertSQLTemplate);

                // 각 값에 대해 처리
                for (String value : values) {
                    insertSQL.append("'").append(value.trim().replace("'", "''")).append("',");
                }
                insertSQL.deleteCharAt(insertSQL.length() - 1); // 마지막 콤마 제거
                insertSQL.append(")");

                try {
                    statement.executeUpdate(insertSQL.toString());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            logger.info("Table에 Data 삽입 완료: {}", tableName);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            logger.error("Statement 생성 중 오류 발생", e); //SQLException
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
