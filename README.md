# AI-Assisted Smart Task & Note Management System (Azure)

Web page (nginx + React, port 8080) -> auth-service (8084), note-service (8081), task-service (8082), ai-service (8083).
Storage: PostgreSQL (users, tasks) and MongoDB (notes). Observability: Prometheus, Loki + Promtail, Grafana (3000).
Users sign up / log in on the web page; each user only sees their own notes and tasks.

## Run locally
    cp .env.example .env      # optionally add GEMINI_API_KEY
    docker compose up -d --build
    open http://localhost:8080

## Deploy to Azure
    cd terraform
    terraform init
    terraform apply -var subscription_id=<id> -var allowed_cidr=<your-ip>/32
    ssh azureuser@<vm_public_ip>   # git clone, create .env (set JWT_SECRET), docker compose up -d --build
    open http://<vm_public_ip>:8080
