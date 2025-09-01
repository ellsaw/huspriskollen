package com.scraper.model.DatabaseInteraction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import io.github.cdimascio.dotenv.Dotenv;

public class PostgreSQLGateway {
    private Connection connection;

    private PostgreSQLGateway(Connection connection) {
        this.connection = connection;
    }

    public static PostgreSQLGateway initialise(String propertyType) {
        try {
            Dotenv dotenv = Dotenv.load();

            final String JDBC_URL = dotenv.get("JDBC_URL");

            Connection connection = DriverManager.getConnection(JDBC_URL);

            createTable(connection, propertyType);

            return new PostgreSQLGateway(connection);

        } catch (Exception e) {
            System.err.println("Database initialization error: " + e);
            System.exit(1);
            return null;

        }
    }

    private static void createTable(Connection connection, String propertyType) throws SQLException {
        String sqlStatement;

        switch (propertyType) {
            case "Villa":
                sqlStatement = "CREATE TABLE IF NOT EXISTS VILLA (id SERIAL PRIMARY KEY, latitude DOUBLE PRECISION, longitude DOUBLE PRECISION, price INTEGER, living_area_metres_squared INTEGER, age_years INTEGER, maintainance_cost_per_month INTEGER, additional_area_metres_squared INTEGER, plot_area_metres_squared INTEGER, date_unix INTEGER)";
                break;

            default:
                throw new IllegalArgumentException("Invalid property type");
        }

        Statement statement = connection.createStatement();

        statement.execute(sqlStatement);
    }

    public void writeVilla(double latitude, double longitude, int price, int livingAreaMetresSquared, int ageYears,
            int maintenanceCostPerMonth, int additionalAreaMetresSquared, int plotAreaMetresSquared, int dateUnix) {
        try {
            String sqlStatement = "INSERT INTO VILLA (latitude, longitude, price, living_area_metres_squared, age_years, maintainance_cost_per_month, additional_area_metres_squared, plot_area_metres_squared, date_unix) values (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement);
            preparedStatement.setDouble(1, latitude);
            preparedStatement.setDouble(2, longitude);
            preparedStatement.setInt(3, price);
            preparedStatement.setInt(4, livingAreaMetresSquared);
            preparedStatement.setInt(5, ageYears);
            preparedStatement.setInt(6, maintenanceCostPerMonth);
            preparedStatement.setInt(7, additionalAreaMetresSquared);
            preparedStatement.setInt(8, plotAreaMetresSquared);
            preparedStatement.setInt(9, dateUnix);

            preparedStatement.execute();

            System.out.println("Write successfull (Villa)");
        } catch (Exception e) {
            throw new RuntimeException("Database writing error: " + e);
        }

    }
}
