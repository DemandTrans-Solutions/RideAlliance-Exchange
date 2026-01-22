package com.clearinghouse.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UberProductDeserializationTest {

    @Test
    public void deserializeProductWithUnknownProperty_shouldIgnoreUnknown() throws Exception {
        String json = "{\n" +
                "  \"product_estimates\": [\n" +
                "    {\n" +
                "      \"fare\": { \"display\": \"$10.00\", \"value\": 10.0 },\n" +
                "      \"product\": { \"capacity\": 4, \"display_name\": \"UberX\", \"product_id\": \"uberX\", \"description\": \"some extra field\" }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        ObjectMapper mapper = new ObjectMapper();
        UberTripEstimatesResponse resp = mapper.readValue(json, UberTripEstimatesResponse.class);
        assertNotNull(resp);
        assertNotNull(resp.productEstimates());
        assertEquals(1, resp.productEstimates().size());
        UberProductEstimate pe = resp.productEstimates().get(0);
        assertNotNull(pe.fare());
        assertEquals(10.0, pe.fare().value());
        assertNotNull(pe.product());
        assertEquals("uberX", pe.product().productId());
        assertEquals("UberX", pe.product().displayName());
        assertEquals(4, pe.product().capacity().intValue());
    }
}

