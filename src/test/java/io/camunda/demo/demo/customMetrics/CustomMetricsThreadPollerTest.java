package io.camunda.demo.demo.customMetrics;

import com.google.common.util.concurrent.AtomicDouble;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CustomMetricsThreadPollerTest {

    final CustomMetricsThreadedInterface interfaceNull = (ActivatedJob job, AtomicDouble metric) -> null;
    final CustomMetricsThreadedInterface interfaceResult = (ActivatedJob job, AtomicDouble metric) -> 1d;
    final CustomMetricsThreadedInterface interfaceThrow = (ActivatedJob job, AtomicDouble metric) -> {
        throw new RuntimeException();
    };

    @Mock
    ActivatedJob job;

    @Test
    void testConstructorDefault() {
        assertDoesNotThrow(() -> new CustomMetricsThreadPoller(job, 10, interfaceNull));
    }

    @Test
    void testConstructorTimeoutNegative() {
        assertThrows(RuntimeException.class, () -> new CustomMetricsThreadPoller(job, -10, interfaceNull));
    }

    @Test
    void testConstructorNullJob() {
        assertThrows(RuntimeException.class, () -> new CustomMetricsThreadPoller(null, 10, interfaceNull));
    }

    @Test
    void testConstructorNullInterface() {
        assertThrows(RuntimeException.class, () -> new CustomMetricsThreadPoller(job, 10, null));
    }

    @Test
    void testStopRunning() {
        CustomMetricsThreadPoller poller = new CustomMetricsThreadPoller(job, 10, interfaceNull);
        poller.start();
        poller.stopRunning();
        try {
            poller.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetAtomicMetricValue() {
        CustomMetricsThreadPoller poller = new CustomMetricsThreadPoller(job, 10, interfaceNull);
        assertNotNull(poller.getAtomicMetricValue());
        assertEquals(0d, poller.getAtomicMetricValue().get());
    }

    @Test
    void testThreadRunNullInterface() {
        CustomMetricsThreadPoller poller = new CustomMetricsThreadPoller(job, 10, interfaceNull);
        assertEquals(0d, poller.getAtomicMetricValue().get());
        poller.start();
        try {
            poller.stopRunning();
            poller.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertEquals(0d, poller.getAtomicMetricValue().get());
    }

    @Test
    void testThreadRunResultInterface() {
        CustomMetricsThreadPoller poller = new CustomMetricsThreadPoller(job, 10, interfaceResult);
        assertEquals(1d, poller.getAtomicMetricValue().get());
        poller.start();
        try {
            poller.stopRunning();
            poller.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertEquals(1d, poller.getAtomicMetricValue().get());
    }

    @Test
    void testThreadRunThrowInterface() {
        CustomMetricsThreadPoller poller = new CustomMetricsThreadPoller(job, 10, interfaceThrow);
        assertEquals(0d, poller.getAtomicMetricValue().get());
        poller.start();
        try {
            poller.stopRunning();
            poller.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertEquals(0d, poller.getAtomicMetricValue().get());
    }
}
