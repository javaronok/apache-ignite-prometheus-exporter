package biz.redsoft.mvv.ignite.prometheus.exporter;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Set;

public class IgniteMetricExporter {
  private final CollectorRegistry collectorRegistry = new CollectorRegistry();

  public String writeRegistry(Set<String> metricsToInclude) {
    try {
      Writer writer = new StringWriter();
      TextFormat.write004(writer, collectorRegistry.filteredMetricFamilySamples(metricsToInclude));
      return writer.toString();
    } catch (IOException e) {
      // This actually never happens since StringWriter::write() doesn't throw any IOException
      throw new RuntimeException("Writing metrics failed", e);
    }
  }

  public void register(Collector collector) {
    collectorRegistry.register(collector);
  }
}
