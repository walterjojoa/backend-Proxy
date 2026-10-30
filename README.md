# AI Platform with Rate Limiting and Quota Proxies — Backend

Spring Boot backend for the **Proxy Pattern** workshop. Users consume a simulated AI text generation
service; two chained proxies control how many requests each user can make per minute and how many
tokens they can spend per month, according to their plan.

## Proxy pattern

```
Client (AIController)
        │  AIGenerationService
        ▼
RateLimitProxyService   → 429 Too Many Requests + Retry-After
        │  AIGenerationService
        ▼
QuotaProxyService       → 402 Payment Required, discounts tokens
        │  AIGenerationService
        ▼
MockAIGenerationService (real subject: Thread.sleep(1200ms) + predefined text)
```

| Role in the pattern | Class |
|---|---|
| Subject (common interface) | `service/AIGenerationService` |
| Real subject | `service/MockAIGenerationService` |
| Proxy 1 | `proxy/RateLimitProxyService` |
| Proxy 2 | `proxy/QuotaProxyService` |
| Chain assembly | `config/ProxyChainConfig` |

The controller depends only on `AIGenerationService`; it does not know whether it is talking to a
proxy or to the real service.

## Plans

| Plan | Requests / minute | Tokens / month |
|---|---|---|
| FREE | 10 | 50,000 |
| PRO | 60 | 500,000 |
| ENTERPRISE | unlimited | unlimited |

Tokens are estimated as `ceil(characters / 4)` for the prompt plus the generated text.

## API

Every endpoint identifies the user with the `X-User-Id` header. A new user starts on the FREE plan.

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/ai/generate` | Body `{"prompt": "..."}`. Goes through the full proxy chain |
| GET | `/api/quota/status` | Tokens used, tokens remaining, reset date, current plan and rate-limit state |
| GET | `/api/quota/history` | Daily usage for the last 7 days |
| POST | `/api/quota/upgrade` | Body `{"plan": "PRO"}` (optional, defaults to PRO) |
| GET | `/api/health` | Health check |

Unlimited values (ENTERPRISE) are returned as `null`.

### Example

```bash
curl -X POST http://localhost:8080/api/ai/generate \
  -H "X-User-Id: walter" -H "Content-Type: application/json" \
  -d '{"prompt": "What is the proxy pattern?"}'
```

## Scheduled tasks

- Every minute (second 0): resets the requests-per-minute counters.
- First day of every month at 00:00: resets the monthly token quota.

## Run locally

Requires Java 17.

```bash
./mvnw spring-boot:run      # starts on http://localhost:8080
./mvnw test                 # runs the tests
```

## Configuration

| Environment variable | Default | Description |
|---|---|---|
| `PORT` | `8080` | HTTP port (set automatically by Railway) |
| `CORS_ALLOWED_ORIGINS` | `*` | Comma-separated allowed origins, e.g. `https://my-app.vercel.app` |
| `APP_TIMEZONE` | `America/Bogota` | Time zone for daily history and resets |
| `MOCK_DELAY_MS` | `1200` | Simulated latency of the AI service |

## Deployment (Railway)

The repository includes a `Dockerfile`. Create a new Railway project from this GitHub repository,
generate a public domain and set `CORS_ALLOWED_ORIGINS` to the Vercel URL of the frontend.

> Data is stored in memory, so usage is reset when the service restarts.
