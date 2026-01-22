package com.clearinghouse.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import com.clearinghouse.entity.TripTicket;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReportingServiceQueryTest {
    @Mock EntityManager entityManager;
    @Mock CriteriaBuilder cb;
    @Mock CriteriaQuery<Object[]> cq;
    @Mock Root<TripTicket> root;
    @Mock TypedQuery<Object[]> typedQuery;
    @InjectMocks ReportingService service = new ReportingService();

    public ReportingServiceQueryTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testExecuteReportingQuery_countByDate() {
        Map<String, Object> params = new HashMap<>();
        params.put("aggregation", "count");
        params.put("groupBy", "requestedPickupDate");
        params.put("filters", Map.of());

        // Mock Criteria API chain
        when(entityManager.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Object[].class)).thenReturn(cq);
        when(cq.from(TripTicket.class)).thenReturn(root);
        Path<Object> groupByPath = mock(Path.class);
        when(root.get("requestedPickupDate")).thenReturn(groupByPath);
        Expression<Long> countExpr = mock(Expression.class);
        when(cb.count(root)).thenReturn(countExpr);
        when(cq.multiselect(any(), any())).thenReturn(cq);
        when(cq.where(any(Predicate[].class))).thenReturn(cq);
        when(cq.groupBy(any(Expression.class))).thenReturn(cq);
        when(cq.orderBy(any(Order.class))).thenReturn(cq);
        when(entityManager.createQuery(cq)).thenReturn(typedQuery);
        Object[] row = new Object[]{new Date(), 5L};
        List<Object[]> resultList = new ArrayList<>();
        resultList.add(row);
        when(typedQuery.getResultList()).thenReturn(resultList);

        List<Map<String, Object>> result = service.executeReportingQuery(params);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).containsKey("requestedPickupDate"));
        assertTrue(result.get(0).containsKey("count"));
    }

    @Test
    public void testExecuteReportingQuery_multipleFilters() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("purpose", "Medical");
        filters.put("serviceLevel", "Standard");
        Map<String, Object> params = new HashMap<>();
        params.put("aggregation", "count");
        params.put("groupBy", "requestedPickupDate");
        params.put("filters", filters);

        // Mock Criteria API chain
        when(entityManager.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Object[].class)).thenReturn(cq);
        when(cq.from(TripTicket.class)).thenReturn(root);
        Path<Object> groupByPath = mock(Path.class);
        when(root.get("requestedPickupDate")).thenReturn(groupByPath);
        Expression<Long> countExpr = mock(Expression.class);
        when(cb.count(root)).thenReturn(countExpr);
        when(cq.multiselect(any(), any())).thenReturn(cq);
        when(cq.where(any(Predicate[].class))).thenReturn(cq);
        when(cq.groupBy(any(Expression.class))).thenReturn(cq);
        when(cq.orderBy(any(Order.class))).thenReturn(cq);
        when(entityManager.createQuery(cq)).thenReturn(typedQuery);
        Object[] row = new Object[]{new Date(), 3L};
        List<Object[]> resultList = new ArrayList<>();
        resultList.add(row);
        when(typedQuery.getResultList()).thenReturn(resultList);

        List<Map<String, Object>> result = service.executeReportingQuery(params);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).containsKey("requestedPickupDate"));
        assertTrue(result.get(0).containsKey("count"));
    }

    @Test
    public void testExecuteReportingQuery_unsupportedField() {
        Map<String, Object> params = new HashMap<>();
        params.put("aggregation", "count");
        params.put("groupBy", "nonexistentField");
        params.put("filters", Map.of());
        assertThrows(IllegalArgumentException.class, () -> service.executeReportingQuery(params));
    }

    @Test
    public void testExecuteReportingQuery_rangeFilter() {
        List<Object> range = List.of(1, 10);
        Map<String, Object> filters = new HashMap<>();
        filters.put("estimatedTripDistance", range);
        Map<String, Object> params = new HashMap<>();
        params.put("aggregation", "count");
        params.put("groupBy", "requestedPickupDate");
        params.put("filters", filters);

        // Mock Criteria API chain
        when(entityManager.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Object[].class)).thenReturn(cq);
        when(cq.from(TripTicket.class)).thenReturn(root);
        Path<Object> groupByPath = mock(Path.class);
        when(root.get("requestedPickupDate")).thenReturn(groupByPath);
        Path<Object> rangePath = mock(Path.class);
        when(root.get("estimatedTripDistance")).thenReturn(rangePath);
        Expression<Long> countExpr = mock(Expression.class);
        when(cb.count(root)).thenReturn(countExpr);
        when(cq.multiselect(any(), any())).thenReturn(cq);
        when(cq.where(any(Predicate[].class))).thenReturn(cq);
        when(cq.groupBy(any(Expression.class))).thenReturn(cq);
        when(cq.orderBy(any(Order.class))).thenReturn(cq);
        when(entityManager.createQuery(cq)).thenReturn(typedQuery);
        Object[] row = new Object[]{new Date(), 2L};
        List<Object[]> resultList = new ArrayList<>();
        resultList.add(row);
        when(typedQuery.getResultList()).thenReturn(resultList);

        List<Map<String, Object>> result = service.executeReportingQuery(params);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).containsKey("requestedPickupDate"));
        assertTrue(result.get(0).containsKey("count"));
    }
}
