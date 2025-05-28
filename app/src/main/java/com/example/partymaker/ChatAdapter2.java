package com.example.partymaker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter2 extends RecyclerView.Adapter<ChatAdapter2.MessageViewHolder> {
    private List<SimpleChatMessage> messages;

    public ChatAdapter2(List<SimpleChatMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_message_item, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        SimpleChatMessage message = messages.get(position);
        holder.messageText.setText(message.content);

        if ("user".equals(message.role)) {
            holder.messageText.setBackgroundResource(R.drawable.user_message_background);
        } else {
            holder.messageText.setBackgroundResource(R.drawable.assistant_message_background);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        MessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
        }
    }
} 