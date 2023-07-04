#!/bin/bash

# Build java project
JAVA_PROJECT_PATH="codebase/flink-complex-ml-benchmark"
mvn -f $JAVA_PROJECT_PATH/pom.xml clean && mvn -f $JAVA_PROJECT_PATH/pom.xml test