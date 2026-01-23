# Microservices Architecture - Complete Overview

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         CLIENT                              │
│                  (Web, Mobile, Desktop)                     │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            │ HTTP Requests
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    API GATEWAY (8080)                       │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  - Request Routing                                     │ │
│  │  - Load Balancing (via Eureka)                        │ │
│  │  - CORS                                                │ │
│  │  - Logging                                             │ │
│  │  - Health Checks                                       │ │
│  └────────────────────────────────────────────────────────┘ │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            │ Service Discovery
                            ▼
┌─────────────────────────────────────────────────────────────┐
│              EUREKA SERVICE REGISTRY (8761)                 │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  Registered Services:                                  │ │
│  │  - api-gateway:8080           [UP]                    │ │
│  │  - forum-service:8081         [UP]                    │ │
│  │  - study-management-service:8082  [UP]                │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────┬───────────────────────┬───────────────────────┘
              │                       │
    ┌─────────┴────────┐    ┌────────┴──────────┐
    │                  │    │                   │
    ▼                  ▼    ▼                   ▼
┌─────────┐      ┌─────────────┐          ┌──────────┐
│ FORUM   │      │   STUDY     │          │  FUTURE  │
│ SERVICE │◄────►│ MANAGEMENT  │          │ SERVICES │
│ (8081)  │ gRPC │  SERVICE    │          │          │
│         │      │   (8082)    │          │          │
└────┬────┘      └──────┬──────┘          └──────────┘
     │                  │
     │                  │
     ▼                  ▼
┌─────────────────────────────────────┐
│      POSTGRESQL DATABASE (5432)     │
│  ┌───────────┐    ┌───────────────┐ │
│  │  forum    │    │study_mgmt     │ │
│  │  database │    │database       │ │
│  └───────────┘    └───────────────┘ │
└─────────────────────────────────────┘
           │
           ▼
┌─────────────────────────┐
│    REDIS CACHE (6379)   │
│  - Session Storage      │
│  - Rate Limiting        │
└─────────────────────────┘
```

## 📊 Service Details

### 1. Eureka Server (Port 8761)
**Purpose:** Service Discovery & Registry

**Responsibilities:**
- Service registration
- Service health monitoring
- Service instance management
- Load balancing support

**Technology:**
- Spring Cloud Netflix Eureka Server
- Spring Boot 3.2.0

**Endpoints:**
- Dashboard: `http://localhost:8761`
- API: `http://localhost:8761/eureka/apps`

---

### 2. API Gateway (Port 8080)
**Purpose:** Single Entry Point for all client requests

**Responsibilities:**
- Request routing to appropriate services
- Service discovery integration
- Load balancing
- Cross-cutting concerns (logging, CORS)
- Future: Authentication, rate limiting

**Technology:**
- Spring Cloud Gateway
- Spring Cloud Netflix Eureka Client
- Spring Boot 3.2.0

**Routes:**
```yaml
/api/forum/**  → lb://forum-service
/api/study/**  → lb://study-management-service
```

**Key Features:**
- Dynamic routing via Eureka service names
- StripPrefix filter (removes /api/{service})
- Request header injection (X-Source: Gateway)
- Health checks & monitoring

---

### 3. Forum Service (Port 8081)
**Purpose:** Forum/Discussion management

**Responsibilities:**
- Topic CRUD operations
- Comment management
- Voting system
- User info via gRPC call to Study Management

**Technology:**
- Spring Boot 4.0.1
- Spring Data JPA
- PostgreSQL
- gRPC Client
- Eureka Client

**API Endpoints:**
```
GET    /topics
POST   /topics
GET    /topics/{id}
DELETE /topics/{id}
POST   /topics/{id}/comments
GET    /topics/{id}/comments
POST   /topics/{id}/votes
```

**Database:** PostgreSQL (forum database)

**Service Name:** `forum-service`

---

### 4. Study Management Service (Port 8082, gRPC: 9090)
**Purpose:** User & Study management with gRPC server

**Responsibilities:**
- User authentication (JWT)
- User management
- Subject management
- Subject registration
- Mentor-mentee management
- gRPC server for user info

**Technology:**
- Spring Boot 3.2.5
- Spring Data JPA
- Spring Security + JWT
- PostgreSQL
- Redis (caching)
- gRPC Server
- MapStruct (object mapping)
- Eureka Client

**API Endpoints:**
```
POST   /auth/login
POST   /auth/register
GET    /users
GET    /subjects
POST   /subject-registrations
POST   /mentor-mentee-registrations
```

**gRPC Service:**
```protobuf
service UserService {
    rpc GetUserById (UserRequest) returns (UserResponse);
}
```

**Database:** PostgreSQL (study_management database)

**Cache:** Redis

**Service Name:** `study-management-service`

---

## 🔀 Request Flow

