# PartyMaker 🎉

PartyMaker is a comprehensive social event planning Android application that enables users to organize gatherings, create communities through groups, manage invitations, and communicate seamlessly with attendees. Built with modern Android development practices and Firebase integration.

## 🛠️ Technical Stack

- **Platform**: Android (Kotlin/Java)
- **Backend**: Firebase (Authentication, FireStore, Cloud Messaging)
- **Architecture**: MVVM with Repository pattern
- **UI Framework**: Android Views with Material Design
- **Real-time Features**: Firebase Realtime Database
- **Location Services**: Google Maps integration
- **AI Integration**: Custom chatbot implementation
- **CI/CD**: GitHub Actions with secure secrets management

## ✨ Key Features

### 🔐 User Management
- **Secure Authentication**: Complete registration, login, and password reset system
- **Profile Customization**: Create and edit detailed user profiles
- **Friend Network**: Add and manage friends with dedicated friend lists
- **User Discovery**: Browse and connect with other users

### 👥 Group & Community System
- **Public & Private Groups**: Create or join groups based on interests
- **Group Management**: Advanced group administration tools
- **Group Activities**: Dedicated screens for group interactions and management
- **Flexible Membership**: Easy joining/leaving mechanisms

### 🎊 Event Planning & Management
- **Event Creation**: Comprehensive event setup with date/time management
- **Invitation System**: Send invitations and track responses
- **Attendance Tracking**: Monitor who's coming to your events
- **Event Administration**: Tools for event organizers and administrators

### 💬 Communication Hub
- **Real-time Messaging**: In-app chat system for seamless communication
- **Message History**: Persistent conversation tracking
- **AI-Powered Chatbot**: Intelligent assistant for user support and engagement

### 🛠️ Administrative Tools
- **Admin Dashboard**: Special controls for platform administrators
- **Content Moderation**: Tools for managing users and content
- **Analytics & Insights**: Track app usage and engagement

## 📱 Application Architecture

For detailed architecture documentation, see [ARCHITECTURE.docx](./ARCHITECTURE.docx)

### Core Data Models
- **User Management**: User profiles, authentication, and preferences
- **Group System**: Group creation, membership, and administration
- **Messaging**: Chat functionality with message persistence
- **Event Planning**: Event creation, invitations, and attendance tracking

### Utility Components
- **Common Utilities**: Shared functionality and helper classes
- **Metadata Handling**: Extended app metadata and configuration
- **Map Integration**: Location-based features and utilities
- **UI Theming**: Consistent design system and themes

## 🖥️ User Interface Screens

### 🚀 Onboarding Experience
- **Dynamic Splash Screen**: Engaging app introduction
- **Interactive Intro**: Multi-slide app overview with smooth transitions
- **Streamlined Authentication**: User-friendly registration and login flows

### 👤 Profile & Social Features
- **Profile Management**: Comprehensive profile editing capabilities
- **Social Discovery**: Friend finding and connection features
- **Activity Tracking**: User engagement and activity monitoring

### 🎯 Event Management Interface
- **Event Creation Wizard**: Step-by-step event setup
- **Date Management**: Flexible scheduling and rescheduling tools
- **Guest Management**: Invitation sending, tracking, and attendee management
- **Communication Hub**: Event-specific messaging and updates

### 💬 Communication Features
- **Chat Interface**: Modern messaging experience
- **Conversation Management**: Message threading and history
- **Real-time Updates**: Live messaging with instant notifications

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Latest stable version
- **Android SDK**: API level 23 (Android 6.0) or higher
- **Firebase Account**: For backend services
- **Internet Connection**: Required for social features and real-time sync

### Installation Steps

1. **Clone the Repository**
   ```bash
   git clone https://github.com/yourusername/partymaker.git
   cd partymaker
   ```

2. **Firebase Configuration**
   ```bash
   # Copy the template file
   cp google-services.json.template app/google-services.json
   
   # Edit with your Firebase configuration
   # Get your config from Firebase Console
   ```

3. **API Keys Setup**
   ```bash
   # Copy the API keys template
   cp api_keys.xml.template app/src/main/res/values/api_keys.xml
   
   # Add your actual API keys (Maps, Analytics, etc.)
   ```

4. **Build and Run**
   - Open the project in Android Studio
   - Sync Gradle files
   - Build and run on device or emulator

### Configuration Files
⚠️ **Security Note**: Configuration files containing sensitive data are gitignored:
- `google-services.json` - Firebase configuration
- `local.properties` - API keys and secrets


## 📋 Recent Updates & Improvements

### ✅ Bug Fixes
- Resolved authentication flow errors in login and registration
- Fixed user location display issues on home screen
- Improved group joining functionality and error handling

### 🎯 Feature Enhancements
- Enhanced invitation system with better tracking
- Improved chat performance and message delivery
- Streamlined admin controls and moderation tools

### 🔄 Performance Optimizations
- Optimized database queries for faster loading
- Improved memory management for large friend lists
- Enhanced image loading and caching

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. **Clone the Repository**
   ```bash
   git clone https://github.com/yourusername/partymaker.git
   ```

2. **Create Feature Branch**
   ```bash
   git checkout -b feature/your-amazing-feature
   ```

3. **Commit Changes**
   ```bash
   git commit -m "feat: add your amazing feature"
   ```

4. **Push and Create PR**
   ```bash
   git push origin feature/your-amazing-feature
   ```

### Development Guidelines
- Follow Android coding conventions
- Write meaningful commit messages
- Add tests for new features
- Update documentation as needed

## 📄 License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.

## 🆘 Support & Contact

- **Issues**: Report bugs and request features via GitHub Issues
- **Documentation**: Check the Wiki for detailed guides
- **Community**: Join our community discussions

---

**Made with ❤️ for bringing people together through amazing events!**
