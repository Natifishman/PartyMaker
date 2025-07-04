package com.example.partymaker.ui.group;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.partymaker.R;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.data.model.User;
import com.example.partymaker.ui.adapters.UserAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class UsersListActivity extends AppCompatActivity {
  private ListView lv;
  public static Context contextOfApplication;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_friend_list);
    contextOfApplication = getApplicationContext();

    // Actionbar settings
    ActionBar actionBar = getSupportActionBar();
    assert actionBar != null;
    actionBar.setTitle("All users");
    actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0081d1")));

    lv = findViewById(R.id.lv);
    ShowData();
    eventHandler();
  }

  private void eventHandler() {
    lv.setOnItemClickListener((parent, view, position, id) -> {});
    lv.setOnItemLongClickListener((parent, view, position, id) -> false);
  }

  private void ShowData() {
    DBRef.refUsers.addValueEventListener(
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            ArrayList<User> ArrUsers = new ArrayList<>();
            for (DataSnapshot data : dataSnapshot.getChildren()) {
              User p = data.getValue(User.class);
              ArrUsers.add(p);
            }
            UserAdapter adapter = new UserAdapter(UsersListActivity.this, 0, 0, ArrUsers);
            lv.setAdapter(adapter);
          }

          @Override
          public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
  }

  public static Context getContextOfApplication() {
    return contextOfApplication;
  }
}
