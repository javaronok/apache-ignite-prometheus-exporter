#!/usr/bin/env bash
docker run -itd -p 9090:9000 --name apache-ignite-prometheus-exporter \
 -e IGNITE_REST_URL="10.81.1.36:8080" -e IGNITE_REGION_FILTER="Default_Region" \
 -e METRICS_NODE_ENABLED="true" -e METRICS_QUERY_ENABLED="true" \
 apache-ignite-prometheus-exporter
