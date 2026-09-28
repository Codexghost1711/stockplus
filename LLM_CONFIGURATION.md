# LLM Configuration

StockPulse has two independent settings: the commerce strategy and the LLM gateway provider.

## Select a Commerce Strategy

Set `stockpulse.commerce.strategy` to:

- `RULE` for deterministic local recommendations (the default).
- `AI` to use the AI commerce strategy.

The AI strategy uses `stockpulse.ai.provider` to select the gateway:

- `LITE` or `LITELLM` selects the LiteLLM gateway.
- `UNKNOWN` selects the no-op gateway, which is suitable for local tests without network access.

## LiteLLM Properties

The gateway reads these Spring properties:

| Property | Purpose |
| --- | --- |
| `stockpulse.ai.base-url` | Provider base URL; defaults to the configured LiteLLM endpoint |
| `stockpulse.ai.api-key` | Bearer token |
| `stockpulse.ai.model` | Model name |
| `stockpulse.ai.product` | Provider-specific product header |
| `stockpulse.ai.cookie` | Optional provider cookie header |

Provide these through an external configuration source, such as `SPRING_APPLICATION_JSON`, a deployment secret store, or local IDE run configuration. For example, in PowerShell:

```powershell
$env:SPRING_APPLICATION_JSON = '{"stockpulse":{"commerce":{"strategy":"AI"},"ai":{"provider":"LITE","base-url":"https://your-provider.example/v1","model":"your-model","api-key":"set-locally","product":"your-product","cookie":"set-locally"}}}'
cd C:\stockplus\backend
mvn spring-boot:run
```

Replace the example values locally; never put real credentials in this file or in source-controlled `application.properties`. The old `STOCKPULSE_LLM_*` names are not directly read by the current application configuration.

## Security

Do not commit API keys or cookies. Rotate any credential that has been committed or shared, and keep local credentials outside source control.

## Testing

The automated AI strategy tests use a mocked `LLMGateway`, so they validate parsing and fallback behavior without contacting a provider. The gateway wiring tests also do not verify live credentials or network connectivity. To smoke-test a live provider, select `AI`, configure local credentials, run the backend, and request an on-demand suggestion with `POST /products/{id}/suggest-pricing`.