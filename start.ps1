# Red compartida (si ya existe, no pasa nada)
docker network inspect pedidos360-net *> $null
if ($LASTEXITCODE -ne 0) { docker network create pedidos360-net }

function Wait-Healthy($name) {
  Write-Host "Esperando $name..."
  do {
    Start-Sleep -Seconds 5
    $s = docker inspect --format '{{.State.Health.Status}}' $name 2>$null
  } while ($s -ne 'healthy')
}

# 1. Kafka + Zookeeper
docker compose -f docker-compose.kafka.yml up -d
Wait-Healthy pedidos360-kafka
Wait-Healthy pedidos360-kafka2

# 2. RabbitMQ
docker compose -f docker-compose.rabbitmq.yml up -d
Wait-Healthy rabbitmq1
Wait-Healthy rabbitmq2

# 3. Kafka UI
docker compose -f docker-compose.kafka-ui.yml up -d

# 4. Microservicios
docker compose -f docker-compose.yml up -d --build

docker ps --format "table {{.Names}}\t{{.Status}}"