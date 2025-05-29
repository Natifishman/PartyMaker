package com.example.partymaker.data;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import com.example.partymaker.R;

import java.util.List;

public class ChatAdpter extends ArrayAdapter<ChatMessage> {
    Context context;
    List<ChatMessage> MessageList;

    public ChatAdpter(@NonNull Context context, @LayoutRes int resource, @IdRes int textViewResourceId, @NonNull List<ChatMessage> MessageList) {
        super(context, resource, textViewResourceId, MessageList);
        this.context = context;
        this.MessageList = MessageList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.chat_message_item, parent, false);
        ChatMessage temp = MessageList.get(position);

        String currentUser = DBref.Auth.getCurrentUser().getEmail();
        boolean isMine = temp.getMessageUser().equals(currentUser);

        LinearLayout bubbleLayout = view.findViewById(R.id.bubbleLayout);
        TextView tvSender = view.findViewById(R.id.tvSender);
        TextView tvTime = view.findViewById(R.id.tvTime);
        TextView tvMessage = view.findViewById(R.id.messageText);
        Space spaceLeft = view.findViewById(R.id.spaceLeft);
        Space spaceRight = view.findViewById(R.id.spaceRight);

        tvMessage.setText(temp.getMessageText());
        tvTime.setText(temp.getMessageTime().substring(11, 16)); // show only HH:mm

        if (isMine) {
            // הודעות שלי: יישור לימין, בועה ירוקה, בלי שם
            bubbleLayout.setBackgroundResource(R.drawable.bg_message_bubble_mine);
            tvSender.setVisibility(View.GONE);
            spaceLeft.setVisibility(View.VISIBLE);
            spaceRight.setVisibility(View.GONE);
            tvMessage.setTextColor(context.getResources().getColor(R.color.black));
        } else {
            // הודעות של אחרים: יישור לשמאל, בועה אפורה, עם שם
            bubbleLayout.setBackgroundResource(R.drawable.bg_message_bubble);
            tvSender.setText(temp.getMessageUser());
            tvSender.setVisibility(View.VISIBLE);
            spaceLeft.setVisibility(View.GONE);
            spaceRight.setVisibility(View.VISIBLE);
            tvMessage.setTextColor(context.getResources().getColor(R.color.black));
        }

        // הוסף מרווח בין הודעות
        if (position > 0) {
            ChatMessage prevMessage = MessageList.get(position - 1);
            if (prevMessage.getMessageUser().equals(temp.getMessageUser())) {
                // אם זו אותה משתמש, הקטן את המרווח
                view.setPadding(0, 2, 0, 0);
            } else {
                // אם זה משתמש אחר, הגדל את המרווח
                view.setPadding(0, 8, 0, 0);
            }
        }

        return view;
    }
}
