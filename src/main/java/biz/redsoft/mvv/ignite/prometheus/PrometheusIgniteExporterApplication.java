package biz.redsoft.mvv.ignite.prometheus;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PrometheusIgniteExporterApplication {

  public static void main(String[] args) {
    SpringApplication sa = new SpringApplication(PrometheusIgniteExporterApplication.class);
    sa.setLogStartupInfo(false);
    sa.setBannerMode(Banner.Mode.OFF);
    sa.run(args);
  }
}
