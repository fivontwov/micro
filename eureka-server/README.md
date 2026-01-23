# Eureka Service Discovery Server

Netflix Eureka Server for microservices discovery and registration.

## Overview

Eureka Server acts as a service registry where all microservices register themselves. Other services can discover and communicate with each other through Eureka.

## Architecture

```
┌─────────────────────────────────────────┐
│      Eureka Server (Port 8761)          │
│    http://localhost:8761/eureka         │
└─────────────┬───────────────────────────┘
              │
    ┌─────────┼─────────┬─────────────┐
    │         │         │             │
    ▼         ▼         ▼             ▼
┌────────┐ ┌─────┐ ┌─────────┐ ┌──────────┐
│Gateway │ │Forum│ │  Study  │ │  Future  │
│:8080   │ │:8081│ │  :8082  │ │ Services │
└────────┘ └─────┘ └─────────┘ └──────────┘
```

## Features

- **Service Registry**: All microservices register with Eureka
- **Service Discovery**: Services can find each other by name
- **Health Checks**: Monitor service health
- **Load Balancing**: Automatic load balancing through ribbon/load balancer
- **Dashboard**: Web UI to view registered services

## Running

### Standalone
```bash
cd eureka-server
mvn spring-boot:run
```

### Docker
```bash
docker build -t eureka-server .
docker run -p 8761:8761 eureka-server
```

### With Docker Compose
```bash
# From project root
docker-compose up eureka-server
```

## Accessing Dashboard

Once running, open browser:
```
http://localhost:8761
```

You'll see:
- Registered instances
- Service status
- Availability zones
- Renew and fetch registry information

## Configuration

### application.yml
```yaml
server:
  port: 8761

eureka:
  client:
    register-with-eureka: false  # Don't register itself
    fetch-registry: false
  server:
    enable-self-preservation: false  # Disable in dev
```

## Registered Services

Once all services are running, you should see:

| Service Name | Instance ID | Status | Port |
|-------------|-------------|--------|------|
| API-GATEWAY | api-gateway:8080 | UP | 8080 |
| FORUM-SERVICE | forum-service:8081 | UP | 8081 |
| STUDY-MANAGEMENT-SERVICE | study-management-service:8082 | UP | 8082 |

## Client Configuration

Services need to add Eureka client dependency and configuration:

### pom.xml
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

### application.yml
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

## Health Check

```bash
# Check if Eureka is running
curl http://localhost:8761/actuator/health

# Get all registered services
curl http://localhost:8761/eureka/apps
```

## Troubleshooting

### No services showing up
1. Check if services have Eureka client dependency
2. Verify `eureka.client.service-url.defaultZone` is correct
3. Check service logs for registration errors

### Services showing as DOWN
1. Check if services are actually running
2. Verify health check endpoints
3. Check network connectivity

## Production Configuration

For production:
1. Enable self-preservation mode
2. Use multiple Eureka servers for high availability
3. Configure proper security
4. Set up monitoring and alerts

## Next Steps

1. ✅ All microservices register with Eureka
2. ✅ API Gateway uses service discovery
3. ⏭️ Configure load balancing
4. ⏭️ Add Circuit Breaker with Resilience4j
5. ⏭️ Implement distributed tracing
