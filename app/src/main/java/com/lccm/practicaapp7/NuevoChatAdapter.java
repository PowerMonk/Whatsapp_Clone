package com.lccm.practicaapp7;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NuevoChatAdapter extends RecyclerView.Adapter<NuevoChatAdapter.ViewHolder> {

    private List<String> numerosOriginales;
    private List<String> numerosFiltrados;
    private Context context;

    public NuevoChatAdapter(List<String> numeros) {
        this.numerosOriginales = new ArrayList<>(numeros);
        this.numerosFiltrados = new ArrayList<>(numeros);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_nuevo_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String numero = numerosFiltrados.get(position);
        holder.numeroTextView.setText(numero);

        // Configurar el clic en cada elemento
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("NOMBRE_CONTACTO", numero);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return numerosFiltrados.size();
    }

    public void filtrar(String texto) {
        numerosFiltrados.clear();
        if (texto.isEmpty()) {
            numerosFiltrados.addAll(numerosOriginales);
        } else {
            for (String numero : numerosOriginales) {
                if (numero.contains(texto)) {
                    numerosFiltrados.add(numero);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView numeroTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            numeroTextView = itemView.findViewById(R.id.numeroTextView);
        }
    }
}