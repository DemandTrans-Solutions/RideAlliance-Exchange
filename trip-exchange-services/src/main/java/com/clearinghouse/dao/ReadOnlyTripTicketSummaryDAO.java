package com.clearinghouse.dao;


import com.clearinghouse.entity.ReadOnlyTripTicketSummary;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@AllArgsConstructor
@Slf4j
public class ReadOnlyTripTicketSummaryDAO extends AbstractDAO<Integer, ReadOnlyTripTicketSummary> {
    public ReadOnlyTripTicketSummary findById(Integer id) {
        return getEntityManager().find(ReadOnlyTripTicketSummary.class, id);
    }

    public List<ReadOnlyTripTicketSummary> findAll() {
        List<ReadOnlyTripTicketSummary> summaries = getEntityManager()
                .createQuery("SELECT t FROM ReadOnlyTripTicketSummary t", ReadOnlyTripTicketSummary.class)
                .getResultList();
        log.info("Found {} trip ticket summaries", summaries.size());
        return summaries;
    }

}
