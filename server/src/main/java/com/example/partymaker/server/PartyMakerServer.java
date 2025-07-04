package com.example.partymaker.server;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.sentry.Sentry;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static spark.Spark.*;

/**
 * Simple server that handles database update operations.
 * This server acts as an intermediary between the app and Firebase database for write operations.
 */
public class PartyMakerServer {
    private static final Logger logger = LoggerFactory.getLogger(PartyMakerServer.class);
    private static final int PORT = 8080;
    private static final Gson gson = new Gson();
    private static FirebaseDatabase database;
    private static FirebaseAuth auth;
    
    // Metrics registry
    private static final MetricRegistry metrics = new MetricRegistry();
    private static final Meter requestsMeter = metrics.meter("requests");
    private static final Meter errorsMeter = metrics.meter("errors");
    private static final Map<String, Meter> endpointMeters = new HashMap<>();

    public static void main(String[] args) {
        // Initialize error tracking with Sentry
        initializeSentry();
        
        // Initialize metrics
        initializeMetrics();
        
        // Initialize Firebase
        initializeFirebase();
        
        // Configure server
        port(PORT);
        
        // Enable CORS
        enableCORS();
        
        // Configure exception handling
        configureExceptionHandling();
        
        // Define routes
        get("/", PartyMakerServer::handleRoot);
        get("/generate_204", PartyMakerServer::handleGenerate204);
        get("/metrics", PartyMakerServer::handleMetrics);
        get("/health", PartyMakerServer::handleHealth);
        post("/api/setValue", PartyMakerServer::handleSetValue);
        post("/api/updateChildren", PartyMakerServer::handleUpdateChildren);
        post("/api/removeValue", PartyMakerServer::handleRemoveValue);
        post("/api/signIn", PartyMakerServer::handleSignIn);
        
        // Catch-all route for undefined paths
        get("*", PartyMakerServer::handleNotFound);
        post("*", PartyMakerServer::handleNotFound);
        
        // Add request tracking
        before((request, response) -> {
            requestsMeter.mark();
            String endpoint = request.pathInfo();
            endpointMeters.computeIfAbsent(endpoint, path -> metrics.meter("endpoint." + path.replace('/', '.'))).mark();
        });
        
        // Configure Jetty with custom error handler
        configureJetty();
        
        // Start server
        logger.info("Server started on port " + PORT);
    }
    
    private static void configureJetty() {
        try {
            // Wait for server initialization
            Spark.awaitInitialization();
            
            // Set custom error handler for Jetty
            ErrorHandler errorHandler = new JettyErrorHandler();
            Spark.staticFiles.expireTime(600);
            
            // Add special handling for null URI requests
            before((request, response) -> {
                if (request.raw().getRequestURI() == null) {
                    logger.warn("Received request with null URI, setting to /");
                    // We can't directly set the URI, but we can redirect
                    response.redirect("/");
                    halt(302);
                }
            });
            
            logger.info("Jetty configured with custom error handler");
        } catch (Exception e) {
            logger.error("Failed to configure Jetty server", e);
            Sentry.captureException(e);
        }
    }
    
    private static void initializeSentry() {
        try {
            // In development mode, disable Sentry to avoid DSN errors
            logger.info("Sentry initialization skipped in development mode");
            
            // If you want to enable Sentry in production, uncomment and configure with valid DSN:
            /*
            Sentry.init(options -> {
                options.setDsn("YOUR_VALID_DSN_HERE");
                options.setTracesSampleRate(0.2);
                options.setDebug(false);
                options.setEnvironment("production");
            });
            logger.info("Sentry initialized successfully");
            */
        } catch (Exception e) {
            logger.error("Failed to initialize Sentry", e);
        }
    }
    
