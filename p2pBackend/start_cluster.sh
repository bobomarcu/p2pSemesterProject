#!/bin/bash

echo "Building p2pBackend..."
./mvnw clean package -DskipTests

BASE_PATH="/Users/bogdan.marcu/shards"
mkdir -p $BASE_PATH
mkdir -p $BASE_PATH/node1
mkdir -p $BASE_PATH/node2
mkdir -p $BASE_PATH/node3

pkill -f p2pBackend || true

echo "Starting Node 1 (Bootstrap)..."
java -jar target/p2pBackend-0.0.1-SNAPSHOT.jar \
  --server.port=5555 \
  --grpc.server.port=9090 \
  --p2p.server.path=$BASE_PATH/node1 \
  --p2p.server.bootstrap=localhost:5555 \
  --logging.file.name=node1.log &
PID1=$!
echo "Node 1 started (PID: $PID1). Logs at node1.log"

sleep 5

echo "Starting Node 2..."
java -jar target/p2pBackend-0.0.1-SNAPSHOT.jar \
  --server.port=5556 \
  --grpc.server.port=9091 \
  --p2p.server.path=$BASE_PATH/node2 \
  --p2p.server.bootstrap=localhost:5555 \
  --logging.file.name=node2.log &
PID2=$!
echo "Node 2 started (PID: $PID2). Logs at node2.log"

sleep 5

echo "Starting Node 3..."
java -jar target/p2pBackend-0.0.1-SNAPSHOT.jar \
  --server.port=5557 \
  --grpc.server.port=9092 \
  --p2p.server.path=$BASE_PATH/node3 \
  --p2p.server.bootstrap=localhost:5555 \
  --logging.file.name=node3.log &
PID3=$!
echo "Node 3 started (PID: $PID3). Logs at node3.log"

echo "Cluster is running!"
echo "Node 1: http://localhost:5555 (gRPC 9090)"
echo "Node 2: http://localhost:5556 (gRPC 9091)"
echo "Node 3: http://localhost:5557 (gRPC 9092)"
echo "Press Ctrl+C to stop all nodes."

trap "kill $PID1 $PID2 $PID3; exit" INT
wait
