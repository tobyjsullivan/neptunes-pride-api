Neptune's Pride II API
===

Get an auth token by making a POST call to /login.

```bash
curl -X POST https://neptunes-pride-api.herokuapp.com/login --data 'username=YOUR_USERNAME&password=YOUR_PASSWORD'
```

For all other requests, include your auth token in the `X-Auth-Token` header.

```bash
curl https://neptunes-pride-api.herokuapp.com/games -H 'X-Auth-Token: 48d4bdbc98e64cc5d38fc361e6d9a39f'
curl https://neptunes-pride-api.herokuapp.com/games/5536910481031168 -H 'X-Auth-Token: 48d4bdbc98e64cc5d38fc361e6d9a39f'
```

You can find a live copy of the API running at [neptunes-pride-api.herokuapp.com](https://neptunes-pride-api.herokuapp.com).
