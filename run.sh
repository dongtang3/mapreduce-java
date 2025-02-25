#!/bin/bash

# Create temporary and log directories
mkdir -p mr-tmp
mkdir -p logs

# Clean previous temporary files and logs
rm -f mr-tmp/*
rm -f logs/*

# Compile project using Maven
mvn clean compile

# Set coordinator host address
export MR_COORDINATOR_HOST=localhost

# Run coordinator
mvn exec:java -Dexec.mainClass="MainCoordinator" -Dexec.args="input/pg-being_ernest.txt input/pg-dorian_gray.txt input/pg-grimm.txt" > logs/coordinator.log 2>&1 &
COORD_PID=$!

# Wait for coordinator to start
sleep 3

# Run worker processes
for i in {1..3}
do
    mvn exec:java -Dexec.mainClass="MainWorker" > logs/worker-$i.log 2>&1 &
done

# Wait for all processes to complete
wait $COORD_PID