package com.lccm.practicaapp7;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 0;
    private final List<Message> messages;

    public MessageAdapter(List<Message> messages) {
        this.messages = messages;
    }

    public static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage;

        public SentMessageViewHolder(View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.textViewMessageContentSent);
        }

        public void bind(Message msg) {
            textMessage.setText(msg.getContenido());
        }
    }

    public static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage;

        public ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.textViewMessageContentReceived);
        }

        public void bind(Message msg) {
            textMessage.setText(msg.getContenido());
        }
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isEnviadoPorMi() ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message msg = messages.get(position);
        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(msg);
        } else {
            ((ReceivedMessageViewHolder) holder).bind(msg);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}
