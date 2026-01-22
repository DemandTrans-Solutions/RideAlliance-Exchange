/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dao;


import com.clearinghouse.dto.PaginationDTO;
import com.clearinghouse.dto.DateReadMismatchDTO;
import com.clearinghouse.dto.ProvidersWeeklyReportDTO;
import com.clearinghouse.dto.TripTicketDTO;
import com.clearinghouse.dto.TripTicketDistanceDTO;
import com.clearinghouse.entity.ProviderCost;
import com.clearinghouse.entity.TripTicket;
import com.clearinghouse.entity.TripTicketDistance;
import com.clearinghouse.enumentity.TripTicketStatusConstants;
import com.clearinghouse.exceptions.InvalidInputException;
import com.clearinghouse.exceptions.NoInternetConnectionException;
import com.clearinghouse.listresponseentity.ProviderList;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.Time;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;


/**
 * @author chaitanyaP
 */
@Repository
@AllArgsConstructor
@Slf4j
public class TripTicketDAO extends AbstractDAO<Integer, TripTicket> {

    private static final String API_VERSION = "1.0";
    private static final String TRAVEL_MODE = "car";
    private static final double METERS_TO_MILES = 1609.34;
    private static final int SECONDS_PER_HOUR = 3600;
    private static final int SECONDS_PER_MINUTE = 60;
    private final Environment environment;
    private final ProviderDAO providerDao;

    public List<TripTicket> findAllTripTickets() {

        List<TripTicket> tripTickets = getEntityManager().createQuery(
                        " SELECT DISTINCT t FROM TripTicket t LEFT JOIN FETCH t.tripClaims tc LEFT JOIN FETCH tc.claimantProvider cp WHERE t.tripTicketInvisible=:isTripTicketInvisible AND t.status.statusId NOT IN(:status) ORDER BY t.requestedPickupDate ASC ,t.requestedPickupTime ASC ")
                .setParameter("isTripTicketInvisible", false)
                .setParameter("status", TripTicketStatusConstants.noShow.tripTicketStatusUpdate()).getResultList();
        return tripTickets;
    }


    public List<TripTicket> findAllDetailedTicketsForAdapter(String timestamp, int providerId) {
        if (timestamp != null) {
            /**
             * converting UTC timestamp to GMT-6(DENVER) date time
             */
            long updated_since_in_premivitve_type = Long.parseLong(timestamp);
            LocalDateTime localDateTime = Instant.ofEpochMilli(updated_since_in_premivitve_type * 1000L)
                    .atZone(TimeZone.getTimeZone(environment.getRequiredProperty("timezone.syncAPI")).toZoneId())
                    .toLocalDateTime();

            // Compare as a ZonedDateTime to the entity's updatedAt (which is stored as a timestamp with timezone)
            ZonedDateTime updatedSince = localDateTime.atZone(TimeZone.getTimeZone(environment.getRequiredProperty("timezone.syncAPI")).toZoneId());

            TypedQuery<TripTicket> query = getEntityManager()
                    .createQuery("SELECT DISTINCT t FROM TripTicket AS t LEFT JOIN FETCH t.tripClaims tc LEFT JOIN FETCH tc.claimantProvider cp " +
                            "WHERE ((t.originProvider.providerId = :providerId OR cp.providerId = :providerId) " +
                            "AND t.updatedAt > :updatedSince) " +
                            "ORDER BY (CASE WHEN t.requestedPickupDate IS NULL THEN 1 ELSE 0 END), t.requestedPickupDate ASC, " +
                            "(CASE WHEN t.requestedPickupTime IS NULL THEN 1 ELSE 0 END), t.requestedPickupTime ASC, t.requestedDropoffDate ASC, t.requestedDropOffTime", TripTicket.class);
            query.setParameter("providerId", providerId);
            query.setParameter("updatedSince", updatedSince);

            List<TripTicket> tripTickets = query.getResultList();
            return tripTickets;

        }
        /*timestamp is optional parameter*/

        String lastSyncDateTimeforProvider = providerDao.getProviderLastSyncDateTime(providerId);
        if (lastSyncDateTimeforProvider != null) {
            LocalDateTime getLastUpdatedDate = LocalDateTime.parse(lastSyncDateTimeforProvider);
            //if sync API call first time by provider then lastSyncDatetime is null, fetch all tripticket
            List<TripTicket> tripTickets = getEntityManager()

                    .createQuery(" SELECT DISTINCT(t) FROM TripTicket AS t LEFT JOIN FETCH t.tripClaims tc LEFT JOIN FETCH tc.claimantProvider cp WHERE ( t.originProvider.providerId =" + providerId + " OR  tc.claimantProvider.providerId =" + providerId + ")  AND  (t.updatedAt > '" + getLastUpdatedDate + "') ORDER BY (CASE WHEN t.requestedPickupDate IS NULL THEN 1 ELSE 0 END), t.requestedPickupDate ASC, (CASE WHEN t.requestedPickupTime IS NULL THEN 1 ELSE 0 END), t.requestedPickupTime ASC,t.requestedDropoffDate ASC,t.requestedPickupTime")
                    .getResultList();
            return tripTickets;
        } else {
            List<TripTicket> tripTickets = getEntityManager()

                    .createQuery(" SELECT DISTINCT t FROM TripTicket AS t LEFT JOIN FETCH t.tripClaims tc LEFT JOIN FETCH tc.claimantProvider cp WHERE ( t.originProvider.providerId =" + providerId + " OR  tc.claimantProvider.providerId =" + providerId + ")  ORDER BY (CASE WHEN t.requestedPickupDate IS NULL THEN 1 ELSE 0 END), t.requestedPickupDate ASC, (CASE WHEN t.requestedPickupTime IS NULL THEN 1 ELSE 0 END), t.requestedPickupTime ASC,t.requestedDropoffDate ASC,t.requestedDropOffTime")
                    .getResultList();
            return tripTickets;
        }
    }


