#!/bin/bash

SERVICES=(
  customer-service
  fraud-service
  gateway-service
  notification-service
  payment-service
  settlement-service
  wallet-service
)

for service in "${SERVICES[@]}"; do
  echo "========== Building $service =========="
  mvn -f "$service/pom.xml" clean package -DskipTests
  if [ $? -ne 0 ]; then
    echo "FAILED: $service"
    exit 1
  fi
done

echo "========== All services built successfully =========="
