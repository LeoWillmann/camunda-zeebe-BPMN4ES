package io.camunda.demo.demo.customMetrics;

import io.camunda.demo.demo.customMetricsImplementations.RandomEndpoint;

import java.util.Map;

public interface CustomMetricsProcessConstants {
    String METRIC_PREFIX_NAME = "custom-metrics_";
    String METRIC_TYPE_VAR_NAME = "__customMetricsType";
    String METRIC_DATA_VAR_NAME = "__customMetricsData";
    String METRIC_TARGET_VAR_NAME = "__customMetricsTarget";
    String METRIC_SEPARATOR = "#"; // The separator should not appear in the metric endpoint map

    /*
    Static mapping of a metric type (String) to retrieval endpoint implementation of the CustomMetricsEndpointInterface type.
     */
    Map<String, CustomMetricsEndpointInterface> METRIC_ENDPOINT_MAP = Map.of(
            "water", new RandomEndpoint(),
            "renewable-energy", new RandomEndpoint(),
            "energy-consumption", new RandomEndpoint()
    );
}