    public List<TripTicket> findAllDetailedTripTickets(String timestamp) {

        if (timestamp != null) {

            long updated_since_in_premivitve_type = Long.parseLong(timestamp);
            LocalDateTime dateLocal = Instant.ofEpochMilli(updated_since_in_premivitve_type * 1000).atZone(ZoneId.systemDefault()).toLocalDateTime();

            ZonedDateTime dateTime = dateLocal.atZone(ZoneOffset.of(environment.getRequiredProperty("timezone.syncAPI")));
            List<TripTicket> tripTickets = getEntityManager()
                    .createQuery(" SELECT DISTINCT t FROM TripTicket t LEFT JOIN FETCH t.tripClaims tc LEFT JOIN FETCH tc.claimantProvider cp where t.tripTicketInvisible=:isTripTicketInvisible and t.status.statusId NOT IN(:status) AND t.updatedAt >=:timestamp  ORDER BY t.requestedPickupDate ASC ,t.requestedPickupTime ASC")
                    .setParameter("isTripTicketInvisible", false)
                    .setParameter("status", TripTicketStatusConstants.noShow.tripTicketStatusUpdate())
                    .setParameter("timestamp", dateTime)
                    .getResultList();
            return tripTickets;

        }

        List<TripTicket> tripTickets = findAllTripTickets();

        return tripTickets;

    }


    public List<TripTicket> findAllTripTicketsByTripIds(List<Integer> tripIds) {
        if (tripIds == null || tripIds.isEmpty()) {
            return new ArrayList<>();
        }
        TypedQuery<TripTicket> query = getEntityManager()
                .createQuery("SELECT DISTINCT t FROM TripTicket t LEFT JOIN FETCH t.tripClaims tc LEFT JOIN FETCH tc.claimantProvider WHERE t.id IN (:ids)", TripTicket.class)
                .setParameter("ids", tripIds);
        return query.getResultList();
    }


