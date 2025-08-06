# PartyMaker - Managers & Utility Components UML Diagram

## 🛠️ Manager Classes & Utility Architecture

This UML diagram shows all manager classes and utility components that provide cross-cutting concerns and support functionality throughout the PartyMaker application.

---

## 🏗️ Managers & Utilities Class Diagram

```mermaid
classDiagram
    %% System Managers
    class MemoryManager {
        -Runtime runtime
        -ActivityManager activityManager
        -Map~String,Long~ memorySnapshots
        
        +MemoryManager()
        +performMemoryCleanup() void
        +optimizeForGroupView() void
        +clearImageCache() void
        +releaseUnusedResources() void
        +getMemoryInfo() MemoryInfo
        +isLowMemory() boolean
        +takeMemorySnapshot(tag) void
        +getMemoryUsage() long
        +forceGarbageCollection() void
        -clearViewCaches() void
        -optimizeDatabase() void
    }
    
    class PermissionManager {
        -Context context
        -Map~String,Boolean~ permissionCache
        
        +PermissionManager(context)
        +hasPermission(permission) boolean
        +requestPermission(activity, permission, requestCode) void
        +requestMultiplePermissions(activity, permissions, requestCode) void
        +shouldShowRationale(activity, permission) boolean
        +openAppSettings(activity) void
        +checkCameraPermission() boolean
        +checkStoragePermission() boolean
        +checkLocationPermission() boolean
        -updatePermissionCache(permission, granted) void
    }
    
    %% UI Managers
    class LoadingStateManager {
        -ProgressBar progressBar
        -SwipeRefreshLayout refreshLayout
        -View loadingOverlay
        -TextView loadingText
        
        +LoadingStateManager(activity)
        +showLoading() void
        +hideLoading() void
        +showRefreshing() void
        +hideRefreshing() void
        +showLoadingWithText(text) void
        +setLoadingText(text) void
        +isLoading() boolean
        +setProgressValue(progress) void
        
        <<Builder>>
        class Builder {
            +setProgressBar(progressBar) Builder
            +setRefreshLayout(refreshLayout) Builder
            +setLoadingOverlay(overlay) Builder
            +build() LoadingStateManager
        }
    }
    
    class UiStateManager {
        -Context context
        -View rootView
        -Snackbar currentSnackbar
        
        +UiStateManager(context, rootView)
        +showError(message) void
        +showSuccess(message) void
        +showWarning(message) void
        +showSnackbar(message) void
        +showSnackbarWithAction(message, actionText, action) void
        +showToast(message) void
        +showEmptyState() void
        +hideEmptyState() void
        +dismissCurrentSnackbar() void
        -createSnackbar(message, type) Snackbar
    }
    
    class NotificationManager {
        -Context context
        -android.app.NotificationManager systemNotificationManager
        -Map~String,NotificationChannel~ channels
        
        +NotificationManager(context)
        +createNotificationChannel(channelId, name, importance) void
        +showNotification(title, message, channelId) void
        +showProgressNotification(title, progress, channelId) void
        +cancelNotification(notificationId) void
        +cancelAllNotifications() void
        +createGroupNotification(groupName, message) void
        +createChatNotification(senderName, message, groupName) void
        -buildNotification(title, message, channelId) Notification
        -getPendingIntent(action) PendingIntent
    }
    
    class UserFeedbackManager {
        -Context context
        -Handler mainHandler
        
        +UserFeedbackManager(context)
        +showSuccessMessage(message) void
        +showErrorMessage(message) void
        +showInfoMessage(message) void
        +showConfirmationDialog(title, message, callback) void
        +showInputDialog(title, hint, callback) void
        +showProgressDialog(message) ProgressDialog
        +dismissProgressDialog() void
        +vibrate(duration) void
        +playSuccessSound() void
        +playErrorSound() void
        -createDialog(title, message, type) AlertDialog
    }
    
    %% Navigation & Maps
    class NavigationManager {
        -Context context
        -Intent currentIntent
        
        +NavigationManager(context)
        +navigateToActivity(activityClass) void
        +navigateToActivityWithData(activityClass, extras) void
        +navigateToActivityForResult(activity, activityClass, requestCode) void
        +navigateBack(activity) void
        +navigateToMain(activity) void
        +navigateToLogin(activity) void
        +clearBackStack(activity) void
        +shareContent(title, content) void
        -createIntent(activityClass) Intent
        -addExtrasToIntent(intent, extras) void
    }
    
    class MapUtilitiesManager {
        -Context context
        -GoogleMap googleMap
        -LocationManager locationManager
        
        +MapUtilitiesManager(context)
        +initializeMap(mapFragment, callback) void
        +addMarker(location, title, description) Marker
        +moveCamera(location, zoom) void
        +getCurrentLocation(callback) void
        +calculateDistance(from, to) double
        +openInGoogleMaps(location) void
        +searchNearbyPlaces(location, radius, type) void
        +getAddressFromCoordinates(lat, lng) String
        +getCoordinatesFromAddress(address) LatLng
        -requestLocationPermission() void
        -isLocationEnabled() boolean
    }
    
    %% Business Logic Managers
    class GroupDataManager {
        +formatGroupDateTime(group) String
        +validateGroupData(group) ValidationResult
        +calculateGroupStatistics(group) GroupStats
        +generateGroupInviteLink(groupKey) String
        +parseGroupInviteLink(link) String
        +isGroupExpired(group) boolean
    }
    
    class GroupDateTimeManager {
        +formatDateTime(group) String
        +parseDateTime(dateString, timeString) Date
        +isValidDateTime(date, time) boolean
        +calculateDuration(startDate, endDate) long
        +getTimeUntilEvent(group) long
        +isEventToday(group) boolean
        +isEventUpcoming(group) boolean
        -getDateFormat() SimpleDateFormat
        -getTimeFormat() SimpleDateFormat
    }
    
    class ContentSharingManager {
        -Context context
        -ClipboardManager clipboardManager
        
        +ContentSharingManager(context)
        +shareText(title, content) void
        +shareImage(imageUri, caption) void
        +shareGroupInvite(group) void
        +copyToClipboard(text) void
        +shareMultipleImages(imageUris) void
        +shareGroupDetails(group) void
        +createShareIntent(content, type) Intent
        -formatGroupShareContent(group) String
    }
    
    %% Authentication & Security
    class AuthenticationManager {
        -FirebaseAuth firebaseAuth
        -Context context
        -EncryptedSharedPreferencesManager prefsManager
        -MutableLiveData~User~ currentUser
        
        +AuthenticationManager(context)
        +signInWithEmail(email, password) Task~AuthResult~
        +createUserWithEmail(email, password) Task~AuthResult~
        +signInWithGoogle(credential) Task~AuthResult~
        +signOut() void
        +getCurrentUser() FirebaseUser
        +isUserLoggedIn() boolean
        +setCurrentUser(user) void
        +saveUserSession(user) void
        +clearUserSession() void
        +sendPasswordResetEmail(email) Task~Void~
        -updateUserPreferences(user) void
    }
    

    
    %% File & Media Management
    class FileManager {
        -Context context
        -File cacheDirectory
        -File externalDirectory
        
        +FileManager(context)
        +saveImageToCache(bitmap, filename) File
        +saveImageToExternal(bitmap, filename) File
        +loadImageFromCache(filename) Bitmap
        +deleteImageFromCache(filename) boolean
        +clearCache() void
        +getCacheSize() long
        +getExternalStorageDir() File
        +isExternalStorageWritable() boolean
        +isExternalStorageReadable() boolean
        +compressImage(inputFile, outputFile, quality) void
        -createImageFile(directory, filename) File
        -ensureDirectoryExists(directory) void
    }
    

    
    class ImageCompressor {
        +compressImage(imageUri, quality) Bitmap
        +compressImageToFile(inputFile, outputFile, quality) void
        +resizeImage(bitmap, maxWidth, maxHeight) Bitmap
        +saveCompressedImage(bitmap, file) void
        +calculateInSampleSize(options, reqWidth, reqHeight) int
        +getImageOrientation(imageUri) int
        +rotateImage(bitmap, degrees) Bitmap
        -decodeSampledBitmapFromUri(uri, reqWidth, reqHeight) Bitmap
        -getMaxImageSize() int
    }
    
    %% Server & Network Utilities
    class ServerModeManager {
        -Context context
        -SharedPreferences preferences
        
        +ServerModeManager(context)
        +isServerModeEnabled() boolean
        +enableServerMode(serverUrl) void
        +disableServerMode() void
        +getServerUrl() String
        +validateServerUrl(url) boolean
        +testServerConnection(url, callback) void
        -saveServerPreferences(url, enabled) void
    }
    
    %% Core Utilities
    class IntentExtrasManager {
        +putGroupKey(intent, groupKey) void
        +getGroupKey(intent) String
        +putUserKey(intent, userKey) void
        +getUserKey(intent) String
        +putGroupObject(intent, group) void
        +getGroupObject(intent) Group
        +putUserObject(intent, user) void
        +getUserObject(intent) User
        +putStringExtra(intent, key, value) void
        +getStringExtra(intent, key, defaultValue) String
        +putBooleanExtra(intent, key, value) void
        +getBooleanExtra(intent, key, defaultValue) boolean
        +hasExtra(intent, key) boolean
        +clearExtras(intent) void
    }
    
    class ThreadUtils {
        -ExecutorService backgroundExecutor
        -Handler mainHandler
        
        +ThreadUtils()
        +runOnBackground(runnable) void
        +runOnMain(runnable) void
        +runOnMainDelayed(runnable, delay) void
        +runOnBackgroundWithCallback(task, callback) void
        +isMainThread() boolean
        +shutdown() void
        -createBackgroundExecutor() ExecutorService
        -getMainHandler() Handler
    }

    %% Relationships
    LoadingStateManager *-- Builder
    
    MemoryManager --> Runtime : uses
    MemoryManager --> ActivityManager : uses
    
    PermissionManager --> Context : uses
    
    LoadingStateManager --> ProgressBar : manages
    LoadingStateManager --> SwipeRefreshLayout : manages
    
    UiStateManager --> Snackbar : creates
    UiStateManager --> Toast : creates
    
    NotificationManager --> android.app.NotificationManager : uses
    NotificationManager --> NotificationChannel : creates
    
    UserFeedbackManager --> AlertDialog : creates
    UserFeedbackManager --> ProgressDialog : creates
    
    NavigationManager --> Intent : creates
    NavigationManager --> Context : uses
    
    MapUtilitiesManager --> GoogleMap : uses
    MapUtilitiesManager --> LocationManager : uses
    
    ContentSharingManager --> ClipboardManager : uses
    ContentSharingManager --> Intent : creates
    
    AuthenticationManager --> FirebaseAuth : uses
    AuthenticationManager --> EncryptedSharedPreferencesManager : uses
    
    SecureAuthenticationManager --> GoogleSignInClient : uses
    SecureAuthenticationManager --> AuthenticationManager : uses
    
    FileManager --> File : manages
    FileManager --> Bitmap : processes
    
    GlideImageLoader --> RequestOptions : uses
    GlideImageLoader --> DiskLruCache : uses
    
    ImageCompressor --> Bitmap : processes
    
    ServerModeManager --> SharedPreferences : uses
    
    ThreadUtils --> ExecutorService : uses
    ThreadUtils --> Handler : uses
```

