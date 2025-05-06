package io.camunda.demo.demo.customMetrics;

import com.google.common.util.concurrent.AtomicDouble;
import io.camunda.zeebe.client.api.response.ActivatedJob;

public interface CustomMetricsThreadedInterface {

    /*
    This interface allows for abstract queries to endpoints for the CustomMetricsThread class.
    The method argument is the Zeebe's ActivatedJob class through which one can access all available parameters.
    And the current metric value is also passed along as an AtomicDouble.

    Abstract query metric function to query a double metric value from.
     */
    public Double queryMetric(final ActivatedJob job, final AtomicDouble atomicMetricValue);
}
