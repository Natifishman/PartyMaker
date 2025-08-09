Inspection Results
Android Lint: Accessibility
Accessibility in Custom Views
MainActivity.java
Custom view `FloatingActionButton` has setOnTouchListener called on it but does not override performClick
Android Lint: Correctness
Behavior change when requesting photo library access
AndroidManifest.xml
Your app is currently not handling Selected Photos Access introduced in Android 14+
Implied default locale in case conversion
SecurityScanActivity.java
Implicitly using the default locale is a common source of bugs: Use String.format(Locale, ...) instead
Incorrect constant
MainActivity.java
Must be one of: LottieDrawable.RESTART, LottieDrawable.REVERSE
Incorrect ObjectAnimator Property
ButtonAnimationHelper.java
Could not find property setter method setCardElevation on android.view.View
Could not find property setter method setCardElevation on android.view.View
Obsolete Gradle Dependency
libs.versions.toml
A newer version of com.google.firebase:firebase-bom than 34.0.0 is available: 34.1.0
Using discouraged APIs
SecurityAgent.java
Use of this function is discouraged because resource reflection makes it harder to perform build optimizations and compile-time verification of code. It is much more efficient to retrieve resources by identifier (e.g. R.foo.bar) than by name (e.g. getIdentifier("bar", "foo", null)).
Android Lint: Internationalization
Hardcoded text
activity_main.xml
Hardcoded string "Search", should use @string resource
Hardcoded string "Search your parties...", should use @string resource
Hardcoded string "Voice search", should use @string resource
Hardcoded string "Loading your parties...", should use @string resource
activity_main_chatbot.xml
Hardcoded string "Online", should use @string resource
Hardcoded string "Chat options", should use @string resource
Hardcoded string "Attachment options", should use @string resource
Hardcoded string "AI is thinking...", should use @string resource
activity_main_edit_profile.xml
Hardcoded string "Edit profile picture", should use @string resource
Hardcoded string "Profile Information", should use @string resource
Hardcoded string "Sign Out", should use @string resource
activity_main_public_parties.xml
Hardcoded string "Search", should use @string resource
Hardcoded string "Search parties...", should use @string resource
Hardcoded string "Filter parties", should use @string resource
Hardcoded string "All", should use @string resource
Hardcoded string "Today", should use @string resource
Hardcoded string "This Week", should use @string resource
Hardcoded string "Free", should use @string resource
Hardcoded string "No parties", should use @string resource
Hardcoded string "No Public Parties", should use @string resource
Hardcoded string "There are no public parties available right now.nCheck back later or create your own!", should use @string resource
Hardcoded string "Create Party", should use @string resource
activity_party_chat_enterprise.xml
Hardcoded string "Party avatar", should use @string resource
Hardcoded string "Party Name", should use @string resource
Hardcoded string "5 members", should use @string resource
Hardcoded string "Chat options", should use @string resource
Hardcoded string "Attach file", should use @string resource
Hardcoded string "Type a message...", should use @string resource
Hardcoded string "Send message", should use @string resource
Hardcoded string "Someone is typing...", should use @string resource
activity_party_create.xml
Hardcoded string "Success!", should use @string resource
activity_party_create_enterprise.xml
Hardcoded string "Party creation", should use @string resource
Hardcoded string "Basic Information", should use @string resource
Hardcoded string "Party Name", should use @string resource
Hardcoded string "Description (Optional)", should use @string resource
Hardcoded string "Date & Time", should use @string resource
Hardcoded string "Select Date", should use @string resource
Hardcoded string "Tap to select date", should use @string resource
Hardcoded string "Select Time", should use @string resource
Hardcoded string "Tap to select time", should use @string resource
Hardcoded string "Location", should use @string resource
Hardcoded string "Party Location", should use @string resource
Hardcoded string "Party Settings", should use @string resource
Hardcoded string "Privacy", should use @string resource
Hardcoded string "Make Public", should use @string resource
Hardcoded string "Allow anyone to find and join", should use @string resource
Hardcoded string "Entry Price (Optional)", should use @string resource
Hardcoded string "Create party", should use @string resource
Hardcoded string "Create Party", should use @string resource
item_chat_message_enterprise.xml
Hardcoded string "This is a sent message example", should use @string resource
Hardcoded string "12:34", should use @string resource
Hardcoded string "Message status", should use @string resource
Hardcoded string "User avatar", should use @string resource
Hardcoded string "John Doe", should use @string resource
Hardcoded string "This is a received message example", should use @string resource
Hardcoded string "12:30", should use @string resource
Hardcoded string "Today", should use @string resource
loading_lottie.xml
Hardcoded string "Main Content", should use @string resource
Hardcoded string "Loading...", should use @string resource
Hardcoded string "Something went wrong", should use @string resource
loading_overlay.xml
Hardcoded string "Creating your amazing party...", should use @string resource
TextView Internationalization
CreateGroupActivity.java
String literal in setText can not be translated. Use Android resources instead.
LottieLoadingDemoActivity.java
String literal in setText can not be translated. Use Android resources instead.
String literal in setText can not be translated. Use Android resources instead.
String literal in setText can not be translated. Use Android resources instead.
MainActivity.java
String literal in setText can not be translated. Use Android resources instead.
Android Lint: Performance
Invalidating All RecyclerView Data
GptChatActivity.java
It will always be more efficient to use more specific change events if you can. Rely on notifyDataSetChanged as a last resort.
Node can be replaced by a TextView with compound drawables
item_chat_message_enterprise.xml
This tag and its children can be replaced by one <TextView/> and a compound drawable
Obsolete SDK_INT Version Check
ButtonAnimationHelper.java
Unnecessary; SDK_INT is always >= 33
Unnecessary; SDK_INT is always >= 33
Unnecessary; SDK_INT is always >= 33
Overdraw: Painting regions more than once
activity_auth_login.xml
Possible overdraw: Root element paints background @drawable/bg_party_screen with a theme that also paints a background (inferred theme is @style/Theme.PartyMaker)
activity_auth_register.xml
Possible overdraw: Root element paints background @drawable/bg_party_screen_reverse with a theme that also paints a background (inferred theme is @style/Theme.PartyMaker)
activity_auth_reset.xml
Possible overdraw: Root element paints background @drawable/bg_party_screen with a theme that also paints a background (inferred theme is @style/Theme.PartyMaker)
activity_intro_slider1.xml
Possible overdraw: Root element paints background @color/bg_screen1 with a theme that also paints a background (inferred theme is @style/Theme.PartyMaker)
activity_intro_slider2.xml
Possible overdraw: Root element paints background @color/bg_screen2 with a theme that also paints a background (inferred theme is @style/Theme.PartyMaker)
activity_intro_slider3.xml
Possible overdraw: Root element paints background @color/bg_screen3 with a theme that also paints a background (inferred theme is @style/Theme.PartyMaker)
activity_main.xml
Possible overdraw: Root element paints background @color/primaryBlue with a theme that also paints a background (inferred theme is @style/Theme.PartyMaker)
activity_main_chatbot.xml
Possible overdraw: Root element paints background @color/primaryBlue with a theme that also paints a background (inferred theme is @style/Theme.PartyMaker)
activity_main_edit_profile.xml
Possible overdraw: Root element paints background @color/primaryBlue with a theme that also paints a background (inferred theme is @style/Theme.PartyMaker)
activity_main_server_settings.xml
Possible overdraw: Root element paints background @drawable/bg_party_screen with a theme that also paints a background (inferred theme is @style/Theme.PartyMaker)
activity_party_change_date.xml
Possible overdraw: Root element paints background @drawable/bg_party_screen with a theme that also paints a background (inferred theme is @style/Theme.PartyMaker)
activity_party_friends_add.xml
Possible overdraw: Root element paints background @drawable/bg_party_screen with a theme that also paints a background (inferred theme is @style/Theme.PartyMaker)
activity_party_friends_remove.xml
Possible overdraw: Root element paints background #6cd7efff with a theme that also paints a background (inferred theme is @style/Theme.PartyMaker)
activity_party_join.xml
Possible overdraw: Root element paints background @drawable/bg_party_screen with a theme that also paints a background (inferred theme is @style/Theme.PartyMaker)
activity_party_settings.xml
Possible overdraw: Root element paints background @drawable/bg_party_screen with a theme that also paints a background (inferred theme is @style/Theme.PartyMaker)
helper_bottom_navigation.xml
Possible overdraw: Root element paints background @color/white with a theme that also paints a background (inferred theme is @style/Theme.PartyMaker)
helper_map_view.xml
Possible overdraw: Root element paints background @drawable/bg_party_screen with a theme that also paints a background (inferred theme is @style/Theme.PartyMaker)
loading_overlay.xml
Possible overdraw: Root element paints background @color/primaryBlue with a theme that also paints a background (inferred theme is @style/Theme.PartyMaker)
Unnecessary parent layout
activity_intro_slider1.xml
This LinearLayout layout or its RelativeLayout parent is unnecessary; transfer the background attribute to the other view
activity_intro_slider2.xml
This LinearLayout layout or its RelativeLayout parent is unnecessary; transfer the background attribute to the other view
activity_intro_slider3.xml
This LinearLayout layout or its RelativeLayout parent is unnecessary; transfer the background attribute to the other view
Unused resources
activity_party_chat_enterprise.xml
The resource R.layout.activity_party_chat_enterprise appears to be unused
activity_party_create_enterprise.xml
The resource R.layout.activity_party_create_enterprise appears to be unused
bottom_nav_background.xml
The resource R.drawable.bottom_nav_background appears to be unused
circle_button.xml
The resource R.drawable.circle_button appears to be unused
colors.xml
The resource R.color.md_theme_surfaceTint appears to be unused
The resource R.color.md_theme_scrim appears to be unused
The resource R.color.party_primary appears to be unused
The resource R.color.party_primary_variant appears to be unused
The resource R.color.party_secondary appears to be unused
The resource R.color.party_secondary_variant appears to be unused
The resource R.color.party_surface_elevated appears to be unused
The resource R.color.party_background_gradient_start appears to be unused
The resource R.color.party_background_gradient_end appears to be unused
The resource R.color.modern_accent_blue appears to be unused
The resource R.color.modern_accent_purple appears to be unused
The resource R.color.modern_accent_green appears to be unused
The resource R.color.modern_accent_orange appears to be unused
The resource R.color.party_type_birthday appears to be unused
The resource R.color.party_type_wedding appears to be unused
The resource R.color.party_type_corporate appears to be unused
The resource R.color.party_type_casual appears to be unused
The resource R.color.party_type_celebration appears to be unused
ic_arrow_forward.xml
The resource R.drawable.ic_arrow_forward appears to be unused
ic_check.xml
The resource R.drawable.ic_check appears to be unused
ic_delete.xml
The resource R.drawable.ic_delete appears to be unused
ic_edit.xml
The resource R.drawable.ic_edit appears to be unused
ic_location.xml
The resource R.drawable.ic_location appears to be unused
ic_monetization.xml
The resource R.drawable.ic_monetization appears to be unused
ic_my_location.xml
The resource R.drawable.ic_my_location appears to be unused
ic_person_add.xml
The resource R.drawable.ic_person_add appears to be unused
ic_public.xml
The resource R.drawable.ic_public appears to be unused
ic_schedule.xml
The resource R.drawable.ic_schedule appears to be unused
item_chat_message_enterprise.xml
The resource R.layout.item_chat_message_enterprise appears to be unused
navigation_rail_item_color.xml
The resource R.color.navigation_rail_item_color appears to be unused
rounded_edittext.xml
The resource R.drawable.rounded_edittext appears to be unused
strings.xml
The resource R.string.running_security_scan appears to be unused
The resource R.string.scan_completed_successfully appears to be unused
The resource R.string.scan_failed appears to be unused
The resource R.string.grade_with_value appears to be unused
The resource R.string.no_security_issues_found appears to be unused
styles_material3.xml
The resource R.style.Widget_PartyMaker_CardView_Filled appears to be unused
The resource R.style.Widget_PartyMaker_CardView_Outlined appears to be unused
The resource R.style.Widget_PartyMaker_Button appears to be unused
The resource R.style.Widget_PartyMaker_Button_Tonal appears to be unused
The resource R.style.Widget_PartyMaker_Button_Outlined appears to be unused
The resource R.style.Widget_PartyMaker_Button_Text appears to be unused
The resource R.style.Widget_PartyMaker_FloatingActionButton_Large appears to be unused
The resource R.style.Widget_PartyMaker_TextInputLayout appears to be unused
The resource R.style.Widget_PartyMaker_TextInputLayout_Filled appears to be unused
The resource R.style.Widget_PartyMaker_Chip_Filter appears to be unused
The resource R.style.Widget_PartyMaker_Chip_Action appears to be unused
The resource R.style.TextAppearance_PartyMaker_DisplayLarge appears to be unused
The resource R.style.TextAppearance_PartyMaker_HeadlineLarge appears to be unused
The resource R.style.TextAppearance_PartyMaker_LabelLarge appears to be unused
The resource R.style.TextAppearance_PartyMaker_HeadlineMedium appears to be unused
The resource R.style.TextAppearance_PartyMaker_DisplayMedium appears to be unused
The resource R.style.TextAppearance_PartyMaker_TitleMedium appears to be unused
The resource R.style.TextAppearance_PartyMaker_TitleSmall appears to be unused
The resource R.style.Widget_PartyMaker_Chip_Choice appears to be unused
The resource R.style.Widget_PartyMaker_BottomNavigationView appears to be unused
The resource R.style.Widget_PartyMaker_BottomNavigationView_ActiveIndicator appears to be unused
The resource R.style.ThemeOverlay_PartyMaker_Dialog appears to be unused
The resource R.style.ThemeOverlay_PartyMaker_BottomSheetDialog appears to be unused
The resource R.style.Widget_PartyMaker_BottomSheet_Modal appears to be unused
The resource R.style.ShapeAppearance_PartyMaker_BottomSheet appears to be unused
themes.xml
The resource R.style.Theme_PartyMaker_IntroSlider appears to be unused
typing_dot.xml
The resource R.drawable.typing_dot appears to be unused
Android Lint: Security
Implements custom TLS trust manager
SSLPinningManager.java
Implementing a custom X509TrustManager is error-prone and likely to be insecure. It is likely to disable certificate validation altogether, and is non-trivial to implement correctly without calling Android's default implementation.
Android Lint: Usability
Button should be borderless
activity_party_friends_add.xml
Buttons in button bars should be borderless; use style="?android:attr/buttonBarButtonStyle" (and ?android:attr/buttonBarStyle on the parent)
Buttons in button bars should be borderless; use style="?android:attr/buttonBarButtonStyle" (and ?android:attr/buttonBarStyle on the parent)
Icon densities validation
drawable-mdpi
Missing the following drawables in drawable-mdpi: bg_chat.jpg, bg_party_screen.png, bg_party_screen_reverse.jpg, ic_cake.png, ic_cake_black.png... (3 more)
drawable-xxhdpi
Missing the following drawables in drawable-xxhdpi: ic_cake_about_us.png (found in drawable-mdpi)
Icon is specified both as .xml file and as a bitmap
ic_launcher.webp
The following images appear both as density independent .xml files and as bitmap files: srcmainresmipmap-anydpiic_launcher.xml, srcmainresmipmap-hdpiic_launcher.webp
ic_launcher.webp
The following images appear both as density independent .xml files and as bitmap files: srcmainresmipmap-anydpiic_launcher.xml, srcmainresmipmap-hdpiic_launcher.webp
ic_launcher.webp
The following images appear both as density independent .xml files and as bitmap files: srcmainresmipmap-anydpiic_launcher.xml, srcmainresmipmap-hdpiic_launcher.webp
ic_launcher.webp
The following images appear both as density independent .xml files and as bitmap files: srcmainresmipmap-anydpiic_launcher.xml, srcmainresmipmap-hdpiic_launcher.webp
ic_launcher.webp
The following images appear both as density independent .xml files and as bitmap files: srcmainresmipmap-anydpiic_launcher.xml, srcmainresmipmap-hdpiic_launcher.webp
ic_launcher.xml
The following images appear both as density independent .xml files and as bitmap files: srcmainresmipmap-anydpiic_launcher.xml, srcmainresmipmap-hdpiic_launcher.webp
ic_launcher_round.webp
The following images appear both as density independent .xml files and as bitmap files: srcmainresmipmap-anydpiic_launcher_round.xml, srcmainresmipmap-hdpiic_launcher_round.webp
ic_launcher_round.webp
The following images appear both as density independent .xml files and as bitmap files: srcmainresmipmap-anydpiic_launcher_round.xml, srcmainresmipmap-hdpiic_launcher_round.webp
ic_launcher_round.webp
The following images appear both as density independent .xml files and as bitmap files: srcmainresmipmap-anydpiic_launcher_round.xml, srcmainresmipmap-hdpiic_launcher_round.webp
ic_launcher_round.webp
The following images appear both as density independent .xml files and as bitmap files: srcmainresmipmap-anydpiic_launcher_round.xml, srcmainresmipmap-hdpiic_launcher_round.webp
ic_launcher_round.webp
The following images appear both as density independent .xml files and as bitmap files: srcmainresmipmap-anydpiic_launcher_round.xml, srcmainresmipmap-hdpiic_launcher_round.webp
ic_launcher_round.xml
The following images appear both as density independent .xml files and as bitmap files: srcmainresmipmap-anydpiic_launcher_round.xml, srcmainresmipmap-hdpiic_launcher_round.webp
Missing density folder
res
Missing density variation folders in src\main\res: drawable-hdpi, drawable-xhdpi
Monochrome icon is not defined
ic_launcher.xml
The application adaptive icon is missing a monochrome tag
ic_launcher_round.xml
The application adaptive roundIcon is missing a monochrome tag
Use Autofill
activity_main_public_parties.xml
Missing autofillHints attribute
Gradle
Style
Unused version catalog entry
libs.versions.toml
Dependency alias 'spring-boot-starter-validation' is not used in build scripts
Groovy
Data flow
Unused assignment
build.gradle
Assignment is not used
HTML
Obsolete attribute
problems-report.html
Obsolete attribute
README.md
Obsolete attribute
JVM languages
Empty method
AsyncTaskReplacement
All implementations of this method are empty
LoginActivity
The method is empty
MainActivity
The method is empty
ProfileViewModel
Method only calls its super
RegisterActivity
The method is empty
SecurityAgentExample
The method is empty
SecurityScanActivity
The method is empty
UiStateManager
The method is empty
Unstable API Usage
settings.gradle.kts
'getRepositoriesMode()' is marked unstable with @Incubating
'org.gradle.api.initialization.resolve.RepositoriesMode' is marked unstable with @Incubating
'FAIL_ON_PROJECT_REPOS' is declared in unstable enum 'org.gradle.api.initialization.resolve.RepositoriesMode' marked with @Incubating
'repositories(org.gradle.api.Action<? super org.gradle.api.artifacts.dsl.RepositoryHandler>)' is marked unstable with @Incubating
Java
Class structure
Field can be local
CreateGroupActivity
Field can be converted to a local variable
Code maturity
Commented out code
SecurityAgentExample
Commented out code (6 lines)
Deprecated member is still used
AuthenticationManager
Deprecated member 'isFirebaseAuthAvailable' is still used
Deprecated member 'setCurrentUserSession' is still used
Method can be extracted
DateManagementViewModel
It's possible to extract method returning 'suggestion' from a long surrounding method
PartyMainActivity
It's possible to extract method returning 'groupUpdates' from a long surrounding method
Compiler issues
Unchecked warning
EditProfileActivity
Unchecked cast: 'java.lang.Object' to 'java.util.Map<java.lang.String,java.lang.Object>'
EncryptedSharedPreferencesManager
Unchecked cast: 'java.lang.Object' to 'java.util.Set<java.lang.String>'
Control flow issues
'if' statement with identical branches or common parts
GroupDiscoveryViewModel
'if' statement can be collapsed
Data flow
Boolean method is always inverted
ContentSharingManager
Calls to boolean method 'validateInputs()' are always inverted
GroupAdapter
Calls to boolean method 'isObjectNotExistError()' are always inverted
Redundant local variable
SecureConfigManager
Local variable 'apiKey' is redundant
Declaration redundancy
Access static member via instance reference
GroupCreationViewModel
Static member 'com.example.partymaker.utils.media.ImageCompressor.compressImage(android.content.Context, android.net.Uri, com.example.partymaker.utils.media.ImageCompressor.CompressCallback)' accessed via instance reference
Method always returns the same value
FirebaseAccessManager
Method 'isServerModeEnabled()' always returns 'true'
GroupAdapter
Method 'handleItemLongClick()' always returns 'true'
GroupDiscoveryViewModel
Method 'canUserJoinGroup()' always returns 'false'
Method can be made 'void'
AuthenticationManager
Return value of the method is never used
Method parameter always has the same value
AuthViewModel
Value of parameter 'message' is always '"Email login successful"'
Value of parameter 'baseMessage' is always '"Email login failed"'
FirebaseServerClient
Value of parameter 'timeout' is always 'Network.DEFAULT_TIMEOUT_MS'
GroupRepositoryTest
Value of parameter 'name' is always '"Test Group"'
ImageCompressor
Value of parameter 'maxWidth' is always 'ImageCompressor.MAX_WIDTH'
Value of parameter 'maxHeight' is always 'ImageCompressor.MAX_HEIGHT'
IntroActivity
Value of parameter 'i' is always 'IntroActivity.PAGE_OFFSET'
MainActivity
Value of parameter 'showToast' is always 'false'
Value of parameter 'activityClass' is always 'ServerSettingsActivity.class'
MemoryManager
Value of parameter 'message' is always '"Context cannot be null"'
PartyMainActivity
Value of parameter 'forceRefresh' is always 'false'
Redundant 'throws' clause
AsyncTaskReplacement
The declared exception 'Exception' is never thrown in any method implementation
The declared exception 'Exception' is never thrown in any method implementation
Unused declaration
Entry Points
AdminOptionsActivity
AdminSettingsActivity
AsyncTaskReplacement
DatabaseTask
DatabaseException
DatabaseException(String)
DatabaseException(String, Throwable)
NetworkTask
NetworkException
NetworkException(String)
NetworkException(String, Throwable)
AuthenticationManager
AuthException
AuthException(String)
AuthException(String, Throwable)
isFirebaseAuthAvailable(Context)
isSessionValid(Context)
isUserAuthenticated(Context)
refreshSession(Context)
setCurrentUserSession(Context, String)
BaseViewModelTest
rule
setUp()
testClearError_ClearsErrorRelatedData()
testClearMessages_ClearsAllMessages()
testDefaultTimeout_IsSetCorrectly()
testExecuteIfNotLoading_WhenLoading_DoesNotExecuteOperation()
testExecuteIfNotLoading_WhenNotLoading_ExecutesOperation()
testInitialState_AllValuesAreDefault()
testOnCleared_ClearsAllStatesAndMessages()
testSetError_WithErrorType_UpdatesLiveDataAndGeneratesMessage()
testSetError_WithMessage_UpdatesLiveData()
testSetLoading_False_UpdatesLiveData()
testSetLoading_True_UpdatesLiveData()
testSetSuccess_UpdatesLiveData()
ChangeDateActivity
ChatActivity
ChatAdapter
ChatAdapter(Context, int, int, List<ChatMessage>)
ChatAdapter(Context, int, List<ChatMessage>)
ChatMessage
setMessageContent(HashMap<String, Object>)
ChatMessageDao
Converters
booleanMapToString(Map<String, Boolean>)
fromString(String)
fromStringToBoolean(String)
mapToString(Map<String, Object>)
CreateGroupActivity
DateManagementViewModel
DateManagementViewModel(Application)
EditProfileActivity
EncryptedSharedPreferencesManager
SecurityAuditLogger
ExampleInstrumentedTest
useAppContext()
ExampleUnitTest
addition_isCorrect()
FriendsAddActivity
FriendsRemoveActivity
GptChatActivity
GroupCreationViewModel
GroupCreationViewModel(Application)
GroupDao
GroupDiscoveryViewModel
GroupDiscoveryViewModel(Application)
GroupManagementViewModel
GroupManagementViewModel(Application)
GroupRepositoryTest
setUp()
testGetGroup_CacheHit_ReturnsFromCache()
testGetGroup_CacheMiss_FallsBackToRemote()
testGetGroup_ForceRefresh_SkipsCache()
testObserveGroup_ReturnsLiveData()
testSaveGroup_Success_SavesRemoteAndLocal()
GroupViewModel
GroupViewModel(Application)
HybridMessageEncryption
HybridMessageEncryption(Context, String)
IntroActivity
btnNextClick(View)
btnSkipClick(View)
JoinGroupActivity
LoadingStateManager
LoadingStateManager(View, ProgressBar, TextView, View)
LoadingStateManager(View, ProgressBar, TextView, View, LottieAnimationView)
StateChangeListener
LoginActivity
LoginViewModel
LoginViewModel(Application)
LottieLoadingDemoActivity
MainActivity
MembersComingActivity
MembersInvitedActivity
MembersViewModel
MembersViewModel(Application)
MessageEncryptionManager
MessageEncryptionManager(Context, String)
PartyApplication
PartyMainActivity
PartyMainViewModel
PartyMainViewModel(Application)
PasswordValidator
PasswordValidatorTest
testGetStrengthText_VariousScores()
testPasswordsMatch_NonMatching_ReturnsFalse()
testPasswordsMatch_NullPasswords_ReturnsFalse()
testPasswordsMatch_ValidMatching_ReturnsTrue()
testValidate_CommonPassword_ReturnsInvalid()
testValidate_EmptyPassword_ReturnsInvalid()
testValidate_MaxLength_IsEnforced()
testValidate_NoDigit_ReturnsInvalid()
testValidate_NoLowercase_ReturnsInvalid()
testValidate_NoSpecialChar_ReturnsInvalid()
testValidate_NoUppercase_ReturnsInvalid()
testValidate_NullPassword_ReturnsInvalid()
testValidate_RepeatedCharacters_ReducesStrength()
testValidate_SequentialCharacters_ReducesStrength()
testValidate_StrongPassword_HighScore()
testValidate_TooShort_ReturnsInvalid()
testValidate_ValidPassword_ReturnsValid()
PermissionManager
PermissionResult
PermissionResult(String[], int[])
PublicGroupsActivity
RegisterActivity
RegisterViewModel
RegisterViewModel(Application)
ResetPasswordActivity
SecurityAgentExample
SecurityScanActivity
SecurityScanViewModel
SecurityScanViewModel(Application)
ServerDBRef
ServerSettingsActivity
ServerSettingsViewModel
ServerSettingsViewModel(Application)
SimpleSecureStorage
SimpleSecureStorage(Context)
SplashActivity
SSLPinningManager
CustomTrustManager
ThreadUtils
runOnBackground(Runnable)
UiStateManager
FeedbackBuilder
FeedbackBuilder(View)
FeedbackListener
User
getCreatedAt()
getFriendKeys()
UserDao
UsersListActivity
AdminOptionsActivity
ERROR_ADMIN_VERIFICATION
AdminSettingsActivity
TAG
AppConstants
DatabaseFields
ADMIN_KEY
FRIEND_KEYS
PROFILE_IMAGE_URL
USER_NAME
USERNAME
FirebasePaths
PROFILE_IMAGE_PRIMARY
PROFILE_IMAGE_SECONDARY
PROFILE_IMAGE_TEMPLATE
UI
DOT_ALPHA_MAX
DOT_ALPHA_MIN
DOT_ANIMATION_DELAY_MS
DOT_ANIMATION_DURATION_MS
DOT_ANIMATION_LOOP_MS
DOT_ANIMATION_STAGGER_MS
DOT_SCALE_FACTOR
NETWORK_STATUS_UPDATE_DELAY_MS
RESUME_NETWORK_CHECK_DELAY_MS
SPLASH_DELAY_MS
Validation
DOWNLOAD_BUFFER_SIZE
MAX_USERNAME_LENGTH
MIN_USERNAME_LENGTH
AppDatabase
recreateDatabase(Context)
AsyncTaskReplacement
DatabaseTask
DatabaseOperation
execute()
executeDbOperation(DatabaseOperation<T>, DatabaseCallback<T>)
anonymous (SimpleUICallback)
onError(Exception)
onPostExecute(T)
execute(BackgroundTask<T>, ResultCallback<T>)
anonymous (SimpleUICallback)
onError(Exception)
onPostExecute(T)
execute(Runnable)
getExecutorStatus()
isShutdown()
NetworkTask
executeRequest(HttpRequestTask, NetworkCallback)
anonymous (SimpleUICallback)
onError(Exception)
onPostExecute(String)
HttpRequestTask
executeRequest()
shutdown()
submit(Callable<T>)
AuthViewModel
clearAuthenticationState()
clearPasswordResetStatus()
getCurrentUser()
getIsPasswordResetSent()
isValidPassword(String)
isValidUsername(String)
MIN_USERNAME_LENGTH
RC_SIGN_IN
resetPassword(String)
signOut(Context)
BaseViewModel
TAG
BaseViewModelTest
errorObserver
errorTypeObserver
loadingObserver
mockApplication
successObserver
ButtonAnimationHelper
applyErrorShake(View)
applyFabMorphAnimation(FloatingActionButton, int)
applyPulseAnimation(View)
applyStaggerAnimation(View[])
enhanceCard(CardView)
ChangeDateActivity
TAG
ChatActivity
ANIMATION_HIDE_DELAY_MS
GPT_TIMEOUT_MS
retryHandler
sendMessageWithRetry(String, ChatMessage, int)
anonymous (OperationCallback)
onError(String)
onSuccess()
SUCCESS_ANIMATION_DELAY_MS
updateGroupMessageKeys(String)
anonymous (DataCallback)
onError(String)
onSuccess(Group)
updateGroupWithRetry(Group, int)
anonymous (OperationCallback)
onError(String)
onSuccess()
ChatAdapter
addMessage(ChatMessage)
TAG
ChatMessage
canBeEncrypted()
createCopy()
getEncryptionMetadata()
markAsDecrypted()
markAsEncrypted()
ChatMessageDao
deleteMessageByKey(String)
deleteMessagesForGroup(String)
getMessageByKey(String)
getMessagesForGroup(String)
insertMessage(ChatMessage)
insertMessages(List<ChatMessage>)
observeMessagesForGroup(String)
updateMessage(ChatMessage)
ConnectivityManager
getLastNetworkError()
setLastNetworkError(ErrorType)
ContentSharingManager
sharePartyImage(Context, Group, Bitmap)
Converters
dateToTimestamp(Date)
fromTimestamp(Long)
CreateGroupActivity
handleBackToFirstStep(View)
Parameter 'v' is not used
handleBackToSecondStep(View)
Parameter 'v' is not used
handleChatFabClick(View)
Parameter 'view' is not used
handleCreateGroup(View)
Parameter 'v' is not used
handleDoneClick(View)
Parameter 'v' is not used
handleImageSelection(View)
Parameter 'v' is not used
handleNextButtonClick(View)
Parameter 'v' is not used
handleSecondStepNavigation(View)
Parameter 'v' is not used
loadingStateManager
CustomRefreshAnimationHelper
createPullDownFeedback(View, float)
createRefreshSuccessFeedback(View)
createShimmerEffect(View)
resetPullDownFeedback(View)
stopRefreshBounceAnimation(View)
DataSource
clearCache()
deleteItem(K, OperationCallback)
getAllItems(DataCallback<List<T>>)
getItem(K, DataCallback<T>)
observeAllItems()
observeItem(K)
saveItem(K, T, OperationCallback)
updateItem(K, Map<String, Object>, OperationCallback)
DateManagementViewModel
DATETIME_FORMAT_FULL
getCurrentGroup()
getDateTimeUpdated()
getIsDateTimeChanged()
getIsDateValid()
getIsTimeValid()
getMaxSelectableDate()
getMinSelectableDate()
getOriginalDate()
getOriginalTime()
getSchedulingSuggestion()
getSelectedDate()
getSelectedDay()
getSelectedHour()
getSelectedMinute()
getSelectedMonth()
getSelectedTime()
getSelectedYear()
getUpdateInProgress()
getValidationError()
initialize(String, String)
resetToOriginal()
setToCurrentTime()
setToToday()
timeFormat
updateDateTime()
DBRef
checkImageExists(String, OnImageExistsListener)
currentUser
EditProfileActivity
ACTION_BAR_ELEVATION
ACTION_BAR_TITLE_COLOR
createActionBarGradient()
EncryptedSharedPreferencesManager
clear()
contains(String)
Editor
clear()
commit()
Editor(Editor)
remove(String)
EncryptedSharedPreferencesManager(Context)
getBoolean(String, boolean)
getFloat(String, float)
getInstance(Context)
getInt(String, int)
getLong(String, long)
getString(String, String)
getStringSet(String, Set<String>)
isEncryptionAvailable()
migrateFromRegularPreferences(String)
putBoolean(String, boolean)
putFloat(String, float)
putInt(String, int)
putLong(String, long)
putString(String, String)
putStringSet(String, Set<String>)
remove(String)
SecurityAuditLogger
EventType
EnhancedSecureStorage
getLong(String, long)
putLong(String, long)
FileManager
copyFile(Context, Uri, File, FileOperationCallback)
createImageFile(Context)
deleteFile(File)
executor
getUriForFile(Context, File)
saveBitmapToFile(Bitmap, File, FileOperationCallback)
FirebaseAccessManager
getMessagesRef()
getUsersRef()
isServerModeEnabled()
FirebaseServerClient
createUser(User, DataCallback<User>)
anonymous (SimpleUICallback)
onError(Exception)
onPostExecute(User)
deleteData(String, OperationCallback)
anonymous (SimpleUICallback)
onError(Exception)
onPostExecute(Boolean)
makePostRequest(String, String)
networkManager
saveUser(String, User, OperationCallback)
anonymous (SimpleUICallback)
onError(Exception)
onPostExecute(Boolean)
GlideImageLoader
clearDiskCache(Context)
loadImage(Context, String, ImageView)
loadProfileImage(Context, String, ImageView)
preloadImages(Context, List<String>)
GptViewModel
clearChat()
getCurrentResponse()
getGptResponse(String)
sendMessage(String, String, String)
Group
GROUP_TYPE_PRIVATE
GROUP_TYPE_PUBLIC
GroupAdapter
GroupViewHolder
handleItemClick(View)
Parameter 'view' is not used
GroupChatViewModel
addMessage(ChatMessage)
getGroupKey()
GroupCreationViewModel
createGroup(String)
getCreatedGroup()
getGroupCreated()
getGroupDescription()
getGroupLocation()
getGroupName()
getGroupPrice()
getImageUploadInProgress()
getIsFormValid()
getIsPublicGroup()
getMaxMembers()
getSelectedDate()
getSelectedImagePath()
getSelectedImageUri()
getSelectedTime()
selectGroupImage(Uri)
anonymous (CompressCallback)
onCompressError(String)
onCompressSuccess(File)
setGroupDescription(String)
setGroupLocation(String)
setGroupName(String)
setGroupPrice(String)
setIsPublicGroup(boolean)
setMaxMembers(int)
setSelectedDate(String)
setSelectedTime(String)
GroupDao
getGroupsForUser()
insertGroups(List<Group>)
GroupDiscoveryViewModel
clearFilters()
filterByDate(String)
filterByLocation(String)
getDateFilter()
getFilteredGroups()
getHasMoreGroups()
getIsLoadingMore()
getJoinedGroupKey()
getJoinInProgress()
getLeftGroupKey()
getLocationFilter()
getPublicGroups()
getSearchQuery()
getSelectedGroup()
getShowOnlyJoinable()
joinGroup(Group)
leaveGroup(Group)
loadMoreGroups()
refreshGroups()
searchGroups(String)
selectGroup(Group)
setCurrentUser(String)
setShowOnlyJoinable(boolean)
GroupKeyManager
deleteGroupEncryption(String)
getStatus()
GroupKeyManager(Context, String)
Variable 'userStorage' is never used
INITIAL_KEY_VERSION
KEY_CREATED
KEY_CREATOR
KEY_LAST_ROTATED
KEY_LAST_UPDATED
KEY_VERSION
MEMBER_QUERY_LIMIT
PATH_MEMBERS
PATH_METADATA
GroupManagementViewModel
addMember(User)
deleteGroup()
demoteMember(User)
getActiveMembers()
getAvailableUsers()
getGroupDeleted()
getGroupMembers()
getGroupUpdateInProgress()
getIsUserAdmin()
getLastOperationResult()
getManagedGroup()
getMemberOperationInProgress()
getPendingInvitations()
getPendingInvites()
getTotalMembers()
initialize(String, String)
loadAvailableUsers()
promoteMember(User)
removeMember(User)
updateGroupSettings(Group)
GroupMessageEncryption
clearAllGroupKeys()
getEncryptedGroups()
getGroupKeyForSharing(String)
storeGroupKey(String, String)
GroupRepository
CACHE_TIMEOUT_MS
deleteGroup(String, Callback<Boolean>)
anonymous (OperationCallback)
onComplete()
onError(String)
deleteGroup(String, OperationCallback)
anonymous (OperationCallback)
onComplete()
anonymous (OperationCallback)
onComplete()
onError(String)
onError(String)
fetchAllFromRemoteAndCache(DataCallback<List<Group>>)
anonymous (DataCallback)
onDataLoaded(List<Group>)
anonymous (OperationCallback)
onComplete()
onError(String)
onError(String)
anonymous (DataCallback)
onDataLoaded(List<Group>)
onError(String)
FIELD_ADMIN_KEY
FIELD_PROFILE_IMAGE_URL
FIELD_USER_NAME
FIELD_USERNAME
getAllGroups(DataCallback<List<Group>>, boolean)
anonymous (DataCallback)
onDataLoaded(List<Group>)
onError(String)
getGroup(String, Callback<Group>)
anonymous (DataCallback)
onDataLoaded(Group)
onError(String)
getPublicGroups(boolean, Callback<List<Group>>)
Parameter 'forceRefresh' is not used
inviteMemberToGroup(String, String, Callback<Boolean>)
Parameter 'groupKey' is not used
Parameter 'userKey' is not used
joinGroup(String, String, Callback<Boolean>)
anonymous (OperationCallback)
onComplete()
onError(String)
joinGroup(String, String, OperationCallback)
anonymous (OperationCallback)
onComplete()
onError(String)
leaveGroup(String, String, Callback<Boolean>)
anonymous (OperationCallback)
onComplete()
onError(String)
leaveGroup(String, String, OperationCallback)
anonymous (OperationCallback)
onComplete()
onError(String)
MAX_CACHE_RETRIES
observeAllGroups()
updateAttendanceStatus(String, String, boolean, Callback<Boolean>)
Parameter 'groupKey' is not used
Parameter 'userKey' is not used
Parameter 'isComing' is not used
updateGroup(String, Map<String, Object>, OperationCallback)
anonymous (OperationCallback)
onComplete()
anonymous (OperationCallback)
onComplete()
onError(String)
onError(String)
GroupRepositoryTest
mockLocalDataSource
mockRemoteDataSource
GroupViewModel
addGroupMember(User)
addPublicGroup(Group)
getCurrentGroup()
getGroupKey()
getGroupMembers()
getIsGroupCreated()
getIsGroupJoined()
getPublicGroups()
setCurrentGroup(Group)
setGroupCreated(boolean)
setGroupJoined(boolean)
setGroupKey(String)
setGroupMembers(List<User>)
setPublicGroups(List<Group>)
TAG
HybridMessageEncryption
decryptMessage(String)
encryptForGroup(String, Map<String, String>)
ensureUserKeyPair()
Variable 'keyPair' is never used
getEncryptionStatus()
getMyPublicKey()
HybridMessageEncryption(Context, String)
Parameter 'context' is not used
IntroViewModel
getCurrentPage()
getTotalPages()
LoadingStateManager
currentLoadingMessage
getCurrentState()
isLoading()
isNetworkSyncing()
isShowingCelebration()
isShowingContent()
isShowingError()
isShowingSuccess()
setLottieAnimation(int)
showCelebration()
showEmpty()
showEmptyWithAnimation()
showError()
showError(String)
Parameter 'errorMessage' is not used
Variable 'errorText' is never used
showErrorWithAnimation()
showLoading()
showNetworkSync()
showSuccess()
StateChangeListener
onStateChanged(LoadingState, LoadingState)
LoginActivity
createOrLoginTestUser(String, String)
handleAboutClick(View)
Parameter 'view' is not used
handleGoogleSignInClick(View)
Parameter 'view' is not used
handleLoginClick(View)
Parameter 'view' is not used
handleRegisterClick(View)
Parameter 'view' is not used
handleResetPasswordClick(View)
Parameter 'view' is not used
setupLoadingStateManager()
Variable 'loadingStateManager' is never used
LoginViewModel
createUserProfileFromFirebase(FirebaseUser)
getCurrentUser()
getIsEmailValid()
getIsFormValid()
getIsGoogleSignInAvailable()
getIsPasswordValid()
getLoggedInUser()
getLoginSuccess()
getRememberMe()
getSavedEmail()
handleError(Exception)
isUserLoggedIn()
loginWithEmail(String, String)
loginWithGoogle(String)
logout()
setRememberMe(boolean)
validateEmail(String)
validatePassword(String)
MainActivity
createLoadingStateManager(ProgressBar, TextView)
createLoadingStateManagerWithLottie(ProgressBar, TextView, LottieAnimationView, View)
Parameter 'errorView' is not used
findOrCreateProgressBar()
hideLoadingState()
isCurrentUserMemberOfGroup(Group)
isValidAdapterPosition(int)
updateRecyclerViewVisibility(boolean)
Parameter 'isLoading' is not used
MainActivityViewModel
createGroup(String, Group)
deleteGroup(String)
getSelectedGroup()
joinGroup(String, String)
anonymous (OperationCallback)
onComplete()
onError(String)
leaveGroup(String, String)
anonymous (OperationCallback)
onComplete()
onError(String)
loadAllGroups(boolean)
selectGroup(String, boolean)
updateGroup(String, Map<String, Object>)
MembersComingActivity
LOADING_MESSAGE
NO_MEMBERS_MESSAGE
MembersInvitedActivity
showUserInfo(User)
MembersViewModel
clearFilters()
getAllMembers()
getCanInviteMembers()
getCanRemoveMembers()
getComingMembers()
getComingMembersCount()
getCurrentGroup()
getCurrentViewType()
getFilteredMembers()
getFriendMembers()
getInvitedMembers()
getInvitedMembersCount()
getIsCurrentUserAdmin()
getLastOperationResult()
getMemberOperationInProgress()
getOnlineMembersCount()
getSearchQuery()
getSelectedMember()
getShowOnlineOnly()
getTotalMembersCount()
initialize(String, String)
inviteMember(User)
loadFriendMembers(Group)
Parameter 'group' is not used
refreshMembers()
removeMember(User)
selectMember(User)
setSearchQuery(String)
setShowOnlineOnly(boolean)
setViewType(MemberType)
updateMemberAttendance(User, boolean)
MemoryManager
BYTES_TO_KB
getCacheSize(Context)
getProcessId()
isLowMemory(Context)
MessageEncryptionManager
decryptChatMessage(ChatMessage)
encryptChatMessage(ChatMessage)
getEncryptionStatus()
isMessageEncrypted(String)
rotateEncryptionKey(Context)
NavigationManager
setupBottomNavigation(Activity)
NetworkErrorHandler
categorizeException(Exception)
getRetryDelay(ErrorType, int)
getUserFriendlyMessage(ErrorType, Context)
Parameter 'context' is not used
isRecoverable(ErrorType)
logError(String, String, ErrorType, Exception)
NetworkManager
executeWithTimeout(Runnable, long, Runnable)
getNetworkAvailabilityLiveData()
getSecureClient()
testSSLConnection(String)
NetworkUtils
testConnection(String)
NotificationManager
showMessageNotification(Context, String, String, String)
showPartyNotification(Context, String, String, Group)
unsubscribeFromGlobalAnnouncements()
unsubscribeFromGroup(String)
OptimizedRecyclerAdapter
addItem(T)
clearItems()
getItems()
isEmpty()
removeItem(int)
PartyMainActivity
ANIMATION_DURATION_MS
deleteGroupCompletely()
anonymous (OperationCallback)
onSuccess()
Variable 'repository' is never used
EDIT_TEXT_PADDING_DP
GEO_URI_PREFIX
getMonthNumber(String)
MAP_ZOOM_LEVEL
PANEL_COLLAPSE_OFFSET
PANEL_EXPAND_OFFSET
QUERY_PARAM
setupPullUpGesture()
togglePaymentPanel()
ZOOM_PARAM
PartyMainViewModel
canPerformAdminActions()
getComingMembers()
getComingMembersCount()
getCurrentGroup()
getCurrentUser()
getFormattedGroupDate()
getFormattedGroupTime()
getGroupMembers()
getInvitedMembers()
getInvitedMembersCount()
getIsUserAdmin()
getIsUserComing()
getIsUserInvited()
getIsUserMember()
getJoinLeaveInProgress()
getLastActionResult()
getStatusUpdateInProgress()
getTotalMembersCount()
initialize(String, String)
joinGroup()
leaveGroup()
refreshGroupData()
updateAttendanceStatus(boolean)
PasswordValidator
MIN_REPEATED_SEQUENCE
MIN_SEQUENTIAL_SEQUENCE
SEQUENTIAL_CHAR_PENALTY
PasswordValidator
getStrengthDescription(String)
isRecommendedStrength(String)
RECOMMENDED_LENGTH
ValidationResult
ValidationResult(boolean, String)
PermissionManager
canUseNotificationFeatures(Context)
canUseStorageFeatures(Context)
getPermissionRationale(String)
PermissionResult
allGranted
REQUEST_NOTIFICATION_PERMISSION
REQUEST_STORAGE_PERMISSION
requestCameraPermissionsIfNeeded(Activity)
requestLocationPermissionsIfNeeded(Activity)
shouldShowPermissionRationale(Activity, String)
ProfileViewModel
createUser(String, User)
anonymous (OperationCallback)
onComplete()
onError(String)
getSelectedUser()
getUsers()
loadAllUsers(boolean)
loadUser(String, boolean)
PublicGroupsActivity
groups
RegisterActivity
handleRegistrationError(Exception)
RegisterViewModel
getEmailVerificationSent()
getIsEmailValid()
getIsFormValid()
getIsPasswordValid()
getIsUsernameValid()
getRegisteredUser()
getRegistrationSuccess()
getValidationError()
registerUser(String, String, String, String)
RegisterViewModel(Application)
Variable 'serverClient' is never used
resendConfirmationEmail()
validateEmail(String)
validatePassword(String)
validateUsername(String)
RemoteGroupDataSource
deleteItem(String, OperationCallback)
anonymous (OperationCallback)
onError(String)
onSuccess()
getAllItems(DataCallback<List<Group>>)
anonymous (DataCallback)
onError(String)
onSuccess(Map<String, Group>)
updateItem(String, Map<String, Object>, OperationCallback)
anonymous (OperationCallback)
onError(String)
onSuccess()
ResetPasswordActivity
handleDarkThemeClick(View)
Parameter 'view' is not used
handleHelpClick(View)
Parameter 'view' is not used
handleHideInstructionsClick(View)
Parameter 'view' is not used
handleLightThemeClick(View)
Parameter 'view' is not used
handleResetPasswordClick(View)
Parameter 'view' is not used
ResetPasswordViewModel
getCanSendReset()
getNextResetAllowedTime()
getRemainingCooldownSeconds()
getResetEmailSent()
getTargetEmail()
resendPasswordResetEmail()
resetRateLimit()
validateEmail(String)
Result
error(String, ErrorType)
SecureAuthenticationManager
clearFailedAttempts(Context, String)
getCurrentUserKey(Context)
getRemainingLockoutTime(Context, String)
isLockedOut(Context, String)
isLoggedIn(Context)
KEY_REFRESH_TOKEN
logout(Context)
recordFailedAttempt(Context, String)
refreshSession(Context)
SecureConfigManager
clearAll()
isGoogleMapsConfigured()
isOpenAiConfigured()
setGoogleMapsApiKey(String)
setOpenAiApiKey(String)
setServerUrl(String)
SecurityAgent
areFirebaseRulesSecure()
Variable 'db' is never used
DEFAULT_TIMEOUT_MS
saveReportToFile(SecurityReport, File)
SecurityAgentExample
exportReportExample(Context)
Variable 'htmlReport' is never used
Variable 'markdownReport' is never used
integrateWithApp(Context)
runBasicScan(Context)
schedulePeriodicScans(Context)
Parameter 'context' is not used
uploadReportExample(Context)
SecurityIssue
setCategory(String)
setTitle(String)
SecurityReport
getAppInfo()
getCriticalIssues()
getDeviceInfo()
getHighIssues()
getIssues()
getLowIssues()
getMediumIssues()
getRecommendations()
getScanDuration()
getTimestamp()
getTotalIssues()
SecurityScanActivity
displayDetailedIssues(SecurityReport)
Parameter 'report' is not used
TAG
SecurityScanViewModel
applyAutoFixes()
clearScanHistory()
generateSecurityReport()
getAutoFixAvailable()
getCriticalIssues()
getCurrentScanStep()
getHighIssues()
getLastScanTime()
getLatestReport()
getLowIssues()
getMediumIssues()
getNextRecommendedAction()
getResolvedIssues()
getScanHistory()
getScanInProgress()
getScanProgress()
getSecurityIssues()
getSecurityRecommendations()
getSecurityScore()
getTotalIssuesFound()
resolveSecurityIssue(SecurityIssue)
SecurityScanViewModel(Application)
Variable 'securityAgent' is never used
startSecurityScan()
stopScan()
ServerDBRef
Auth
checkImageExists(String, OnImageExistsListener)
CurrentUser
getServerClient()
TAG
ServerModeManager
isServerModeEnabled(Context)
ServerSettingsActivity
LOCAL_SERVER_URL
ServerSettingsViewModel
getCurrentServerUrl()
getIsSaved()
getIsServerReachable()
getIsTestingConnection()
resetSavedStatus()
resetToDefault()
testConnection(String)
SimpleSecureStorage
BUFFER_SIZE
clear()
contains(String)
edit()
getLong(String, long)
putLong(String, long)
remove(String)
SplashActivity
initializeDependencies()
Variable 'serverUrl' is never used
SplashViewModel
getIsInitializationComplete()
setFirstTimeUser(boolean)
SSLPinningManager
updateCertificatePins(String[])
ThreadUtils
getBackgroundExecutor()
getLightweightExecutor()
getMainThreadExecutor()
getNetworkExecutor()
runOnNetworkThread(Runnable)
TAG
UiAnimationHelper
bounceSuccess(View)
createPulseAnimation(View)
crossFade(View, View)
fadeIn(View)
fadeOut(View)
scalePress(View)
shakeError(View)
slideInFromBottom(View)
slideOutToBottom(View, Runnable)
UiStateManager
dismissActiveSnackbars(View)
Parameter 'containerView' is not used
FeedbackBuilder
action(String, Runnable)
asError()
asInfo()
asSuccess()
asWarning()
duration(int)
message(String)
show()
FeedbackListener
onActionClicked()
onDismissed()
onRetryRequested()
showConfirmation(View, String, String, Runnable)
showError(View, ErrorType)
showLoading(View)
showLongToast(Context, String)
showNetworkStatus(View, boolean)
showProgress(View, String, int)
showSuccess(Context, View, int)
showToast(Context, String)
User
getUserName()
setUserName(String)
UserDao
deleteUserByKey(String)
getUserByEmail(String)
getUserByKey(String)
insertUser(User)
observeAllUsers()
observeUserByKey(String)
updateUser(User)
UserFeedbackManager
dismissDialog(AlertDialog)
FeedbackCallback
onFeedbackCancelled()
onFeedbackSubmitted(String, int)
handleResult(Context, View, Result<T>, ResultHandler<T>)
Parameter 'context' is not used
showChoiceDialog(Context, String, String[], ChoiceCallback)
showDestructiveConfirmationDialog(Context, String, String, String, Runnable)
showFeedbackDialog(Context, FeedbackCallback)
Parameter 'callback' is not used
showLoadingDialog(Context, String)
showNetworkErrorDialog(Context, ErrorType, Runnable)
showOfflineNotification(View)
showOnlineNotification(View)
showProgressToast(Context, String, int)
showSingleChoiceDialog(Context, String, String[], int, SingleChoiceCallback)
showSuccessWithAction(View, String, String, Runnable)
UserRepository
createUser(User, Callback<User>)
anonymous (DataCallback)
onError(String)
onSuccess(User)
getAllUsersFromServer(DataCallback<List<User>>)
anonymous (DataCallback)
onError(String)
onSuccess(Map<String, User>)
getAllUsersLiveData()
getCurrentUser(Context, DataCallback<User>, boolean)
getCurrentUserLiveData()
getUser(String, Callback<User>)
anonymous (DataCallback)
onError(String)
onSuccess(User)
getUser(String, DataCallback<User>, boolean)
anonymous (DataCallback)
onError(String)
onSuccess(User)
saveUser(String, User, OperationCallback)
anonymous (OperationCallback)
onError(String)
onSuccess()
UsersListActivity
getContextOfApplication()
onCreate(Bundle)
Variable 'accessManager' is never used
Imports
Unused import
GroupKeyManager.java
Unused import 'import androidx.annotation.NonNull;'
Unused import 'import com.google.firebase.database.DataSnapshot;'
Unused import 'import com.google.firebase.database.DatabaseError;'
Unused import 'import com.google.firebase.database.ValueEventListener;'
Unused import 'import java.util.HashMap;'
SecurityScanActivity.java
Unused import 'import com.example.partymaker.utils.security.monitoring.SecurityIssue;'
UserAdapter.java
Unused import 'import android.annotation.SuppressLint;'
Java language level migration aids
Java 8
Lambda can be replaced with method reference
MainActivity
Lambda can be replaced with method reference
Statement lambda can be replaced with expression lambda
LottieLoadingDemoActivity
Statement lambda can be replaced with expression lambda
Javadoc
Dangling Javadoc comment
FirebaseServerClient
Dangling Javadoc comment
Dangling Javadoc comment
Memory
Inner class may be 'static'
SSLPinningManager
Inner class 'CustomHostnameVerifier' may be 'static'
Probable bugs
Nullability problems
@NotNull/@Nullable problems
GroupCreationViewModel
Not annotated parameter overrides @NonNull parameter
GroupDateTimeManager
Not annotated method overrides method annotated with @RecentlyNonNull
ImageCompressor
Overriding method parameters are not annotated
PasswordValidator
Not annotated method overrides method annotated with @RecentlyNonNull
SecurityIssue
Not annotated method overrides method annotated with @RecentlyNonNull
Constant values
CreateGroupActivity
Condition 'animationOverlay != null' is always 'true'
Condition 'animationOverlay != null' is always 'true'
EditProfileActivity
Condition 'username != null' is always 'true'
FirebaseServerClient
Condition 'response.body() != null' is always 'true'
GroupViewModel
Condition 'existingUser.getUserKey() != null' is always 'true'
LocalGroupDataSource
Condition 'group.getGroupKey() == null' is always 'false'
MainActivity
Condition 'loadingOverlay != null' is always 'true'
PartyMainActivity
Condition 'newAdminKey == null' is always 'true'
RemoteGroupDataSource
Condition 'group.getGroupKey() == null' is always 'false'
SecureConfigManager
Condition 'apiKey != null' is always 'true'
Condition 'apiKey != null && !apiKey.isEmpty() && !apiKey.equals(DEFAULT_API_KEY_PLACEHOLDER)' is always 'false'
Condition '!apiKey.isEmpty()' is always 'false'
'equals()' called on itself
PartyMainActivity
'equals()' called on itself
Instantiation of utility class
GroupCreationViewModel
Instantiation of utility class 'ImageCompressor'
Mismatched case in 'String' operation
SecureConfigManager
Method 'equals()' always returns false: the argument contains an uppercase symbol while the qualifier doesn't contain uppercase symbols
Nullability and data flow problems
EditProfileActivity
Method invocation 'setDisplayHomeAsUpEnabled' may produce 'NullPointerException'
LoginActivity
Passing 'null' argument to parameter annotated as @NotNull
Passing 'null' argument to parameter annotated as @NotNull
Passing 'null' argument to parameter annotated as @NotNull
Passing 'null' argument to parameter annotated as @NotNull
Passing 'null' argument to parameter annotated as @NotNull
Result of method call ignored
FileManager
Result of 'File.delete()' is ignored
PasswordValidator
Result of 'String.length()' is ignored
SecurityScanActivity
Result of 'File.mkdirs()' is ignored
Result of 'File.mkdirs()' is ignored
Statement with empty body
LoginViewModel
'if' statement has empty body
Unused assignment
DateManagementViewModel
Variable 'suggestion' initializer '""' is redundant
EditProfileActivity
Variable 'username' initializer '""' is redundant
Threading issues
Busy wait
NetworkUtils
Call to 'Thread.sleep()' in a loop, probably busy-waiting
ResetPasswordViewModel
Call to 'Thread.sleep()' in a loop, probably busy-waiting
Verbose or redundant code constructs
Duplicate branches in 'switch'
ChangeDateActivity
Branch in 'switch' is a duplicate of the default branch
SecurityAgent
Branch in 'switch' is a duplicate of the default branch
Redundant type cast
GroupRepository
Casting 'remoteDataSource' to 'RemoteGroupDataSource' is redundant
Visibility
Class is exposed outside of its visibility scope
ChatbotAdapter
Class 'MessageViewHolder' is exposed outside its defined visibility scope
Class 'MessageViewHolder' is exposed outside its defined visibility scope
GroupAdapter
Class 'GroupViewHolder' is exposed outside its defined visibility scope
Class 'GroupViewHolder' is exposed outside its defined visibility scope
Class 'GroupViewHolder' is exposed outside its defined visibility scope
Manifest
Unknown or misspelled header name
MANIFEST.MF
Header name is unknown or spelled incorrectly
Header name is unknown or spelled incorrectly
Header name is unknown or spelled incorrectly
Header name is unknown or spelled incorrectly
Header name is unknown or spelled incorrectly
Header name is unknown or spelled incorrectly
Markdown
Incorrect table formatting
CODE_POLISH_TRACKER.md
Table is not correctly formatted
fix-bot.md
Table is not correctly formatted
README.md
Table is not correctly formatted
Table is not correctly formatted
Table is not correctly formatted
Table is not correctly formatted
Table is not correctly formatted
README.md
Table is not correctly formatted
Incorrectly numbered list item
library.md
Item is not correctly numbered. Expected item number 1, but got 4.
Item is not correctly numbered. Expected item number 2, but got 5.
Item is not correctly numbered. Expected item number 3, but got 6.
Item is not correctly numbered. Expected item number 1, but got 7.
Item is not correctly numbered. Expected item number 2, but got 8.
Item is not correctly numbered. Expected item number 3, but got 9.
Item is not correctly numbered. Expected item number 4, but got 10.
Item is not correctly numbered. Expected item number 1, but got 11.
Item is not correctly numbered. Expected item number 2, but got 12.
Item is not correctly numbered. Expected item number 1, but got 13.
Item is not correctly numbered. Expected item number 2, but got 14.
Item is not correctly numbered. Expected item number 1, but got 15.
Item is not correctly numbered. Expected item number 2, but got 16.
Item is not correctly numbered. Expected item number 1, but got 17.
Item is not correctly numbered. Expected item number 2, but got 18.
Item is not correctly numbered. Expected item number 1, but got 19.
Item is not correctly numbered. Expected item number 2, but got 20.
Item is not correctly numbered. Expected item number 1, but got 21.
Shell script
ShellCheck
gradlew
APP_NAME appears unused. Verify use (or export if used externally).
In POSIX sh, ulimit -H is undefined.
In POSIX sh, ulimit -n is undefined.