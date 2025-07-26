package com.example.partymaker.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.utilities.ExtrasMetadata;
import com.example.partymaker.utilities.MapUtilities;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

public class AdminOptionsViewModel extends ViewModel {
    // Group and admin data
    private final MutableLiveData<Boolean> isAdminVerified = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> groupPrice = new MutableLiveData<>();
    private final MutableLiveData<String> groupLocation = new MutableLiveData<>();
    private final MutableLiveData<Boolean> priceChanged = new MutableLiveData<>();
    private final MutableLiveData<Boolean> locationChanged = new MutableLiveData<>();
    private final MutableLiveData<Boolean> finishActivity = new MutableLiveData<>();

    private String adminKey;
    private String userKey;
    private String groupKey;
    private HashMap<String, Object> friendKeys, comingKeys, messageKeys;
    private ExtrasMetadata extras;

    public void setExtras(ExtrasMetadata extras, String userKey) {
        this.extras = extras;
        this.userKey = userKey;
        this.adminKey = extras.getAdminKey();
        this.groupKey = extras.getGroupKey();
        this.friendKeys = extras.getFriendKeys();
        this.comingKeys = extras.getComingKeys();
        this.messageKeys = extras.getMessageKeys();
        groupPrice.setValue(extras.getGroupPrice());
        groupLocation.setValue(extras.getGroupLocation());
    }

    public LiveData<Boolean> getIsAdminVerified() { return isAdminVerified; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<String> getGroupPrice() { return groupPrice; }
    public LiveData<String> getGroupLocation() { return groupLocation; }
    public LiveData<Boolean> getPriceChanged() { return priceChanged; }
    public LiveData<Boolean> getLocationChanged() { return locationChanged; }
    public LiveData<Boolean> getFinishActivity() { return finishActivity; }

    public void verifyAdminStatus() {
        FirebaseServerClient serverClient = FirebaseServerClient.getInstance();
        serverClient.getGroup(
            groupKey,
            new FirebaseServerClient.DataCallback<Group>() {
                @Override
                public void onSuccess(Group group) {
                    if (group != null && group.getAdminKey() != null && group.getAdminKey().equals(userKey)) {
                        isAdminVerified.setValue(true);
                        adminKey = group.getAdminKey();
                    } else {
                        errorMessage.setValue("Access denied. Only group admin can access this page.");
                        finishActivity.setValue(true);
                    }
                }
                @Override
                public void onError(String errorMsg) {
                    errorMessage.setValue("Failed to verify admin status: " + errorMsg);
                    finishActivity.setValue(true);
                }
            });
    }

    public void changeGroupPrice(String newPrice) {
        if (!isAdminVerified.getValue() || !adminKey.equals(userKey)) {
            errorMessage.setValue("Access denied. Admin verification failed.");
            return;
        }
        groupPrice.setValue(newPrice);
        DBRef.refGroups.child(groupKey).child("groupPrice").setValue(newPrice);
        priceChanged.setValue(true);
    }

    public void changeGroupLocation(LatLng latLng) {
        if (!isAdminVerified.getValue() || !adminKey.equals(userKey)) {
            errorMessage.setValue("Access denied. Admin verification failed.");
            return;
        }
        String locationValue = MapUtilities.encodeCoordinatesToStringLocation(latLng);
        DBRef.refGroups.child(groupKey).child("groupLocation").setValue(locationValue);
        groupLocation.setValue(locationValue);
        locationChanged.setValue(true);
    }

    public ExtrasMetadata getExtras() {
        return extras;
    }
}
