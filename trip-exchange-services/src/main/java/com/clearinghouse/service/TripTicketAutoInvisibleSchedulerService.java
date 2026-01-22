package com.clearinghouse.service;

import com.clearinghouse.dao.ProviderDAO;
import com.clearinghouse.dao.TripTicketDAO;
import com.clearinghouse.dto.ActivityDTO;
import com.clearinghouse.entity.Provider;
import com.clearinghouse.entity.TripTicket;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 *
 * @author shankarI
 */
@Service("tripTicketAutoInvisibleSchedulerServiceImpl")
@AllArgsConstructor
@Slf4j
public class TripTicketAutoInvisibleSchedulerService {


    private final TripTicketDAO tripTicketDAO;


    private final ProviderDAO providerDAO;


    private final ActivityService activityService;

    @Transactional(propagation = Propagation.REQUIRED)
    // @Scheduled(cron = "0 0 8 * * *", zone = "${cronExpressionTimeZone}") // GMT-7 everyday 8 am ( DO NOT MAKE EXPIRED TICKETS INVISIBLE, WHY WOULD WE DO THIS???)
    public void tripTicketAutoinvisible() {

        List<TripTicket> expiredTripTicketList = getAllExpiredTripTicketsHavingInvisibleFalse();
        if (!expiredTripTicketList.isEmpty()) {
            for (TripTicket tripTicket : expiredTripTicketList) {

                // expiration date time must not be empty
                if (tripTicket.getRequestedPickupDate() != null) {
                    LocalDate currentDate = LocalDate.now(Clock.systemDefaultZone());// ZoneId.of("UTC-7") // // MDT
                    LocalDate pickupDate = tripTicket.getRequestedPickupDate();

                    log.debug("TripInvisible with ticketId=" + tripTicket.getId() + "   pickupDate"
                            + pickupDate + "  currentDate=" + currentDate);
                    long daysDifference = ChronoUnit.DAYS.between(pickupDate, currentDate);

                    if (daysDifference > 0) {
                        tripTicket.setTripTicketInvisible(true);
                        tripTicketDAO.updateTripTicket(tripTicket);
                        // add activity records
                        addActivityForExpiredTicketInvisible(tripTicket);

                    }
                } else {

                    LocalDate currentDate = LocalDate.now(Clock.systemDefaultZone());// ZoneId.of("UTC-6") // // MDT
                    LocalDate dropOffDate = tripTicket.getRequestedDropoffDate();
                    if (dropOffDate == null) {
                        continue;
                    }

                    log.debug("TripInvisible with ticketId=" + tripTicket.getId() + "   dropOffDate"
                            + dropOffDate + "  currentDate=" + currentDate);
                    long daysDifference = ChronoUnit.DAYS.between(dropOffDate, currentDate);

                    if (daysDifference > 0) {
                        tripTicket.setTripTicketInvisible(true);
                        tripTicketDAO.updateTripTicket(tripTicket);
                        // add activity records
                        addActivityForExpiredTicketInvisible(tripTicket);

                    }
                }
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<TripTicket> getAllExpiredTripTicketsHavingInvisibleFalse() {
        List<TripTicket> tripTicketBoList = tripTicketDAO.getExpiredTripTickets();
        return tripTicketBoList;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void addActivityForExpiredTicketInvisible(TripTicket tripTicket) {
        Provider provider = providerDAO.findProviderByProviderId(tripTicket.getOriginProvider().getProviderId());
        ActivityDTO activityDTO = new ActivityDTO();
        activityDTO.setAction("Expired ticket gets Invisible");
        activityDTO.setTripTicketId(tripTicket.getId());
        activityDTO.setActionTakenBy(provider.getProviderName());
        activityDTO.setActionDetails(
                "originatorProvider=" + provider.getProviderName() + "status=Expired, isInvisible=true");
        activityService.createActivity(activityDTO);
    }

}
