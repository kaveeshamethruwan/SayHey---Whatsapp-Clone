package com.example.sayhey.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sayhey.Interfaces.UserClickEvent;
import com.example.sayhey.Models.User;
import com.example.sayhey.R;
import com.example.sayhey.databinding.RowConversationBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {

    private Context context;
    private ArrayList<User>usersList;
    private static UserClickEvent clickEvent;

    public UsersAdapter(Context context, ArrayList<User> usersList, UserClickEvent clickEvent) {
        this.context = context;
        this.usersList = usersList;
        UsersAdapter.clickEvent = clickEvent;

    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UsersViewHolder(LayoutInflater.from(context).inflate(R.layout.row_conversation, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {

        User user = usersList.get(position);

        String senderID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String senderRoom = senderID+user.getUid();

        FirebaseDatabase.getInstance().getReference().child("Chats").child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {

                            try {

                                String lastMSG = snapshot.child("lastMSG").getValue(String.class);
                                Long lastMessageTime = snapshot.child("lastMessageTime").getValue(Long.class);
                                holder.binding.timeText.setText(new SimpleDateFormat("hh:mm a").format(new Date(lastMessageTime)));

                                holder.binding.lastMSG.setText(lastMSG);

                            } catch (Exception e) {

                                e.printStackTrace();

                            }

                        } else {

                            String tapToChat = "Tap to Chat";
                            holder.binding.lastMSG.setText(tapToChat);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

        try {

            holder.binding.userName.setText(user.getName());

            Glide.with(context)
                    .load(user.getProfileImage())
                    .placeholder(ContextCompat.getDrawable(context, R.drawable.avatar))
                    .into(holder.binding.userImage);

        } catch (Exception e) {

            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    static class UsersViewHolder extends RecyclerView.ViewHolder {

        RowConversationBinding binding;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = RowConversationBinding.bind(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    clickEvent.onClick(getAdapterPosition());

                }
            });

        }
    }

}
