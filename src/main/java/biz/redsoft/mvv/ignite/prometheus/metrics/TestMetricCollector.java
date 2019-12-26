package biz.redsoft.mvv.ignite.prometheus.metrics;

import io.prometheus.client.Collector;

import java.util.Collections;
import java.util.List;

public class TestMetricCollector extends Collector {
  @Override
  public List<MetricFamilySamples> collect() {
    MetricFamilySamples single = new MetricFamilySamples("test_metric", Type.GAUGE, "test_metric",
            Collections.singletonList(
                    new MetricFamilySamples.Sample("test_metric_count",
                            Collections.emptyList(), Collections.emptyList(), 0.0)));
    return Collections.singletonList(single);
  }
}
