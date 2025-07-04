# PartyMaker Server

A simple Java server that acts as an intermediary between the PartyMaker Android app and Firebase Realtime Database.

## Overview

This server provides a REST API for the PartyMaker app to perform database update operations. The server handles:

- Setting values in the database
- Updating multiple values at once
- Removing values from the database

## Prerequisites

- Java 8 or higher
- Gradle
- Firebase project with Realtime Database

## Setup

1. Create a Firebase project and download the service account key file
2. Place the service account key file in the root directory as `service-account.json`
3. Update the database URL in `PartyMakerServer.java` to match your Firebase project

```java
.setDatabaseUrl("https://your-firebase-project.firebaseio.com")
```

## Building and Running

To build the server:

```
gradle build
```

To run the server:

```
gradle run
```

The server will start on port 8080 by default.

## API Endpoints

### Set Value

```
POST /api/setValue
```

Request body:

```json
{
  "path": "Groups/groupId/groupType",
  "value": 1
}
```

### Update Children

```
POST /api/updateChildren
```

Request body:

```json
{
  "path": "Groups/groupId/FriendKeys",
  "values": {
    "user1@example com": "true",
    "user2@example com": "true"
  }
}
```

### Remove Value

```
POST /api/removeValue
```

Request body:

```json
{
  "path": "Groups/groupId/FriendKeys"
}
```

## Integration with the PartyMaker App

To use this server with the PartyMaker app, update the `DatabaseUpdateServer.java` file in the app to send HTTP requests to this server instead of directly updating the Firebase database. 