    public TripTicket findByOriginTripId(String id) {
        // Use JOIN FETCH to ensure tripClaims and claimantProvider are loaded to avoid lazy-init later
        TypedQuery<TripTicket> query = getEntityManager()
                .createQuery("SELECT t FROM TripTicket t LEFT JOIN FETCH t.tripClaims tc LEFT JOIN FETCH tc.claimantProvider cp WHERE t.requesterTripId = :id", TripTicket.class)
                .setParameter("id", id);
        List<TripTicket> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public TripTicket findTripTicketByTripTicketId(int id) {
        // Use JOIN FETCH to ensure tripClaims and claimantProvider are loaded to avoid lazy-init later
        TypedQuery<TripTicket> query = getEntityManager()
                .createQuery("SELECT t FROM TripTicket t LEFT JOIN FETCH t.tripClaims tc LEFT JOIN FETCH tc.claimantProvider cp WHERE t.id = :id", TripTicket.class)
                .setParameter("id", id);
        List<TripTicket> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Compare JDBC-read LocalDate vs server-cast string for requested dates, returning mismatches.
     * This reads DATE columns and their CAST(... AS CHAR) counterparts in one roundtrip, so we can
     * detect rows where LocalDate.toString() differs from server string (raw DB value).
     *
     * @param maxRows limit of rows to scan (applied via setMaxResults)
     * @return list of mismatches
     */
    public List<DateReadMismatchDTO> findDateReadMismatches(int maxRows) {
        String sql = "SELECT TripTicketID, " +
                "RequestedPickupDate AS rp_date, CAST(RequestedPickupDate AS CHAR) AS rp_str, " +
                "RequestedDropOffDate AS rd_date, CAST(RequestedDropOffDate AS CHAR) AS rd_str, " +
                "AddedOn, UpdatedOn " +
                "FROM tripticket ORDER BY UpdatedOn DESC";

        Query q = getEntityManager().createNativeQuery(sql);
        if (maxRows > 0) {
            q.setMaxResults(maxRows);
        }

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<DateReadMismatchDTO> out = new ArrayList<>();
        for (Object[] r : rows) {
            int idx = 0;
            Integer id = (Integer) r[idx++];
            java.sql.Date rpDateSql = (java.sql.Date) r[idx++];
            String rpStr = (String) r[idx++];
            java.sql.Date rdDateSql = (java.sql.Date) r[idx++];
            String rdStr = (String) r[idx++];
            java.sql.Timestamp addedTs = (java.sql.Timestamp) r[idx++];
            java.sql.Timestamp updatedTs = (java.sql.Timestamp) r[idx++];

            LocalDate rpLocal = rpDateSql != null ? rpDateSql.toLocalDate() : null;
            LocalDate rdLocal = rdDateSql != null ? rdDateSql.toLocalDate() : null;
            String rpLocalStr = rpLocal != null ? rpLocal.toString() : null;
            String rdLocalStr = rdLocal != null ? rdLocal.toString() : null;

            boolean pickupMismatch = rpLocal != null && rpStr != null && !rpLocalStr.equals(rpStr);
            boolean dropoffMismatch = rdLocal != null && rdStr != null && !rdLocalStr.equals(rdStr);

            if (pickupMismatch || dropoffMismatch) {
                out.add(new DateReadMismatchDTO(
                        id,
                        rpLocal,
                        rpStr,
                        rdLocal,
                        rdStr,
                        addedTs != null ? addedTs.toLocalDateTime() : null,
                        updatedTs != null ? updatedTs.toLocalDateTime() : null
                ));
            }
        }
        return out;
    }

    /**
     * Same as findDateReadMismatches, but constrained to a specific TripTicketID.
     * Returns data for the ticket even if there's no mismatch (for diagnostics).
     */
    public List<DateReadMismatchDTO> findDateReadMismatchesById(int tripTicketId) {
        String sql = "SELECT TripTicketID, " +
                "RequestedPickupDate AS rp_date, CAST(RequestedPickupDate AS CHAR) AS rp_str, " +
                "RequestedDropOffDate AS rd_date, CAST(RequestedDropOffDate AS CHAR) AS rd_str, " +
                "AddedOn, UpdatedOn " +
                "FROM tripticket WHERE TripTicketID = :id";

        Query q = getEntityManager().createNativeQuery(sql);
        q.setParameter("id", tripTicketId);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<DateReadMismatchDTO> out = new ArrayList<>();
        for (Object[] r : rows) {
            int idx = 0;
            Integer id = (Integer) r[idx++];
            java.sql.Date rpDateSql = (java.sql.Date) r[idx++];
            String rpStr = (String) r[idx++];
            java.sql.Date rdDateSql = (java.sql.Date) r[idx++];
            String rdStr = (String) r[idx++];
            java.sql.Timestamp addedTs = (java.sql.Timestamp) r[idx++];
            java.sql.Timestamp updatedTs = (java.sql.Timestamp) r[idx++];

            LocalDate rpLocal = rpDateSql != null ? rpDateSql.toLocalDate() : null;
            LocalDate rdLocal = rdDateSql != null ? rdDateSql.toLocalDate() : null;
            String rpLocalStr = rpLocal != null ? rpLocal.toString() : null;
            String rdLocalStr = rdLocal != null ? rdLocal.toString() : null;

            boolean pickupMismatch = rpLocal != null && rpStr != null && !rpLocalStr.equals(rpStr);
            boolean dropoffMismatch = rdLocal != null && rdStr != null && !rdLocalStr.equals(rdStr);

            // ForById query, return data even if no mismatch (for diagnostics)
            out.add(new DateReadMismatchDTO(
                    id,
                    rpLocal,
                    rpStr,
                    rdLocal,
                    rdStr,
                    addedTs != null ? addedTs.toLocalDateTime() : null,
                    updatedTs != null ? updatedTs.toLocalDateTime() : null
            ));
        }
        return out;
    }



    public TripTicket createTripTicket(TripTicket tripTicket) {
        log.info("[DAO-BEFORE-INSERT] pickupDate={}, class={}, dropoffDate={}, class={}", 
            tripTicket.getRequestedPickupDate(), 
            tripTicket.getRequestedPickupDate() != null ? tripTicket.getRequestedPickupDate().getClass().getName() : "null",
            tripTicket.getRequestedDropoffDate(),
            tripTicket.getRequestedDropoffDate() != null ? tripTicket.getRequestedDropoffDate().getClass().getName() : "null");
        add(tripTicket);
        log.info("[DAO-AFTER-INSERT] ID={}, pickupDate={}, dropoffDate={}", 
            tripTicket.getId(),
            tripTicket.getRequestedPickupDate(),
            tripTicket.getRequestedDropoffDate());
        
        // Verify what was actually inserted into the database
        getEntityManager().flush();
        getEntityManager().clear();
        TripTicket verified = getEntityManager().find(TripTicket.class, tripTicket.getId());
        log.info("[DAO-VERIFIED-FROM-DB] ID={}, pickupDate={}, dropoffDate={}", 
            verified.getId(),
            verified.getRequestedPickupDate(),
            verified.getRequestedDropoffDate());
        
        return tripTicket;
    }


    public TripTicket updateTripTicket(TripTicket tripTicket) {

        return update(tripTicket);

    }


    public void deleteTripTicketByTripTicketId(int id) {
        TripTicket tripTicket = (TripTicket) getEntityManager()
                .createQuery("SELECT t FROM TripTicket t LEFT JOIN FETCH t.tripClaims tc LEFT JOIN FETCH tc.claimantProvider cp WHERE t.id = :id ")
                .setParameter("id", id)
                .getSingleResult();
        tripTicket.setExpired(true);
    }

    public List<TripTicket> findAllTickets() {
        return getEntityManager()
                .createQuery("SELECT DISTINCT t FROM TripTicket t LEFT JOIN FETCH t.tripClaims tc LEFT JOIN FETCH tc.claimantProvider WHERE t.tripTicketInvisible=:isTripTicketInvisible " +
                        "AND t.status.statusId NOT IN(:status) " +
                        "ORDER BY (CASE WHEN t.requestedPickupDate IS NULL THEN 1 ELSE 0 END), t.requestedPickupDate ASC, (CASE WHEN t.requestedPickupTime IS NULL THEN 1 ELSE 0 END), t.requestedPickupTime ASC")
                .setParameter("isTripTicketInvisible", false)
                .setParameter("status", TripTicketStatusConstants.noShow.tripTicketStatusUpdate())
                .getResultList();
    }

    public List<TripTicket> findAllTripTicketsByProvider(int providerId) {
        return getEntityManager()
                .createQuery("SELECT DISTINCT t FROM TripTicket t LEFT JOIN FETCH t.tripClaims tc LEFT JOIN FETCH tc.claimantProvider WHERE t.tripTicketInvisible=false " +
                        "AND t.status.statusId NOT IN(11) " +
                        "AND t.originProvider.providerId=:providerId " +
                        "ORDER BY ISNULL(t.requestedPickupDate), t.requestedPickupDate ASC, " +
                        "ISNULL(t.requestedPickupTime), t.requestedPickupTime ASC, " +
                        "t.requestedDropoffDate ASC, t.requestedDropOffTime")
                .setParameter("providerId", providerId)
                .getResultList();
    }

    public List<TripTicket> findAllTicketsWithpagination(PaginationDTO paginationDTO) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<TripTicket> criteriaQuery = criteriaBuilder.createQuery(TripTicket.class);
        Root<TripTicket> from = criteriaQuery.from(TripTicket.class);
        CriteriaQuery<TripTicket> select = criteriaQuery.select(from);

        // Apply sorting based on paginationDTO
        if (paginationDTO.getSortField() != null && !paginationDTO.getSortField().isEmpty()) {
            if (paginationDTO.getSortField().contains(".")) {
                // Handle nested properties (e.g., "originProvider.providerId")
                String[] parts = paginationDTO.getSortField().split("\\.");
                if (paginationDTO.getSortOrder() == 1) {
                    select.orderBy(criteriaBuilder.asc(from.get(parts[0]).get(parts[1])));
                } else {
                    select.orderBy(criteriaBuilder.desc(from.get(parts[0]).get(parts[1])));
                }
            } else {
                // Handle simple properties
                if (paginationDTO.getSortOrder() == 1) {
                    select.orderBy(criteriaBuilder.asc(from.get(paginationDTO.getSortField())));
                } else {
                    select.orderBy(criteriaBuilder.desc(from.get(paginationDTO.getSortField())));
                }
            }
        } else {
            // Default sorting if no sortField provided
            select.orderBy(
                    criteriaBuilder.asc(from.get("requestedPickupDate")),
                    criteriaBuilder.asc(from.get("requestedDropoffDate"))
            );
        }

        TypedQuery<TripTicket> typedQuery = entityManager.createQuery(select);
        typedQuery.setFirstResult((paginationDTO.getCurrentPageNumber() - 1) * paginationDTO.getPageSize());
        typedQuery.setMaxResults(paginationDTO.getPageSize());

        return typedQuery.getResultList();
    }


    public long getTotalcountOftickets() {

        Query query = getEntityManager()
                .createQuery("SELECT COUNT(t.id) FROM  TripTicket t ");
        long count = (long) query.getSingleResult();
        return count;

    }



    public List<TripTicket> getAvailableTripTickets() {

        List<TripTicket> tripTickets = getEntityManager()
                .createQuery("SELECT DISTINCT t FROM  TripTicket t  LEFT JOIN FETCH t.tripClaims tc LEFT JOIN FETCH tc.claimantProvider WHERE  t.status.statusId =:statusId ")
                .setParameter("statusId", TripTicketStatusConstants.available.tripTicketStatusUpdate())
                .getResultList();
        return tripTickets;
    }


    public List<TripTicket> findAllTripTicketsByOriginatorProviderList(List<ProviderList> providerList) {

        String queryString = "SELECT DISTINCT t FROM TripTicket t LEFT JOIN FETCH t.tripClaims tc LEFT JOIN FETCH tc.claimantProvider where t.tripTicketInvisible=false ";
        String attachedStringForId = " ";
        int sizeOfList = providerList.size();
        int i = 0;
        if (sizeOfList > 0) {
            queryString += " AND ";
        }
        for (ProviderList listParam : providerList) {
            i++;
            attachedStringForId = attachedStringForId + "t.originProvider.providerId=" + (listParam.getProviderId());

            if (i < sizeOfList) {
                attachedStringForId = attachedStringForId + " OR ";
            } else {
                attachedStringForId = "(" + attachedStringForId + ")";
            }

        }
        queryString = queryString + attachedStringForId;

        entityManager = getEntityManager();
        Query query = entityManager.createQuery(queryString + " ORDER BY t.requestedPickupDate DESC , t.requestedPickupTime ASC ");
        List<TripTicket> tripTickets = query.getResultList();
        return tripTickets;
    }


    public List<TripTicket> findAllTripTicketsByOriginatorPrividerId(int providerId) {

        List<TripTicket> tripTickets = getEntityManager()
                .createQuery(" SELECT DISTINCT t FROM TripTicket t LEFT JOIN FETCH t.tripClaims tc LEFT JOIN FETCH tc.claimantProvider WHERE t.tripTicketInvisible=false AND t.status.statusId NOT IN(11) AND t.originProvider.providerId=:providerId ORDER BY ISNULL(t.requestedPickupDate), t.requestedPickupDate ASC ,ISNULL(t.requestedPickupTime),t.requestedPickupTime ASC,t.requestedDropoffDate ASC,t.requestedDropOffTime")
                .setParameter("providerId", providerId)
                .getResultList();
        return tripTickets;

    }

    public TripTicket findTripTicketsByClaimantProviderOriginTripId(int claimantProviderId, String originTripId) {

        List<TripTicket> results = getEntityManager()
                .createQuery(" SELECT DISTINCT t FROM TripTicket t LEFT JOIN FETCH t.tripClaims tc LEFT JOIN FETCH tc.claimantProvider WHERE  t.requesterTripId =: originTripId AND t.tripTicketInvisible=false AND t.status.statusId NOT IN(11) AND tc.claimantProvider.id=:providerId ORDER BY ISNULL(t.requestedPickupDate), t.requestedPickupDate ASC ,ISNULL(t.requestedPickupTime),t.requestedPickupTime ASC,t.requestedDropoffDate ASC,t.requestedDropOffTime")
                .setParameter("providerId", claimantProviderId)
                .setParameter("originTripId", originTripId)
                .getResultList();

        return results.isEmpty() ? null : results.get(0);
    }



    //new added by shankarI
    public Integer findLatestAddressForCustomer() {
        Query query = getEntityManager()
                .createQuery("SELECT MAX(t.customerAddress.addressId) FROM  TripTicket t");
        Integer custAddrsId = (Integer) query.getSingleResult();
        return custAddrsId;
    }


    public TripTicket getFundingSourcebyTripTicketId(int id) {

        TripTicket tripTicket = (TripTicket) getEntityManager()
                .createQuery("SELECT t FROM TripTicket t LEFT JOIN FETCH t.tripClaims tc LEFT JOIN FETCH tc.claimantProvider cp WHERE t.id = :id ")
                .setParameter("id", id)
                .getSingleResult();
        return tripTicket;

    }

    //newly added

    public TripTicketDistanceDTO convertLatLongToGetTimeAndDistance(TripTicketDTO tripTicket) {
        HttpURLConnection connection = null;
        try {
            URL url = buildAzureApiUrl(tripTicket);
            String jsonResponse = fetchDataFromApi(url);
            return processJsonResponse(jsonResponse);
        } catch (UnknownHostException e) {
            throw new NoInternetConnectionException("No internet connection.");
        } catch (Exception e) {
            log.error("Error processing trip ticket distance using unpopulated ticket distance", e);

            var result = new TripTicketDistanceDTO();
            result.setTripTicketDistance(convertMetersToMiles(0));
            result.setTripTicketTime(convertSecondsToHours(0));
            result.setTimeInString(formatTravelTime(0));
            return result;
        }
    }

    private URL buildAzureApiUrl(TripTicketDTO tripTicket) throws MalformedURLException {
        var apiUrl = environment.getRequiredProperty("azureApiUrl");
        var subscriptionKey = environment.getRequiredProperty("subscriptionKey");
        var pickup = tripTicket.getPickupAddress();
        var dropOff = tripTicket.getDropOffAddress();

        return new URL(String.format("%ssubscription-key=%s&api-version=%s&query=%f,%f:%f,%f&travelMode=%s&computeTravelTimeFor=all",
                apiUrl,
                subscriptionKey,
                API_VERSION,
                pickup.getLatitude(),
                pickup.getLongitude(),
                dropOff.getLatitude(),
                dropOff.getLongitude(),
                TRAVEL_MODE));
    }


    private String fetchDataFromApi(URL url) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new InvalidInputException("The Lat-Long is invalid, Ticket is not created. Please try again.");
            }

