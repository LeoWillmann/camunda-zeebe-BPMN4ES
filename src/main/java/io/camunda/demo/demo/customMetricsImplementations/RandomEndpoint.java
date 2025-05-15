package io.camunda.demo.demo.customMetricsImplementations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.demo.demo.customMetrics.CustomMetricsEndpointInterface;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

import static io.camunda.demo.demo.customMetrics.CustomMetricsProcessConstants.METRIC_DATA_VAR_NAME;

/*
Implements the CustomMetricsEndpointInterface and returns a random generated value.
This requires the METRIC_DATA_VAR_NAME to contain a JSON object of RandomEndpointDataObject type.
 */
public class RandomEndpoint implements CustomMetricsEndpointInterface {
    private final static Logger LOG = LoggerFactory.getLogger(RandomEndpoint.class);
    private final static Random RANDOM = new Random();
    private final static ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public Double queryMetric(final ActivatedJob job) {
        String data = null;
        try {
            data = job.getVariable(METRIC_DATA_VAR_NAME).toString();
            RandomEndpointDataObject dataObject = MAPPER.readValue(data, RandomEndpoint.RandomEndpointDataObject.class);
            Double value = Math.round(RANDOM.nextDouble() * dataObject.max) + dataObject.offset;
            LOG.info("Construct random value with max: {} and offset: {}, Value: {}",
                    dataObject.max, dataObject.offset, value);
            return value;
        } catch (JsonProcessingException e) {
            LOG.error("JSON formatting error, Could not construct variables {}", data);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return null;
    }

    // data object for json data object.
    private static class RandomEndpointDataObject {
        public Double max = 1.0;
        public Double offset = 0.0;
    }
}
