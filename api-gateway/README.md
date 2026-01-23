# API Gateway for Microservices

This API Gateway serves as a single entry point for all client requests to your microservices architecture.

## Features

- **Routing**: Routes requests to appropriate services (Forum, Study Management)
- **Security**: JWT authentication and authorization
- **Rate Limiting**: Prevents abuse with configurable limits
- **Logging**: Comprehensive request/response logging
- **CORS**: Cross-origin resource sharing configuration
- **Circuit Breaker**: Fault tolerance for service failures
- **Monitoring**: Actuator endpoints for health checks and metrics

## Architecture

```
                    ┌──────────────────────────────┐
                    │         API Gateway           │
                    │        (Port: 8080)          │
Client → Request →  │  - Request Routing           │
                    │  - Logging                   │
                    │  - CORS                      │
                    │  - Load Balancing            │
                    └──┬───────────┬───────────────┘
                       │           │
                       ▼           ▼
                  ┌────────┐  ┌────────────┐
                  │ Forum  │  │ Study Mgmt │
                  │Service │  │  Service   │
                  │:8081   │  │   :8082    │
                  └────────┘  └────────────┘
```

## API Routes

### Forum Service (Port 8081)
- `GET /api/forum/topics` → `GET /topics` (Forum Service)
- `POST /api/forum/topics` → `POST /topics` (Forum Service)
- `GET /api/forum/topics/1` → `GET /topics/1` (Forum Service)
- `POST /api/forum/topics/1/comments` → `POST /topics/1/comments` (Forum Service)

### Study Management Service (Port 8082)
- `POST /api/study/auth/login` → `POST /auth/login` (Study Management)
- `GET /api/study/users` → `GET /users` (Study Management)
- `GET /api/study/subjects` → `GET /subjects` (Study Management)

## Running Locally

### Prerequisites
- Java 17+
- Maven 3.6+
- Redis (for rate limiting)

### Start Services
1. Start Redis:
   ```bash
   docker run -d -p 6379:6379 redis:7-alpine
   ```

2. Start your microservices (Forum and Study Management)

3. Start API Gateway:
   ```bash
   cd api-gateway
   mvn spring-boot:run
   ```

### Test Gateway
```bash
# Test forum service through gateway
curl http://localhost:8080/api/forum/topics

# Test study management through gateway
curl http://localhost:8080/api/study/users
```

## Docker Deployment

### Build and Run All Services
```bash
# From project root
docker-compose up --build
```

### Individual Service
```bash
# Build gateway
cd api-gateway
docker build -t api-gateway .

# Run gateway
docker run -p 8080:8080 api-gateway
```

## Configuration

### application.yml
- **Server Port**: 8080
- **Routes**: Configured for forum (8081) and study (8082) services
- **StripPrefix**: 2 (removes /api/{service-name} from path)
- **Security**: Disabled for now (permitAll)
- **Redis**: Commented out (enable when needed)
- **CORS**: Allow all origins (configure for production)

### Environment Variables
- `SPRING_PROFILES_ACTIVE`: Set to `docker` for containerized deployment
- `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI`: JWT issuer URI

## Monitoring

### Actuator Endpoints
- Health: `GET /actuator/health`
- Metrics: `GET /actuator/metrics`
- Gateway Routes: `GET /actuator/gateway/routes`
- Gateway Filters: `GET /actuator/gateway/routefilters`

### Logs
All requests are logged with:
- Request method and path
- Client IP
- Response time
- Status code

## Security

### JWT Authentication
- Protected routes require valid JWT token
- `/api/auth/**` endpoints are public
- Token validation via OAuth2 resource server

### Rate Limiting
- Configurable via Redis
- Default: 100 requests/minute per user
- Key resolver uses `X-User-Id` header

## Development

### Adding New Routes
1. Update `application.yml` routes section
2. Add predicates and filters as needed
3. Test the new route

### Custom Filters
Implement `GlobalFilter` interface in `filter` package:
```java
@Component
public class CustomFilter implements GlobalFilter, Ordered {
    // Implementation
}
```

## Production Considerations

1. **Security**:
   - Configure proper CORS origins
   - Use HTTPS
   - Set secure JWT issuer URI

2. **Rate Limiting**:
   - Adjust limits based on service capacity
   - Use distributed Redis cluster

3. **Monitoring**:
   - Integrate with Prometheus/Grafana
   - Set up alerts for service health

4. **Load Balancing**:
   - Use service discovery (Eureka/Consul)
   - Configure load balancer URIs

## Troubleshooting

### Common Issues
1. **Connection Refused**: Ensure backend services are running
2. **Authentication Failed**: Check JWT token and issuer URI
3. **Rate Limited**: Wait for rate limit reset or increase limits

### Logs
Check gateway logs for detailed error information:
```bash
docker logs micro-api-gateway
```

## Next Steps

1. Implement service discovery
2. Add circuit breaker patterns
3. Configure distributed tracing
4. Set up API documentation (Swagger)
5. Implement request/response transformation
