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

  private static final String METRIC_IGNITE_REGION_NAME = "name";
  private static final String METRIC_LABEL_NODE_ID = "nodeId";

  private final RestTemplate restTemplate = new RestTemplate();

  private final String url;

  private final Set<String> memoryRegionFilters;

  public IgniteNodeCollector(String url, Set<String> memoryRegionFilters) {
    this.url = url;
    this.memoryRegionFilters = memoryRegionFilters;
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

    Map<String, Map> metricMap = (Map<String, Map>) response.getResponse();
    List<MetricFamilySamples> memoryMetrics = collectMemoryMetrics("memoryMetrics", metricMap, memoryRegionFilters);
    samples.addAll(memoryMetrics);
    List<MetricFamilySamples> persistenceMetrics = collectPersistenceMetrics("persistenceMetrics", metricMap);
    samples.addAll(persistenceMetrics);
    return samples;
  }

  private static List<MetricFamilySamples> collectMemoryMetrics(String metricGroup, Map<String, Map> response,
                                                                Set<String> memoryRegionFilters
  ) {
    Map<String, Map> result = (Map<String, Map>) response.get("result");
    Map<String, List<Map<String, Object>>> metrics = result.get(metricGroup);
    List<String> labels = Arrays.asList(METRIC_LABEL_NODE_ID, "regionName");
    return metrics.entrySet().stream().flatMap(e -> {
      String nodeId = e.getKey();
      List<Map<String, Object>> regionMetrics = e.getValue();
      return regionMetrics.stream()
              .filter(region -> {
                String regionName = (String) region.get(METRIC_IGNITE_REGION_NAME);
                return memoryRegionFilters == null || memoryRegionFilters.isEmpty() || memoryRegionFilters.contains(regionName);
              })
              .flatMap(region -> {
                String regionName = (String) region.get(METRIC_IGNITE_REGION_NAME);
                List<String> labelValues = Arrays.asList(nodeId, regionName);
                return region.entrySet().stream()
                        .filter(entry -> !METRIC_IGNITE_REGION_NAME.equals(entry.getKey()))
                        .map(entry -> createMetricSample(metricGroup, entry.getKey(), labels, labelValues, entry.getValue()));
              });
    }).collect(Collectors.toList());
  }

  private static List<MetricFamilySamples> collectPersistenceMetrics(String metricGroup, Map<String, Map> response) {
    Map<String, Map> result = (Map<String, Map>) response.get("result");
    Map<String, Map<String, Object>> metrics = result.get(metricGroup);
    List<String> labels = Collections.singletonList(METRIC_LABEL_NODE_ID);
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
