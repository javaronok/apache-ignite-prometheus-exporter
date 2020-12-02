docker run -itd --name prometheus -p 9090:9090 -v /srv/prometheus/:/etc/prometheus --net=host prom/prometheus --config.file=/etc/prometheus/prometheus.yml
