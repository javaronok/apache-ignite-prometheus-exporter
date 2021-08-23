package biz.redsoft.mvv.ignite.prometheus.config;

import biz.redsoft.mvv.ignite.prometheus.exporter.IgniteMetricExporter;
import biz.redsoft.mvv.ignite.prometheus.metrics.IgniteNodeCollector;
import biz.redsoft.mvv.ignite.prometheus.metrics.IgniteQueryMetricCollector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.HashSet;
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
  public IgniteMetricExporter metricExporter() {
    IgniteMetricExporter exporter = new IgniteMetricExporter();
    Set<String> memoryRegionFilters = parseMemoryRegionFilters(igniteMemoryRegionFilter);
    exporter.register(new IgniteNodeCollector(igniteRestUrl, memoryRegionFilters));
    exporter.register(new IgniteQueryMetricCollector(igniteRestUrl));
    return exporter;
  }

  private static Set<String> parseMemoryRegionFilters(String igniteMemoryRegionFilter) {
    return igniteMemoryRegionFilter != null && !StringUtils.isEmpty(igniteMemoryRegionFilter)
            ? Stream.of(igniteMemoryRegionFilter.split(",")).map(String::trim).collect(Collectors.toSet())
            : new HashSet<>();
  }
}
