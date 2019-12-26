package biz.redsoft.mvv.ignite.prometheus.controller;

import biz.redsoft.mvv.ignite.prometheus.exporter.IgniteMetricExporter;
import io.prometheus.client.exporter.common.TextFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@RestController
public class PrometheusIgniteExportMetricController {
  private final IgniteMetricExporter exporter;

  @Autowired
  public PrometheusIgniteExportMetricController(IgniteMetricExporter exporter) {
    this.exporter = exporter;
  }

  @ResponseBody
  @RequestMapping(value = "/prometheus", method = RequestMethod.GET)
  public ResponseEntity metrics(@RequestParam(value = "name[]", required = false, defaultValue = "") Set<String> names) {
    String response = exporter.writeRegistry(names);
    return ResponseEntity.ok().header(CONTENT_TYPE, TextFormat.CONTENT_TYPE_004).body(response);
  }
}
