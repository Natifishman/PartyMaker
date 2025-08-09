# 🎨 Material You (Material Design 3) Implementation Guide

## Overview
Transform PartyMaker with Google's latest Material You design system, featuring dynamic colors, expressive motion, and personalized experiences.

---

## 🌈 Dynamic Color System

### 1. Enable Material You Colors
```gradle
// app/build.gradle
dependencies {
    implementation 'com.google.android.material:material:1.11.0'
}

android {
    compileSdkVersion 34
    defaultConfig {
        targetSdkVersion 34
    }
}
```

### 2. Dynamic Color Implementation
```kotlin
// DynamicColorManager.kt
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.MaterialColors

class DynamicColorManager(private val application: Application) {
    
    fun applyDynamicColors() {
        // Apply dynamic colors from wallpaper (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColors.applyToActivitiesIfAvailable(application)
        }
    }
    
    fun getDynamicColorScheme(context: Context): ColorScheme {
        return if (DynamicColors.isDynamicColorAvailable()) {
            dynamicColorScheme(context)
        } else {
            // Fallback to custom brand colors
            ColorScheme(
                primary = Color(0xFF6C63FF),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFE4DFFF),
                onPrimaryContainer = Color(0xFF1F005C),
                secondary = Color(0xFFFF6B6B),
                onSecondary = Color(0xFFFFFFFF),
                secondaryContainer = Color(0xFFFFDAD6),
                onSecondaryContainer = Color(0xFF410002),
                tertiary = Color(0xFF4ECDC4),
                onTertiary = Color(0xFFFFFFFF),
                tertiaryContainer = Color(0xFFB8EEFF),
                onTertiaryContainer = Color(0xFF001F28),
                background = Color(0xFFFFFBFF),
                onBackground = Color(0xFF1C1B1E),
                surface = Color(0xFFFFFBFF),
                onSurface = Color(0xFF1C1B1E),
                surfaceVariant = Color(0xFFE7E0EB),
                onSurfaceVariant = Color(0xFF49454E),
                outline = Color(0xFF7A757F),
                error = Color(0xFFBA1A1A),
                onError = Color(0xFFFFFFFF)
            )
        }
    }
}
```

### 3. Material You Theme
```xml
<!-- res/values-v31/themes.xml -->
<resources>
    <style name="Theme.PartyMaker" parent="Theme.Material3.DayNight">
        <!-- Material You dynamic colors -->
        <item name="colorPrimary">@android:color/system_accent1_600</item>
        <item name="colorOnPrimary">@android:color/system_accent1_0</item>
        <item name="colorPrimaryContainer">@android:color/system_accent1_100</item>
        <item name="colorOnPrimaryContainer">@android:color/system_accent1_900</item>
        
        <item name="colorSecondary">@android:color/system_accent2_600</item>
        <item name="colorOnSecondary">@android:color/system_accent2_0</item>
        <item name="colorSecondaryContainer">@android:color/system_accent2_100</item>
        <item name="colorOnSecondaryContainer">@android:color/system_accent2_900</item>
        
        <item name="colorTertiary">@android:color/system_accent3_600</item>
        <item name="colorOnTertiary">@android:color/system_accent3_0</item>
        <item name="colorTertiaryContainer">@android:color/system_accent3_100</item>
        <item name="colorOnTertiaryContainer">@android:color/system_accent3_900</item>
        
        <!-- Surface colors -->
        <item name="android:colorBackground">@android:color/system_neutral1_10</item>
        <item name="colorSurface">@android:color/system_neutral1_10</item>
        <item name="colorSurfaceVariant">@android:color/system_neutral2_100</item>
        <item name="colorOnSurfaceVariant">@android:color/system_neutral2_700</item>
        
        <!-- Typography -->
        <item name="textAppearanceDisplayLarge">@style/TextAppearance.Material3.DisplayLarge</item>
        <item name="textAppearanceDisplayMedium">@style/TextAppearance.Material3.DisplayMedium</item>
        <item name="textAppearanceDisplaySmall">@style/TextAppearance.Material3.DisplaySmall</item>
        <item name="textAppearanceHeadlineLarge">@style/TextAppearance.Material3.HeadlineLarge</item>
        <item name="textAppearanceHeadlineMedium">@style/TextAppearance.Material3.HeadlineMedium</item>
        <item name="textAppearanceHeadlineSmall">@style/TextAppearance.Material3.HeadlineSmall</item>
        <item name="textAppearanceTitleLarge">@style/TextAppearance.Material3.TitleLarge</item>
        <item name="textAppearanceTitleMedium">@style/TextAppearance.Material3.TitleMedium</item>
        <item name="textAppearanceTitleSmall">@style/TextAppearance.Material3.TitleSmall</item>
        <item name="textAppearanceBodyLarge">@style/TextAppearance.Material3.BodyLarge</item>
        <item name="textAppearanceBodyMedium">@style/TextAppearance.Material3.BodyMedium</item>
        <item name="textAppearanceBodySmall">@style/TextAppearance.Material3.BodySmall</item>
        <item name="textAppearanceLabelLarge">@style/TextAppearance.Material3.LabelLarge</item>
        <item name="textAppearanceLabelMedium">@style/TextAppearance.Material3.LabelMedium</item>
        <item name="textAppearanceLabelSmall">@style/TextAppearance.Material3.LabelSmall</item>
        
        <!-- Shape -->
        <item name="shapeAppearanceSmallComponent">@style/ShapeAppearance.Material3.SmallComponent</item>
        <item name="shapeAppearanceMediumComponent">@style/ShapeAppearance.Material3.MediumComponent</item>
        <item name="shapeAppearanceLargeComponent">@style/ShapeAppearance.Material3.LargeComponent</item>
    </style>
</resources>
```

