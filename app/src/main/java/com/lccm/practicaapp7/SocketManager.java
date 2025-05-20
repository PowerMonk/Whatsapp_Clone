package com.lccm.practicaapp7;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketManager {
    private static final String TAG = "SocketManager";
    private static SocketManager instance;
    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private boolean isConnected = false;
    private String phoneNumber;
    private ExecutorService executorService;
    private Handler mainHandler;
    private List<MessageListener> messageListeners = new ArrayList<>();
    private Map<String, List<Message>> chatHistory = new HashMap<>();
    private List<String> availableNumbers = new ArrayList<>();
    private Context applicationContext;

    // Interfaz simplificada para mensajes
    public interface MessageListener {
        void onMessageReceived(String from, String message);
        void onConnectionStatusChanged(boolean connected);
        // Nuevo método para notificar cuando se actualiza la lista de números disponibles
        void onAvailableNumbersUpdated(List<String> numbers);
    }

    private SocketManager() {
        executorService = Executors.newCachedThreadPool();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
        }
        return instance;
    }

    public void init(Context context, String phoneNumber) {
        this.applicationContext = context.getApplicationContext();
        this.phoneNumber = phoneNumber;
        Log.d(TAG, "SocketManager inicializado con número: " + phoneNumber);
    }

    public boolean connectToServer(String serverIp, int port) {
        Log.d(TAG, "Intentando conectar al servidor: " + serverIp + ":" + port);

        executorService.execute(() -> {
            try {
                socket = new Socket(serverIp, port);
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Mostrar un mensaje en la UI para confirmar la conexión
                showToastInMainThread("Conexión establecida con " + serverIp + ":" + port);

                // Registrarse en el servidor con nuestro número
                String regMessage = "REG:" + phoneNumber;
                sendMessage(regMessage);
                Log.d(TAG, "Mensaje de registro enviado: " + regMessage);

                isConnected = true;
                notifyConnectionStatusChanged(true);

                // Iniciar hilo de escucha para mensajes entrantes
                startListening();

                Log.d(TAG, "Conectado al servidor: " + serverIp + ":" + port);
            } catch (IOException e) {
                Log.e(TAG, "Error al conectar con el servidor", e);
                showToastInMainThread("Error al conectar con el servidor: " + e.getMessage());
                isConnected = false;
                notifyConnectionStatusChanged(false);
            }
        });
        return true;
    }

    private void startListening() {
        Log.d(TAG, "Iniciando hilo de escucha para mensajes entrantes");

        executorService.execute(() -> {
            try {
                String lineRead;
                while (isConnected && reader != null && (lineRead = reader.readLine()) != null) {
                    final String receivedMessage = lineRead;
                    Log.d(TAG, "Mensaje recibido del servidor: [" + receivedMessage + "]");

                    // Para depuración, mostrar cada mensaje recibido en un Toast
                    showToastInMainThread("Recibido: " + receivedMessage);

                    // Procesar el mensaje en el hilo principal para evitar problemas de concurrencia
                    mainHandler.post(() -> processIncomingMessage(receivedMessage));
                }
            } catch (IOException e) {
                Log.e(TAG, "Error al leer mensajes del servidor", e);
                showToastInMainThread("Error en comunicación: " + e.getMessage());
                isConnected = false;
                notifyConnectionStatusChanged(false);
            }
        });
    }

    private void processIncomingMessage(String message) {
        Log.d(TAG, "Procesando mensaje: [" + message + "]");

        try {
            if (message.startsWith("FROM:")) {
                // Mensaje de chat normal
                Log.d(TAG, "Procesando mensaje de chat");
                String[] parts = message.split("\\|");
                String from = "";
                String text = "";

                for (String part : parts) {
                    part = part.trim();
                    Log.d(TAG, "Parte del mensaje: [" + part + "]");

                    if (part.startsWith("FROM:")) {
                        from = part.substring(5).trim();
                        Log.d(TAG, "Remitente extraído: [" + from + "]");
                    } else if (part.startsWith("TXT:")) {
                        text = part.substring(4).trim();
                        Log.d(TAG, "Texto extraído: [" + text + "]");
                    }
                }

                if (!from.isEmpty() && !text.isEmpty()) {
                    Log.d(TAG, "Mensaje válido - FROM: [" + from + "] TXT: [" + text + "]");
                    saveMessageToHistory(from, text, false);
                    notifyMessageReceived(from, text);
                } else {
                    Log.w(TAG, "Mensaje con formato incorrecto o campos vacíos");
                }

            } else if (message.startsWith("NUMEROS_NO_USADOS:")) {
                // Números no utilizados
                Log.d(TAG, "Procesando mensaje de números no usados");
                String numerosStr = message.substring("NUMEROS_NO_USADOS:".length()).trim();
                Log.d(TAG, "Cadena de números recibida: [" + numerosStr + "]");

                // Limpiar lista actual
                availableNumbers.clear();

                if (!numerosStr.isEmpty()) {
                    // Añadir los nuevos números
                    String[] numerosArray = numerosStr.split(",");
                    Log.d(TAG, "Cantidad de números recibidos: " + numerosArray.length);

                    for (String numero : numerosArray) {
                        if (numero != null && !numero.trim().isEmpty()) {
                            String numTrimmed = numero.trim();
                            availableNumbers.add(numTrimmed);
                            Log.d(TAG, "Número agregado a la lista: [" + numTrimmed + "]");
                        }
                    }
                }

                Log.d(TAG, "Total números disponibles: " + availableNumbers.size());
                showToastInMainThread("Números disponibles: " + availableNumbers.size());

                // Notificar a los listeners sobre los números disponibles
                notifyAvailableNumbersUpdated();

            } else {
                Log.d(TAG, "Tipo de mensaje no reconocido: [" + message + "]");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al procesar mensaje: " + e.getMessage(), e);
            showToastInMainThread("Error al procesar mensaje: " + e.getMessage());
        }
    }

    private void showToastInMainThread(final String message) {
        mainHandler.post(() ->
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        );
    }

    public List<String> getAvailableNumbers() {
        return new ArrayList<>(availableNumbers);
    }

    private void sendMessage(String message) {
        if (writer == null) {
            Log.e(TAG, "No se puede enviar mensaje, escritor no inicializado");
            return;
        }

        executorService.execute(() -> {
            try {
                writer.write(message + "\n");
                writer.flush();
                Log.d(TAG, "Mensaje enviado: [" + message + "]");
            } catch (IOException e) {
                Log.e(TAG, "Error al enviar mensaje", e);
                showToastInMainThread("Error al enviar mensaje: " + e.getMessage());
                isConnected = false;
                notifyConnectionStatusChanged(false);
            }
        });
    }

    public void sendChatMessage(String to, String message) {
        if (!isConnected) {
            Log.e(TAG, "No estás conectado al servidor");
            showToastInMainThread("No estás conectado al servidor");
            return;
        }

        Log.d(TAG, "Enviando mensaje de chat a [" + to + "]: [" + message + "]");

        executorService.execute(() -> {
            try {
                String formattedMessage = "MSG:FROM:" + phoneNumber + "|TO:" + to + "|TXT:" + message;
                writer.write(formattedMessage + "\n");
                writer.flush();
                Log.d(TAG, "Mensaje de chat enviado: [" + formattedMessage + "]");

                // Guardar mensaje en historial
                saveMessageToHistory(to, message, true);

                // Notificar con Toast para depuración
                showToastInMainThread("Mensaje enviado a " + to);
            } catch (IOException e) {
                Log.e(TAG, "Error al enviar mensaje de chat", e);
                showToastInMainThread("Error al enviar mensaje: " + e.getMessage());
                isConnected = false;
                notifyConnectionStatusChanged(false);
            }
        });
    }

    private void saveMessageToHistory(String contact, String content, boolean sentByMe) {
        Message message = new Message(content, sentByMe);

        if (!chatHistory.containsKey(contact)) {
            chatHistory.put(contact, new ArrayList<>());
        }
        chatHistory.get(contact).add(message);
        Log.d(TAG, "Mensaje guardado en historial - Contacto: [" + contact + "] Enviado por mí: " + sentByMe);
    }

    @NonNull
    public List<Message> getChatHistory(String contact) {
        if (chatHistory.containsKey(contact)) {
            return chatHistory.get(contact);
        }
        return new ArrayList<>();
    }

    public void registerListener(MessageListener listener) {
        if (!messageListeners.contains(listener)) {
            messageListeners.add(listener);
            Log.d(TAG, "Listener registrado, total: " + messageListeners.size());
        }
        if (listener != null) {
            mainHandler.post(() -> {
                listener.onConnectionStatusChanged(isConnected);
                listener.onAvailableNumbersUpdated(new ArrayList<>(availableNumbers));
            });
        }
    }

    public void unregisterListener(MessageListener listener) {
        messageListeners.remove(listener);
        Log.d(TAG, "Listener eliminado, total: " + messageListeners.size());
    }

    private void notifyMessageReceived(final String from, final String message) {
        mainHandler.post(() -> {
            for (MessageListener listener : messageListeners) {
                listener.onMessageReceived(from, message);
            }
        });
    }

    private void notifyConnectionStatusChanged(final boolean connected) {
        mainHandler.post(() -> {
            for (MessageListener listener : messageListeners) {
                listener.onConnectionStatusChanged(connected);
            }
        });
    }

    private void notifyAvailableNumbersUpdated() {
        mainHandler.post(() -> {
            for (MessageListener listener : messageListeners) {
                listener.onAvailableNumbersUpdated(new ArrayList<>(availableNumbers));
            }
        });
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void disconnect() {
        if (!isConnected) {
            Log.d(TAG, "Ya estás desconectado del servidor");
            return;
        }

        isConnected = false;
        notifyConnectionStatusChanged(false);

        executorService.execute(() -> {
            try {
                if (writer != null) writer.close();
                if (reader != null) reader.close();
                if (socket != null) socket.close();

                Log.d(TAG, "Desconectado del servidor");
                showToastInMainThread("Desconectado del servidor");
            } catch (IOException e) {
                Log.e(TAG, "Error al cerrar conexión", e);
            }
        });
    }

    public void requestNonUsedNumbers() {
        if (!isConnected) {
            Log.e(TAG, "No se pueden solicitar números, no hay conexión");
            showToastInMainThread("No hay conexión para solicitar números");
            return;
        }

        Log.d(TAG, "Solicitando números no usados...");
        showToastInMainThread("Solicitando números disponibles...");

        executorService.execute(() -> {
            try {
                writer.write("OBT\n");
                writer.flush();
                Log.d(TAG, "Solicitud de números no usados enviada");
            } catch (IOException e) {
                Log.e(TAG, "Error solicitando números no usados", e);
                showToastInMainThread("Error al solicitar números: " + e.getMessage());
            }
        });
    }
}