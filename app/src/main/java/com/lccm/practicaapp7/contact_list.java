package com.lccm.practicaapp7;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class contact_list extends AppCompatActivity implements ChatAdapter.OnChatClickListener, SocketManager.MessageListener {

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<ChatModel> listaChats;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contact_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.contact_list), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerViewChats);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Configurar SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(this::refreshChatList);

        // Botón para nuevo chat
        FloatingActionButton fab = findViewById(R.id.floating_action_button);
        fab.setOnClickListener(v -> onNuevoChatClick(v));

        // Cargar chats desde el historial
        loadChatsFromHistory();

        // Registrar como listener para actualizaciones
        SocketManager.getInstance().registerListener(this);
    }

    private void refreshChatList() {
        // Solicitar al servidor lista actualizada de números
        SocketManager.getInstance().requestNonUsedNumbers();

        // La recarga actual completará cuando llegue la respuesta del servidor
        // Mientras tanto, mostramos el indicador de carga
    }

    private void loadChatsFromHistory() {
        listaChats = new ArrayList<>();

        // Obtener todos los contactos que tienen historial de chat
        for (String contact : SocketManager.getInstance().getAllContactsWithChat()) {
            List<Message> messages = SocketManager.getInstance().getChatHistory(contact);
            String lastMessage = "";

            // Si hay mensajes, mostrar el último
            if (!messages.isEmpty()) {
                Message lastMsg = messages.get(messages.size() - 1);
                lastMessage = lastMsg.getContenido();
            }

            listaChats.add(new ChatModel(contact, lastMessage));
        }

        adapter = new ChatAdapter(listaChats, this);
        recyclerView.setAdapter(adapter);

        // Si estaba refrescando, detener la animación
        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Actualizar la lista cuando volvemos a esta pantalla
        loadChatsFromHistory();
        // Solicitar al servidor la lista actualizada de números
        SocketManager.getInstance().requestNonUsedNumbers();
    }

    @Override
    public void onMessageReceived(String from, String message) {
        // Actualizar la lista cuando se recibe un mensaje nuevo de cualquier contacto
        runOnUiThread(this::loadChatsFromHistory);
    }

    @Override
    public void onConnectionStatusChanged(boolean connected) {
        // Si recuperamos la conexión, actualizar la lista
        if (connected) {
            runOnUiThread(() -> {
                SocketManager.getInstance().requestNonUsedNumbers();
            });
        }
    }

    @Override
    public void onAvailableNumbersUpdated(List<String> numbers) {
        // Actualizar la lista cuando cambian los números disponibles
        runOnUiThread(this::loadChatsFromHistory);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SocketManager.getInstance().unregisterListener(this);
    }

    public void onNuevoChatClick(View view) {
        // Navegar a la pantalla de nuevo chat
        Intent intent = new Intent(this, nuevo_chat.class);
        startActivity(intent);
    }

    @Override
    public void onChatClick(int position, ChatModel chat) {
        // Abrir la conversación con este contacto
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("NOMBRE_CONTACTO", chat.getNombre());
        startActivity(intent);
    }
}