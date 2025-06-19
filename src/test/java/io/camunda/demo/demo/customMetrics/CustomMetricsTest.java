package io.camunda.demo.demo.customMetrics;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import org.junit.jupiter.api.Test;

import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;

import java.util.HashMap;
import java.util.Map;

import static io.camunda.demo.demo.customMetrics.CustomMetricsProcessConstants.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CustomMetricsTest {

    // needs to include a metric type of CustomMetricsProcessConstants.METRIC_ENDPOINT_MAP
    static final String TEST_NULL = "_testNull";
    static final String TEST_RESULT = "_testResult";
    static final String TEST_THROW = "_testThrow";
    static final String ELEMENT_ID = "elementID";

    CustomMetrics customMetrics = new CustomMetrics();
    String varName;

    @Mock
    ActivatedJob job;

    @BeforeEach
    void setUp() {
        varName = "";
    }

    @Test
    void testCompileMetricDataEmptyMap() {
        Map<String, Object> varMap = Map.of();
        Mockito.when(job.getVariablesAsMap()).thenReturn(varMap);

        assertEquals(Map.of(), customMetrics.compileMetrics(job));
    }

    @Test
    void testCompileMetricDataMapWithOtherVariables() {
        Map<String, Object> varMap = Map.of(
                "testVar1", 1d,
                "testVar2", 2d,
                "testVar3", 3d,
                "testVar4", 4d,
                "testVar5", 5d
        );
        Mockito.when(job.getVariablesAsMap()).thenReturn(varMap);

        assertEquals(Map.of(), customMetrics.compileMetrics(job));
    }

    @Test
    void testCompileMetricDataMapWithSingleMetricData() {
        Map<String, Object> varMap = Map.of(
                generateVarName(TEST_RESULT), 1d
        );
        Mockito.when(job.getVariablesAsMap()).thenReturn(varMap);

        assertEquals(expectedCompileResult(TEST_RESULT, 1d), customMetrics.compileMetrics(job));
    }

    @Test
    void testCompileMetricDataMapWithMultipleMetricData() {
        Map<String, Object> varMap = Map.of(
                generateVarName(TEST_RESULT) + "1", 1d,
                generateVarName(TEST_RESULT) + "2", 20d
        );
        Mockito.when(job.getVariablesAsMap()).thenReturn(varMap);

        assertEquals(expectedCompileResult(TEST_RESULT, 21d), customMetrics.compileMetrics(job));
    }

    @Test
    void testCompileMetricDataMapWithMultipleMetricTypes() {
        Map<String, Object> varMap = Map.of(
                generateVarName(TEST_RESULT + "1"), 1d,
                generateVarName(TEST_RESULT + "2"), 20d
        );
        Mockito.when(job.getVariablesAsMap()).thenReturn(varMap);

        Map<String, Object> map = new HashMap<>();
        map.putAll(expectedCompileResult(TEST_RESULT + "1", 1d));
        map.putAll(expectedCompileResult(TEST_RESULT + "2", 20d));

        assertEquals(map, customMetrics.compileMetrics(job));
    }

    @Test
    void testCompileMetricDataMapMultipleTimes() {
        Map<String, Object> varMap = new HashMap<>(Map.of(
                generateVarName(TEST_RESULT), 1d
        ));
        Mockito.when(job.getVariablesAsMap()).thenReturn(varMap);

        // Compile first time
        Map<String, Object> resultMap = expectedCompileResult(TEST_RESULT, 1d);
        assertEquals(resultMap, customMetrics.compileMetrics(job));

        // Compile second time
        varMap.putAll(resultMap);
        assertEquals(resultMap, customMetrics.compileMetrics(job));
    }

    @Test
    void testCompileMetricDataMapMultipleTimesWithVariableChanges() {
        Map<String, Object> varMap = new HashMap<>(Map.of(
                generateVarName(TEST_RESULT) + "1", 1d
        ));
        Mockito.when(job.getVariablesAsMap()).thenReturn(varMap);

        // Compile first time
        Map<String, Object> resultMap = expectedCompileResult(TEST_RESULT, 1d);
        assertEquals(resultMap, customMetrics.compileMetrics(job));

        // Compile second time
        varMap.putAll(resultMap);
        varMap.put(generateVarName(TEST_RESULT) + "2", 20d);
        assertEquals(expectedCompileResult(TEST_RESULT, 21d), customMetrics.compileMetrics(job));
    }

    @Test
    void testCompileMetricDataMapWithInvalidValueTypes() {
        Map<String, Object> varMap = Map.of(
                generateVarName(TEST_RESULT), "This is not a number"
        );
        Mockito.when(job.getVariablesAsMap()).thenReturn(varMap);

        assertEquals(Map.of(), customMetrics.compileMetrics(job));
    }

    @Test
    void testQueryMetricDataDefault() {
        // mock element and metric types
        Mockito.when(job.getElementId()).thenReturn(ELEMENT_ID);
        setJobMetricTypeVariable(TEST_RESULT);

        assertEquals(expectedQueryResult(1d), customMetrics.queryMetricData(job));
    }

    @Test
    void testQueryMetricDataDefaultWithPreviousValue() {
        // mock element and metric types with previous variable containing a value
        Mockito.when(job.getElementId()).thenReturn(ELEMENT_ID);
        setJobMetricTypeVariable(TEST_RESULT);
        Mockito.when(job.getVariable(varName)).thenReturn(20d);

        assertEquals(expectedQueryResult(21d), customMetrics.queryMetricData(job));
    }

    @Test
    void testQueryMetricDataNullEndpointResult() {
        // mock element and metric types
        Mockito.when(job.getElementId()).thenReturn(ELEMENT_ID);
        setJobMetricTypeVariable(TEST_NULL);

        assertNull(customMetrics.queryMetricData(job));
    }

    @Test
    void testQueryMetricDataEndpointThrows() {
        // mock element and metric types
        Mockito.when(job.getElementId()).thenReturn(ELEMENT_ID);
        setJobMetricTypeVariable(TEST_THROW);

        assertNull(customMetrics.queryMetricData(job));
    }

    @Test
    void testQueryMetricDataMetricTypeVariableNotThere() {
        // mock throw result of no element found
        Mockito.when(job.getVariable(METRIC_TYPE_VAR_NAME)).thenThrow(RuntimeException.class);

        assertNull(customMetrics.queryMetricData(job));
    }

    @Test
    void testQueryMetricDataNoMetricType() {
        // mock job with no matching metric type
        Mockito.when(job.getElementId()).thenReturn(ELEMENT_ID);
        setJobMetricTypeVariable("-");

        assertNull(customMetrics.queryMetricData(job));
    }

    /*
    When the job returns the metric type, we also set the corresponding var name.
     */
    private void setJobMetricTypeVariable(String type) {
        Mockito.when(job.getVariable(METRIC_TYPE_VAR_NAME)).thenReturn(type);
        varName = generateVarName(type);
    }

    // generates the variable name with the metric type
    private String generateVarName(String type) {
        return METRIC_PREFIX_NAME + type + METRIC_SEPARATOR + ELEMENT_ID;
    }

    // create the expected query result map
    private Map<String, Object> expectedQueryResult(Double value) {
        return Map.of(varName, value);
    }

    // create the expected compilation result map
    private Map<String, Object> expectedCompileResult(String type, Double value) {
        return Map.of(METRIC_PREFIX_NAME + type, value);
    }
}




