MODEL_NAMES=(mobilenet_v1 damage_vehicle_yolo5 yolos-small-finetuned-license-plate-detection trocr-small-printed)

TORCHSERVE_HOST=localhost

EXPERIMENT_RUNS=1   # may need more runs

ROOT=../../codebase
EXTERNAL_ROOT=$ROOT/external-serving
BENCHMARK_ROOT=$ROOT/flink-complex-ml-benchmark
BENCHMARK_CONFIG_FILE=$BENCHMARK_ROOT/src/main/resources/config.properties
BENCHMARK_CONFIG_FILE_DEFAULT=$BENCHMARK_ROOT/src/main/resources/config.default.properties

FILE_SINK_DIR=/tmp/flink_complex_ml_benchmark_results
MERGED_CSV_FILE=$FILE_SINK_DIR/benchmark_results.csv

EXPERIMENTS_RESULTS_DIR=/tmp/experiments_results