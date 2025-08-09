package com.example.partymaker.ui.features.groups.creation;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.data.repository.GroupRepository;
import com.example.partymaker.utils.infrastructure.system.ThreadUtils;
import com.example.partymaker.viewmodel.BaseViewModel;

/**
 * ViewModel for the party creation wizard functionality.
 * 
 * Handles the creation process through the wizard steps including validation
 * and final party creation.
 */
public class CreatePartyViewModel extends BaseViewModel {
    
    private static final String TAG = "CreatePartyViewModel";
    
    // Dependencies
    private final GroupRepository groupRepository;
    
    // LiveData for creation state
    private final MutableLiveData<Boolean> partyCreated = new MutableLiveData<>();
    private final MutableLiveData<Group> createdParty = new MutableLiveData<>();
    
    // Wizard step data - can be expanded as needed
    private final MutableLiveData<String> partyName = new MutableLiveData<>();
    private final MutableLiveData<String> partyLocation = new MutableLiveData<>();
    private final MutableLiveData<String> partyDate = new MutableLiveData<>();
    private final MutableLiveData<String> partyTime = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isPublicParty = new MutableLiveData<>();
    
    public CreatePartyViewModel(@NonNull Application application) {
        super(application);
        this.groupRepository = GroupRepository.getInstance();
        initializeDefaults();
    }
    
    // Getters for LiveData
    public LiveData<Boolean> getPartyCreated() {
        return partyCreated;
    }
    
    public LiveData<Group> getCreatedParty() {
        return createdParty;
    }
    
    public LiveData<String> getPartyName() {
        return partyName;
    }
    
    public LiveData<String> getPartyLocation() {
        return partyLocation;
    }
    
    public LiveData<String> getPartyDate() {
        return partyDate;
    }
    
    public LiveData<String> getPartyTime() {
        return partyTime;
    }
    
    public LiveData<Boolean> getIsPublicParty() {
        return isPublicParty;
    }
    
    // Setters
    public void setPartyName(String name) {
        partyName.setValue(name);
    }
    
    public void setPartyLocation(String location) {
        partyLocation.setValue(location);
    }
    
    public void setPartyDate(String date) {
        partyDate.setValue(date);
    }
    
    public void setPartyTime(String time) {
        partyTime.setValue(time);
    }
    
    public void setIsPublicParty(boolean isPublic) {
        isPublicParty.setValue(isPublic);
    }
    
    /**
     * Creates a party with the current wizard data.
     * Returns a LiveData that can be observed for the result.
     */
    public LiveData<Result<Group>> createParty() {
        MutableLiveData<Result<Group>> resultLiveData = new MutableLiveData<>();
        
        if (isCurrentlyLoading()) {
            resultLiveData.setValue(new Result<>(false, null, "Creation already in progress"));
            return resultLiveData;
        }
        
        setLoading(true);
        
        ThreadUtils.runInBackground(() -> {
            try {
                // Create a basic group object from the wizard data
                Group newParty = createPartyFromWizardData();
                
                // Use the repository to create the party
                groupRepository.createGroup(newParty, new GroupRepository.Callback<Group>() {
                    @Override
                    public void onSuccess(Group result) {
                        ThreadUtils.runOnMainThread(() -> {
                            setLoading(false);
                            partyCreated.setValue(true);
                            createdParty.setValue(result);
                            resultLiveData.setValue(new Result<>(true, result, "Party created successfully"));
                        });
                    }
                    
                    @Override
                    public void onError(Exception error) {
                        ThreadUtils.runOnMainThread(() -> {
                            setLoading(false);
                            partyCreated.setValue(false);
                            String errorMessage = error.getMessage() != null ? error.getMessage() : "Unknown error";
                            resultLiveData.setValue(new Result<>(false, null, errorMessage));
                        });
                    }
                });
                
            } catch (Exception e) {
                ThreadUtils.runOnMainThread(() -> {
                    setLoading(false);
                    partyCreated.setValue(false);
                    String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
                    resultLiveData.setValue(new Result<>(false, null, errorMessage));
                });
            }
        });
        
        return resultLiveData;
    }
    
    private void initializeDefaults() {
        partyCreated.setValue(false);
        isPublicParty.setValue(true);
    }
    
    private Group createPartyFromWizardData() {
        Group party = new Group();
        
        // Set basic properties from wizard data
        party.setGroupName(partyName.getValue() != null ? partyName.getValue().trim() : "");
        party.setGroupLocation(partyLocation.getValue() != null ? partyLocation.getValue().trim() : "");
        party.setCreatedAt(String.valueOf(System.currentTimeMillis()));
        
        // Set party type (0 = public, 1 = private)
        Boolean isPublic = isPublicParty.getValue();
        party.setGroupType(isPublic != null && isPublic ? 0 : 1);
        
        // Parse date and time if available
        String date = partyDate.getValue();
        if (date != null && date.matches("\\d{2}/\\d{2}/\\d{4}")) {
            String[] dateParts = date.split("/");
            party.setGroupDays(dateParts[0]);
            party.setGroupMonths(dateParts[1]);
            party.setGroupYears(dateParts[2]);
        }
        
        String time = partyTime.getValue();
        if (time != null && time.matches("\\d{2}:\\d{2}")) {
            String[] timeParts = time.split(":");
            party.setGroupHours(timeParts[0]);
            party.setGroupMinutes(timeParts[1]);
        }
        
        // Initialize empty collections and default values
        party.setGroupPrice("0");
        party.setCanAdd(true);
        
        return party;
    }
    
    /**
     * Result wrapper class for operation results.
     */
    public static class Result<T> {
        private final boolean success;
        private final T data;
        private final String error;
        
        public Result(boolean success, T data, String error) {
            this.success = success;
            this.data = data;
            this.error = error;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public T getData() {
            return data;
        }
        
        public String getError() {
            return error;
        }
    }
}