---

## 🔍 Manager Categories & Responsibilities

### **💾 System Resource Managers:**
- **MemoryManager**: Memory optimization, garbage collection, and resource cleanup
- **PermissionManager**: Runtime permission handling and user permission flows
- **ThreadUtils**: Background thread management and main thread coordination

### **🎨 UI State Managers:**
- **LoadingStateManager**: Loading indicators, progress bars, and refresh layouts
- **UiStateManager**: User feedback via Snackbars, Toasts, and state displays
- **NotificationManager**: System notifications and notification channels
- **UserFeedbackManager**: Dialogs, confirmations, and user input collection

### **🧭 Navigation & Location:**
- **NavigationManager**: Activity navigation, intent management, and back stack control
- **MapUtilitiesManager**: Google Maps integration, location services, and place searches

### **💼 Business Logic Managers:**
- **GroupDataManager**: Group-specific business logic and data validation
- **GroupDateTimeManager**: Date/time formatting, parsing, and validation
- **ContentSharingManager**: Content sharing, clipboard operations, and social features

### **🔐 Authentication & Security:**
- **AuthenticationManager**: Firebase authentication and session management
- **SecureAuthenticationManager**: Google Sign-In and OAuth2 integration

### **📁 File & Media Management:**
- **FileManager**: File system operations, cache management, and storage handling
- **GlideImageLoader**: Image loading, caching, and transformation
- **ImageCompressor**: Image compression, resizing, and optimization

