Neptune's Pride II API
===

Get an auth token by making a POST call to /login.

```bash
curl -X POST http://localhost:9000/login --data 'username=YOUR_USERNAME&password=YOUR_PASSWORD'
```

For all other requests, include your auth token in the `X-Auth-Token` header.

```bash
curl http://localhost:9000/games -H "X-Auth-Token: 48d4bdbc98e64cc5d38fc361e6d9a39f"
```
