#!/usr/bin/env python3

from typing import Dict, List
from functools import cmp_to_key
import pickle

def parse_exp_config(exp : str) -> Dict[str, List]:
  with open(f'configurations/{exp}.sh') as f:
    config={}
    keys=('PIPELINES', 'BATCH_SIZES', 'INPUT_RATES', 'PARALLELISM')
    for line in f.readlines():
      tokens = line.split('=')
      if tokens[0] not in keys:
        continue
      
      config[tokens[0]] = tokens[1].strip().replace('(', '').replace(')', '').split(' ')
      
      if tokens[0] != 'PIPELINES':
        config[tokens[0]] = list(map(lambda x : int(x) , config[tokens[0]]))
      
    return config

def parse_exp_runs():
  runs = 0
  with open('common_vars.sh') as f:
    for line in f.readlines():
      tokens = line.split('=')
      if tokens[0] == 'EXPERIMENT_RUNS':
        runs = int(tokens[1].split(' ')[0].strip())
  
  return runs

def cmp_func(a, b):
  '''
  We should not sort the list in lexicographic order because 123 will be smaller than 15 in this case.
  @param a, line in the result, e.g. '12515239498561,12515471693786'
  @param b, another line in the result
  '''
  a_0 = float(a.split(',')[0])
  b_0 = float(b.split(',')[0])
  
  return a_0 - b_0

def sort_lines(lines):
  lines.sort(key=cmp_to_key(cmp_func))

if __name__ == '__main__':
  # benchmark_types = ('external', 'embedded')
  # pipeline_types = ('torchserve', 'tf-serving', 'openvino', 'onnx', 'tf-saved', 'nd4j')
  
  benchmark_type = 'external'
  pipeline_type = 'torchserve'
  exps = ('throughput', 'latency', 'vertical_scalability')
  exp_runs = parse_exp_runs()   # the number of runs for each experiment

  exps_results = {}
  for exp in exps:
    result_dir = f'../../assets/experiments/results/{benchmark_type}/{pipeline_type}/{exp}'
    config = parse_exp_config(exp)

    exps_results[exp] = {}
    for p in config['PIPELINES']:
      exps_results[exp][p] = []
      for batch_size in config['BATCH_SIZES']:
        for rate in config['INPUT_RATES']:
          for parallelism in config['PARALLELISM']:

            all_lines = []
            for run in range(exp_runs):
              result_file_path = f'{result_dir}/{p}-{batch_size}-{rate}-{parallelism}-{run}.csv'
              with open(result_file_path, 'r') as f:
                all_lines += f.readlines()
            
            sort_lines(all_lines)

            splitted_lines = map(lambda line : line.split(','), all_lines)

            results_int = map(lambda line : (int(line[0]), int(line[1])), splitted_lines)
            valid_results = filter(lambda line : line[0] != -1 and line[1] != -1, results_int)
            valid_results_secs = list(map(lambda line : (float(line[0])/10**9, float(line[1])/10**9), valid_results))

            N = len(valid_results_secs)    # number of results for this specific experiment configuration
            avg_latency = sum(map(lambda res : res[1] - res[0], valid_results_secs)) / N
            avg_throughput = N / (valid_results_secs[-1][1] - valid_results_secs[0][1])

            exps_results[exp][p].append((batch_size, rate, parallelism, avg_latency, avg_throughput))
  
  # dump the final results to a file
  with open(f'../../assets/experiments/results/{benchmark_type}/{pipeline_type}_metrics', 'wb') as f:
    pickle.dump(exps_results, f)
    print(f'final results saved to ../../assets/experiments/results/{benchmark_type}/{pipeline_type}_results, please load it with pickle module\n')
  
  print(exps_results)