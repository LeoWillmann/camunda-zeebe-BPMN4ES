package io.camunda.demo.demo.customMetricsImplementations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.demo.demo.customMetrics.CustomMetricsEndpointInterface;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class RandomEndpoint implements CustomMetricsEndpointInterface {
    private final static Logger LOG = LoggerFactory.getLogger(RandomEndpoint.class);
    private final static Random RANDOM = new Random();
    private final static ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public Double queryMetric(final ActivatedJob job) {
        String data = job.getVariable("__customMetricsData").toString();
        try {
            RandomEndpointDataObject dataObject = MAPPER.readValue(data, RandomEndpoint.RandomEndpointDataObject.class);
            Double value = Math.round(RANDOM.nextDouble() * dataObject.max) + dataObject.offset;
            LOG.info("Construct random value with max: {} and offset: {}, Value: {}",
                    dataObject.max, dataObject.offset, value);
            return value;
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
