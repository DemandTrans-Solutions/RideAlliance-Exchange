package com.clearinghouse.tools;

import com.clearinghouse.dao.TripTicketDAO;
import com.clearinghouse.dto.DateReadMismatchDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfEnvironmentVariable(named = "RUN_PROD_EXPORT", matches = "true")
@TestPropertySource(properties = {
        // JDBC overrides must be provided via environment variables when running this test
        // Example: JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD
        "jdbc.driverClassName=com.mysql.cj.jdbc.Driver",
        "jdbc.url=${JDBC_URL}",
        "jdbc.username=${JDBC_USERNAME}",
        "jdbc.password=${JDBC_PASSWORD}",
        // Hibernate basics
        "hibernate.dialect=org.hibernate.dialect.MySQL8Dialect",
        "hibernate.show_sql=false",
        "hibernate.format_sql=true"
})
public class DateMismatchExportTest {

    @Autowired
    private TripTicketDAO tripTicketDAO;

    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Test
    void exportMismatchesToCsv() throws IOException {
        // Optional controls via env vars
        String idEnv = System.getenv("TRIP_TICKET_ID");
        String maxRowsEnv = System.getenv("MAX_ROWS");
        int maxRows = (maxRowsEnv == null || maxRowsEnv.isBlank()) ? 10000 : Integer.parseInt(maxRowsEnv.trim());

        List<DateReadMismatchDTO> mismatches;
        if (idEnv != null && !idEnv.isBlank()) {
            int id = Integer.parseInt(idEnv.trim());
            mismatches = tripTicketDAO.findDateReadMismatchesById(id);
        } else {
            mismatches = tripTicketDAO.findDateReadMismatches(maxRows);
        }

        Path out = Path.of("target", "date_mismatches.csv");
        Files.createDirectories(out.getParent());
        try (BufferedWriter w = Files.newBufferedWriter(out)) {
            // Header
            w.write(String.join(",",
                    "TripTicketID",
                    "RequestedPickupDate_Local",
                    "RequestedPickupDate_DB",
                    "RequestedDropoffDate_Local",
                    "RequestedDropoffDate_DB",
                    "AddedOn",
                    "UpdatedOn"
            ));
            w.newLine();

            for (var m : mismatches) {
                String addedOn = m.getAddedOn() != null ? TS_FMT.format(m.getAddedOn()) : "";
                String updatedOn = m.getUpdatedOn() != null ? TS_FMT.format(m.getUpdatedOn()) : "";
                String rpLocal = m.getRequestedPickupDateLocal() != null ? m.getRequestedPickupDateLocal().toString() : "";
                String rdLocal = m.getRequestedDropoffDateLocal() != null ? m.getRequestedDropoffDateLocal().toString() : "";
                String rpStr = m.getRequestedPickupDateString() != null ? m.getRequestedPickupDateString() : "";
                String rdStr = m.getRequestedDropoffDateString() != null ? m.getRequestedDropoffDateString() : "";

                String line = String.join(",",
                        Integer.toString(m.getTripTicketId()),
                        escapeCsv(rpLocal),
                        escapeCsv(rpStr),
                        escapeCsv(rdLocal),
                        escapeCsv(rdStr),
                        escapeCsv(addedOn),
                        escapeCsv(updatedOn)
                );
                w.write(line);
                w.newLine();
            }
        }

        // Basic assertion so the test registers as executed
        assertThat(Files.exists(out)).isTrue();
    }

    private static String escapeCsv(String s) {
        if (s == null) return "";
        // Simple escaping: wrap in quotes if contains comma or quote; double the quotes
        boolean needsQuoting = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String v = s.replace("\"", "\"\"");
        return needsQuoting ? "\"" + v + "\"" : v;
    }
}
