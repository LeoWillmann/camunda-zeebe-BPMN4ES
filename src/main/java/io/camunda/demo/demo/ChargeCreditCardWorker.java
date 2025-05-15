package io.camunda.demo.demo;

import java.util.Map;

import io.camunda.demo.demo.customMetrics.CustomMetricsThreadPoller;
import io.camunda.demo.demo.customMetricsImplementations.RandomThreadedEndpoint;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;

@Component
public class ChargeCreditCardWorker {
    private final static Logger LOG = LoggerFactory.getLogger(ChargeCreditCardWorker.class);

    @JobWorker(type = "charge-credit-card")
    public Map<String, Double> chargeCreditCard(@Variable(name = "totalWithTax") Double totalWithTax, final ActivatedJob job) {
        CustomMetricsThreadPoller thread = new CustomMetricsThreadPoller(job, 100, new RandomThreadedEndpoint());
        thread.start();
        LOG.info("charging credit card: {}", totalWithTax);
        for (int i = 0; i < 3; i++) {
            try {
                synchronized (this) {
                    this.wait(750);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            LOG.info("Metric value: {}", thread.getAtomicMetricValue().get());
        }
        try {
            thread.stopRunning();
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        LOG.info("Metric value: {}", thread.getAtomicMetricValue().get());
        return Map.of("amountCharged", totalWithTax);
    }
}