    private static void initializeMetrics() {
        // Register JVM metrics
        metrics.register("jvm.memory", new MemoryUsageGaugeSet());
        metrics.register("jvm.gc", new GarbageCollectorMetricSet());
        metrics.register("jvm.threads", new ThreadStatesGaugeSet());
        
        // Start console reporter for metrics (useful for development)
        ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(5, TimeUnit.MINUTES);
        
        logger.info("Metrics initialized");
    }
    
    private static void initializeFirebase() {
        try {
            // Load service account
            FileInputStream serviceAccount = new FileInputStream("service-account.json");
            
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://partymaker-9c966-default-rtdb.firebaseio.com")
                    .build();
            
            FirebaseApp.initializeApp(options);
            database = FirebaseDatabase.getInstance();
            auth = FirebaseAuth.getInstance();
            logger.info("Firebase initialized successfully");
        } catch (IOException e) {
            logger.error("Error initializing Firebase", e);
            Sentry.captureException(e);
            System.exit(1);
        }
    }
    
    private static void enableCORS() {
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            
            return "OK";
        });
        
        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Request-Method", "GET,PUT,POST,DELETE,OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
            response.type("application/json");
            
            // Safe handling of URI to prevent NullPointerException
            if (request.raw() != null && request.raw().getRequestURI() == null) {
                logger.warn("Received request with null URI");
                halt(400, "{\"status\":\"error\",\"message\":\"Invalid request: null URI\"}");
            }
        });
    }
    
    private static void configureExceptionHandling() {
        // Handle NullPointerException specially
        exception(NullPointerException.class, (exception, request, response) -> {
            logger.error("NullPointerException in request processing", exception);
            errorsMeter.mark();
            Sentry.captureException(exception);
            response.status(500);
            response.body("{\"status\":\"error\",\"message\":\"Internal server error\"}");
        });
        
        // Handle general exceptions
        exception(Exception.class, (exception, request, response) -> {
            logger.error("Exception in request processing", exception);
            errorsMeter.mark();
            Sentry.captureException(exception);
            response.status(500);
            response.body("{\"status\":\"error\",\"message\":\"" + exception.getMessage() + "\"}");
        });
    }
    
    private static Object handleRoot(Request request, Response response) {
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("status", "running");
        responseJson.addProperty("message", "PartyMaker Server is running");
        return responseJson.toString();
    }
    
    /**
     * Handle Google's connectivity test requests
     * This endpoint is frequently called by Android's ConnectivityManager
     */
    private static Object handleGenerate204(Request request, Response response) {
        response.status(204); // No content
        response.type("text/html");
        return "";
    }
    
    /**
     * Endpoint to expose server metrics
     */
    private static Object handleMetrics(Request request, Response response) {
        JsonObject metricsJson = new JsonObject();
        
        // Add request rate metrics
        JsonObject requestsJson = new JsonObject();
        requestsJson.addProperty("count", requestsMeter.getCount());
        requestsJson.addProperty("rate_1m", requestsMeter.getOneMinuteRate());
        requestsJson.addProperty("rate_5m", requestsMeter.getFiveMinuteRate());
        requestsJson.addProperty("rate_15m", requestsMeter.getFifteenMinuteRate());
        metricsJson.add("requests", requestsJson);
        
        // Add error rate metrics
        JsonObject errorsJson = new JsonObject();
        errorsJson.addProperty("count", errorsMeter.getCount());
        errorsJson.addProperty("rate_1m", errorsMeter.getOneMinuteRate());
        metricsJson.add("errors", errorsJson);
        
        // Add endpoint metrics
        JsonObject endpointsJson = new JsonObject();
        for (Map.Entry<String, Meter> entry : endpointMeters.entrySet()) {
            JsonObject endpointJson = new JsonObject();
            Meter meter = entry.getValue();
            endpointJson.addProperty("count", meter.getCount());
            endpointJson.addProperty("rate_1m", meter.getOneMinuteRate());
            endpointsJson.add(entry.getKey(), endpointJson);
        }
        metricsJson.add("endpoints", endpointsJson);
        
        return metricsJson.toString();
    }
    
    /**
     * Health check endpoint
     */
    private static Object handleHealth(Request request, Response response) {
        JsonObject healthJson = new JsonObject();
        
        // Check Firebase connection
        boolean firebaseConnected = database != null && auth != null;
        
        healthJson.addProperty("status", firebaseConnected ? "healthy" : "degraded");
        healthJson.addProperty("firebase_connected", firebaseConnected);
        healthJson.addProperty("uptime_seconds", 
            (System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime()) / 1000);
        
        if (!firebaseConnected) {
            response.status(503); // Service Unavailable
        }
        
        return healthJson.toString();
    }
    
    private static Object handleNotFound(Request request, Response response) {
        response.status(404);
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("status", "error");
        responseJson.addProperty("message", "Endpoint not found: " + request.pathInfo());
        return responseJson.toString();
    }
    
    private static Object handleSignIn(Request request, Response response) {
        try {
            if (request.body() == null || request.body().isEmpty()) {
                logger.warn("Empty request body received for sign in");
                return createErrorResponse(new IllegalArgumentException("Request body cannot be empty"));
            }
            
            JsonObject requestJson = JsonParser.parseString(request.body()).getAsJsonObject();
            
            if (!requestJson.has("email") || !requestJson.has("password")) {
                logger.warn("Missing required fields in sign in request");
                return createErrorResponse(new IllegalArgumentException("Email and password are required"));
            }
            
            String email = requestJson.get("email").getAsString();
            String password = requestJson.get("password").getAsString();
            
            if (email == null || email.isEmpty()) {
                return createErrorResponse(new IllegalArgumentException("Email cannot be empty"));
            }
            
            logger.info("Attempting to authenticate user: " + email);
            
            // Try to get the user by email
            try {
                UserRecord userRecord = auth.getUserByEmailAsync(email).get();
                logger.info("User found with email: " + email);
                
                // We can't directly verify the password on the server side with Firebase Admin SDK
                // So we'll just return success if the user exists
                // The client will still need to authenticate directly with Firebase
                JsonObject successResponse = new JsonObject();
                successResponse.addProperty("status", "success");
                successResponse.addProperty("message", "User exists");
                successResponse.addProperty("uid", userRecord.getUid());
                return successResponse.toString();
            } catch (ExecutionException e) {
                if (e.getCause() instanceof FirebaseAuthException) {
                    FirebaseAuthException authException = (FirebaseAuthException) e.getCause();
                    if ("user-not-found".equals(authException.getErrorCode())) {
                        logger.warn("User not found with email: " + email);
                        return createErrorResponse(new Exception("Invalid email or password"));
                    }
                }
                throw e;
            }
        } catch (Exception e) {
            logger.error("Error during sign in", e);
            Sentry.captureException(e);
            return createErrorResponse(e);
        }
    }
    
    private static Object handleSetValue(Request request, Response response) {
        try {
            if (request.body() == null || request.body().isEmpty()) {
                return createErrorResponse(new IllegalArgumentException("Request body cannot be empty"));
            }
            
            JsonObject requestJson = JsonParser.parseString(request.body()).getAsJsonObject();
            
            if (!requestJson.has("path") || !requestJson.has("value")) {
                return createErrorResponse(new IllegalArgumentException("Path and value are required"));
            }
            
            String path = requestJson.get("path").getAsString();
            JsonElement valueElement = requestJson.get("value");
            
            // Fix paths to match correct database structure
            path = correctDatabasePath(path);
            
            DatabaseReference reference = getReference(path);
            reference.setValueAsync(gson.fromJson(valueElement, Object.class));
            
            logger.info("Set value at path: " + path);
            return createSuccessResponse();
        } catch (Exception e) {
            logger.error("Error setting value", e);
            Sentry.captureException(e);
            return createErrorResponse(e);
        }
    }
    
    private static Object handleUpdateChildren(Request request, Response response) {
        try {
            if (request.body() == null || request.body().isEmpty()) {
                return createErrorResponse(new IllegalArgumentException("Request body cannot be empty"));
            }
            
            JsonObject requestJson = JsonParser.parseString(request.body()).getAsJsonObject();
            
            if (!requestJson.has("path") || !requestJson.has("values")) {
                return createErrorResponse(new IllegalArgumentException("Path and values are required"));
            }
            
            String path = requestJson.get("path").getAsString();
            JsonObject valuesJson = requestJson.get("values").getAsJsonObject();
            
            // Fix paths to match correct database structure
            path = correctDatabasePath(path);
            
            DatabaseReference reference = getReference(path);
            Map<String, Object> values = new HashMap<>();
            
            for (String key : valuesJson.keySet()) {
                values.put(key, gson.fromJson(valuesJson.get(key), Object.class));
            }
            
            reference.updateChildrenAsync(values);
            
            logger.info("Updated children at path: " + path);
            return createSuccessResponse();
        } catch (Exception e) {
            logger.error("Error updating children", e);
            Sentry.captureException(e);
            return createErrorResponse(e);
        }
    }
    
    private static Object handleRemoveValue(Request request, Response response) {
        try {
            if (request.body() == null || request.body().isEmpty()) {
                return createErrorResponse(new IllegalArgumentException("Request body cannot be empty"));
            }
            
            JsonObject requestJson = JsonParser.parseString(request.body()).getAsJsonObject();
            
            if (!requestJson.has("path")) {
                return createErrorResponse(new IllegalArgumentException("Path is required"));
            }
            
            String path = requestJson.get("path").getAsString();
            
            // Fix paths to match correct database structure
            path = correctDatabasePath(path);
            
            DatabaseReference reference = getReference(path);
            reference.removeValueAsync();
            
            logger.info("Removed value at path: " + path);
            return createSuccessResponse();
        } catch (Exception e) {
            logger.error("Error removing value", e);
            Sentry.captureException(e);
            return createErrorResponse(e);
        }
    }
    
    /**
     * Corrects database paths to match the actual Firebase structure
     * @param path The original path
     * @return The corrected path
     */
    private static String correctDatabasePath(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        
        // Fix users -> Users
        if (path.startsWith("users/")) {
            String correctedPath = "Users" + path.substring(5);
            logger.info("Path corrected from users/ to Users/: " + correctedPath);
            return correctedPath;
        }
        
        // Fix groups -> Groups (if needed)
        if (path.startsWith("groups/")) {
            String correctedPath = "Groups" + path.substring(6);
            logger.info("Path corrected from groups/ to Groups/: " + correctedPath);
            return correctedPath;
        }
        
        // Fix groupsmessages -> GroupsMessages (if needed)
        if (path.toLowerCase().startsWith("groupsmessages/")) {
            String correctedPath = "GroupsMessages" + path.substring(14);
            logger.info("Path corrected from groupsmessages/ to GroupsMessages/: " + correctedPath);
            return correctedPath;
        }
        
        return path;
    }
    
    private static DatabaseReference getReference(String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Path cannot be empty");
        }
        
        String[] segments = path.split("/");
        DatabaseReference reference = database.getReference(segments[0]);
        
        for (int i = 1; i < segments.length; i++) {
            if (!segments[i].isEmpty()) {
                reference = reference.child(segments[i]);
            }
        }
        
        return reference;
    }
    
    private static String createSuccessResponse() {
        JsonObject response = new JsonObject();
        response.addProperty("status", "success");
        return response.toString();
    }
    
    private static String createErrorResponse(Exception e) {
        JsonObject response = new JsonObject();
        response.addProperty("status", "error");
        response.addProperty("message", e.getMessage() != null ? e.getMessage() : "Unknown error");
        return response.toString();
    }
} 