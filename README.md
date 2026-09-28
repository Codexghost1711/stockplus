# StockPulse

StockPulse is a local-first inventory and pricing recommendation demo. It tracks product stock and demand, creates explainable pricing and reorder suggestions when inventory or demand changes, and lets a user approve or reject each suggestion.

## How It Works

1. A user records an order or updates a product's stock.
2. The backend saves the product and publishes inventory and demand events.
3. An asynchronous event handler checks for stock below the reorder threshold or demand crossing above 15.
4. The selected commerce strategy creates pricing and reorder recommendations. Pending recommendations for the same product and trigger are not generated again.
5. Suggestions are saved as `PENDING`. Accepting a pricing suggestion changes the product price; accepting a reorder suggestion adds the recommended quantity to stock. Rejected suggestions leave the product unchanged.

The default `RULE` strategy increases price by 10% when stock is below the reorder threshold, otherwise by 5% when demand velocity is above 10, and otherwise holds the price. Reorder quantity is `max((reorder threshold * 3) - current stock, 1)`. The optional `AI` strategy uses the configured LLM gateway and has fallback recommendations; see [LLM_CONFIGURATION.md](LLM_CONFIGURATION.md).

## Project Structure

```text
backend/src/main/java/com/stockpulse/
	product/      Product model, catalog API, order and stock operations
	agent/        Inventory and demand events, asynchronous recommendation handler
	commerce/     Rule-based and AI strategies, shared recommendation contracts
	suggestion/   Suggestion persistence, approval workflow, API
	ai/           LLM gateway implementations and provider selection
frontend/src/
	api/          Backend HTTP client
	components/   Dashboard and suggestion review UI
```

The backend uses Java 17+, Spring Boot 3, Spring Data JPA, and H2. The frontend uses React 18 and Vite. H2 runs in memory for local development; seeded demo data is recreated when the backend restarts.

## Run Locally

Prerequisites: JDK 17 or newer, Maven, and Node.js/npm. See [ENVIRONMENT_SETUP.md](ENVIRONMENT_SETUP.md) for installation help.

In one terminal, run the backend:

```powershell
cd C:\stockplus\backend
mvn clean test
mvn spring-boot:run
```

In a second terminal, run the frontend:

```powershell
cd C:\stockplus\frontend
npm ci
npm run dev
```

Open the frontend at `http://localhost:5173`. The backend API is at `http://localhost:8080`; the H2 console is at `http://localhost:8080/h2-console`.

To verify the frontend production build, run `npm run build` from `frontend/`.

## Core API

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/products` | List products; optional `status` and `category` filters |
| `POST` | `/products/{id}/orders` | Simulate a sale with `{"quantity":1}` |
| `PATCH` | `/products/{id}/stock` | Set stock with `{"stockLevel":10}` |
| `GET` | `/products/{id}/pricing-suggestions` | List pricing suggestions |
| `GET` | `/products/{id}/reorder-suggestions` | List reorder suggestions |
| `PATCH` | `/products/pricing-suggestions/{suggestionId}` | Accept or reject a pricing suggestion |
| `PATCH` | `/products/reorder-suggestions/{suggestionId}` | Accept or reject a reorder suggestion |

Status updates use `{"status":"ACCEPTED"}` or `{"status":"REJECTED"}`. Only pending suggestions can be processed.

## Demo Walkthrough

Seeded product 8, the Hoodie, starts with stock 11, reorder threshold 12, and demand velocity 15. Submit an order for one unit:

```powershell
Invoke-RestMethod -Method Post `
	-Uri http://localhost:8080/products/8/orders `
	-ContentType 'application/json' `
	-Body '{"quantity":1}'
```

The sale takes stock below the threshold and demand above 15. Because processing is asynchronous, query the suggestion endpoints again after the order:

```powershell
Invoke-RestMethod http://localhost:8080/products/8/pricing-suggestions
Invoke-RestMethod http://localhost:8080/products/8/reorder-suggestions
```

Review the pending suggestions in the UI, accept or reject them, and check `GET /products` to see the product state after approval.

## Tests

Run the full backend suite with `mvn clean test` from `backend/`. It includes service and strategy unit tests, event-handler tests, and a Spring integration test that verifies an order persists asynchronous recommendations. LLM parsing tests use a mocked gateway; they do not make a live provider request.

## Security Note

Do not commit API keys, cookies, or other credentials. Rotate any credential that has already been checked into a shared repository, and configure LLM credentials locally as described in [LLM_CONFIGURATION.md](LLM_CONFIGURATION.md).
