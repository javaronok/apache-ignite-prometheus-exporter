package biz.redsoft.mvv.ignite.prometheus.metrics;

import io.prometheus.client.Collector;
import org.apache.ignite.internal.processors.rest.GridRestCommand;
import org.apache.ignite.internal.processors.rest.GridRestResponse;
import org.apache.ignite.internal.visor.compute.VisorGatewayTask;
import org.apache.ignite.internal.visor.node.VisorNodeDataCollectorTask;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;

public class IgniteNodeCollector extends Collector {

  private final String url;

  private final RestTemplate restTemplate = new RestTemplate();

  public IgniteNodeCollector(String url) {
    this.url = url;
  }

  @Override
  public List<MetricFamilySamples> collect() {
    String query = UriComponentsBuilder.fromHttpUrl("http://" + url + "/ignite")
            .queryParam("cmd", GridRestCommand.EXE)
            .queryParam("name", VisorGatewayTask.class.getName())
            .queryParam("p1", "null")
            .queryParam("p2", VisorNodeDataCollectorTask.class.getName())
            .build().toUriString();

    GridRestResponse response = restTemplate.getForObject(query, GridRestResponse.class);

    List<MetricFamilySamples> samples = new ArrayList<>();

    List<MetricFamilySamples> memoryMetrics = collectMemoryMetrics("memoryMetrics", (Map<String, Map>) response.getResponse());
    samples.addAll(memoryMetrics);
    List<MetricFamilySamples> persistenceMetrics = collectPersistenceMetrics("persistenceMetrics", (Map<String, Map>) response.getResponse());
    samples.addAll(persistenceMetrics);
    return samples;
  }

  private static List<MetricFamilySamples> collectMemoryMetrics(String metricGroup, Map<String, Map> response) {
    Map<String, Map> result = (Map<String, Map>) response.get("result");
    Map<String, List<Map<String, Object>>> metrics = result.get(metricGroup);
    List<String> labels = Arrays.asList("nodeId", "regionName");
    return metrics.entrySet().stream().flatMap(e -> {
              String nodeId = e.getKey();
              List<Map<String, Object>> regionMetrics = e.getValue();
              return regionMetrics.stream().flatMap(region -> {
                String regionName = (String) region.get("name");
                List<String> labelValues = Arrays.asList(nodeId, regionName);
                return region.entrySet().stream()
                        .filter(entry -> !"name".equals(entry.getKey()))
                        .map(entry -> createMetricSample(metricGroup, entry.getKey(), labels, labelValues, entry.getValue()));
              });
            }).collect(Collectors.toList());
  }

  private static List<MetricFamilySamples> collectPersistenceMetrics(String metricGroup, Map<String, Map> response) {
    Map<String, Map> result = (Map<String, Map>) response.get("result");
    Map<String, Map<String, Object>> metrics = result.get(metricGroup);
    List<String> labels = Collections.singletonList("nodeId");
    return metrics.entrySet().stream().flatMap(e -> {
      List<String> labelValues = Collections.singletonList(e.getKey());
      Map<String, Object> regionMetrics = e.getValue();
      return regionMetrics.entrySet().stream()
              .map(entry -> createMetricSample(metricGroup, entry.getKey(), labels, labelValues, entry.getValue()));
    }).collect(Collectors.toList());
  }

  private static MetricFamilySamples createMetricSample(String metricGroup, String metricName,
                                                        List<String> labelNames, List<String> labelValues,
                                                        Object value
  ) {
    String sampleMetricName = Collector.sanitizeMetricName(metricGroup + "_" + metricName);
    Double sampleValue = Double.valueOf(String.valueOf(value));
    MetricFamilySamples.Sample sample = new MetricFamilySamples.Sample(sampleMetricName, labelNames, labelValues, sampleValue);
    return new MetricFamilySamples(sampleMetricName, Type.GAUGE, sampleMetricName, Collections.singletonList(sample));
  }
}
