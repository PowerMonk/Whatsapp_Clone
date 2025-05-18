package com.lccm.practicaapp7;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    EditText txtIP, txtMensaje;
    TextView tvMessageLog;
    ServerSocket serverSocket;
    Socket socket;
    BufferedWriter writer;
    boolean isServerRunning = true;
    Handler handler = new Handler();
    private String phoneNumber = "";
    private static final int REQUEST_READ_PHONE_STATE = 1;


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
       // txtMensaje = findViewById(R.id.txtMensaje);
        //tvMessageLog = findViewById(R.id.tvMessageLog);
        //Button btnStartServer = findViewById(R.id.btnStartServer);
        Button btnConnect = findViewById(R.id.btnConectar);
        //Button btnSendMessage = findViewById(R.id.btnEnviarMensaje);
        //btnStartServer.setOnClickListener(v -> startServer());
        btnConnect.setOnClickListener(v -> connectToServer());
        //btnSendMessage.setOnClickListener(v -> sendMessage(txtMensaje.getText().toString()));

// Solicitar permiso para leer el número de teléfono
        requestPhoneNumberPermission();

//        Crear hilo para recibir mensajes
//        new Thread(this::listeningForMessages).start();

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
            Toast.makeText(this, "Permiso para leer el número de teléfono no concedido.", Toast.LENGTH_SHORT).show();
        }
        phoneNumber = "No disponible";
        Toast.makeText(this, "Número de teléfono: " + phoneNumber, Toast.LENGTH_LONG).show();
    }

    //      Método para conectarse al servidor
    // 192.168.1.153
    private void connectToServer(){
        String ip = txtIP.getText().toString();

        if(ip.isEmpty()){
            Toast.makeText(this, "Por favor ingrese una dirección IP", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {

            try{
                socket = new Socket(ip, 5000);
                writer = new BufferedWriter( new OutputStreamWriter(socket.getOutputStream()));
                sendMessage("REG:" + phoneNumber);
                isServerRunning = true;
                runOnUiThread(() -> Toast.makeText(this, "Conectado al servidor", Toast.LENGTH_SHORT).show());
                Intent intent = new Intent(MainActivity.this, contact_list.class);
                startActivity(intent);

            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(
                        () -> Toast.makeText(this, "Error al conectarse al servidor", Toast.LENGTH_SHORT).show()
                );
            }

        }).start();

    }

    //    Método para enviar mensajes
    private void sendMessage(String message){
        if(writer == null || message.isEmpty())
            return;

        new Thread(() -> {
            try{

                writer.write(message+"\n");
                writer.flush();
//                runOnUiThread(() -> {
//                    tvMessageLog.append("Yo > " + message +"\n");
//                });
//                txtMensaje.setText("");

            }catch (IOException e){
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error al enviar el mensaje", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        isServerRunning = false;
        try {
            if (serverSocket != null)
                serverSocket.close();

            if (socket != null)
                socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}