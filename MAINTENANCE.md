# Runbook & Maintenance Guide: ECS Sidecar Tunnel Troubleshooting

This document explains the architecture of the zero-inbound-port public routing tunnel, why it can go offline, and step-by-step runbooks for debugging and restoring service.

---

## 🗺️ Sidecar Tunnel Architecture

To maximize security, our AWS ECS Fargate Task is designed with **zero inbound ports exposed to the public internet** (no security group ingress rules on port `8080`).

```
Public Request (https://shrewdly-oval-rockstar.ngrok-free.dev)
       │
       ▼
   ngrok / Cloudflare Edge
       │ (Secure outbound TCP connection)
       ▼
┌────────────────── AWS ECS Fargate Task Namespace ──────────────────┐
│                                                                    │
│  ┌─────────────────────────┐          ┌─────────────────────────┐  │
│  │   Sidecar Container     │  Local   │  Primary App Container  │  │
│  │  (ngrok / cloudflared)  │ ─────────►│      (ledger-api)       │  │
│  │                         │ Loopback │                         │  │
│  └─────────────────────────┘          └─────────────────────────┘  │
└────────────────────────────────────────────────────────────────────┘
```

Because both containers share the **same network namespace** within the Fargate task:
1. The sidecar container opens an *outbound* persistent connection to the edge provider (ngrok/Cloudflare).
2. It proxies incoming public traffic directly to the primary container via local loopback (`127.0.0.1:8080`).

---

## 🚨 Common Reasons for Endpoint Offline Status

If the public domain (e.g., `shrewdly-oval-rockstar.ngrok-free.dev`) is returning a **502 Bad Gateway**, a **504 Gateway Timeout**, or **Connection Refused**, it is usually due to one of the following:

1. **The Fargate Task Restarted / Rotated:**
   - AWS Fargate occasionally terminates and reprovisions instances for underlying platform updates.
   - When a task restarts, the sidecar container must reconnect. If using an ephemeral tunnel (like Cloudflare's `trycloudflare.com` quick tunnels), **a brand new dynamic URL is generated**, rendering the old one in the CV/docs stale.
2. **Ngrok Free-Tier Limits / Tunnel Session Timeout:**
   - Free-tier ngrok tunnels sometimes enforce an automatic timeout or session expiry, requiring a restart of the tunnel container to reconnect.
3. **Application Container Crash (JVM OutOfMemory or startup error):**
   - If the Java `ledger-api` container crashes, the sidecar is still online but cannot proxy requests to `127.0.0.1:8080`, resulting in a `502 Bad Gateway`.
4. **Invalid / Expired Authentication Secrets:**
   - If the ngrok `authtoken` or `api_key` has expired, or the Cloudflare Tunnel credentials became out-of-sync with AWS Secrets Manager/Systems Manager Parameter Store.

---

## 🛠️ Step-by-Step Debugging Guide

When the endpoint goes offline, follow this diagnostic runbook:

### Step 1: Check Fargate Task & Service Status
Verify whether the container is actually running or stuck in a crash loop on ECS:
```bash
# List running tasks in the cluster
aws ecs list-tasks --cluster spring-bank-ledger-cluster --region ap-northeast-1

# Describe the service status
aws ecs describe-services \
  --cluster spring-bank-ledger-cluster \
  --services spring-bank-ledger-service \
  --region ap-northeast-1
```

### Step 2: Read Container Logs (CloudWatch)
This is the most critical step to differentiate between an app crash and a tunnel failure.

* **Check the Sidecar (Tunnel) Logs:**
  Look for tunnel connection status, session timeouts, or authentication errors:
  ```bash
  aws logs filter-log-events \
    --log-group-name "/ecs/spring-bank-ledger" \
    --filter-pattern "tunnel" \
    --limit 50 \
    --region ap-northeast-1
  ```
* **Check the Primary Application (`ledger-api`) Logs:**
  Check if the JVM booted correctly, or if there is an unhandled `OutOfMemoryError` or database connection crash on startup:
  ```bash
  aws logs filter-log-events \
    --log-group-name "/ecs/spring-bank-ledger" \
    --filter-pattern "Starting LedgerApiApplication" \
    --limit 50 \
    --region ap-northeast-1
  ```

### Step 3: Check Tunnel Provider Dashboard
- **Ngrok:** Log in to your [ngrok Dashboard](https://dashboard.ngrok.com/) and navigate to **Tunnels -> Active Tunnels** to check if your active agent is registered and connected.
- **Cloudflare:** Log in to your Cloudflare Zero Trust dashboard and check **Access -> Tunnels** to verify the health status.

---

## 🔧 Actionable Fixes

### Fix A: Force a Fresh redeployment (Fastest Solution)
If the sidecar or app container is frozen, stale, or timed out, triggering an ECS redeployment will terminate the old task and boot a fresh instance with active sessions:
```bash
aws ecs update-service \
  --cluster spring-bank-ledger-cluster \
  --service spring-bank-ledger-service \
  --force-new-deployment \
  --region ap-northeast-1
```

### Fix B: Update Ephemeral Domain (If URL changed)
If you are using Cloudflare's ephemeral quick tunnels (`trycloudflare`), read the newly generated URL from the container logs after restarting the service:
```bash
# Find the new quick tunnel URL in the sidecar logs
aws logs filter-log-events \
  --log-group-name "/ecs/spring-bank-ledger" \
  --filter-pattern "trycloudflare.com" \
  --region ap-northeast-1
```
Then, update your **CV/docs** with the new URL.

### Fix C: Re-align Terraform Secrets
If you changed your ngrok static domain or auth token, update your Terraform configuration or variable overrides:
1. Update `terraform.tfvars` or AWS SSM Parameters.
2. Re-apply IaC configurations:
   ```bash
   cd terraform
   terraform apply -auto-approve
   ```
3. Trigger a force deployment (Fix A) to pull the new task definition values.
