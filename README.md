# Prometheus exporter for Apache Ignite metrics 

### Getting Started

Depends on ignite-rest-http module. 
[Documentation for connection to Ignite over HTTP REST protocol](https://apacheignite.readme.io/docs/rest-api)

### Settings

Settings file for exporter configuration is named `application.properties` 

Application port: `server.port=<port>`, for example `server.port=9000` 

Address for rest-api module is configured as property: `ignite.http.rest.url=<host>:<port>` 

You can setup filter for ignite data region metrics with help property: `ignite.metrics.memory.regions.filter=STATES,REFERENCES`

You can turn on node or query metrics with different settings:
 - `ignite.metrics.node.collector.enabled=true` - Turn on ignite node metrics (memory, WAL, checkpoint, pages) 
 - `ignite.metrics.query.collector.enabled=true` - Turn on ignite query metrics (min, max, avg, count executions and failures)

### Running exporter through the console 

```bash
java -jar apache-ignite-prometheus-exporter-0.1-RELEASE.jar --spring.config.location=file:./application.properties
```

Then fire up your browser and point it to `http://localhost:9000/prometheus`:

```
# HELP memoryMetrics_totalAllocatedPages memoryMetrics_totalAllocatedPages
# TYPE memoryMetrics_totalAllocatedPages gauge
memoryMetrics_totalAllocatedPages{nodeId="25dfe6dc-d7a8-4be7-8dad-6b508d1ca29f",regionName="STATES",} 1470939.0
# HELP memoryMetrics_allocationRate memoryMetrics_allocationRate
# TYPE memoryMetrics_allocationRate gauge
memoryMetrics_allocationRate{nodeId="25dfe6dc-d7a8-4be7-8dad-6b508d1ca29f",regionName="STATES",} 0.0
...
```

You can see text plain metrics in prometheus format 