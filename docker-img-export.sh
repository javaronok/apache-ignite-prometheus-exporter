#!/usr/bin/env bash
docker save apache-ignite-prometheus-exporter:latest | gzip > apache-ignite-prometheus-exporter_latest.tar.gz
