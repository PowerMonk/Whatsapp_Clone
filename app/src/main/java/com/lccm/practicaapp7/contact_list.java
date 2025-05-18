package com.lccm.practicaapp7;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class contact_list extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<Chat> listaChats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contact_list);

        // Ajustes de EdgeToEdge (ya lo tenías)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.contact_list), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //  RecyclerView
        recyclerView = findViewById(R.id.recyclerViewChats);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listaChats = new ArrayList<>();
        listaChats.add(new Chat("Luis", "Hola"));
        listaChats.add(new Chat("Karol", "Que tal?"));
        listaChats.add(new Chat("Chavez", "Bye"));

        adapter = new ChatAdapter(listaChats);
        recyclerView.setAdapter(adapter);
    }
}
