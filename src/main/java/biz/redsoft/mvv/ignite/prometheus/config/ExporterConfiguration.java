package biz.redsoft.mvv.ignite.prometheus.config;

import biz.redsoft.mvv.ignite.prometheus.exporter.IgniteMetricExporter;
import biz.redsoft.mvv.ignite.prometheus.metrics.IgniteNodeCollector;
import biz.redsoft.mvv.ignite.prometheus.metrics.IgniteQueryMetricCollector;
import io.prometheus.client.Collector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class ExporterConfiguration {

  @Value("${ignite.http.rest.url}")
  private String igniteRestUrl;

  @Value("${ignite.metrics.memory.regions.filter}")
  private String igniteMemoryRegionFilter;

  @Bean
  public IgniteMetricExporter metricExporter(List<Collector> collectors) {
    IgniteMetricExporter exporter = new IgniteMetricExporter();
    for (Collector collector : collectors)
      exporter.register(collector);
    return exporter;
  }

  @Bean
  @ConditionalOnProperty(name = "ignite.metrics.node.collector.enabled", havingValue = "true", matchIfMissing = true)
  public IgniteNodeCollector nodeCollector() {
    Set<String> memoryRegionFilters = parseMemoryRegionFilters(igniteMemoryRegionFilter);
    return new IgniteNodeCollector(igniteRestUrl, memoryRegionFilters);
  }

  @Bean
  @ConditionalOnProperty(name = "ignite.metrics.query.collector.enabled", havingValue = "true", matchIfMissing = true)
  public IgniteQueryMetricCollector queryCollector() {
    return new IgniteQueryMetricCollector(igniteRestUrl);
  }

  private static Set<String> parseMemoryRegionFilters(String igniteMemoryRegionFilter) {
    return igniteMemoryRegionFilter != null && !StringUtils.isEmpty(igniteMemoryRegionFilter)
            ? Stream.of(igniteMemoryRegionFilter.split(",")).map(String::trim).collect(Collectors.toSet())
            : new HashSet<>();
  }
}
