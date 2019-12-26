package biz.redsoft.mvv.ignite.prometheus;

import biz.redsoft.mvv.ignite.prometheus.controller.PrometheusIgniteExportMetricController;
import io.prometheus.client.exporter.common.TextFormat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PrometheusIgniteExportMetricController.class)
@Import(PrometheusIgniteExporterTestConfiguration.class)
public class PrometheusIgniteExporterApplicationTests {

  @Autowired
  private MockMvc mvc;

  @Test
  public void testMetricCollectorRest() throws Exception {
    mvc.perform(get("/prometheus")).andExpect(status().isOk())
            .andExpect(content().contentType(TextFormat.CONTENT_TYPE_004))
            .andExpect(content().string(containsString("test_metric")));
  }
}
