package io.camunda.demo.demo.customMetrics;

import com.google.common.util.concurrent.AtomicDouble;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/*
This is for continuous polling of a metric value.
The thread starts a loop which is only stopped by the stopRunning() method.
 */
public class CustomMetricsThreadPoller extends Thread {
    private final static Logger LOG = LoggerFactory.getLogger(CustomMetricsThreadPoller.class);

    private final ActivatedJob job;
    private final AtomicDouble atomicMetricValue = new AtomicDouble();
    private final long timeoutMillis;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private final CustomMetricsThreadedInterface threadedQueryInterface;

    public CustomMetricsThreadPoller(ActivatedJob job, long timeoutMillis,
                                     CustomMetricsThreadedInterface threadedQueryInterface) {
        this.job = job;
        this.timeoutMillis = timeoutMillis;
        this.threadedQueryInterface = threadedQueryInterface;
        LOG.info("CustomMetricsThread created for job {} with interface implementation {}",
                job.getKey(), threadedQueryInterface.getClass().getName());
    }

    // Stops the isRunning while loop in the run() method.
    public void stopRunning() {
        isRunning.set(false);
    }

    public AtomicDouble getAtomicMetricValue() {
        return atomicMetricValue;
    }

    /*
    The thread run() method.
    Starts and ends by logging the job key information.

    We start with querying a metric value, and then loop over the isRunning boolean.
    In every iteration of the loop, we check the isRunning boolean, wait for the timeout, and query a metric value.
     */
    public void run() {
        LOG.info("CustomMetricsThread is running for job {} with interface implementation {}",
                job.getKey(), threadedQueryInterface.getClass().getName());

        queryAndSetMetricValue();

        while (isRunning.get()) {
            // timeout between queries
            try {
                synchronized (this) {
                    this.wait(timeoutMillis);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            queryAndSetMetricValue();
        }
        LOG.info("CustomMetricsThread is done for job {} with interface implementation {}",
                job.getKey(), threadedQueryInterface.getClass().getName());
    }

    /*
    Queries a metric value from the given query implementation
    and sets the Atomic boolean if the returned value is not null.
     */
    private void queryAndSetMetricValue() {
        try {
            Double value = threadedQueryInterface.queryMetric(job, atomicMetricValue);
            if (value != null) {    // check for null condition
                atomicMetricValue.set(value);
            }
        } catch (Exception e) {
        }
    }
}