### **🌐 Server & Network:**
- **ServerModeManager**: Server configuration and connection management

### **🛠️ Core Utilities:**
- **IntentExtrasManager**: Intent data passing and extraction utilities

---

## 🎯 Cross-Cutting Concerns

### **📱 UI Consistency:**
- **Standardized Feedback**: Consistent user feedback across all screens
- **Loading States**: Unified loading indicators and progress displays
- **Error Handling**: Centralized error message display and recovery
- **Theme Integration**: All managers respect app theme and accessibility settings

### **🔒 Security Integration:**
- **Permission Flow**: Seamless permission requests with user education
- **Secure Storage**: Encrypted preferences and secure file handling
- **Authentication State**: Consistent auth state management across features
- **Data Protection**: Secure handling of sensitive user information

### **⚡ Performance Optimization:**
- **Memory Management**: Proactive memory cleanup and optimization
- **Background Processing**: Efficient background task execution
- **Image Optimization**: Smart image loading, caching, and compression
- **Resource Cleanup**: Proper disposal of system resources

---

---

## 📋 **Manager Summary**

### **🔐 Security Managers (6)**
- **SSLPinningManager**: Certificate pinning and secure connections
- **MessageEncryptionManager**: End-to-end message encryption
- **GroupKeyManager**: Group-specific encryption key management
- **EncryptedSharedPreferencesManager**: Secure local data storage
- **SecureConfigManager**: Secure configuration management

### **🎨 UI Managers (7)**
- **NavigationManager**: App navigation and routing
- **MapUtilitiesManager**: Google Maps integration and utilities
- **NotificationManager**: Push notifications and alerts
- **UiStateManager**: UI state management across activities
- **LoadingStateManager**: Loading indicators and progress
- **UserFeedbackManager**: Toast, Snackbar, and dialog management

### **🏗️ Infrastructure Managers (10)**
- **AuthenticationManager**: User authentication and session management
- **FileManager**: File operations and media handling
- **MemoryManager**: Memory optimization and garbage collection
- **PermissionManager**: Runtime permission handling
- **IntentExtrasManager**: Intent data management
- **ContentSharingManager**: Share functionality
- **GroupDateTimeManager**: Date/time utilities for parties
- **GroupDataManager**: Group data validation and processing
- **ServerModeManager**: Server configuration management
- **ConnectivityManager**: Network connectivity monitoring
- **NetworkManager**: Network operations and HTTP client management

---

*23+ Manager classes providing cross-cutting functionality for security, UI management, infrastructure, and utilities throughout the app.* 