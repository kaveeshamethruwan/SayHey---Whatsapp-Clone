package com.example.sayhey.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.sayhey.Models.User;
import com.example.sayhey.R;
import com.example.sayhey.databinding.ActivitySetupProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class SetupProfileActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 45;

    private ActivitySetupProfileBinding binding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private Uri selectedIMG;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetupProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        loadingDialog();

        binding.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE);

            }
        });

        binding.setupProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = binding.nameText.getText().toString();

                if (name.isEmpty()) {

                    binding.nameText.setError("Empty Name");

                } else {

                    loadingDialog.show();

                    if (selectedIMG != null) {

                        StorageReference storageReference = storage.getReference().child("Profiles")
                                .child(auth.getCurrentUser().getUid());
                        storageReference.putFile(selectedIMG)
                                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                        if (task.isSuccessful()) {

                                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {

                                                    String imageURL = uri.toString();
                                                    String uid = auth.getCurrentUser().getUid();
                                                    String phone = auth.getCurrentUser().getPhoneNumber();

                                                    User user = new User(uid, name, phone, imageURL);
                                                    database.getReference().child("Users")
                                                            .child(uid)
                                                            .setValue(user)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()) {

                                                                        Intent intent = new Intent(SetupProfileActivity.this, MainActivity.class);
                                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                        startActivity(intent);

                                                                    } else {

                                                                        Toast.makeText(SetupProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                                                    }

                                                                    loadingDialog.cancel();

                                                                }
                                                            });

                                                }
                                            });

                                        } else {

                                            Toast.makeText(SetupProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            //loadingDialog.cancel();


                                        }

                                    }
                                });

                    } else {

                        StorageReference storageReference = storage.getReference().child("Profiles")
                                .child(auth.getCurrentUser().getUid());
                        storageReference.putFile(selectedIMG)
                                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                        if (task.isSuccessful()) {

                                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {

                                                    String uid = auth.getCurrentUser().getUid();
                                                    String phone = auth.getCurrentUser().getPhoneNumber();

                                                    User user = new User(uid, name, phone, "none-image");
                                                    database.getReference().child("Users")
                                                            .child(uid)
                                                            .setValue(user)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()) {

                                                                        Intent intent = new Intent(SetupProfileActivity.this, MainActivity.class);
                                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                        startActivity(intent);

                                                                    } else {

                                                                        Toast.makeText(SetupProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                                                    }

                                                                    loadingDialog.cancel();

                                                                }
                                                            });

                                                }
                                            });

                                        } else {

                                            Toast.makeText(SetupProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            //loadingDialog.cancel();

                                        }

                                    }
                                });

                    }

                }

            }
        });

    }

    private void loadingDialog() {

        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading_layout);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.progress_background));
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {

            if (data.getData() != null) {

                try {

                    binding.profileImage.setImageURI(data.getData());

                } catch (Exception e) {

                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

                }

                selectedIMG = data.getData();

            }

        }

    }
}