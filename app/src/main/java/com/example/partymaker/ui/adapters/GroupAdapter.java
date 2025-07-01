package com.example.partymaker.ui.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import com.example.partymaker.R;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.ui.group.UsersListActivity;
import com.squareup.picasso.Picasso;
import java.util.List;

public class GroupAdapter extends ArrayAdapter<Group> {
  Context context;
  List<Group> GroupList;
  Context applicationContext;

  public GroupAdapter(
      @NonNull Context context,
      @LayoutRes int resource,
      @IdRes int textViewResourceId,
      @NonNull List<Group> GroupList) {
    super(context, resource, textViewResourceId, GroupList);
    this.context = context;
    this.GroupList = GroupList;
    this.applicationContext = UsersListActivity.getContextOfApplication();
  }

  @NonNull
  @Override
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {

    LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
    @SuppressLint("ViewHolder")
    View view = layoutInflater.inflate(R.layout.item_group, parent, false);
    Group temp = GroupList.get(position);

    TextView tvpGroupName = view.findViewById(R.id.tvGroupName);
    tvpGroupName.setText(temp.getGroupName());

    TextView tvpGroupDate = view.findViewById(R.id.tvGroupDate);
    String GroupDate =
        (temp.getGroupDays() + " " + temp.getGroupMonths() + " " + temp.getGroupYears());
    tvpGroupDate.setText(GroupDate);

    final ImageView imageView = view.findViewById(R.id.imgGroupPicture);

    String GroupKey = temp.getGroupKey();

    DBRef.refStorage
        .child("Groups/" + GroupKey)
        .getDownloadUrl()
        .addOnSuccessListener(
            uri ->
                Picasso.get()
                    .load(uri) // image url goes here
                    .fit()
                    .centerCrop()
                    .into(imageView))
        .addOnFailureListener(
            exception -> {
              // Handle any errors
            });

    return view;
  }
}