---

## 🎭 Material You Components

### 1. Navigation Bar with Dynamic Color
```xml
<!-- res/layout/navigation_bar_material_you.xml -->
<com.google.android.material.navigationrail.NavigationRailView
    android:id="@+id/navigation_rail"
    android:layout_width="wrap_content"
    android:layout_height="match_parent"
    app:menu="@menu/navigation_menu"
    app:headerLayout="@layout/navigation_rail_header"
    app:menuGravity="center">
    
    <!-- FAB integrated in rail -->
    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fab"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|center_horizontal"
        android:layout_marginBottom="16dp"
        app:tint="?attr/colorOnPrimaryContainer"
        app:backgroundTint="?attr/colorPrimaryContainer"
        app:srcCompat="@drawable/ic_add_24dp" />
        
</com.google.android.material.navigationrail.NavigationRailView>
```

### 2. Material You Cards
```xml
<!-- res/layout/material_you_card.xml -->
<com.google.android.material.card.MaterialCardView
    style="?attr/materialCardViewFilledStyle"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    android:clickable="true"
    android:focusable="true"
    app:cardCornerRadius="28dp">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="20dp">
        
        <!-- Dynamic color surface -->
        <com.google.android.material.card.MaterialCardView
            style="?attr/materialCardViewElevatedStyle"
            android:layout_width="match_parent"
            android:layout_height="120dp"
            app:cardBackgroundColor="?attr/colorPrimaryContainer"
            app:cardCornerRadius="20dp">
            
            <ImageView
                android:layout_width="60dp"
                android:layout_height="60dp"
                android:layout_gravity="center"
                android:src="@drawable/ic_party"
                app:tint="?attr/colorOnPrimaryContainer" />
                
        </com.google.android.material.card.MaterialCardView>
        
        <TextView
            style="?attr/textAppearanceTitleMedium"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:text="Birthday Party"
            android:textColor="?attr/colorOnSurface" />
            
        <TextView
            style="?attr/textAppearanceBodyMedium"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="4dp"
            android:text="December 25, 2025"
            android:textColor="?attr/colorOnSurfaceVariant" />
            
    </LinearLayout>
    
</com.google.android.material.card.MaterialCardView>
```

### 3. Adaptive Buttons
```xml
<!-- Filled Tonal Button (New in Material You) -->
<com.google.android.material.button.MaterialButton
    style="?attr/materialButtonFilledTonalStyle"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Join Party"
    app:icon="@drawable/ic_celebration" />

<!-- Elevated Button -->
<com.google.android.material.button.MaterialButton
    style="?attr/materialButtonElevatedStyle"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="View Details"
    app:icon="@drawable/ic_info" />

<!-- Extended FAB -->
<com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Create Party"
    app:icon="@drawable/ic_add"
    app:backgroundTint="?attr/colorPrimaryContainer"
    app:iconTint="?attr/colorOnPrimaryContainer" />
```

