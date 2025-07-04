package com.example.partymaker.ui.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.partymaker.R;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.ui.group.UsersListActivity;
import com.squareup.picasso.Picasso;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;

public class GroupAdapter extends ArrayAdapter<Group> {
  private final Context context;
  private final ArrayList<Group> groupList;

  public GroupAdapter(@NonNull Context context, ArrayList<Group> groupList) {
    super(context, R.layout.group_list_item, groupList);
    this.context = context;
    this.groupList = groupList;
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    View view = convertView;
    
    if (view == null) {
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      view = inflater.inflate(R.layout.group_list_item, parent, false);
    }
    
    Group group = groupList.get(position);
    
    if (group != null) {
      TextView groupNameTextView = view.findViewById(R.id.group_name);
      TextView groupLocationTextView = view.findViewById(R.id.group_location);
      TextView groupDateTextView = view.findViewById(R.id.group_date);
      TextView groupTimeTextView = view.findViewById(R.id.group_time);
      
      // Set the group name
      if (groupNameTextView != null && group.getGroupName() != null) {
        groupNameTextView.setText(group.getGroupName());
      }
      
      // Set the group location
      if (groupLocationTextView != null && group.getGroupLocation() != null) {
        groupLocationTextView.setText(group.getGroupLocation());
      }
      
      // Set the group date
      if (groupDateTextView != null) {
        String date = group.getGroupDays() + "/" + group.getGroupMonths() + "/" + group.getGroupYears();
        groupDateTextView.setText(date);
      }
      
      // Set the group time
      if (groupTimeTextView != null && group.getGroupHours() != null) {
        groupTimeTextView.setText(group.getGroupHours());
      }
    }
    
    return view;
  }
}
