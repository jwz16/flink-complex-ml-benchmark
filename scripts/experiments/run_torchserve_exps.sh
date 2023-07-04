#!/bin/bash
source ./common_vars.sh
source ./exps.functions

parse_torch_serve_host

function setup() {
  if [[ $TORCHSERVE_HOST = "localhost" ]]; then
    pushd $EXTERNAL_ROOT/torchserve/
    ./start_torch_serve.sh
    popd
  fi

  # copy default config file
  cp -f $BENCHMARK_CONFIG_FILE_DEFAULT $BENCHMARK_CONFIG_FILE
}

function teardown() {
  if [[ $TORCHSERVE_HOST = "localhost" ]]; then
    pushd $EXTERNAL_ROOT/torchserve/
    ./stop_torch_serve.sh
    popd
  fi
}

function wait_for_external_server() {
  while true; do
    echo "ping..."
    curl http://${TORCHSERVE_HOST}:8080/ping | grep Healthy
    healthy=$?

    if [[ $healthy -eq 0 ]]; then
      echo "healthy"
      return
    fi

    echo "wait for 5 seconds and retry ..."
    sleep 5
  done
}

function run() {
  ./run_throughput_exps.sh external torchserve
  ./run_latency_exps.sh external torchserve
  ./run_vertical_scalability_exps.sh external torchserve
}

setup
wait_for_external_server
run
teardown