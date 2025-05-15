package io.camunda.demo.demo.customMetrics;

import io.camunda.zeebe.client.api.response.ActivatedJob;

public interface CustomMetricsEndpointInterface {
    /*
    This interface allows for abstract queries to endpoints.
    The method argument is the Zeebe's ActivatedJob class through which one can access all available parameters.
    The queryMetric function returns a Double which contains the quantity of the metric.

    The implementation should not throw exceptions, but return null in case no result.
     */
    Double queryMetric(final ActivatedJob job);
}
