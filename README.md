# PartyMaker

PartyMaker is a comprehensive social event planning and management platform consisting of a Spring Boot server and an Android application. The system enables users to create events, invite friends, manage attendee lists, and share real-time information about events.

## Architecture

The project consists of two main components:

### 1. Spring Boot Server (spring-server)
- Provides REST API for the client application
- Interfaces with Firebase for data storage and user authentication
- Handles business logic of the application

### 2. Android Application (app)
- Complete user interface for event management
- Communicates with the server via REST API
- Supports both online and offline operation modes

## Technologies

### Server
- **Spring Boot**: Framework for Java application development
- **Spring Security**: For security and permissions management
- **Firebase Admin SDK**: For Firebase service integration
- **Swagger/SpringFox**: For API documentation
- **Maven**: For dependency and build management

### Client
- **Android SDK**: For Android application development
- **Firebase SDK**: For data storage, authentication, and notifications
- **Retrofit**: For network calls to the server
- **Glide**: For image loading
- **Material Design Components**: For UI design

## Development Setup

### Prerequisites
- JDK 17 or later
- Maven 3.6 or later
- Android Studio (for client app development)
- Active Firebase account

### Server Setup
1. Navigate to the `spring-server` directory
2. Add your `serviceAccountKey.json` from your Firebase account to the `src/main/resources` directory
3. Adjust settings in `application.properties` as needed
4. Run the server using the command:
```
mvn spring-boot:run
```

### Android Application Setup
1. Open the `app` directory in Android Studio
2. Add your `google-services.json` from your Firebase account to the `app` directory
3. Update the server address in `app/src/main/java/com/example/partymaker/data/firebase/DatabaseUpdateServer.java`
4. Build and run the application

## Server API

The server provides the following endpoints:

### Authentication and User Management
- `POST /api/auth/signin`: Sign in with email and password
- `POST /api/auth/signup`: Sign up with email and password
- `GET /api/users/{userId}`: Get user details
- `PUT /api/users/{userId}`: Update user details
- `DELETE /api/users/{userId}`: Delete user

### Event Management
- `GET /api/parties`: Get list of events
- `POST /api/parties`: Create new event
- `GET /api/parties/{partyId}`: Get event details
- `PUT /api/parties/{partyId}`: Update event details
- `DELETE /api/parties/{partyId}`: Delete event
- `POST /api/parties/{partyId}/join`: Join an event
- `POST /api/parties/{partyId}/leave`: Leave an event
- `GET /api/parties/search`: Search events by location
- `GET /api/parties/upcoming`: Get upcoming events
- `GET /api/parties/user`: Get events the user is participating in
- `GET /api/parties/host`: Get events the user is hosting

### Database Access
- `GET /api/getValue/{path}`: Get value from a specific path
- `GET /api/getChildren/{path}`: Get children of a specific path
- `POST /api/setValue`: Set value at a specific path
- `POST /api/updateChildren`: Update children at a specific path
- `DELETE /api/database/remove/{path}`: Delete value from a specific path

### File Management
- `POST /api/storage/upload`: Upload a file
- `DELETE /api/storage/delete`: Delete a file
- `GET /api/storage/download`: Download a file
- `GET /api/storage/url`: Get download URL for a file
- `GET /api/storage/exists`: Check if a file exists
- `GET /api/storage/list`: List files in a directory

## API Documentation

Swagger documentation is available at `http://localhost:8085/swagger-ui/index.html` after starting the server.

## Security

The system uses Firebase Authentication for user authentication. Each request to the server must include a JWT token in the Authorization header.

Example:
```
Authorization: Bearer <firebase_id_token>
```

## Configuration

### Server Configuration (application.properties)
```
# Server Configuration
server.port=8085

# Firebase Configuration
firebase.database.url=https://partymaker-9c966-default-rtdb.firebaseio.com
firebase.storage.bucket=partymaker-9c966.appspot.com
firebase.storage.enabled=false
firebase.database.enabled=true

# Swagger Configuration
springfox.documentation.swagger-ui.enabled=true
springfox.documentation.swagger-ui.path=/swagger-ui.html
```

## Common Troubleshooting

1. **Server Connection Issues**: Ensure the server is running and accessible at the address configured in the app.
2. **Authentication Issues**: Verify that the `serviceAccountKey.json` is valid and in the correct location.
3. **Firebase Issues**: Ensure Firebase settings are correct and required services (Authentication, Realtime Database, Storage) are enabled in your Firebase project.

## Development and Contribution

1. Fork the project
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

Distributed under the MIT License. See `LICENSE` for more information.
