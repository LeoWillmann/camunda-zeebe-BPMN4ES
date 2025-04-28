package io.camunda.demo.demo.customMetrics;

import java.util.Map;

import io.camunda.demo.demo.customMetricsImplementations.RandomEndpoint;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.camunda.zeebe.spring.client.annotation.JobWorker;

import static io.camunda.demo.demo.customMetrics.CustomMetricsProcessConstants.PREFIX_NAME;

@Component
public class CustomMetrics {
    private final static Logger LOG = LoggerFactory.getLogger(CustomMetrics.class);
    private final static Map<String, CustomMetricsEndpoint> endpointMap = Map.of(
            "energy", new RandomEndpoint(),
            "water", new RandomEndpoint()
    );

    /*
    Job Worker annotation and only fetch required variables
     */
    @JobWorker(type = PREFIX_NAME + "query", fetchVariables = {"__customMetricsType", "__customMetricsData"})
    public Map<String, Object> queryMetricData(final ActivatedJob job,
                                               @Variable(name = "__customMetricsType") String metricType,
                                               @Variable(name = "__customMetricsData") String metricData) {
        CustomMetricsEndpoint endpoint = endpointMap.get(metricType);

        if (endpoint != null) {
            return Map.of("CustomMetric_" + metricType + "_" + job.getElementId(), endpoint.queryMetric(metricData));
        }
        return Map.of();
    }

    @JobWorker(type = PREFIX_NAME + "compile")
    public Map<String, Object> compileMetrics(final ActivatedJob job) {
        LOG.info("{}", job.getVariablesAsMap());
        return Map.of();
    }
}
