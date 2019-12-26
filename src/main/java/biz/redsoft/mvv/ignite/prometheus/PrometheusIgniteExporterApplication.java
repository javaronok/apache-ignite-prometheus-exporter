package biz.redsoft.mvv.ignite.prometheus;

import biz.redsoft.mvv.ignite.prometheus.exporter.IgniteMetricExporter;
import biz.redsoft.mvv.ignite.prometheus.metrics.IgniteNodeCollector;
import biz.redsoft.mvv.ignite.prometheus.metrics.TestMetricCollector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PrometheusIgniteExporterApplication {

  @Value("${ignite.http.rest.url}")
  private String igniteRestUrl;

  public static void main(String[] args) {
    SpringApplication sa = new SpringApplication(PrometheusIgniteExporterApplication.class);
    sa.setLogStartupInfo(false);
    sa.setBannerMode(Banner.Mode.OFF);
    sa.run(args);
  }

  @Bean
  public IgniteMetricExporter metricExporter() {
    IgniteMetricExporter exporter = new IgniteMetricExporter();
    exporter.register(new IgniteNodeCollector(igniteRestUrl));
    return exporter;
  }
}
