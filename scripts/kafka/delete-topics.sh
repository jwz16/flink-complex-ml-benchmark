#!/bin/bash

docker compose -f ../../docker/kafka/docker-compose.yml exec kafka kafka-topics.sh --delete --topic complex-ml-input --bootstrap-server localhost:9094
docker compose -f ../../docker/kafka/docker-compose.yml exec kafka kafka-topics.sh --delete --topic complex-ml-output --bootstrap-server localhost:9094