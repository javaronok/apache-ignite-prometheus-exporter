package biz.redsoft.mvv.ignite.prometheus;

import biz.redsoft.mvv.ignite.prometheus.exporter.IgniteMetricExporter;
import biz.redsoft.mvv.ignite.prometheus.metrics.IgniteNodeCollector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
public class PrometheusIgniteExporterApplication {

  @Value("${ignite.http.rest.url}")
  private String igniteRestUrl;

  @Value("${ignite.metrics.memory.regions.filter}")
  private String igniteMemoryRegionFilter;

  public static void main(String[] args) {
    SpringApplication sa = new SpringApplication(PrometheusIgniteExporterApplication.class);
    sa.setLogStartupInfo(false);
    sa.setBannerMode(Banner.Mode.OFF);
    sa.run(args);
  }

  @Bean
  public IgniteMetricExporter metricExporter() {
    IgniteMetricExporter exporter = new IgniteMetricExporter();
    Set<String> memoryRegionFilters = parseMemoryRegionFilters(igniteMemoryRegionFilter);
    exporter.register(new IgniteNodeCollector(igniteRestUrl, memoryRegionFilters));
    return exporter;
  }

  private static Set<String> parseMemoryRegionFilters(String igniteMemoryRegionFilter) {
    return igniteMemoryRegionFilter != null && !StringUtils.isEmpty(igniteMemoryRegionFilter)
            ? Stream.of(igniteMemoryRegionFilter.split(",")).map(String::trim).collect(Collectors.toSet())
            : new HashSet<>();
  }
}
