package com.example.sayhey.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sayhey.Models.Message;
import com.example.sayhey.R;
import com.example.sayhey.databinding.ItemReceiveBinding;
import com.example.sayhey.databinding.ItemSendBinding;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MessagesAdapter extends RecyclerView.Adapter {

    private Context context;
    private ArrayList<Message>messageList;
    private String senderRoom;
    private String receiverRoom;

    private final int ITEM_SENT = 1;
    private final int ITEM_RECEIVE = 2;

    public MessagesAdapter(Context context, ArrayList<Message> messageList, String senderRoom, String receiverRoom) {
        this.context = context;
        this.messageList = messageList;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;

        if (viewType == ITEM_SENT) {

            view = LayoutInflater.from(context).inflate(R.layout.item_send, parent, false);
            return new SentViewHolder(view);

        } else {

            view = LayoutInflater.from(context).inflate(R.layout.item_receive, parent, false);
            return new ReceiverViewHolder(view);

        }

    }

    @Override
    public int getItemViewType(int position) {

        Message message = messageList.get(position);

        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(message.getSenderID())) {

            return ITEM_SENT;

        } else {

            return ITEM_RECEIVE;

        }

        //return super.getItemViewType(position);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Message message = messageList.get(position);

        int [] reactions = new int[]{
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry};

        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();

        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {

            if (holder.getClass() == SentViewHolder.class) {

                if (pos != -1) {

                    SentViewHolder sentViewHolder = (SentViewHolder) holder;
                    sentViewHolder.itemSendBinding.reactionIcon.setImageResource(reactions[pos]);
                    sentViewHolder.itemSendBinding.reactionIcon.setVisibility(View.VISIBLE);

                }

            } else {

                if (pos != -1) {

                    ReceiverViewHolder receiverViewHolder = (ReceiverViewHolder) holder;
                    receiverViewHolder.itemReceiveBinding.reactionIcon.setImageResource(reactions[pos]);
                    receiverViewHolder.itemReceiveBinding.reactionIcon.setVisibility(View.VISIBLE);

                }

            }

            message.setFeeling(pos);

            FirebaseDatabase.getInstance().getReference().child("Chats").child(senderRoom).child("messages")
                    .child(message.getMessageID())
                    .setValue(message);

            FirebaseDatabase.getInstance().getReference().child("Chats").child(receiverRoom).child("messages")
                    .child(message.getMessageID())
                    .setValue(message);

            return true; // true is closing popup, false is requesting a new selection
        });

        if (holder.getClass() == SentViewHolder.class) {

            SentViewHolder sentViewHolder = (SentViewHolder) holder;

            if (message.getMessage().equals("photo")) {

                Glide.with(context).load(message.getImageURL())
                        .placeholder(R.drawable.placeholder)
                        .into(sentViewHolder.itemSendBinding.sendPhoto);
                sentViewHolder.itemSendBinding.sendText.setVisibility(View.GONE);
                sentViewHolder.itemSendBinding.sendPhoto.setVisibility(View.VISIBLE);

            }

            sentViewHolder.itemSendBinding.sendText.setText(message.getMessage());

            if (message.getFeeling() >= 0) {

                //message.setFeeling(reactions[(int)message.getFeeling()]);
                sentViewHolder.itemSendBinding.reactionIcon.setImageResource(reactions[message.getFeeling()]);
                sentViewHolder.itemSendBinding.reactionIcon.setVisibility(View.VISIBLE);

            } else {

                sentViewHolder.itemSendBinding.reactionIcon.setVisibility(View.GONE);

            }

            sentViewHolder.itemSendBinding.sendText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    popup.onTouch(view, motionEvent);

                    return true;
                }
            });

            sentViewHolder.itemSendBinding.sendPhoto.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    popup.onTouch(view, motionEvent);

                    return true;
                }
            });

        } else {

            ReceiverViewHolder receiverViewHolder = (ReceiverViewHolder) holder;

            if (message.getMessage().equals("photo")) {

                Glide.with(context).load(message.getImageURL())
                        .placeholder(R.drawable.placeholder)
                        .into(receiverViewHolder.itemReceiveBinding.receiveImage);
                receiverViewHolder.itemReceiveBinding.receiveText.setVisibility(View.GONE);
                receiverViewHolder.itemReceiveBinding.receiveImage.setVisibility(View.VISIBLE);

            }

            receiverViewHolder.itemReceiveBinding.receiveText.setText(message.getMessage());

            if (message.getFeeling() >= 0) {

               // message.setFeeling(reactions[(int)message.getFeeling()]);
                receiverViewHolder.itemReceiveBinding.reactionIcon.setImageResource(reactions[message.getFeeling()]);
                receiverViewHolder.itemReceiveBinding.reactionIcon.setVisibility(View.VISIBLE);

            } else {

                receiverViewHolder.itemReceiveBinding.reactionIcon.setVisibility(View.GONE);

            }

            receiverViewHolder.itemReceiveBinding.receiveText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    popup.onTouch(view, motionEvent);

                    return true;
                }
            });

            receiverViewHolder.itemReceiveBinding.receiveImage.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    popup.onTouch(view, motionEvent);

                    return true;
                }
            });

        }

    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class SentViewHolder extends RecyclerView.ViewHolder {

        ItemSendBinding itemSendBinding;

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);

            itemSendBinding = ItemSendBinding.bind(itemView);

        }
    }

    static class ReceiverViewHolder extends RecyclerView.ViewHolder {

        ItemReceiveBinding itemReceiveBinding;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);

            itemReceiveBinding = ItemReceiveBinding.bind(itemView);

        }
    }

}
