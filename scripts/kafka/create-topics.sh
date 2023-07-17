#!/bin/bash

ocker compose -f ../../docker/kafka/docker-compose.yml exec kafka kafka-topics.sh --create --topic complex-ml-input --bootstrap-server localhost:9092