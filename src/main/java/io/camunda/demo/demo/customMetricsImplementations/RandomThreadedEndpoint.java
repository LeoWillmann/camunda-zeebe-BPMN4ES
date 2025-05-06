package io.camunda.demo.demo.customMetricsImplementations;

import com.google.common.util.concurrent.AtomicDouble;
import io.camunda.demo.demo.customMetrics.CustomMetricsThreadedInterface;
import io.camunda.zeebe.client.api.response.ActivatedJob;

public class RandomThreadedEndpoint implements CustomMetricsThreadedInterface {
    @Override
    public Double queryMetric(ActivatedJob job, AtomicDouble atomicMetricValue) {
        // returns the null safe metric sum
        Double value = new RandomEndpoint().queryMetric(job);
        return value == null ? null : value + atomicMetricValue.get();
    }
}
