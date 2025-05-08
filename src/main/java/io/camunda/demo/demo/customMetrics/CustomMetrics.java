package io.camunda.demo.demo.customMetrics;

import java.util.Map;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import io.camunda.zeebe.spring.client.annotation.JobWorker;

// Relative imports
import static io.camunda.demo.demo.customMetrics.CustomMetricsProcessConstants.METRIC_TYPE_VAR_NAME;
import static io.camunda.demo.demo.customMetrics.CustomMetricsProcessConstants.PREFIX_NAME;

import io.camunda.demo.demo.customMetricsImplementations.RandomEndpoint;

@Component
public class CustomMetrics {
    private final static Logger LOG = LoggerFactory.getLogger(CustomMetrics.class);

    /*
    Mapping of a metric type (String) to retrieval endpoint implementation.
     */
    private final static Map<String, CustomMetricsEndpointInterface> endpointMap = Map.of(
            "energy", new RandomEndpoint(),
            "water", new RandomEndpoint()
    );

    /*
    Job worker delegates the query for metric data to CustomMetricsEndpoint implementations.
    If a variable of the same name already exists (i.e., in case of a looping model), we add to the value.
     */
    @JobWorker(type = PREFIX_NAME + "query")
    public Map<String, Object> queryMetricData(
            final ActivatedJob job,
            @Variable(name = METRIC_TYPE_VAR_NAME) String metricType) {

        // Name where we store our metric value.
        String varName = PREFIX_NAME + metricType + "_" + job.getElementId();
        Double result;
        try {
            result = Double.valueOf(job.getVariable(varName).toString());
        } catch (Exception e) {
            // variable of varName is not present.
            result = 0.0;
        }

        CustomMetricsEndpointInterface endpoint = endpointMap.get(metricType);
        if (endpoint != null) {
            Double query = endpoint.queryMetric(job);
            result += query;
            LOG.info("Logging queried metric of value: {}", query);
        }
        return Map.of(varName, result);
    }

    @JobWorker(type = PREFIX_NAME + "compile")
    public Map<String, Object> compileMetrics(final ActivatedJob job) {
        LOG.info("{}", job.getVariablesAsMap());
        return Map.of();
    }
}
