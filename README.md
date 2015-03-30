# Neptune's Pride II API

## Quick Start

Get an auth token by making a POST call to /login.

```bash
curl -X POST https://neptunes-pride-api.herokuapp.com/login --data 'username=YOUR_USERNAME&password=YOUR_PASSWORD'
```

For all other requests, include your auth token in the `X-Auth-Token` header.

```bash
curl https://neptunes-pride-api.herokuapp.com/games -H 'X-Auth-Token: 48d4bdbc98e64cc5d38fc361e6d9a39f'
curl https://neptunes-pride-api.herokuapp.com/games/5536910481031168 -H 'X-Auth-Token: 48d4bdbc98e64cc5d38fc361e6d9a39f'
```

And when using post commands, send data as JSON.

```bash
curl -X POST localhost:9000/games/5536910481031168/carriers -H 'X-Auth-Token: 48d4bdbc98e64cc5d38fc361e6d9a39f' -H 'Content-Type: application/json' --data '{"starId": 4, "ships": 14}'
```

You can find a live copy of the API running at [neptunes-pride-api.herokuapp.com](https://neptunes-pride-api.herokuapp.com).

## Endpoints

* [POST /login](#post-login)
* [GET /games](#get-games)
* [GET /games/:gameId](#get-gamesgameid)
* [GET /games/:gameId/players](#get-gamesgameidplayers)
* [GET /games/:gameId/stars](#get-gamesgameidstars)
* [POST /games/:gameId](#post-gamesgameid)
* [POST /games/:gameId/carriers](#post-gamesgameidcarriers)

### POST /login
Takes a username (or 'True Alias') and password and returns an auth token.

This endpoint is unique from all others in that it expects data as `application/x-www-form-urlencoded` (See the
[HTML Spec](http://www.w3.org/TR/html401/interact/forms.html#h-17.13.4.1) for details)

- Requires auth: No
- Request format: application/x-www-form-urlencoded
- Response format: JSON

#### Parameters

Parameter | Required | Description | Type
--- | --- | --- | ---
username  | true     | Your 'true alias' for signing in. | Url-escaped string.
password  | true     | Your password used for signing in. | Url-escaped string.

#### Example Response
```json
{
  "auth-token":"dd2ceeb9d03aaa17975c2d4c44e5266b"
}
```

### GET /games
Lists all games the player is a part of.

- Requires auth: Yes
- Request format: N/A
- Response format: JSON

#### Example Response
```json
{
  "result":[
    {
      "gameId":6562594263400448,
      "name":"Test turn-based"
    },
    {
      "gameId":5756402947588096,
      "name":"HS Test Game (Real Time)"
    }
  ]
}
```

### GET /games/:gameId
Get metadata for the specified game

- Requires auth: Yes
- Request format: N/A
- Response format: JSON

#### Example Response
```json
{
  "result":{
    "name":"Test turn-based",
    "details":{
      "turnBased":true,
      "turnBasedTimeout":0,
      "war":false,
      "tickRate":60,
      "productionRate":24,
      "totalStars":34,
      "starsForVictory":18,
      "tradeCost":15,
      "tradeScanned":false,
      "carrierSpeed":0.041666666666666664
    },
    "status":{
      "startTime":1426556808467,
      "now":1427664880048,
      "started":false,
      "paused":true,
      "gameOver":false,
      "productions":0,
      "productionCounter":0,
      "tick":0,
      "tickFragment":0.0
    },
    "player":{
      "playerId":0,
      "admin":false
    }
  }
}
```

### GET /games/:gameId/players
Returns available data about all players in the game.

- Requires auth: Yes
- Request format: N/A
- Response format: JSON

#### Example Response
```json
{
  "result": [
    {
      "playerId": 0,
      "totalEconomy": 5,
      "totalIndustry": 5,
      "totalScience": 1,
      "aiControlled": false,
      "totalStars": 6,
      "totalCarriers": 2,
      "totalShips": 60,
      "name": "Josh G",
      "scanning": {
        "value": 0.375,
        "level": 1
      },
      "hyperspaceRange": {
        "value": 0.5,
        "level": 1
      },
      "terraforming": {
        "value": 1,
        "level": 1
      },
      "experimentation": {
        "value": 120,
        "level": 1
      },
      "weapons": {
        "value": 1,
        "level": 1
      },
      "banking": {
        "value": 1,
        "level": 1
      },
      "manufacturing": {
        "value": 1,
        "level": 1
      },
      "conceded": "active",
      "ready": false,
      "missedTurns": 0,
      "renownToGive": 5
    },
    {
      "playerId": 1,
      "totalEconomy": 5,
      "totalIndustry": 5,
      "totalScience": 1,
      "aiControlled": false,
      "totalStars": 6,
      "totalCarriers": 1,
      "totalShips": 60,
      "name": "Mack",
      "scanning": {
        "value": 0.375,
        "level": 1
      },
      "hyperspaceRange": {
        "value": 0.5,
        "level": 1
      },
      "terraforming": {
        "value": 1,
        "level": 1
      },
      "experimentation": {
        "value": 120,
        "level": 1
      },
      "weapons": {
        "value": 1,
        "level": 1
      },
      "banking": {
        "value": 1,
        "level": 1
      },
      "manufacturing": {
        "value": 1,
        "level": 1
      },
      "conceded": "active",
      "ready": false,
      "missedTurns": 0,
      "renownToGive": 5
    },
    {
      "playerId": 2,
      "totalEconomy": 17,
      "totalIndustry": 5,
      "totalScience": 1,
      "aiControlled": false,
      "totalStars": 6,
      "totalCarriers": 6,
      "totalShips": 60,
      "name": "tobyjsullivan",
      "scanning": {
        "value": 0.375,
        "level": 1
      },
      "hyperspaceRange": {
        "value": 0.5,
        "level": 1
      },
      "terraforming": {
        "value": 1,
        "level": 1
      },
      "experimentation": {
        "value": 120,
        "level": 1
      },
      "weapons": {
        "value": 1,
        "level": 1
      },
      "banking": {
        "value": 1,
        "level": 1
      },
      "manufacturing": {
        "value": 1,
        "level": 1
      },
      "conceded": "active",
      "ready": false,
      "missedTurns": 0,
      "renownToGive": 5
    },
    ...
  ]
}
```

### GET /games/:gameId/stars
Receives all data about stars in the game which is currently visible to the player.

- Requires auth: Yes
- Request format: N/A
- Response format: JSON

#### Example Response
```json
{
  "result": [
    {
      "starId": 1,
      "name": "Rukbat",
      "playerId": 4,
      "visible": false,
      "position": {
        "x": -0.0278,
        "y": 2.2629
      }
    },
    {
      "starId": 2,
      "name": "Mira",
      "playerId": 2,
      "visible": true,
      "position": {
        "x": -1.5278,
        "y": 2.2629
      },
      "economy": 5,
      "industry": 5,
      "science": 1,
      "naturalResources": 50,
      "terraformedResources": 55,
      "warpGate": false,
      "ships": 0
    },
    {
      "starId": 3,
      "name": "Sceptrum",
      "playerId": 3,
      "visible": false,
      "position": {
        "x": -3.0278,
        "y": 2.2629
      }
    },
    {
      "starId": 4,
      "name": "Spy",
      "playerId": 1,
      "visible": false,
      "position": {
        "x": -2.2778,
        "y": 3.562
      }
    },
    {
      "starId": 119,
      "name": "Grafias",
      "playerId": -1,
      "visible": false,
      "position": {
        "x": 2.3615,
        "y": 0.0174
      }
    },
    ...
  ]
}
```

### POST /games/:gameId
Submit a turn for a turn-based game.

- Requires auth: Yes
- Request format: N/A
- Response format: JSON

#### Parameters
None

#### Response
```json
{
  "result":"ok"
}
```

### POST /games/:gameId/carriers
Create a new carrier.

- Requires auth: Yes
- Request format: JSON
- Response format: JSON

#### Parameters

Parameter | Required | Description | Type
--- | --- | --- | ---
starId | true | The star on which to build the carrier | Integer
numShips | true | Your password used for signing in. | Url-escaped string.

#### Example Request
```json
{
  "starId": 33,
  "ships": 25
}
```

#### Example Response
````json
{
  "result": {
    "carrierId": 8,
    "starId": 4,
    "ships": 1,
    "name": "Tania IV",
    "loopingOrders": false,
    "orders": [],
    "playerId": 0,
    "position": {
      "x": -0.4487,
      "y": -0.5284
    },
    "lastPosition": {
      "x": -0.4487,
      "y": -0.5284
    }
  }
}
````