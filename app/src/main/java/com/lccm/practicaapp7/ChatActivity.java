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

public class ChatActivity extends AppCompatActivity implements SocketManager.MessageListener {

    private List<Message> listaMensajes;
    private MessageAdapter adapter;
    private RecyclerView recyclerView;
    private EditText editText;
    private String nombreContacto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.chat);

        // Inicialización de views para mensajes
        recyclerView = findViewById(R.id.recyclerViewMessages);
        editText = findViewById(R.id.editTextMessageInput);

        // Crear lista y adaptador vacíos inicialmente
        listaMensajes = new ArrayList<>();
        adapter = new MessageAdapter(listaMensajes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Obtener el nombre del contacto del intent
        nombreContacto = getIntent().getStringExtra("NOMBRE_CONTACTO");

        // Configurar el título con el nombre del contacto
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        if (nombreContacto != null && !nombreContacto.isEmpty()) {
            toolbar.setTitle("Chat: " + nombreContacto);

            // Cargar historial de mensajes si existe
            cargarHistorialMensajes();
        }

        toolbar.setNavigationOnClickListener(v -> {
            finish(); // Esto cierra la pantalla actual y vuelve a la anterior
        });

        // Registrar como listener para recibir mensajes nuevos
        SocketManager.getInstance().registerListener(this);

        // Manejar insets correctamente para que el layout_chatbox se ajuste cuando el teclado está visible
        // [Código existente para insets]
    }

    private void cargarHistorialMensajes() {
        // Limpiar lista actual
        listaMensajes.clear();

        // Obtener historial para este contacto
        List<Message> historial = SocketManager.getInstance().getChatHistory(nombreContacto);

        // Agregar todos los mensajes del historial
        if (historial != null && !historial.isEmpty()) {
            listaMensajes.addAll(historial);
            adapter.notifyDataSetChanged();

            // Hacer scroll al último mensaje
            recyclerView.post(() -> {
                recyclerView.scrollToPosition(listaMensajes.size() - 1);
            });
        }
    }

    public void onEnviarMensajeClick(View view) {
        String mensaje = editText.getText().toString().trim();

        if (!mensaje.isEmpty() && nombreContacto != null) {
            // Crear un nuevo mensaje local
            Message nuevoMensaje = new Message(mensaje, true);
            listaMensajes.add(nuevoMensaje);
            adapter.notifyItemInserted(listaMensajes.size() - 1);
            recyclerView.scrollToPosition(listaMensajes.size() - 1);

            // Enviar mensaje al servidor para reenvío al destinatario
            SocketManager.getInstance().sendMessage(nombreContacto, mensaje);

            // Limpiar campo de texto
            editText.setText("");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SocketManager.getInstance().unregisterListener(this);
    }

    // Implementación de la interfaz MessageListener
    @Override
    public void onMessageReceived(String from, String message) {
        // Actualizar la lista si recibimos un mensaje nuevo del contacto actual
        if (from.equals(nombreContacto)) {
            runOnUiThread(this::cargarHistorialMensajes);
        }
    }

    @Override
    public void onConnectionStatusChanged(boolean connected) {
        // Opcional: mostrar estado de conexión
    }

    @Override
    public void onAvailableNumbersUpdated(List<String> numbers) {
        // No es relevante para esta actividad
    }
}