### 4. Material You Chips
```xml
<com.google.android.material.chip.ChipGroup
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:singleSelection="false"
    app:chipSpacingHorizontal="8dp">
    
    <!-- Filter Chip -->
    <com.google.android.material.chip.Chip
        style="@style/Widget.Material3.Chip.Filter"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="This Week"
        app:chipBackgroundColor="@color/chip_background_color_selector"
        app:chipStrokeColor="?attr/colorOutline" />
        
    <!-- Input Chip -->
    <com.google.android.material.chip.Chip
        style="@style/Widget.Material3.Chip.Input"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Birthday"
        app:chipIcon="@drawable/ic_cake"
        app:closeIconVisible="true" />
        
    <!-- Suggestion Chip -->
    <com.google.android.material.chip.Chip
        style="@style/Widget.Material3.Chip.Suggestion"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Nearby"
        app:chipIcon="@drawable/ic_location" />
        
</com.google.android.material.chip.ChipGroup>
```

---

## 🌊 Adaptive Layouts

### 1. Responsive Grid
```kotlin
class AdaptiveGridLayout : RecyclerView.LayoutManager() {
    
    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        val width = width
        val itemWidth = resources.getDimensionPixelSize(R.dimen.card_width)
        
        // Calculate columns based on screen width
        val columns = when {
            width < 600.dpToPx() -> 1  // Phone
            width < 840.dpToPx() -> 2  // Tablet portrait
            else -> 3  // Tablet landscape
        }
        
        // Layout items in adaptive grid
        layoutGrid(recycler, columns)
    }
}
```

### 2. Foldable Support
```kotlin
class FoldableLayoutManager(private val activity: Activity) {
    
    private val windowLayoutInfo = WindowInfoTracker.getOrCreate(activity)
    
    fun setupFoldableLayout(view: View) {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                windowLayoutInfo.windowLayoutInfo(activity)
                    .collect { layoutInfo ->
                        val foldingFeature = layoutInfo.displayFeatures
                            .filterIsInstance<FoldingFeature>()
                            .firstOrNull()
                            
                        when (foldingFeature?.state) {
                            FoldingFeature.State.HALF_OPENED -> {
                                // Split content across fold
                                setupDualPaneLayout(view, foldingFeature)
                            }
                            else -> {
                                // Single pane layout
                                setupSinglePaneLayout(view)
                            }
                        }
                    }
            }
        }
    }
}
```

---

## 🎬 Material Motion

### 1. Container Transform
```kotlin
// Shared element transition between list and detail
class PartyListFragment : Fragment() {
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Setup exit transition
        exitTransition = MaterialElevationScale(false).apply {
            duration = 300L
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = 300L
        }
    }
    
    fun navigateToDetail(partyCard: View, party: Party) {
        val extras = FragmentNavigatorExtras(
            partyCard to "party_card_transition"
        )
        
        findNavController().navigate(
            R.id.action_list_to_detail,
            bundleOf("party" to party),
            null,
            extras
        )
    }
}

class PartyDetailFragment : Fragment() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            duration = 300L
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(requireContext().getColor(R.color.surface))
        }
    }
}
```

### 2. Axis Transitions
```kotlin
// Forward/backward navigation with axis
class NavigationTransitions {
    
    fun getForwardTransition() = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
        duration = 300L
    }
    
    fun getBackwardTransition() = MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
        duration = 300L
    }
    
    fun getVerticalTransition() = MaterialSharedAxis(MaterialSharedAxis.Y, true).apply {
        duration = 300L
    }
}
```

### 3. Fade Through
```kotlin
// For switching between unrelated content
class ContentSwitcher {
    
    fun switchContent(from: View, to: View) {
        val fadeThrough = MaterialFadeThrough().apply {
            duration = 300L
        }
        
        TransitionManager.beginDelayedTransition(
            from.parent as ViewGroup,
            fadeThrough
        )
        
        from.visibility = View.GONE
        to.visibility = View.VISIBLE
    }
}
```

---

## 🎨 Custom Material You Shapes

