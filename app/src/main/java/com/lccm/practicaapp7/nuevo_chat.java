// nuevochat.java
package com.lccm.practicaapp7;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

        inputBusqueda = findViewById(R.id.input_busqueda);
        recyclerView = findViewById(R.id.recycler_view_numeros);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listaNumeros = Arrays.asList(
                "452 229 17 94",
                "452 166 50 55",
                "452 338 49 34",
                "66 3466 66 66",
                "123 456 7890",
                "987 654 3210"
        );

        adapter = new NuevoChatAdapter(new ArrayList<>(listaNumeros));
        recyclerView.setAdapter(adapter);

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
    public void onContactoClick(View view) {
        if (view instanceof TextView) {
            TextView contacto = (TextView) view;
            String numero = contacto.getText().toString();

            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("NOMBRE_CONTACTO", numero);
            startActivity(intent);
        }
    }
}


