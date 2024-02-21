package com.example.sayhey.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.sayhey.Adapters.MessagesAdapter;
import com.example.sayhey.Models.Message;
import com.example.sayhey.databinding.ActivityGroupBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Date;

public class GroupChatActivity extends AppCompatActivity {

    ActivityGroupBinding binding;

    private ArrayList<Message> messageList;
    private MessagesAdapter adapter;

    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private ProgressDialog dialog;

    private String senderUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.chatToolBar);

        messageList = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        senderUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        setLoadingDialog();

        binding.sendIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendMessageProcess();

            }
        });

    }

    private void sendMessageProcess() {

        String message = binding.messageText.getText().toString();

        if (message.isEmpty()) {

            Toast.makeText(this, "Empty Message", Toast.LENGTH_SHORT).show();

        } else {

            binding.messageText.getText().clear();

            try {

                Message messageObject = new Message(message, senderUID, new Date().getTime());
                database.getReference().child("Public").push().setValue(messageObject);

            } catch (Exception e) {

                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

            }

        }

    }

    private void setLoadingDialog() {

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading...");
        dialog.setCancelable(false);

    }
}