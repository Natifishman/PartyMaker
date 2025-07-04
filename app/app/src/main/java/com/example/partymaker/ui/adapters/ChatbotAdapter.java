package com.example.partymaker.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.partymaker.R;
import com.example.partymaker.data.model.ChatMessageGpt;
import java.util.List;

public class ChatbotAdapter extends RecyclerView.Adapter<ChatbotAdapter.MessageViewHolder> {
  private final List<ChatMessageGpt> messages;

  public ChatbotAdapter(List<ChatMessageGpt> messages) {
    this.messages = messages;
  }

  @NonNull
  @Override
  public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_chatbot_message, parent, false);
    return new MessageViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
    ChatMessageGpt message = messages.get(position);
    holder.messageText.setText(message.content);
    TextView messageText = holder.messageText;

    messageText.setPadding(32, 32, 32, 32);
    if ("user".equals(message.role)) {
      holder.messageText.setBackgroundResource(R.drawable.msg_user_bg);
    } else {

      holder.messageText.setBackgroundResource(R.drawable.msg_bg_assistant);
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
