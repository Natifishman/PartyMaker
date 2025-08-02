# Base Classes - Enterprise Foundation

This package contains the base classes that provide common functionality for all UI components in the PartyMaker app.

## Overview

The base classes implement the **Template Method Pattern** to standardize common operations while allowing specific implementations in subclasses.

## Base Classes

### 1. BaseActivity<VM extends BaseViewModel>

**Purpose**: Provides common functionality for all activities.

**Features**:
- Automatic ViewModel initialization
- Standardized ActionBar setup
- Consistent lifecycle management
- Common error/success message handling
- Performance monitoring with logging

**Usage Example**:
```java
public class ChatActivity extends BaseActivity<GroupChatViewModel> {
    
    @Override
    protected int getLayoutId() {
        return R.layout.activity_party_chat;
    }
    
    @Override
    protected Class<GroupChatViewModel> getViewModelClass() {
        return GroupChatViewModel.class;
    }
    
    @Override
    protected String getActivityTitle() {
        return "Chat";
    }
    
    @Override
    protected void initViews() {
        // Initialize your views here
        messagesList = findViewById(R.id.messagesList);
        sendButton = findViewById(R.id.sendButton);
    }
    
    @Override
    protected void setupObservers() {
        // Setup LiveData observers
        viewModel.getMessages().observe(this, this::updateMessages);
    }
    
    @Override
    protected void setupClickListeners() {
        // Setup click listeners
        sendButton.setOnClickListener(v -> sendMessage());
    }
}
```

### 2. BaseFragment<VM extends BaseViewModel>

**Purpose**: Provides common functionality for all fragments.

**Features**:
- Automatic ViewModel initialization
- Standardized lifecycle management
- Memory leak prevention
- Common error/success message handling
- Host activity communication

**Usage Example**:
```java
public class GroupListFragment extends BaseFragment<GroupViewModel> {
    
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_group_list;
    }
    
    @Override
    protected Class<GroupViewModel> getViewModelClass() {
        return GroupViewModel.class;
    }
    
    @Override
    protected void initViews(@NonNull View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }
    
    @Override
    protected void setupObservers() {
        viewModel.getGroups().observe(this, this::updateGroups);
    }
}
```

### 3. BaseAdapter<T, VH extends BaseViewHolder>

**Purpose**: Provides common functionality for all RecyclerView adapters.

**Features**:
- Type-safe item handling
- Built-in click listeners
- Common list operations (add, remove, update)
- Performance optimizations
- Null safety

**Usage Example**:
```java
public class GroupAdapter extends BaseAdapter<Group, GroupAdapter.GroupViewHolder> {
    
    @Override
    protected int getItemLayoutId() {
        return R.layout.item_group;
    }
    
    @Override
    protected GroupViewHolder createViewHolder(@NonNull View view) {
        return new GroupViewHolder(view);
    }
    
    @Override
    protected void onBindViewHolder(@NonNull GroupViewHolder holder, @NonNull Group item, int position) {
        // Additional binding logic if needed
    }
    
    static class GroupViewHolder extends BaseViewHolder {
        TextView groupName;
        TextView memberCount;
        
        GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.groupName);
            memberCount = itemView.findViewById(R.id.memberCount);
        }
        
        @Override
        public void bind(@NonNull Object item, int position) {
            Group group = (Group) item;
            groupName.setText(group.getGroupName());
            memberCount.setText(group.getMemberCount() + " members");
        }
    }
}
```

## Benefits

### 1. **DRY Principle (Don't Repeat Yourself)**
- Common code written once instead of in every Activity/Fragment/Adapter
- Single source of truth for common functionality

### 2. **Consistency**
- All Activities have the same ActionBar styling
- Consistent ViewModel initialization across the app
- Standardized error handling

### 3. **Maintainability**
- Changes to common functionality only need to be made in one place
- Easier to add new features that affect all Activities
- Reduced code duplication means fewer bugs

### 4. **Enterprise Standards**
- Follows Android best practices
- Implements Template Method pattern correctly
- Provides scalable architecture for large teams

### 5. **Performance**
- Reduced memory usage through shared code
- Consistent lifecycle management prevents memory leaks
- Built-in performance monitoring

## Migration Guide

### Converting Existing Activities

**Before (Traditional approach)**:
```java
public class MyActivity extends AppCompatActivity {
    private static final String TAG = "MyActivity";
    private MyViewModel viewModel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        
        // Setup ActionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("My Title");
        actionBar.setBackgroundDrawable(...);
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(MyViewModel.class);
        
        // Initialize views
        initViews();
        setupObservers();
        setupClickListeners();
    }
    
    // ... rest of the code
}
```

**After (Using BaseActivity)**:
```java
public class MyActivity extends BaseActivity<MyViewModel> {
    
    @Override
    protected int getLayoutId() { return R.layout.activity_my; }
    
    @Override
    protected Class<MyViewModel> getViewModelClass() { return MyViewModel.class; }
    
    @Override
    protected String getActivityTitle() { return "My Title"; }
    
    @Override
    protected void initViews() {
        // Only your specific view initialization
    }
    
    @Override
    protected void setupObservers() {
        // Only your specific observers
    }
    
    @Override
    protected void setupClickListeners() {
        // Only your specific click listeners
    }
}
```

## Best Practices

1. **Always extend the appropriate base class** for new UI components
2. **Override only what you need** - base classes provide sensible defaults
3. **Use the error/success methods** provided by base classes for consistent UX
4. **Follow the template method pattern** - call super when overriding lifecycle methods
5. **Keep specific logic in subclasses** - base classes should only contain common functionality

## Architecture Integration

The base classes integrate seamlessly with the existing PartyMaker architecture:

- **MVVM Pattern**: Automatic ViewModel initialization and lifecycle management
- **Repository Pattern**: ViewModels can easily access repositories
- **Security**: Error handling respects security guidelines
- **Utils Integration**: Base classes use existing utility classes

This foundation supports the enterprise-grade architecture and ensures consistent, maintainable code across the entire application.