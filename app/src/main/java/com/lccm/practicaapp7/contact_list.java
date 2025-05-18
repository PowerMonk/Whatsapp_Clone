package com.lccm.practicaapp7;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class contact_list extends AppCompatActivity implements ChatAdapter.OnChatClickListener {

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<ChatModel> listaChats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contact_list);

        // Ajustes de EdgeToEdge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.contact_list), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerViewChats);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Configurar el botón flotante para ir a nuevo_chat
        FloatingActionButton fab = findViewById(R.id.floating_action_button);
        fab.setOnClickListener(v -> goToNewChatScreen());

        listaChats = new ArrayList<>();
        listaChats.add(new ChatModel("Luis", "Hola"));
        listaChats.add(new ChatModel("Karol", "Que tal?"));
        listaChats.add(new ChatModel("Chavez", "Bye"));

        // Pasar this como listener para manejar los clicks
        adapter = new ChatAdapter(listaChats, this);
        recyclerView.setAdapter(adapter);
    }

    // Implementación del método de la interfaz para manejar clics
    public void onChatClick(int position, ChatModel chat) {
        // Abrir la actividad de chat
        Intent intent = new Intent(this, ChatActivity.class);
        // Pasar el nombre del contacto como extra para usarlo como título
        intent.putExtra("NOMBRE_CONTACTO", chat.getNombre()); // Name, value
        startActivity(intent);
    }

    private void goToNewChatScreen() {
        Intent intent = new Intent(contact_list.this, nuevo_chat.class);
        startActivity(intent);
    }
}