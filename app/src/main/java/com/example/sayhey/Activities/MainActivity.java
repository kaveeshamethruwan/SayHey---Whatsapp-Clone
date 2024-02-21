package com.example.sayhey.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.sayhey.Adapters.TopStatusAdapter;
import com.example.sayhey.Adapters.UsersAdapter;
import com.example.sayhey.Interfaces.UserClickEvent;
import com.example.sayhey.Models.Status;
import com.example.sayhey.Models.User;
import com.example.sayhey.Models.UserStatus;
import com.example.sayhey.R;
import com.example.sayhey.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements UserClickEvent {

    private ActivityMainBinding binding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;

    private ArrayList<User>usersList;
    private UsersAdapter usersAdapter;

    private ArrayList<UserStatus>userStatusList;
    private TopStatusAdapter topStatusAdapter;

    private User user;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        usersList = new ArrayList<>();
        binding.chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.chatRecyclerView.setHasFixedSize(true);
        usersAdapter = new UsersAdapter(this, usersList, this);
        binding.chatRecyclerView.setAdapter(usersAdapter);

        database.getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        user = snapshot.getValue(User.class);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

        //for display user status
        setDataForStatusRecyclerView();
        //for display users
        setDataForRecyclerView();
        //for handle bottom navigation menu
        HandleBottomNavigationMenu();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading...");
        dialog.setCancelable(false);


    }

    private void HandleBottomNavigationMenu() {

        binding.bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.status:
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent, 75);
                        break;

                }

                return true;
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {

            if (data.getData() != null) {

                dialog.show();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference db = storage.getReference().child("Status").child(new Date().getTime()+"");
                db.putFile(data.getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            db.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    UserStatus userStatus = new UserStatus();
                                    userStatus.setName(user.getName());
                                    userStatus.setProfileImage(user.getProfileImage());
                                    userStatus.setLastUpdated(new Date().getTime());

                                    HashMap<String, Object> object = new HashMap<>();
                                    object.put("name", userStatus.getName());
                                    object.put("profileImage", userStatus.getProfileImage());
                                    object.put("lastUpdated", userStatus.getLastUpdated());

                                    Status status = new Status(uri.toString(), userStatus.getLastUpdated());

                                    database.getReference().child("Stories").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                    .updateChildren(object);

                                    database.getReference().child("Stories").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                    .child("statusList").push().setValue(status);

                                    dialog.cancel();

                                }
                            });

                        } else {

                            Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            dialog.cancel();

                        }

                    }
                });
                
            }

        }

    }

    private void setDataForStatusRecyclerView() {

        userStatusList = new ArrayList<>();
        topStatusAdapter = new TopStatusAdapter(this, userStatusList);
        binding.statusRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        binding.statusRecyclerView.setHasFixedSize(true);
        binding.statusRecyclerView.setAdapter(topStatusAdapter);

        database.getReference().child("Stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {

                    userStatusList.clear();
                    for (DataSnapshot storiesData : snapshot.getChildren()) {

                        UserStatus userStatus = new UserStatus();
                        userStatus.setName(storiesData.child("name").getValue(String.class));
                        userStatus.setProfileImage(storiesData.child("profileImage").getValue(String.class));
                        userStatus.setLastUpdated(storiesData.child("lastUpdated").getValue(Long.class));

                        ArrayList<Status>statuses = new ArrayList<>();

                        for (DataSnapshot data : storiesData.child("statusList").getChildren()) {

                            Status status = data.getValue(Status.class);
                            statuses.add(status);

                        }

                        userStatus.setStatusList(statuses);

                        userStatusList.add(userStatus);

                    }

                    topStatusAdapter.notifyDataSetChanged();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void setDataForRecyclerView() {

        database.getReference().child("Users")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        usersList.clear();

                        for (DataSnapshot data : snapshot.getChildren()) {

                            User user = data.getValue(User.class);

                            if (!user.getUid().equals(auth.getCurrentUser().getUid())) {

                                usersList.add(user);

                            }

                        }

                        usersAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.groups:
                startActivity(new Intent(this, GroupChatActivity.class));
                break;

            case R.id.search:
                Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.top_menu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public void onClick(int position) {

        User user = usersList.get(position);

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("userName", user.getName());
        intent.putExtra("UID", user.getUid());
        startActivity(intent);

    }

    @Override
    protected void onStart() {
        super.onStart();

        System.out.println("onStart");
        database.getReference().child("presence").child(auth.getCurrentUser().getUid()).setValue("Online");

    }

    @Override
    protected void onResume() {
        super.onResume();

        System.out.println("onResume");
        database.getReference().child("presence").child(auth.getCurrentUser().getUid()).setValue("Online");

    }

    @Override
    protected void onPause() {

        long currentTime = System.currentTimeMillis();
        String time = "last seen at " + new SimpleDateFormat("hh:mm a").format(new Date(currentTime));
        database.getReference().child("presence").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(time);
        super.onPause();

    }
}