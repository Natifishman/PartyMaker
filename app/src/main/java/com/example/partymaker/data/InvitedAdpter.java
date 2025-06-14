package com.example.partymaker.data;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.squareup.picasso.Picasso;
import java.util.List;

public class InvitedAdpter extends ArrayAdapter<User> {
  Context context;
  List<User> InvitedList;

  public InvitedAdpter(
      @NonNull Context context,
      @LayoutRes int resource,
      @IdRes int textViewResourceId,
      @NonNull List<User> InvitedList) {
    super(context, resource, textViewResourceId, InvitedList);
    this.context = context;
    this.InvitedList = InvitedList;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
    View view = layoutInflater.inflate(R.layout.invited_list, parent, false);
    User temp = InvitedList.get(position);

    TextView tvpUserName = view.findViewById(R.id.tvILusername);
    tvpUserName.setText(temp.getUserName());

    TextView tvpEmail = view.findViewById(R.id.tvILemail);
    tvpEmail.setText(temp.getEmail());

    final ImageView imageView = view.findViewById(R.id.imgILprofile);

    String UserImageProfile = temp.getEmail();
    String email = UserImageProfile.replace('.', ' ');

    DBref.refStorage
        .child("Users/" + email)
        .getDownloadUrl()
        .addOnSuccessListener(
            new OnSuccessListener<Uri>() {
              @Override
              public void onSuccess(Uri uri) {
                Picasso.get()
                    .load(uri) // image url goes here
                    .fit()
                    .centerCrop()
                    .into(imageView);
              }
            })
        .addOnFailureListener(
            new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception exception) {
                // Handle any errors
              }
            });

    return view;
  }
}
