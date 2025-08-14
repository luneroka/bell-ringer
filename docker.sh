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
    docker-compose down
    ;;
  "logs")
    echo "📋 Showing logs..."
    docker-compose logs -f ${2:-}
    ;;
  "clean")
    echo "🧹 Cleaning up containers and volumes..."
    docker-compose down -v
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
  "shell")
    echo "🐚 Opening shell in backend container..."
    docker-compose exec backend sh
    ;;
  "help"|*)
    echo "Bell-Ringer Docker Management"
    echo ""
    echo "Usage: ./docker.sh [command]"
    echo ""
    echo "Commands:"
    echo "  dev      - Start in development mode (with live reload)"
    echo "  prod     - Start in production mode (detached)"
    echo "  stop     - Stop all services"
    echo "  logs     - Show logs (optionally specify service name)"
    echo "  clean    - Stop services and remove volumes"
    echo "  db-only  - Start only the database"
    echo "  build    - Build the backend image"
    echo "  shell    - Open shell in backend container"
    echo "  help     - Show this help message"
    echo ""
    echo "Examples:"
    echo "  ./docker.sh dev          # Start development environment"
    echo "  ./docker.sh logs backend # Show backend logs"
    echo "  ./docker.sh prod         # Start production environment"
    ;;
esac
