# PartyMaker - Firebase Integration UML Diagram

## 🔥 Firebase Integration Architecture

This UML diagram shows all Firebase integration components, including Authentication, Realtime Database, Storage, and Cloud Messaging services.

---

## 🏗️ Firebase Integration Class Diagram

```mermaid
classDiagram
    %% Core Firebase Integration Classes
    class FirebaseAccessManager {
        -Context context
        
        +FirebaseAccessManager(context)
        +isServerModeEnabled() boolean
        +getGroupsRef() FirebaseServerClient
        +getUsersRef() FirebaseServerClient
        +getMessagesRef() FirebaseServerClient
    }
    
    class DBRef {
        +FirebaseAuth Auth
        +FirebaseDatabase DataBase
        +DatabaseReference refGroups
        +DatabaseReference refUsers
        +DatabaseReference refMessages
        +FirebaseStorage Storage
        +StorageReference refStorage
        +String CurrentUser
        
        +init() void
        +checkImageExists(path, listener) void
    }
    
    class ServerDBRef {
        +FirebaseAuth Auth
        +FirebaseStorage Storage
        +StorageReference refStorage
        +String CurrentUser
        -FirebaseServerClient serverClient
        
        +checkImageExists(path, listener) void
        +getServerClient() FirebaseServerClient
    }
    
    %% Callback Interfaces
    class OnImageExistsListener {
        <<interface>>
        +onImageExists(exists) void
    }
    
    %% Relationships
    FirebaseAccessManager --> FirebaseServerClient : uses
    
    DBRef --> FirebaseAuth : manages
    DBRef --> FirebaseDatabase : manages
    DBRef --> FirebaseStorage : manages
    DBRef --> OnImageExistsListener : uses
    
    ServerDBRef --> FirebaseAuth : manages
    ServerDBRef --> FirebaseStorage : manages
    ServerDBRef --> FirebaseServerClient : uses
    ServerDBRef --> OnImageExistsListener : uses
```

---

## 🔍 Firebase Integration Components

### **🔥 Core Firebase Classes (3):**
- **DBRef**: Central Firebase service references (Auth, Database, Storage)
- **FirebaseAccessManager**: Routes operations to server client
- **ServerDBRef**: Server-mode replacement for direct Firebase access

### **🔧 Callback Interfaces (1):**
- **OnImageExistsListener**: Image existence check callbacks for Firebase Storage

---

## 🔄 Firebase Service Integration

### **📊 Simple Architecture:**
- **Server-First**: Most operations route through Spring Boot server
- **Direct Firebase**: Only Auth and Storage used directly
- **Callback Pattern**: Clean async operation handling
- **Reference Management**: Centralized Firebase service references

### **🔐 Authentication:**
- **Firebase Auth**: Direct Firebase Authentication integration
- **Server Integration**: Auth tokens passed to server for validation

### **📁 Storage:**
- **Firebase Storage**: Direct file upload/download capabilities
- **Image Management**: Profile and group image storage
- **Existence Checks**: Verify file existence before operations
---

## 📋 **Firebase Summary**

### **🔥 Core Firebase Components (3)**
- **FirebaseAccessManager**: Access manager that routes to server client
- **DBRef**: Firebase references helper for Auth, Database, and Storage
- **ServerDBRef**: Server-mode replacement for direct Firebase access

### **🔧 Callback Interfaces (1)**
- **OnImageExistsListener**: Image existence check callbacks for Firebase Storage

### **🏗️ Architecture**
- **Server-First Approach**: Uses Spring Boot server instead of direct Firebase access
- **Firebase Services**: Auth and Storage still used directly for specific features
- **Simple Integration**: Lightweight wrapper classes around Firebase SDK
- **Callback Management**: Clean callback interfaces for async operations

---

*Simplified Firebase integration with 3 core classes and 1 callback interface, using server-first architecture for data operations.* 