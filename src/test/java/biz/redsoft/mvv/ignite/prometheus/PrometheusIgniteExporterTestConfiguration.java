package biz.redsoft.mvv.ignite.prometheus;

import biz.redsoft.mvv.ignite.prometheus.exporter.IgniteMetricExporter;
import biz.redsoft.mvv.ignite.prometheus.metrics.TestMetricCollector;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class PrometheusIgniteExporterTestConfiguration {
  @Bean
  public IgniteMetricExporter metricExporter() {
    IgniteMetricExporter exporter = new IgniteMetricExporter();
    exporter.register(new TestMetricCollector());
    return exporter;
  }
}

