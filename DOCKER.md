# Bell-Ringer Docker Setup

This directory contains the Docker configuration for the Bell-Ringer application.

## Quick Start

### Prerequisites

- Docker and Docker Compose installed
- Firebase project configured (update `.env` with your Firebase credentials)

### Development Mode

```bash
# Start the application in development mode
./docker.sh dev

# Or manually:
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up --build
```

### Production Mode

```bash
# Start the application in production mode
./docker.sh prod

# Or manually:
docker-compose up --build -d
```

## Configuration

### Environment Variables

Update the `.env` file with your configuration:

```env
# Database
POSTGRES_DB=bell_ringer_db
POSTGRES_USER=your_username
POSTGRES_PASSWORD=your_password

# Firebase (required for authentication)
FIREBASE_PROJECT_ID=your-project-id
FIREBASE_PRIVATE_KEY_ID=your-private-key-id
FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n"
FIREBASE_CLIENT_EMAIL=your-service-account@your-project.iam.gserviceaccount.com
FIREBASE_CLIENT_ID=your-client-id
FIREBASE_CLIENT_X509_CERT_URL=https://www.googleapis.com/robot/v1/metadata/x509/your-service-account%40your-project.iam.gserviceaccount.com
```

## Services

### Backend Service

- **Port**: 8080
- **Health Check**: `/actuator/health`
- **Technology**: Spring Boot 3.5.4 with Java 21
- **Profile**: Automatically set based on environment

### PostgreSQL Database

- **Port**: 5332 (external), 5432 (internal)
- **Version**: PostgreSQL 15 Alpine
- **Data**: Persisted in Docker volume `postgres_data`
- **Initialization**: Seed data loaded from `backend/src/main/resources/seed/`

## Docker Management Script

Use the included `docker.sh` script for common operations:

```bash
./docker.sh dev      # Development mode
./docker.sh prod     # Production mode
./docker.sh stop     # Stop all services
./docker.sh logs     # View logs
./docker.sh clean    # Clean up containers and volumes
./docker.sh db-only  # Start only database
./docker.sh build    # Build backend image
./docker.sh shell    # Open shell in backend container
```

## Database Access

### From Host Machine

```bash
# Connect to database from host
psql -h localhost -p 5332 -U your_username -d bell_ringer_db
```

### From Backend Container

The backend automatically connects to the database using the internal network.

## Troubleshooting

### Check Service Health

```bash
# Check all services
docker-compose ps

# Check backend health
curl http://localhost:8080/actuator/health

# Check database connection
docker-compose exec postgres pg_isready -U your_username -d bell_ringer_db
```

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f postgres
```

### Rebuild Services

```bash
# Rebuild backend only
docker-compose build backend

# Rebuild all services
docker-compose build

# Force rebuild (no cache)
docker-compose build --no-cache
```

## Development vs Production

### Development Mode

- Uses `application-dev.properties`
- Connects to database on localhost:5332
- Verbose logging enabled
- Live reload capabilities

### Production Mode

- Uses `application-prod.properties`
- Connects to database via Docker network
- Optimized logging
- Security hardened (non-root user)

## Security Notes

- The backend runs as a non-root user (appuser:appgroup)
- PostgreSQL data is persisted in a Docker volume
- Environment variables are used for all sensitive configuration
- Health checks ensure services are running properly

## File Structure

```
/
├── docker-compose.yml          # Main Docker Compose configuration
├── docker-compose.dev.yml      # Development overrides
├── docker.sh                   # Management script
├── .env                        # Environment variables
└── backend/
    ├── Dockerfile              # Backend container definition
    ├── .dockerignore           # Files to exclude from build
    └── src/main/resources/
        ├── application.properties
        ├── application-dev.properties
        ├── application-prod.properties
        └── seed/               # Database initialization scripts
```
