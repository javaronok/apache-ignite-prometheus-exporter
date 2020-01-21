#!/usr/bin/env bash
docker run -itd -p 9000:9000 --name apache-ignite-prometheus-exporter --net=host \
 -e IGNITE_REST_URL="localhost:8080" -e IGNITE_REGION_FILTER="Default_Region" \
 apache-ignite-prometheus-exporter
