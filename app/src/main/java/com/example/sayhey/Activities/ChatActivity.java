package com.example.sayhey.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.sayhey.Adapters.MessagesAdapter;
import com.example.sayhey.Models.Message;
import com.example.sayhey.Models.User;
import com.example.sayhey.R;
import com.example.sayhey.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private String senderUID;
    private String receiverUID;

    private ArrayList<Message>messageList;
    private MessagesAdapter adapter;

    private String senderRoom;
    private String receiverRoom;

    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        messageList = new ArrayList<>();

        String userName = intent.getStringExtra("userName");
        receiverUID = intent.getStringExtra("UID");
        senderUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        setSupportActionBar(binding.chatToolBar);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        senderRoom = senderUID+receiverUID;
        receiverRoom = receiverUID+senderUID;

        if (getSupportActionBar()!= null) {

            //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            setDataForToolBar(receiverUID);

        }

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading...");
        dialog.setCancelable(false);


        chatRecyclerView();

        //send message button process
        binding.sendIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendMessageProcess(senderUID);

            }
        });

        binding.attachmentIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendAttachmentProcess();

            }
        });

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();

            }
        });

        binding.messageText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {


                String status = "Typing...";
                database.getReference().child("presence").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(status);

                if (binding.messageText.getText().toString().isEmpty()) {

                    status = "Online";
                    database.getReference().child("presence").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(status);

                }

            }
        });

    }

    private void setDataForToolBar(String receiverUID) {

        database.getReference().child("Users").child(receiverUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                User user = snapshot.getValue(User.class);

                try {

                    if (user != null) {

                        Glide.with(ChatActivity.this).load(user.getProfileImage()).into(binding.profileImage);
                        binding.personName.setText(user.getName());

                    }

                } catch (Exception e) {

                    Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(ChatActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

        database.getReference().child("presence").child(receiverUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                try {

                    if (snapshot.exists()) {

                        String status = snapshot.getValue(String.class);
                        binding.onlineStatus.setText(status);

                    }

                } catch (Exception e) {

                    Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(ChatActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void sendAttachmentProcess() {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 25);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 25) {

            if (data != null) {

                if (data.getData() !=  null) {

                    dialog.show();
                    Uri selectedIMG = data.getData();
                    Calendar calendar = Calendar.getInstance();
                    StorageReference storageReference = storage.getReference().child("Chats")
                            .child(calendar.getTimeInMillis()+"");
                    storageReference.putFile(selectedIMG)
                            .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                    if (task.isSuccessful()) {

                                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {

                                                String filePath = uri.toString();

                                                String message = binding.messageText.getText().toString();

                                                binding.messageText.getText().clear();
                                                Message messageObject = new Message(message, senderUID, new Date().getTime());
                                                messageObject.setMessage("photo");
                                                messageObject.setImageURL(filePath);

                                                //for get random key
                                                String randomKey = database.getReference().push().getKey();

                                                database.getReference().child("Chats").child(receiverRoom).child("messages").child(randomKey)
                                                        .setValue(messageObject)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {

                                                                database.getReference().child("Chats").child(senderRoom).child("messages").child(randomKey)
                                                                        .setValue(messageObject)
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void unused) {

                                                                            }
                                                                        });

                                                                HashMap<String, Object>lastMessageObject = new HashMap<>();
                                                                lastMessageObject.put("lastMSG", messageObject.getMessage());
                                                                lastMessageObject.put("lastMessageTime", new Date().getTime());

                                                                database.getReference().child("Chats").child(senderRoom).updateChildren(lastMessageObject);
                                                                database.getReference().child("Chats").child(receiverRoom).updateChildren(lastMessageObject);

                                                            }
                                                        });

                                            }
                                    });

                                    } else {

                                        Toast.makeText(ChatActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                    }

                                    dialog.cancel();

                                }
                            });

                }

            }

        }

    }

    private void sendMessageProcess(String senderUID) {

        String message = binding.messageText.getText().toString();

        if (message.isEmpty()) {

            Toast.makeText(this, "Empty Message", Toast.LENGTH_SHORT).show();

        } else {

            String status = "Online";
            database.getReference().child("presence").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(status);
            binding.messageText.getText().clear();
            Message messageObject = new Message(message, senderUID, new Date().getTime());

            //for get random key
            String randomKey = database.getReference().push().getKey();

            database.getReference().child("Chats").child(receiverRoom).child("messages").child(randomKey)
                    .setValue(messageObject)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                            database.getReference().child("Chats").child(senderRoom).child("messages").child(randomKey)
                                    .setValue(messageObject)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {

                                        }
                                    });

                            HashMap<String, Object>lastMessageObject = new HashMap<>();
                            lastMessageObject.put("lastMSG", messageObject.getMessage());
                            lastMessageObject.put("lastMessageTime", new Date().getTime());

                            database.getReference().child("Chats").child(senderRoom).updateChildren(lastMessageObject);
                            database.getReference().child("Chats").child(receiverRoom).updateChildren(lastMessageObject);

                        }
                    });

        }

    }

    private void chatRecyclerView() {

        adapter = new MessagesAdapter(this, messageList, senderRoom, receiverRoom);
        binding.chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.chatRecyclerView.setHasFixedSize(true);
        binding.chatRecyclerView.setAdapter(adapter);

        database.getReference().child("Chats").child(senderRoom).child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        messageList.clear();

                        for (DataSnapshot data : snapshot.getChildren()) {

                            Message message = data.getValue(Message.class);
                            message.setMessageID(data.getKey());
                            messageList.add(message);

                        }

                        adapter.notifyDataSetChanged();
                        binding.chatRecyclerView.scrollToPosition(messageList.size() - 1);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Toast.makeText(ChatActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    @Override
    public boolean onSupportNavigateUp() {

        finish();

        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.top_menu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public void onBackPressed() {

        //database.getReference().child("presence").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue("Online");
        finish();

    }

    @Override
    protected void onStart() {
        super.onStart();

        database.getReference().child("presence").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue("Online");
        System.out.println("onStart");

    }

    @Override
    protected void onResume() {
        super.onResume();

        database.getReference().child("presence").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue("Online");
        System.out.println("onResume");

    }

    @Override
    protected void onPause() {

        long currentTime = System.currentTimeMillis();
        String time = "last seen at " + new SimpleDateFormat("hh:mm a").format(new Date(currentTime));
        database.getReference().child("presence").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(time);
        super.onPause();

    }

}