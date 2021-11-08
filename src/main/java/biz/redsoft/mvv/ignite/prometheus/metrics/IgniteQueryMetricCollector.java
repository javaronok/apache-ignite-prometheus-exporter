package biz.redsoft.mvv.ignite.prometheus.metrics;

import io.prometheus.client.Collector;
import org.apache.ignite.internal.processors.rest.GridRestCommand;
import org.apache.ignite.internal.processors.rest.GridRestResponse;
import org.apache.ignite.internal.visor.compute.VisorGatewayTask;
import org.apache.ignite.internal.visor.query.VisorCacheQueryMetricsCollectorTask;
import org.apache.ignite.internal.visor.query.VisorCacheQueryMetricsCollectorTaskArg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static biz.redsoft.mvv.ignite.prometheus.metrics.MetricCollectorUtils.createMetricSample;

public class IgniteQueryMetricCollector extends Collector {
  private static final Logger LOGGER = LoggerFactory.getLogger(IgniteQueryMetricCollector.class);

  private static final String METRICS_GROUP = "sqlQueryMetrics";

  private static final String METRIC_IGNITE_CACHE_NAME = "cacheName", METRIC_LABEL_NODE_ID = "nodeId";

  private static final List<String> METRIC_LABELS = Arrays.asList(METRIC_IGNITE_CACHE_NAME, METRIC_LABEL_NODE_ID);

  private final RestTemplate restTemplate = new RestTemplate();

  private final String url;

  public IgniteQueryMetricCollector(String url) {
    this.url = url;
  }

  @Override
  public List<MetricFamilySamples> collect() {
    String query = UriComponentsBuilder.fromHttpUrl("http://" + url + "/ignite")
            .queryParam("cmd", GridRestCommand.EXE)
            .queryParam("name", VisorGatewayTask.class.getName())
            .queryParam("p1", "null")
            .queryParam("p2", VisorCacheQueryMetricsCollectorTask.class.getName())
            .queryParam("p3", VisorCacheQueryMetricsCollectorTaskArg.class.getName())
            .build().toUriString();

    GridRestResponse response = restTemplate.getForObject(query, GridRestResponse.class);

    List<MetricFamilySamples> samples = new ArrayList<>();

    final int status = response.getSuccessStatus();
    if (status == GridRestResponse.STATUS_SUCCESS) {
      Map<String, Object> responseMap = (Map<String, Object>) response.getResponse();
      Boolean finished = (Boolean) responseMap.get("finished");
      if (finished) {
        final List<Map<String, ?>> result = (List<Map<String, ?>>) responseMap.get("result");
        List<MetricFamilySamples> cacheMetrics = collectCacheMetrics(result);
        if (cacheMetrics != null)
          samples.addAll(cacheMetrics);
      } else {
        String error = (String) responseMap.get("error");
        LOGGER.warn("Response retrieved with error: " + error);
      }
    } else {
      LOGGER.warn("Request" + query + " finished (status: " + status + " ) with error: " + response.getError());
    }
    return samples;
  }

  private static List<MetricFamilySamples> collectCacheMetrics(List<Map<String, ?>> result) {
    return result.stream().flatMap(item -> {
      final String cacheName = (String) item.get("name");
      Map<String, ?> nodeMetrics = (Map<String, ?>) item.get("nodes");

      return nodeMetrics.entrySet().stream().flatMap(node -> {
        final String nodeId = node.getKey();

        List<String> labelValues = Arrays.asList(cacheName, nodeId);

        Map<String, Object> values = (Map<String, Object>) node.getValue();

        return Stream.of(
                createMetricSampleByName("executions", Type.COUNTER, labelValues),
                createMetricSampleByName("failures", Type.COUNTER, labelValues),
                createMetricSampleByName("minimumTime", Type.GAUGE, labelValues),
                createMetricSampleByName("maximumTime", Type.GAUGE, labelValues),
                createMetricSampleByName("averageTime", Type.GAUGE, labelValues)
        ).map(sample -> sample.apply(values));
      });
    }).collect(Collectors.toList());
  }

  private static Function<Map<String, Object>, MetricFamilySamples> createMetricSampleByName(String metricName,
                                                                                             Type type,
                                                                                             List<String> labelValues
  ) {
    return values -> {
      Object value = values.get(metricName);
      return createMetricSample(METRICS_GROUP, metricName, value, type, METRIC_LABELS, labelValues);
    };
  }
}