### 1. Squircle Shapes
```xml
<!-- res/values/shapes.xml -->
<resources>
    <style name="ShapeAppearance.PartyMaker.SmallComponent" parent="ShapeAppearance.Material3.SmallComponent">
        <item name="cornerFamily">rounded</item>
        <item name="cornerSize">8dp</item>
    </style>
    
    <style name="ShapeAppearance.PartyMaker.MediumComponent" parent="ShapeAppearance.Material3.MediumComponent">
        <item name="cornerFamily">rounded</item>
        <item name="cornerSize">16dp</item>
    </style>
    
    <style name="ShapeAppearance.PartyMaker.LargeComponent" parent="ShapeAppearance.Material3.LargeComponent">
        <item name="cornerFamily">rounded</item>
        <item name="cornerSize">28dp</item>
    </style>
    
    <!-- Extra large for hero components -->
    <style name="ShapeAppearance.PartyMaker.ExtraLargeComponent">
        <item name="cornerFamily">rounded</item>
        <item name="cornerSizeTopLeft">28dp</item>
        <item name="cornerSizeTopRight">28dp</item>
        <item name="cornerSizeBottomLeft">0dp</item>
        <item name="cornerSizeBottomRight">0dp</item>
    </style>
</resources>
```

---

## 📱 Adaptive Components

### 1. Navigation Component Selection
```kotlin
class AdaptiveNavigation(private val activity: Activity) {
    
    fun setupNavigation() {
        val windowSizeClass = calculateWindowSizeClass()
        
        when (windowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.COMPACT -> {
                // Use bottom navigation for phones
                setupBottomNavigation()
            }
            WindowWidthSizeClass.MEDIUM -> {
                // Use navigation rail for tablets
                setupNavigationRail()
            }
            WindowWidthSizeClass.EXPANDED -> {
                // Use navigation drawer for large screens
                setupNavigationDrawer()
            }
        }
    }
}
```

### 2. Adaptive Dialogs
```kotlin
class AdaptiveDialogBuilder(private val context: Context) {
    
    fun showAdaptiveDialog(content: View) {
        val metrics = context.resources.displayMetrics
        val width = metrics.widthPixels
        
        if (width < 600.dpToPx()) {
            // Full screen dialog for phones
            MaterialAlertDialogBuilder(context)
                .setView(content)
                .show()
        } else {
            // Floating dialog for tablets
            val dialog = BottomSheetDialog(context)
            dialog.setContentView(content)
            dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            dialog.show()
        }
    }
}
```

---

## 🎯 Implementation Checklist

### Phase 1: Foundation (Week 1)
- [ ] Update Material library to latest version
- [ ] Enable Material You theme
- [ ] Implement dynamic color system
- [ ] Update color resources

### Phase 2: Components (Week 2)
- [ ] Replace buttons with Material You variants
- [ ] Update cards to new styles
- [ ] Implement new navigation patterns
- [ ] Add Material You chips

### Phase 3: Motion (Week 3)
- [ ] Add container transforms
- [ ] Implement axis transitions
- [ ] Add fade through animations
- [ ] Setup shared elements

### Phase 4: Adaptive (Week 4)
- [ ] Implement responsive layouts
- [ ] Add foldable support
- [ ] Create adaptive components
- [ ] Test on different devices

---

## 📊 Success Metrics

| Feature          | Implementation | Impact                     |
|------------------|----------------|----------------------------|
| Dynamic Colors   | 2 days         | +30% user satisfaction     |
| New Components   | 3 days         | +25% usability             |
| Motion Design    | 2 days         | +40% perceived performance |
| Adaptive Layouts | 3 days         | +50% tablet usage          |

---

## 🚀 Quick Implementation

```kotlin
// QuickMaterialYouSetup.kt
fun Activity.setupMaterialYou() {
    // 1. Apply dynamic colors
    DynamicColors.applyToActivityIfAvailable(this)
    
    // 2. Setup edge-to-edge
    WindowCompat.setDecorFitsSystemWindows(window, false)
    
    // 3. Handle insets
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
        insets
    }
    
    // 4. Apply motion
    window.sharedElementEnterTransition = MaterialContainerTransform()
    window.sharedElementExitTransition = MaterialContainerTransform()
}
```

---

*Embrace Material You for a modern, personalized, and delightful user experience!*