            try (var reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                return reader.lines().collect(Collectors.joining());
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private TripTicketDistanceDTO processJsonResponse(String jsonResponse) {
        JSONObject json = new JSONObject(jsonResponse);
        if (json.isEmpty()) {
            throw new InvalidInputException("The Lat-Long is invalid, Ticket is not created. Please try again.");
        }

        JSONObject summary = json.getJSONArray("routes")
                .getJSONObject(0)
                .getJSONObject("summary");

        int lengthInMeters = summary.getInt("lengthInMeters");
        int travelTimeInSeconds = summary.getInt("historicTrafficTravelTimeInSeconds");

        var result = new TripTicketDistanceDTO();
        result.setTripTicketDistance(convertMetersToMiles(lengthInMeters));
        result.setTripTicketTime(convertSecondsToHours(travelTimeInSeconds));
        result.setTimeInString(formatTravelTime(travelTimeInSeconds));

        return result;
    }

    private float convertMetersToMiles(int meters) {
        return (float) (meters / METERS_TO_MILES);
    }

    private float convertSecondsToHours(int seconds) {
        return seconds / (float) SECONDS_PER_HOUR;
    }

    private String formatTravelTime(int seconds) {
        int totalMinutes = seconds / SECONDS_PER_MINUTE;
        int hours = totalMinutes / SECONDS_PER_MINUTE;
        int minutes = totalMinutes % SECONDS_PER_MINUTE;

        if (seconds < SECONDS_PER_HOUR) {
            return minutes + " Min.";
        }
        return hours + (hours < 2 ? " Hour  " : " Hours  ") + minutes + " Min.";
    }



    public List<ProvidersWeeklyReportDTO> getWeeklyTripTicketsRecords(String startDate, String endDate) {
        try {
            String query = "CALL SP_ProvidersWeeklyData(?1, ?2)";
            Query finalOutput = entityManager.createNativeQuery(query)
                    .setParameter(1, startDate)
                    .setParameter(2, endDate);
            List<Object[]> result = finalOutput.getResultList();
            List<ProvidersWeeklyReportDTO> providersWeeklyReportDTOList = new ArrayList<ProvidersWeeklyReportDTO>();
            for (Object[] obj : result) {
                ProvidersWeeklyReportDTO providersWeeklyReportDTO = new ProvidersWeeklyReportDTO();
                providersWeeklyReportDTO.setProviderId((Integer) obj[0]);
                providersWeeklyReportDTO.setProviderAs((String) obj[1]);
                providersWeeklyReportDTO.setTripticketId((Integer) obj[2]);
                providersWeeklyReportDTO.setDate((String) obj[3]);
                providersWeeklyReportDTO.setPickupDate((String) obj[4]);
                providersWeeklyReportDTO.setDropOffDate((String) obj[5]);
                providersWeeklyReportDTO.setPickupTime((Time) obj[6]);
                providersWeeklyReportDTO.setDropOffTime((Time) obj[7]);
                providersWeeklyReportDTO.setPickupAddress((String) obj[8]);
                providersWeeklyReportDTO.setDropOffAddress((String) obj[9]);
                providersWeeklyReportDTO.setDistance((BigDecimal) obj[10]);
                providersWeeklyReportDTO.setTime((String) obj[11]);
                providersWeeklyReportDTO.setFinalProposedCost((BigDecimal) obj[12]);
                providersWeeklyReportDTO.setFundingSource((String) obj[13]);
                providersWeeklyReportDTO.setStatus((String) obj[14]);
                providersWeeklyReportDTOList.add(providersWeeklyReportDTO);
            }
            return providersWeeklyReportDTOList;
        } catch (NoResultException e) {
            log.error("No result found for weekly trip tickets records between {} and {}", startDate, endDate, e);
            return null;
        }
    }


    public List<Integer> sendMailToProvidersWeeklyReportForNoAnyTransactions(String startDate, String endDate) {
        try {
            String query = "CALL SP_ProviderListWithNoTransactions(?1, ?2)";
            Query finalOutput = entityManager.createNativeQuery(query)
                    .setParameter(1, startDate)
                    .setParameter(2, endDate);
            List<Integer> result = finalOutput.getResultList();
            return result;
        } catch (NoResultException e) {
            log.error("No result found for provider list with no transactions between {} and {}", startDate, endDate, e);
            return null;
        }
    }


    public String getTripTicketStatusById(int statusId) {
        String tripTicketStutus = (String) getEntityManager()
                .createQuery("SELECT s.type FROM Status s WHERE s.statusId = :id ")
                .setParameter("id", statusId)
                .getSingleResult();
        return tripTicketStutus;
    }


    public boolean checkForTicketExistsByRequesterTripIdAndOriginProvider(TripTicketDTO tripTicketDTO) {
        boolean resultFlag = false;
        Query query = getEntityManager()
                .createQuery("SELECT COUNT(t.id) FROM  TripTicket t WHERE t.requesterTripId = :id and t.originProvider.providerId= :providerId")
                .setParameter("id", tripTicketDTO.getRequesterTripId())
                .setParameter("providerId", tripTicketDTO.getOriginProviderId());
        long count = (long) query.getSingleResult();
        if (count > 0) {
            resultFlag = true;
        }
        return resultFlag;
    }

    public List<String> listOfDistinctCustomerEligibility() {
        List<String> listOfDistinctCustomerEligibility = getEntityManager()
                .createQuery("SELECT DISTINCT t.customerEligibilityFactors FROM  TripTicket t order by t.customerEligibilityFactors ASC")
                .getResultList();
        return listOfDistinctCustomerEligibility;
    }


    public List<TripTicket> getExpiredTripTickets() {

        List<TripTicket> tripTickets = getEntityManager()
                .createQuery("SELECT t FROM TripTicket t  WHERE  t.status.statusId =:statusId AND t.tripTicketInvisible=false ")
                .setParameter("statusId", TripTicketStatusConstants.expired.tripTicketStatusUpdate()).getResultList();
        return tripTickets;
    }

    public Page<TripTicket> findAll(Pageable pageable) {
        // Get total count for pagination
        Query countQuery = getEntityManager()
                .createQuery("SELECT COUNT(t) FROM TripTicket t");
        Long count = (Long) countQuery.getSingleResult();

        // Get paginated data
        List<TripTicket> tripTickets = getEntityManager()
                .createQuery("SELECT t FROM TripTicket t ORDER BY (CASE WHEN t.requestedPickupDate IS NULL THEN 1 ELSE 0 END), " +
                        "t.requestedPickupDate ASC, (CASE WHEN t.requestedPickupTime IS NULL THEN 1 ELSE 0 END), " +
                        "t.requestedPickupTime ASC, t.requestedDropoffDate ASC, t.requestedDropOffTime")
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        return new PageImpl<>(tripTickets, pageable, count);
    }


    // @Override
    public void deleteById(int id) {
        TripTicket tripTicket = (TripTicket) getEntityManager()
                .createQuery("SELECT t FROM TripTicket t LEFT JOIN FETCH t.tripClaims tc LEFT JOIN FETCH tc.claimantProvider cp WHERE t.id = :id ")
                .setParameter("id", id)
                .getSingleResult();
        tripTicket.setExpired(true);
    }
}
