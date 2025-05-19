// 1. Renombrar la clase actual Chat.java a ChatActivity.java
package com.lccm.practicaapp7;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private List<Message> listaMensajes;
    private MessageAdapter adapter;
    private RecyclerView recyclerView;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.chat);

        // Inicialización de views para mensajes
        recyclerView = findViewById(R.id.recyclerViewMessages);
        editText = findViewById(R.id.editTextMessageInput);
        listaMensajes = new ArrayList<>();
        adapter = new MessageAdapter(listaMensajes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Obtener el nombre del contacto del intent
        String nombreContacto = getIntent().getStringExtra("NOMBRE_CONTACTO");

        // Configurar el título con el nombre del contacto
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        if (nombreContacto != null && !nombreContacto.isEmpty()) {
            toolbar.setTitle("Chat: " + nombreContacto);
        }
        toolbar.setNavigationOnClickListener(v -> {
            finish(); // Esto cierra la pantalla actual y vuelve a la anterior
        });

        // Manejar insets correctamente para que el layout_chatbox se ajuste cuando el teclado está visible
        View rootView = findViewById(R.id.ChatView);
        View chatboxLayout = findViewById(R.id.layout_chatbox);

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());

            // Aplicar insets del sistema a toda la vista
            rootView.setPadding(systemBars.left, systemBars.top, systemBars.right,
                    ime.bottom > 0 ? 0 : systemBars.bottom);

            // Ajustar espacio entre el campo de texto y el borde inferior cuando el teclado está visible
            chatboxLayout.setTranslationY(-ime.bottom);

            return WindowInsetsCompat.CONSUMED;
        });
    }

    public void onEnviarMensajeClick(View view) {
        String mensaje = editText.getText().toString().trim();

        if (!mensaje.isEmpty()) {
            Message nuevoMensaje = new Message(mensaje, true);
            listaMensajes.add(nuevoMensaje);
            adapter.notifyItemInserted(listaMensajes.size() - 1);
            recyclerView.scrollToPosition(listaMensajes.size() - 1);
            editText.setText("");
        }
    }

}