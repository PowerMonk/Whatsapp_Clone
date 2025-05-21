package com.lccm.practicaapp7;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private EditText txtIP;
    private String phoneNumber;
    private static final int REQUEST_READ_PHONE_STATE = 1;
    private SocketManager socketManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtIP = findViewById(R.id.txtIP);
        Button btnConnect = findViewById(R.id.btnConectar);
        btnConnect.setOnClickListener(v -> connectToServer());


        // Solicitar permiso para leer el número de teléfono
        requestPhoneNumberPermission();

        // Inicializar el singleton SocketManager
        socketManager = SocketManager.getInstance();
        initSocketManager();
    }

    private void requestPhoneNumberPermission() {
        // Revisar si tuvo el permission granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS)
                != PackageManager.PERMISSION_GRANTED) {
            // Si el permiso NO ha sido concedido, lo solicitamos al usuario
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_NUMBERS},
                    REQUEST_READ_PHONE_STATE);
        } else {
            // Si el permiso YA ha sido concedido, obtenemos el número directamente
            getPhoneNumber();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Este método se llama cuando el usuario responde a la solicitud de permisos
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_PHONE_STATE) {
            // Verificamos si la solicitud de permiso que estamos manejando es la de READ_PHONE_NUMBERS
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // El usuario ha concedido el permiso, así que obtenemos el número
                getPhoneNumber();

            } else {
                // El usuario ha denegado el permiso
                Toast.makeText(this, "Permiso denegado para leer el número de teléfono", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint({"HardwareIds", "MissingPermission"})
    private void getPhoneNumber() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED) {
            // Volvemos a verificar si el permiso está concedido justo antes de usar la funcionalidad protegida
            if (telephonyManager != null) {
                String number = telephonyManager.getLine1Number();
                if (number != null && !number.isEmpty()) {
                    phoneNumber = number;
                    Toast.makeText(this, "Número (TlphMgr): " + phoneNumber, Toast.LENGTH_LONG).show();
                    return;
                }
            }
        } else {
            // Generar un número aleatorio como respaldo
            phoneNumber = generateRandomPhoneNumber();
            Toast.makeText(this, "Número generado" + phoneNumber, Toast.LENGTH_SHORT).show();
        }

        // Generar un número aleatorio como respaldo
        phoneNumber = generateRandomPhoneNumber();
        Toast.makeText(this, "Número de teléfono generado: " + phoneNumber, Toast.LENGTH_LONG).show();
    }

    // 192.168.1.153
    private void initSocketManager() {
        socketManager.init(getApplicationContext(), phoneNumber);
    }

    private void connectToServer() {
        String ip = txtIP.getText().toString();

        if (ip.isEmpty()) {
            Toast.makeText(this, "Por favor ingrese una dirección IP", Toast.LENGTH_SHORT).show();
            return;
        }

        if (socketManager.connectToServer(ip, 5000)) {
            Toast.makeText(this, "Conectando al servidor...", Toast.LENGTH_SHORT).show();

            // Pasar a la pantalla de contactos
            Intent intent = new Intent(MainActivity.this, contact_list.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Error al conectarse al servidor", Toast.LENGTH_SHORT).show();
        }
    }

    public void onConectarClick(View view) {
        connectToServer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private String generateRandomPhoneNumber() {
        Random random = new Random();
        StringBuilder phoneBuilder = new StringBuilder("+52");

        // Generar 10 dígitos aleatorios para el número
        for (int i = 0; i < 10; i++) {
            phoneBuilder.append(random.nextInt(10));
        }

        String randomNumber = phoneBuilder.toString();
        Toast.makeText(this, "Número aleatorio generado: " + randomNumber, Toast.LENGTH_SHORT).show();
        return randomNumber;
    }
}