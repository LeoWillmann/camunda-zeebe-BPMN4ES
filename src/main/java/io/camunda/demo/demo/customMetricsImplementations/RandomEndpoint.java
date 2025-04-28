package io.camunda.demo.demo.customMetricsImplementations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.demo.demo.customMetrics.CustomMetricsEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class RandomEndpoint implements CustomMetricsEndpoint {
    private final static Logger LOG = LoggerFactory.getLogger(RandomEndpoint.class);
    private final static Random RANDOM = new Random();
    private final static ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public Double queryMetric(String data) {
        try {
            RandomEndpointDataObject value = MAPPER.readValue(data, RandomEndpoint.RandomEndpointDataObject.class);
            LOG.info("Construct random value with max: {} and offset: {}", value.max, value.offset);
            return (double) Math.round(RANDOM.nextDouble() * value.max + value.offset);
        } catch (JsonProcessingException e) {
            LOG.error("JSON formatting error, Could not construct variables");
            throw new RuntimeException(e);
        }
    }

    // data object for json data object.
    private static class RandomEndpointDataObject {
        public Double max = 1.0;
        public Double offset = 0.0;
    }
}
