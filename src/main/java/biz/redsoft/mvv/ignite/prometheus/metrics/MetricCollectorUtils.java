package biz.redsoft.mvv.ignite.prometheus.metrics;

import io.prometheus.client.Collector;

import java.util.Collections;
import java.util.List;

public class MetricCollectorUtils {

  static Collector.MetricFamilySamples createMetricSample(String metricGroup, String metricName,
                                                          Object value, Collector.Type type,
                                                          List<String> labelNames, List<String> labelValues
  ) {
    String sampleMetricName = Collector.sanitizeMetricName(metricGroup + "_" + metricName);
    double sampleValue = Double.parseDouble(String.valueOf(value));
    Collector.MetricFamilySamples.Sample sample = new Collector.MetricFamilySamples.Sample(
            sampleMetricName, labelNames, labelValues, sampleValue
    );
    return new Collector.MetricFamilySamples(sampleMetricName, type, metricName, Collections.singletonList(sample));
  }
}

