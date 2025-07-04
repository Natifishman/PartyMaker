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
import com.example.partymaker.data.model.User;
import com.squareup.picasso.Picasso;
import java.util.List;

public class UserAdapter extends ArrayAdapter<User> {
  Context context;
  List<User> UserList;

  public UserAdapter(
      @NonNull Context context,
      @LayoutRes int resource,
      @IdRes int textViewResourceId,
      @NonNull List<User> UserList) {
    super(context, resource, textViewResourceId, UserList);
    this.context = context;
    this.UserList = UserList;
  }

  @NonNull
  @Override
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {

    LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
    @SuppressLint("ViewHolder") View view = layoutInflater.inflate(R.layout.item_user, parent, false);
    User temp = UserList.get(position);

    TextView tvpUserName = view.findViewById(R.id.tvUserListUsername);
    tvpUserName.setText(temp.getUserName());

    TextView tvpEmail = view.findViewById(R.id.tvUserListEmail);
    tvpEmail.setText(temp.getEmail());

    final ImageView imageView = view.findViewById(R.id.imgUserListProfile);

    String UserImageProfile = temp.getEmail();
    String email = UserImageProfile.replace('.', ' ');

    DBRef.refStorage
        .child("Users/" + email)
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
