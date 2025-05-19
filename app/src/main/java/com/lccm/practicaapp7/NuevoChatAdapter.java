package com.lccm.practicaapp7;

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

    public NuevoChatAdapter(List<String> numeros) {
        this.numerosOriginales = new ArrayList<>(numeros);
        this.numerosFiltrados = new ArrayList<>(numeros);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nuevo_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.numeroTextView.setText(numerosFiltrados.get(position));
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
