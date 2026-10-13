# AI-Assisted Smart Task & Note Management System (Azure)

Services: note-service (8081, MongoDB), task-service (8082, PostgreSQL), ai-service (8083, Gemini API).
Observability: Prometheus (9090), Loki + Promtail, Grafana (3000).

## Run locally
    cp .env.example .env      # optionally add GEMINI_API_KEY
    docker compose up --build

## Try it
    curl -X POST localhost:8081/notes -H "Content-Type: application/json" \
      -d '{"title":"Cloud","content":"Docker packages apps into containers...","tags":["study"]}'
    curl -X POST localhost:8081/notes/<id>/summarize
    curl -X POST localhost:8082/tasks -H "Content-Type: application/json" \
      -d '{"title":"Finish report","priority":"HIGH","dueDate":"2026-10-20"}'
    curl localhost:8081/debug/error     # induce an error, then find it in Grafana

## Deploy to Azure
    cd terraform
    terraform init
    terraform apply -var subscription_id=<id> -var allowed_cidr=<your-ip>/32
    ssh azureuser@<vm_public_ip>   # then: git clone <repo> && cd <repo> && docker compose up -d --build
