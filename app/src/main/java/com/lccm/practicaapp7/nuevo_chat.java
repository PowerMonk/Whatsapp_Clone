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

public class nuevo_chat extends AppCompatActivity {

    private TextInputEditText inputBusqueda;
    private RecyclerView recyclerView;
    private NuevoChatAdapter adapter;
    private List<String> listaNumeros;

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

        listaNumeros = new ArrayList<>();

        SocketManager.getInstance().requestNonUsedNumbers();

        listaNumeros = Arrays.asList(
                "452 229 17 94",
                "452 166 50 55"
        );

        adapter = new NuevoChatAdapter(new ArrayList<>(SocketManager.getInstance().getAvailableNumbers()));
        recyclerView.setAdapter(adapter);

        // Solicitar números no usados al servidor

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
}