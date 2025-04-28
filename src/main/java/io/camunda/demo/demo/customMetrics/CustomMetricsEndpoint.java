package io.camunda.demo.demo.customMetrics;

import java.util.Map;

public interface CustomMetricsEndpoint {
    /*
    This interface allows for abstract queries to endpoints with a String data object which may contain JSON data.
    The queryMetric function returns a Double which contains the quantity of the metric.
     */
    Double queryMetric(String data);
}
