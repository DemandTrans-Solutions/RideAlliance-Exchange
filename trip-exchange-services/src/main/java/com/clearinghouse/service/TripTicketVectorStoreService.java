package com.clearinghouse.service;

import com.clearinghouse.dao.ReadOnlyTripTicketSummaryDAO;
import com.clearinghouse.dao.TripTicketDAO;
import com.clearinghouse.dto.TripTicketDTO;
import com.clearinghouse.entity.ReadOnlyTripTicketSummary;
import com.clearinghouse.entity.TripTicket;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class TripTicketVectorStoreService {


    private TripTicketDAO tripTicketDAO;

    private ReadOnlyTripTicketSummaryDAO readOnlyTripTicketSummaryDAO;

    private ModelMapper tripTicketModelMapper;

    private EmbeddingModel embeddingModel;

    private ObjectMapper objectMapper;

    private VectorStore vectorStore;

    private String createTripTicketSummary(TripTicketDTO dto) {
        return String.format(
                "TripTicket %d for %s %s: Pickup at %s, Dropoff at %s, Service: %s, Status: %s, Dropoff Date: %s",
                dto.getId(),
                dto.getCustomerFirstName(),
                dto.getCustomerLastName(),
                dto.getPickupAddress() != null ? dto.getPickupAddress().getStreet1() : "",
                dto.getDropOffAddress() != null ? dto.getDropOffAddress().getStreet1() : "",
                dto.getServiceLevel(),
                dto.getStatus() != null ? dto.getStatus().getType() : "",
                dto.getRequestedDropoffDate()
        );
    }

    private String createReadOnlyTripTicketSummary(ReadOnlyTripTicketSummary summary) {
        // Build a concise, human-readable summary string with each field trimmed to 15 chars
        return String.format(
                "TripTicket %d | CustNameFirst: %s | CustPhone: %s | Impairment: %s | Notes: %s | Seats: %s | Mobility: %s | ServiceAnimals: %s | TripFunders: %s | Assistance: %s | ServiceLevel: %s | ReqPickupDate: %s | ReqPickupTime: %s | ReqDropOffDate: %s | ReqDropOffTime: %s | EstTripDist: %s | EstTripTime: %s | TripIsolation: %s | ProvisionalTime: %s | Expiration: %s | Expired: %s | Rejected: %s | RejectReason: %s | Invisible: %s | ReqProvFare: %s | OriginProvName: %s | LastStatusProvName: %s | ProvisionalProvName: %s | ProvisionalProvEmail: %s | PickupStreet1: %s | PickupCity: %s | PickupZip: %s | PickupCommon: %s | PickupLat: %s | PickupLong: %s | DropStreet1: %s | DropCity: %s | DropZip: %s | DropCommon: %s | DropLat: %s | DropLong: %s | StatusDesc: %s | ApprovedClaimAck: %s | ApprovedClaimPickup: %s | ApprovedClaimProvFare: %s | ApprovedClaimCalcFare: %s | ApprovedClaimPropFare: %s | ApprovedClaimExp: %s | ApprovedClaimIsExp: %s | NoShow: %s | ActualPickupArrive: %s | ActualDropArrive: %s | FareCollected: %s | NumPassengers: %s | NumGuests: %s | NumAttendants: %s | Rate: %s | DriverName: %s | VehicleName: %s | FareType: %s | Fare: %s | MilesTraveled: %s | BillableMileage: %s | TripResultNotes: %s | TripResultOutcome: %s",
                summary.getTripTicketID(),
                trim((summary.getCustomerFirstName() + " " + summary.getCustomerLastName())),
                trim(summary.getCustomerHomePhone()),
                trim(summary.getImpairmentDescription()),
                trim(summary.getCustomerNotes()),
                trim(summary.getSeatsRequired()),
                trim(summary.getCustomerMobilityFactors()),
                trim(summary.getIsServiceAnimals()),
                trim(summary.getTripFunders()),
                trim(summary.getCustomerAssistanceNeeds()),
                trim(summary.getServiceLevel()),
                trim(summary.getRequestedPickupDate()),
                trim(summary.getRequestedPickupTime()),
                trim(summary.getRequestedDropOffDate()),
                trim(summary.getRequestedDropOffTime()),
                trim(summary.getEstimatedTripDistance()),
                trim(summary.getEstimatedTripTravelTime()),
                trim(summary.getIsTripIsolation()),
                trim(summary.getTripTicketProvisionalTime()),
                trim(summary.getExpirationDate()),
                trim(summary.getIsExpired()),
                trim(summary.getIsRejected()),
                trim(summary.getRejectedReason()),
                trim(summary.getIsInvisible()),
                trim(summary.getRequesterProviderFare()),
                trim(summary.getOriginProviderName()),
                trim(summary.getLastStatusChangedByProviderName()),
                trim(summary.getProvisionalProviderName()),
                trim(summary.getProvisionalProviderEmail()),
                trim(summary.getPickupAddressStreet1()),
                trim(summary.getPickupAddressCity()),
                trim(summary.getPickupAddressZipCode()),
                trim(summary.getPickupAddressCommonName()),
                trim(summary.getPickupAddressLatitude()),
                trim(summary.getPickupAddressLongitude()),
                trim(summary.getDropOffAddressStreet1()),
                trim(summary.getDropOffAddressCity()),
                trim(summary.getDropOffAddressZipCode()),
                trim(summary.getDropOffAddressCommonName()),
                trim(summary.getDropOffAddressLatitude()),
                trim(summary.getDropOffAddressLongitude()),
                trim(summary.getStatusDescription()),
                trim(summary.getApprovedTripClaimAcknowledgementStatus()),
                trim(summary.getApprovedTripClaimProposedPickupTime()),
                trim(summary.getApprovedTripClaimRequesterProviderFare()),
                trim(summary.getApprovedTripClaimCalculatedProposedFare()),
                trim(summary.getApprovedTripClaimProposedFare()),
                trim(summary.getApprovedTripClaimExpirationDate()),
                trim(summary.getApprovedTripClaimIsExpired()),
                trim(summary.getTripResultNoShowFlag()),
                trim(summary.getTripResultActualPickupArriveTime()),
                trim(summary.getTripResultActualDropOffArriveTime()),
                trim(summary.getTripResultFareCollected()),
                trim(summary.getTripResultNumberOfPassengers()),
                trim(summary.getTripResultNumberOfGuests()),
                trim(summary.getTripResultNumberOfAttendants()),
                trim(summary.getTripResultRate()),
                trim(summary.getTripResultDriverName()),
                trim(summary.getTripResultVehicleName()),
                trim(summary.getTripResultFareType()),
                trim(summary.getTripResultFare()),
                trim(summary.getTripResultMilesTraveled()),
                trim(summary.getTripResultBillableMileage()),
                trim(summary.getTripResultNotes()),
                trim(summary.getTripResultOutcome())
        );
    }

    private String trim(Object value) {
        if (value == null) return "";
        String str = value.toString();
        return str.length() > 15 ? str.substring(0, 15) : str;
    }


    @Transactional
    public void populateVectorStoreFromReadOnlySummaries() {
        List<ReadOnlyTripTicketSummary> summaries = readOnlyTripTicketSummaryDAO.findAll();

        for (var ticketSummary : summaries) {
            try {
                String summary = createReadOnlyTripTicketSummary(ticketSummary);
                String referenceId = getReferenceId(ticketSummary.getTripTicketID());
                Map<String, Object> metadata = Map.of("summary", summary);
                Document doc = new Document(referenceId, summary, metadata);
                vectorStore.add(List.of(doc));

                var entity = tripTicketDAO.findTripTicketByTripTicketId(ticketSummary.getTripTicketID());
                entity.setVectorStoreId(referenceId);
                tripTicketDAO.updateTripTicket(entity);
            } catch (Exception e) {
                log.error("Error processing ReadOnlyTripTicketSummary for vectorStore: {}", ticketSummary, e);
            }
        }
    }


    public void populateVectorStore() {
        List<TripTicket> tickets = tripTicketDAO.findAllTickets();
        List<TripTicketDTO> dtos = tickets.stream()
                .map(ticket -> tripTicketModelMapper.map(ticket, TripTicketDTO.class))
                .collect(Collectors.toList());
        for (TripTicketDTO dto : dtos) {
            try {
                String summary = createTripTicketSummary(dto);
                String referenceId = getReferenceId(dto.getId());
                Map<String, Object> metadata = Map.of("summary", summary);
                Document doc = new Document(referenceId, summary, metadata);
                vectorStore.add(List.of(doc));

                tickets.stream().filter(t -> Objects.equals(t.getId(), dto.getId())).findFirst()
                        .ifPresent(ticket -> {
                            ticket.setVectorStoreId(referenceId);
                            tripTicketDAO.updateTripTicket(ticket);
                        });

            } catch (Exception e) {
                log.error("Error processing TripTicketDTO for vectorStore: {}", dto, e);
            }
        }
    }

    @Transactional
    public TripTicketDTO addTripTicket(TripTicketDTO dto) {
        /*
        TripTicket saved = tripTicketModelMapper.map(dto, TripTicket.class);
        try {
            var readOnly = readOnlyTripTicketSummaryDAO.findById(saved.getId());
            String summary = createReadOnlyTripTicketSummary(readOnly);
            String referenceId = getReferenceId(saved.getId());
            Map<String, Object> metadata = Map.of("summary", summary);
            Document doc = new Document(referenceId, summary, metadata);
            vectorStore.add(List.of(doc));
            saved.setVectorStoreId(referenceId);
            tripTicketDAO.updateTripTicket(saved);
        } catch (Exception e) {
            log.error("Error adding TripTicket to vector store: {}", dto, e);
        }
        return tripTicketModelMapper.map(saved, TripTicketDTO.class);       */
        return null; // This will be added when ready

    }

    @Transactional
    public TripTicketDTO updateTripTicket(TripTicketDTO dto) {
        var entity = tripTicketDAO.findTripTicketByTripTicketId(dto.getId());
        TripTicket updated = null;
        try {
            var readOnly = readOnlyTripTicketSummaryDAO.findById(dto.getId());
            String summary = createReadOnlyTripTicketSummary(readOnly);
            if (summary.length() > 1000) {
                log.warn("TripTicket summary too long for embedding: {}", summary);
                summary = summary.substring(0, 1000);
            }
            String referenceId = entity.getVectorStoreId();
            if (referenceId == null || referenceId.isEmpty()) {
                log.warn("TripTicket does not have a vector store ID, creating new entry: {}", dto);
                referenceId = getReferenceId(entity.getId());
                entity.setVectorStoreId(referenceId);
            }
            Map<String, Object> metadata = Map.of("summary", summary);
            vectorStore.delete(List.of(referenceId));
            Document doc = new Document(referenceId, summary, metadata);
            vectorStore.add(List.of(doc));
            updated = tripTicketDAO.updateTripTicket(entity);
        } catch (Exception e) {
            log.error("Error updating TripTicket in vector store: {}", dto, e);
        }
        if (updated != null) {
            return tripTicketModelMapper.map(updated, TripTicketDTO.class);
        } else {
            log.error("TripTicket update failed, returning original DTO: {}", dto);
            return dto; // Return the original DTO if update fails
        }
    }

    @Transactional
    public void deleteTripTicket(TripTicket entity) {
        var referenceId = entity.getVectorStoreId();
        tripTicketDAO.deleteById(entity.getId()); // Delete the TripTicket entity from the database
        try {
            vectorStore.delete(List.of(referenceId));
        } catch (Exception e) {
            log.error("Error deleting TripTicket from vector store: {}", entity.getId(), e);
        }
    }

    /**
     * Search the vector store for a query and return the number of matching documents.
     */
    public int countVectorStoreResults(String query) {
        try {
            var results = vectorStore.similaritySearch(query);
            log.debug("similaritySearch returned class: {}", results != null ? results.getClass().getName() : "null");
            log.debug("similaritySearch result: {}", results);
            if (results instanceof java.util.List<?>) {
                return results.size();
            }
            return results != null ? 1 : 0;
        } catch (Exception e) {
            log.error("Error searching vector store for query: {}", query, e);
            return -1;
        }
    }

    private String getReferenceId(int tripTicketId) {
        //return String.valueOf(tripTicketId);
        return UUID.nameUUIDFromBytes(String.valueOf(tripTicketId).getBytes()).toString();
    }
}
