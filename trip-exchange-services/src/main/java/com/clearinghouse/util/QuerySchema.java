package com.clearinghouse.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class QuerySchema {
    public static void main(String[] args) {
        String url = "jdbc:mysql://trip-exchange-mysql.cp4faxipyhv1.us-east-1.rds.amazonaws.com:3306/clearinghouse?useSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift=false&useLegacyDatetimeCode=false&serverTimezone=America/Denver&connectionTimeZone=America/Denver";
        String user = "admin";
        String password = "TripXChange0108";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            System.out.println("=== TRIPTICKET TABLE SCHEMA ===");
            ResultSet rs = stmt.executeQuery("DESCRIBE tripticket");
            while (rs.next()) {
                System.out.printf("%-40s %-20s%n",
                    rs.getString("Field"),
                    rs.getString("Type"));
            }
            rs.close();
            
            System.out.println("\n=== TRIPRESULT TABLE SCHEMA ===");
            ResultSet rs3 = stmt.executeQuery("DESCRIBE tripresult");
            while (rs3.next()) {
                System.out.printf("%-40s %-20s%n",
                    rs3.getString("Field"),
                    rs3.getString("Type"));
            }
            rs3.close();
            
            System.out.println("\n=== SAMPLE DATA FROM TRIPTICKET 1733 ===");
            ResultSet rs2 = stmt.executeQuery(
                "SELECT TripTicketID, RequestedDropOffDate, RequestedPickupDate, " +
                "CustomerDisability, AddedOn, UpdatedOn " +
                "FROM tripticket WHERE TripTicketID = 1733");
            while (rs2.next()) {
                System.out.println("TripTicketID: " + rs2.getInt("TripTicketID"));
                System.out.println("RequestedDropOffDate: " + rs2.getString("RequestedDropOffDate"));
                System.out.println("RequestedPickupDate: " + rs2.getString("RequestedPickupDate"));
                System.out.println("CustomerDisability: " + rs2.getString("CustomerDisability"));
                System.out.println("AddedOn: " + rs2.getString("AddedOn"));
                System.out.println("UpdatedOn: " + rs2.getString("UpdatedOn"));
            }
            rs2.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