### Example 1: Get Topics
```
1. Client → GET http://localhost:8080/api/forum/topics
2. API Gateway:
   - Receives request
   - Logs request
   - Queries Eureka for "forum-service"
   - Routes to forum-service instance
   - Strips "/api/forum" prefix
3. Forum Service:
   - Receives GET /topics
   - Queries database
   - Returns topics
4. API Gateway:
   - Logs response
   - Returns to client
```

### Example 2: Get Topic with User Info (gRPC)
```
1. Client → GET http://localhost:8080/api/forum/topics/1
2. API Gateway → forum-service
3. Forum Service:
   - Gets topic from database
   - Calls gRPC to study-management-service
   - GetUserById(userId)
4. Study Management (gRPC):
   - Returns user info
5. Forum Service:
   - Combines topic + user data
   - Returns to gateway
6. API Gateway → Client
```

## 🔐 Security Model

### Current (Development):
- All endpoints: `permitAll()`
- No authentication required

### Future (Production):
```
Public Endpoints:
  - /api/study/auth/login
  - /api/study/auth/register

Protected Endpoints (JWT Required):
  - /api/forum/**
  - /api/study/users/**
  - /api/study/subjects/**
```

## 📦 Data Storage

### PostgreSQL Databases:
1. **forum** - Forum service data
   - topics
   - comments
   - topic_votes

2. **study_management** - Study management data
   - users
   - subjects
   - subject_registrations
   - mentor_mentee_registrations

### Redis Cache:
- User sessions
- API rate limiting data
- Cache for frequently accessed data

## 🔄 Service Communication

### REST (HTTP):
- Client ↔ API Gateway
- API Gateway ↔ Backend Services

### gRPC:
- Forum Service ↔ Study Management Service
  - GetUserById() calls

### Service Discovery:
- All services ↔ Eureka Server
  - Registration
  - Health checks
  - Service lookup

## 🐳 Deployment

### Development:
```bash
# Start individually
cd eureka-server && mvn spring-boot:run
cd forum && mvn spring-boot:run
cd ssstudy_management && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
```

### Production (Docker):
```bash
docker-compose up --build
```

## 📈 Scalability

### Horizontal Scaling:
```yaml
# Run multiple instances
forum-service:
  replicas: 3  # Gateway auto load-balances via Eureka

study-management-service:
  replicas: 2
```

### Load Balancing:
- Automatic via Spring Cloud LoadBalancer
- Round-robin distribution
- Instance health-based routing

## 🔍 Monitoring & Observability

### Endpoints:
```
Eureka Dashboard:   http://localhost:8761
Gateway Health:     http://localhost:8080/actuator/health
Gateway Routes:     http://localhost:8080/actuator/gateway/routes
Forum Health:       http://localhost:8081/actuator/health
Study Mgmt Health:  http://localhost:8082/actuator/health
```

### Logging:
- All requests logged in Gateway
- Structured logging in all services
- Request ID tracking

## 🚀 Future Enhancements

### Phase 1: Security
- [ ] JWT authentication in Gateway
- [ ] OAuth2 integration
- [ ] API key management

### Phase 2: Resilience
- [ ] Circuit Breaker (Resilience4j)
- [ ] Retry logic
- [ ] Timeout policies
- [ ] Bulkhead pattern

### Phase 3: Performance
- [ ] Redis rate limiting
- [ ] Response caching
- [ ] Database connection pooling
- [ ] gRPC connection pooling

### Phase 4: Observability
- [ ] Distributed tracing (Sleuth + Zipkin)
- [ ] Metrics collection (Prometheus)
- [ ] Grafana dashboards
- [ ] Centralized logging (ELK)

### Phase 5: Configuration
- [ ] Spring Cloud Config Server
- [ ] Externalized configuration
- [ ] Secret management (Vault)

## 📚 Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Java | 17 |
| Build Tool | Maven | 3.6+ |
| Framework | Spring Boot | 3.2.x / 4.0.x |
| Service Discovery | Netflix Eureka | 2023.0.0 |
| API Gateway | Spring Cloud Gateway | 2023.0.0 |
| Database | PostgreSQL | 15 |
| Cache | Redis | 7 |
| RPC | gRPC | 1.58.0 |
| ORM | Spring Data JPA | - |
| Authentication | Spring Security + JWT | jjwt 0.12.6 |
| Object Mapping | MapStruct | 1.6.3 |
| Code Generation | Lombok | 1.18.x |
| Containerization | Docker | - |

## 🎯 Design Principles

1. **Single Responsibility**: Each service has one clear purpose
2. **Loose Coupling**: Services communicate via well-defined APIs
3. **High Cohesion**: Related functionality grouped together
4. **Service Discovery**: No hardcoded service locations
5. **API Gateway Pattern**: Single entry point for clients
6. **Database per Service**: Each service owns its data
7. **Polyglot Persistence**: Different storage for different needs (PostgreSQL + Redis)
8. **Fault Tolerance**: Graceful degradation when services fail
