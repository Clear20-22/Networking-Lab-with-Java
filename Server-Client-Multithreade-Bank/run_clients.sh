#!/bin/bash

# Script to run multiple client instances
# Usage: ./run_clients.sh [number_of_clients]

NUM_CLIENTS=${1:-2}  # Default to 2 if no argument

echo "Starting $NUM_CLIENTS clients..."

for ((i=1; i<=NUM_CLIENTS; i++))
do
    echo "Starting client $i..."
    java Client &
    sleep 1  # Small delay to avoid overwhelming the server
done

echo "All clients started. Press Ctrl+C to stop."

# Wait for all background processes
wait