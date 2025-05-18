// 1. Renombrar la clase actual Chat.java a ChatActivity.java
package com.lccm.practicaapp7;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.chat);

        // Obtener el nombre del contacto del intent
        String nombreContacto = getIntent().getStringExtra("NOMBRE_CONTACTO");

        // Configurar el título con el nombre del contacto
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        if (nombreContacto != null && !nombreContacto.isEmpty()) {
            toolbar.setTitle("Chat: " + nombreContacto);
        }

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
}