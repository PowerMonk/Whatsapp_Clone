package com.lccm.practicaapp7;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class nuevo_chat extends AppCompatActivity implements SocketManager.MessageListener {

    private TextInputEditText inputBusqueda;
    private RecyclerView recyclerView;
    private NuevoChatAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nuevo_chat);

        // Configurar la barra de herramientas
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        inputBusqueda = findViewById(R.id.input_busqueda);
        recyclerView = findViewById(R.id.recycler_view_numeros);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar el adaptador con la lista actual (podría estar vacía)
        adapter = new NuevoChatAdapter(new ArrayList<>(SocketManager.getInstance().getAvailableNumbers()));
        recyclerView.setAdapter(adapter);

        // Registrar listener para actualizaciones
        SocketManager.getInstance().registerListener(this);

        // Solicitar números no usados al servidor
        SocketManager.getInstance().requestNonUsedNumbers();

        inputBusqueda.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filtrar(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Es importante desregistrar el listener para evitar memory leaks
        SocketManager.getInstance().unregisterListener(this);
    }

    // Implementación de MessageListener
    @Override
    public void onMessageReceived(String from, String message) {
        // No necesitamos hacer nada aquí para esta actividad
    }

    @Override
    public void onConnectionStatusChanged(boolean connected) {
        // Opcional: mostrar un mensaje si la conexión se pierde
    }

    @Override
    public void onAvailableNumbersUpdated(List<String> numbers) {
        // Esto es clave: actualizar el adaptador cuando se reciben nuevos números
        runOnUiThread(() -> {
            adapter.setListaCompleta(new ArrayList<>(numbers));
            adapter.notifyDataSetChanged();
        });
    }
}