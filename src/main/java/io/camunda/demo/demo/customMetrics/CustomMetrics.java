package io.camunda.demo.demo.customMetrics;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import io.camunda.zeebe.spring.client.annotation.JobWorker;

// Relative imports

import static io.camunda.demo.demo.customMetrics.CustomMetricsProcessConstants.*;

@Component
public class CustomMetrics {
    private final static Logger LOG = LoggerFactory.getLogger(CustomMetrics.class);

    /*
    Filters a map by Key value with a predicate.
     */
    private static <K, V> Map<K, V> filterByKey(Map<K, V> map, Predicate<K> predicate) {
        return map.entrySet()
                .stream()
                .filter(entry -> predicate.test(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /*
    Job worker delegates the query for metric data to CustomMetricsEndpoint implementations.
    If a variable of the same name already exists (i.e., in case of a looping model), we add to the value.
     */
    @JobWorker(type = METRIC_PREFIX_NAME + "query")
    public Map<String, Object> queryMetricData(final ActivatedJob job) {
        try {
            String metricType = job.getVariable(METRIC_TYPE_VAR_NAME).toString();
            LOG.info("Query metric type: {}", metricType);

            // Name where we store our metric value.
            String varName = METRIC_PREFIX_NAME + metricType + METRIC_SEPARATOR + job.getElementId();

            // Query endpoint and retrieve metric data.
            CustomMetricsEndpointInterface endpoint = METRIC_ENDPOINT_MAP.get(metricType);
            if (endpoint != null) {
                Double result = endpoint.queryMetric(job);
                if (result == null)
                    result = 0d;
                LOG.info("Logging queried metric of value: {}", result);
                return Map.of(varName, getPreviousMetricValue(job, varName) + result);
            } else {
                LOG.error("No endpoint found for metric type: {}", metricType);
                return null;
            }

        } catch (Exception e) {
            LOG.error("Could not query metrics: {}", e.getMessage());
            return null;
        }
    }

    private double getPreviousMetricValue(final ActivatedJob job, String varName) {
        try {
            return Double.parseDouble(job.getVariable(varName).toString());
        } catch (Exception e) {
            // variable of varName is not present.
            return 0.0d;
        }
    }

    /*
    Compiles the metric values of the present metric types into one value.
     */
    @JobWorker(type = METRIC_PREFIX_NAME + "compile")
    public Map<String, Double> compileMetrics(final ActivatedJob job) {
        // filter map by metric prefix name
        Map<String, Object> filteredMap = filterByKey(job.getVariablesAsMap(), string -> string.startsWith(METRIC_PREFIX_NAME));

        // initialize metric map
        Map<String, Double> resultMap = new HashMap<>();

        // for entry in our filtered map
        for (var entry : filteredMap.entrySet()) {
            // get the start and end index of our metric type
            int startIndex = METRIC_PREFIX_NAME.length();
            int endIndex = entry.getKey().indexOf(METRIC_SEPARATOR, startIndex);
            if (endIndex == -1)
                continue; // if the metric separator is not found, skip

            String metricType = entry.getKey().substring(startIndex, endIndex);    // get substring

            Double currentValue = resultMap.getOrDefault(METRIC_PREFIX_NAME + metricType, 0.0d);
            try {
                Double mapValue = Double.parseDouble(entry.getValue().toString());
                resultMap.put(METRIC_PREFIX_NAME + metricType, currentValue + mapValue);
            } catch (NumberFormatException e) {
                LOG.error("Could not compile Double metric for value: '{}' in {}", entry.getValue(), entry.getKey());
            }
        }
        LOG.info("Compiled metrics: {}", resultMap);
        return resultMap;
    }
}
