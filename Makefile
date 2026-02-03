.PHONY: start stop clean logs

# Start all services in the background
start:
	docker compose up -d --build

# Stop all services
stop:
	docker compose down

# Stop services, remove volumes (database), and clear uploaded images
clean:
	docker compose down -v
	rm -rf uploads/*
	@echo "Database and uploads cleaned."

# View logs
logs:
	docker compose logs -f

# Start Jenkins
jenkins:
#docker compose -f docker-compose.jenkins.yml up -d    
	docker compose -f docker-compose.jenkins.yml up -d --build

# Start SonarQube
sonarqube:
	docker compose -f docker-compose.sonarqube.yml up -d
