package com.lccm.practicaapp7;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatModel> chats;
    private OnChatClickListener listener;

    // Interfaz para manejar clics en los chats
    public interface OnChatClickListener {
        void onChatClick(int position, ChatModel chat);
    }

    public ChatAdapter(List<ChatModel> chats, OnChatClickListener listener) {
        this.chats = chats;
        this.listener = listener;
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        ImageView imagen;
        TextView titulo, mensaje;

        public ChatViewHolder(View itemView, final OnChatClickListener listener, final List<ChatModel> chats) {
            super(itemView);
            imagen = itemView.findViewById(R.id.chat_image);
            titulo = itemView.findViewById(R.id.chat_title);
            mensaje = itemView.findViewById(R.id.chat_message);

            // Configurar el listener de clics para todo el elemento
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onChatClick(position, chats.get(position));
                }
            });
        }
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view, listener, chats);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatModel chat = chats.get(position);
        holder.titulo.setText(chat.getNombre());
        holder.mensaje.setText(chat.getMensaje());
        holder.imagen.setImageResource(R.drawable.ic_user_placeholder); // imagen fija
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }
}