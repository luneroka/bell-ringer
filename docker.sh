#!/bin/bash

# Bell-Ringer Docker Management Script

set -e

COMMAND=${1:-help}

case $COMMAND in
  "dev")
    echo "🔧 Starting Bell-Ringer in development mode..."
    docker-compose -f docker-compose.yml -f docker-compose.dev.yml up --build
    ;;
  "prod")
    echo "🚀 Starting Bell-Ringer in production mode..."
    docker-compose up --build -d
    ;;
  "stop")
    echo "⏹️  Stopping Bell-Ringer..."
    docker-compose -f docker-compose.yml -f docker-compose.dev.yml down
    ;;
  "logs")
    echo "📋 Showing logs..."
    docker-compose -f docker-compose.yml -f docker-compose.dev.yml logs -f ${2:-}
    ;;
  "clean")
    echo "🧹 Cleaning up containers and volumes..."
    docker-compose -f docker-compose.yml -f docker-compose.dev.yml down -v
    docker system prune -f
    ;;
  "db-only")
    echo "🗄️  Starting database only..."
    docker-compose up postgres -d
    ;;
  "build")
    echo "🔨 Building backend image..."
    docker-compose build backend
    ;;
  "build-frontend")
    echo "🎨 Building frontend image..."
    docker-compose -f docker-compose.yml -f docker-compose.dev.yml build frontend
    ;;
  "build-all")
    echo "🔨 Building all images..."
    docker-compose -f docker-compose.yml -f docker-compose.dev.yml build
    ;;
  "shell")
    echo "🐚 Opening shell in backend container..."
    docker-compose exec backend sh
    ;;
  "shell-frontend")
    echo "🎨 Opening shell in frontend container..."
    docker-compose -f docker-compose.yml -f docker-compose.dev.yml exec frontend /bin/bash
    ;;
  "frontend-only")
    echo "🎨 Starting frontend only..."
    docker-compose -f docker-compose.yml -f docker-compose.dev.yml up frontend -d
    ;;
  "npm")
    if [ -z "$2" ]; then
      echo "❌ Please specify npm command"
      echo "Example: ./docker.sh npm install axios"
      exit 1
    fi
    echo "📦 Running npm ${@:2} in frontend container..."
    docker-compose -f docker-compose.yml -f docker-compose.dev.yml exec frontend npm ${@:2}
    ;;
  "restart-frontend")
    echo "🔄 Restarting frontend container..."
    docker-compose -f docker-compose.yml -f docker-compose.dev.yml restart frontend
    ;;
  "help"|*)
    echo "Bell-Ringer Docker Management"
    echo ""
    echo "Usage: ./docker.sh [command]"
    echo ""
    echo "🚀 Main Commands:"
    echo "  dev              - Start in development mode (with live reload)"
    echo "  prod             - Start in production mode (detached)"
    echo "  stop             - Stop all services"
    echo "  logs [service]   - Show logs (optionally specify service name)"
    echo "  clean            - Stop services and remove volumes"
    echo ""
    echo "🔨 Build Commands:"
    echo "  build            - Build the backend image"
    echo "  build-frontend   - Build the frontend image"
    echo "  build-all        - Build all images"
    echo ""
    echo "🎯 Service-Specific Commands:"
    echo "  db-only          - Start only the database"
    echo "  frontend-only    - Start only the frontend"
    echo "  restart-frontend - Restart frontend container"
    echo ""
    echo "🐚 Shell Access:"
    echo "  shell            - Open shell in backend container"
    echo "  shell-frontend   - Open shell in frontend container"
    echo ""
    echo "📦 Frontend Package Management:"
    echo "  npm <command>    - Run npm command in frontend container"
    echo ""
    echo "Examples:"
    echo "  ./docker.sh dev                    # Start development environment"
    echo "  ./docker.sh logs frontend          # Show frontend logs"
    echo "  ./docker.sh npm install axios      # Install axios in frontend"
    echo "  ./docker.sh shell-frontend         # Enter frontend container"
    echo "  ./docker.sh build-frontend         # Rebuild frontend only"
    ;;
esac
