package com.lccm.practicaapp7;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<Chat> chats;

    public ChatAdapter(List<Chat> chats) {
        this.chats = chats;
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        ImageView imagen;
        TextView titulo, mensaje;

        public ChatViewHolder(View itemView) {
            super(itemView);
            imagen = itemView.findViewById(R.id.chat_image);
            titulo = itemView.findViewById(R.id.chat_title);
            mensaje = itemView.findViewById(R.id.chat_message);
        }
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chats.get(position);
        holder.titulo.setText(chat.getNombre());
        holder.mensaje.setText(chat.getMensaje());
        holder.imagen.setImageResource(R.drawable.ic_user_placeholder); // imagen fija
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }
}
