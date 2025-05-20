# Camunda Zeebe BPMN4ES job worker

![Camunda version](https://img.shields.io/badge/Camunda_Version-Camunda_Platform_8-orange.svg)

This repository contains a spring boot demo project following
the [Camunda getting started](https://docs.camunda.io/docs/guides/getting-started-java-spring/) guide with
additional job workers to monitor custom metrics.

## Prerequisites

- [OpenJDK 21+](https://openjdk.org/install/)
- [Maven 3](https://maven.apache.org/index.html)
- [Camunda 8 Run](https://docs.camunda.io/docs/self-managed/setup/deploy/local/c8run/)
- [Camunda 8 Desktop Modeler](https://docs.camunda.io/docs/components/modeler/desktop-modeler/)
- [Camunda 8 Desktop Modeler BPMN4ES plugin](https://github.com/rug-student/camunda-modeler-BPMN4ES)

## Install dependencies

```
mvn install
```

## Launch application

Run the main function in [DemoApplication.java](src/main/java/io/camunda/demo/demo/DemoApplication.java)
or execute from the terminal:

```
mvn spring-boot:run
```

## Customize for own usage

### Execution listener

Implement
the [CustomMetricsEndpointInterface.java](src/main/java/io/camunda/demo/demo/customMetrics/CustomMetricsEndpointInterface.java)
and add it to the METRIC_ENDPOINT_MAP
in [CustomMetricsProcessConstants.java](src/main/java/io/camunda/demo/demo/customMetrics/CustomMetricsProcessConstants.java).

Then in the bpmn model provide the correct metric type and JSON data.
The function will then retrieve the metric value and return it to the Zeebe engine.

### Threaded poller

This starts a new thread on which it will poll from an endpoint while another task is ongoing.
A demo implementation is commented
in [ChargeCreditCardWorker.java](src/main/java/io/camunda/demo/demo/ChargeCreditCardWorker.java).

### Self-manage data

Instead of letting Zeebe store and manage the metric data for you (as done in the two steps options above), you can
manage it yourself.
This means that instead of retrieving any data,
the execution listener may send a request to store the metric data under a certain task or other metadata. 


