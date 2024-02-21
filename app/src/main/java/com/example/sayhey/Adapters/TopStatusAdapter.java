package com.example.sayhey.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sayhey.Activities.MainActivity;
import com.example.sayhey.Models.Status;
import com.example.sayhey.Models.UserStatus;
import com.example.sayhey.R;
import com.example.sayhey.databinding.ItemStatusBinding;

import java.util.ArrayList;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class TopStatusAdapter extends RecyclerView.Adapter<TopStatusAdapter.TopStatusViewHolder> {

    private Context context;
    private ArrayList<UserStatus>userStatusList;

    public TopStatusAdapter(Context context, ArrayList<UserStatus> userStatusList) {
        this.context = context;
        this.userStatusList = userStatusList;
    }

    @NonNull
    @Override
    public TopStatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TopStatusViewHolder(LayoutInflater.from(context).inflate(R.layout.item_status, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TopStatusViewHolder holder, int position) {

        UserStatus userStatus = userStatusList.get(position);

        Status lastStatus = userStatus.getStatusList().get(userStatus.getStatusList().size() - 1);
        holder.binding.circularStatusView.setPortionsCount(userStatus.getStatusList().size());
        Glide.with(context).load(lastStatus.getImageURL()).into(holder.binding.lastStatusIMG);

        holder.binding.circularStatusView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ArrayList<MyStory> myStories = new ArrayList<>();

                for (Status status: userStatus.getStatusList()) {

                    myStories.add(new MyStory(status.getImageURL()));

                    new StoryView.Builder(((MainActivity)context).getSupportFragmentManager())
                            .setStoriesList(myStories) // Required
                            .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                            .setTitleText(userStatus.getName()) // Default is Hidden
                            .setSubtitleText("") // Default is Hidden
                            .setTitleLogoUrl(userStatus.getProfileImage()) // Default is Hidden
                            .setStoryClickListeners(new StoryClickListeners() {
                                @Override
                                public void onDescriptionClickListener(int position) {
                                    //your action
                                }

                                @Override
                                public void onTitleIconClickListener(int position) {
                                    //your action
                                }
                            }) // Optional Listeners
                            .build() // Must be called before calling show method
                            .show();

                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return userStatusList.size();
    }

    static class TopStatusViewHolder extends RecyclerView.ViewHolder {

        ItemStatusBinding binding;

        public TopStatusViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = ItemStatusBinding.bind(itemView);

        }
    }

}
