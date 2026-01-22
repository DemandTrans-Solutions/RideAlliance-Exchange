package com.clearinghouse.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Scans tripticket and compares raw DATE (as string) vs JDBC typed reads.
 * Writes mismatches to target/date_diff_tripticket.csv for inspection.
 *
 * Comparison columns:
 * - TripTicketID
 * - raw_pickup, jdbc_pickup_getObject, jdbc_pickup_getDate
 * - raw_dropoff, jdbc_dropoff_getObject, jdbc_dropoff_getDate
 * - pickup_diff, dropoff_diff (0, +1, -1, other)
 */
public class CompareDateReadPaths {
    private static final String URL = "jdbc:mysql://trip-exchange-mysql.cp4faxipyhv1.us-east-1.rds.amazonaws.com:3306/clearinghouse?useSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift=false&useLegacyDatetimeCode=false&serverTimezone=America/Denver&connectionTimeZone=America/Denver";
    private static final String USER = "admin";
    private static final String PASS = "TripXChange0108";

    public static void main(String[] args) throws Exception {
        Integer filterId = null;
        int limit = 1000000; // default large
        String outPath = "target/date_diff_tripticket.csv";
        if (args.length > 0) {
            if (args[0].startsWith("id=")) {
                filterId = Integer.parseInt(args[0].substring(3));
            } else {
                limit = Integer.parseInt(args[0]);
            }
        }
        if (args.length > 1) {
            outPath = args[1];
        }

        Files.createDirectories(Path.of("target"));
        try (BufferedWriter out = new BufferedWriter(new FileWriter(new File(outPath)))) {
            out.write("TripTicketID,raw_pickup,jdbc_pickup_getObject,jdbc_pickup_getDate,raw_dropoff,jdbc_dropoff_getObject,jdbc_dropoff_getDate,pickup_diff,dropoff_diff\n");

            try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
                final String sql = (filterId == null)
                        ? "SELECT TripTicketID, RequestedPickupDate, RequestedDropOffDate FROM tripticket ORDER BY TripTicketID ASC LIMIT ?"
                        : "SELECT TripTicketID, RequestedPickupDate, RequestedDropOffDate FROM tripticket WHERE TripTicketID = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    if (filterId == null) {
                        ps.setInt(1, limit);
                    } else {
                        ps.setInt(1, filterId);
                    }
                    ps.setFetchSize(500);
                    try (ResultSet rs = ps.executeQuery()) {
                        int mismatches = 0;
                        int total = 0;
                        while (rs.next()) {
                            total++;
                            int id = rs.getInt("TripTicketID");

                            // Raw as strings
                            String rawPickupStr = rs.getString("RequestedPickupDate");
                            String rawDropoffStr = rs.getString("RequestedDropOffDate");

                            LocalDate rawPickup = parseLocalDateOrNull(rawPickupStr);
                            LocalDate rawDrop = parseLocalDateOrNull(rawDropoffStr);

                            // JDBC typed reads
                            LocalDate objPickup = getLocalDate(rs, "RequestedPickupDate");
                            LocalDate objDrop = getLocalDate(rs, "RequestedDropOffDate");

                            LocalDate datePickup = getViaSqlDate(rs, "RequestedPickupDate");
                            LocalDate dateDrop = getViaSqlDate(rs, "RequestedDropOffDate");

                            Integer pickupDiff = diffDays(rawPickup, coalesce(objPickup, datePickup));
                            Integer dropDiff = diffDays(rawDrop, coalesce(objDrop, dateDrop));

                            boolean pickupMismatch = pickupDiff != null && pickupDiff != 0;
                            boolean dropMismatch = dropDiff != null && dropDiff != 0;

                            if (pickupMismatch || dropMismatch) {
                                mismatches++;
                                out.write(String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s\n",
                                        id,
                                        toStr(rawPickup), toStr(objPickup), toStr(datePickup),
                                        toStr(rawDrop), toStr(objDrop), toStr(dateDrop),
                                        toDiffStr(pickupDiff), toDiffStr(dropDiff)));
                            }
                        }
                        out.flush();
                        System.out.printf("Scanned %d rows. Found %d rows with date read mismatches. Output: %s%n", total, mismatches, outPath);
                    }
                }
            }
        }
    }

    private static LocalDate parseLocalDateOrNull(String s) {
        try { return s == null ? null : LocalDate.parse(s); } catch (Exception e) { return null; }
    }

    private static LocalDate getLocalDate(ResultSet rs, String col) {
        try { return rs.getObject(col, LocalDate.class); } catch (Exception e) { return null; }
    }

    private static LocalDate getViaSqlDate(ResultSet rs, String col) {
        try {
            Date d = rs.getDate(col);
            return d != null ? d.toLocalDate() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static <T> T coalesce(T a, T b) { return a != null ? a : b; }

    private static Integer diffDays(LocalDate a, LocalDate b) {
        if (a == null || b == null) return null;
        return (int) (b.toEpochDay() - a.toEpochDay());
    }

    private static String toStr(LocalDate d) { return d == null ? "" : d.toString(); }
    private static String toDiffStr(Integer d) { return d == null ? "" : d.toString(); }
}
