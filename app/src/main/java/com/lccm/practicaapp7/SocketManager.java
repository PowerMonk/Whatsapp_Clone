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
import java.util.Random;
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

                phoneNumber = validatePhoneNumber(phoneNumber);

                // Registrarse en el servidor con nuestro número
                String regMessage = "REG:" + phoneNumber;
                sendMessageToServer(regMessage);
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

                // Si salimos del bucle sin excepción y estábamos conectados, es una desconexión silenciosa
                if (isConnected) {
                    Log.w(TAG, "Desconexión silenciosa detectada, intentando reconexión...");
                    isConnected = false;
                    notifyConnectionStatusChanged(false);

                    // Opcional: Intenta reconectar automáticamente
                    // reconnectToServer();
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
        Log.d(TAG, "Procesando mensaje: " + message);

        try {
            // Limpiar el mensaje de posibles caracteres BOM (Byte Order Mark)
            if (message.length() > 0 && message.charAt(0) == '\uFEFF') {
                message = message.substring(1);
                Log.d(TAG, "Se eliminó BOM del mensaje. Mensaje limpio: " + message);
            }

            if (message.startsWith("NUMEROS_NO_USADOS:")) {
                // Números no utilizados
                Log.d(TAG, "Procesando mensaje de números no usados");
                String numerosStr = message.substring("NUMEROS_NO_USADOS:".length()).trim();
                Log.d(TAG, "Cadena de números recibida: [" + numerosStr + "]");

                // Limpiar lista actual
                availableNumbers.clear();

                processAvailableNumbers(numerosStr);

//                if (!numerosStr.isEmpty()) {
//                    // Añadir los nuevos números
//                    String[] numerosArray = numerosStr.split(",");
//                    Log.d(TAG, "Cantidad de números recibidos: " + numerosArray.length);
//
//                    for (String numero : numerosArray) {
//                        if (numero != null && !numero.trim().isEmpty()) {
//                            String numTrimmed = numero.trim();
//                            availableNumbers.add(numTrimmed);
//                            Log.d(TAG, "Número agregado a la lista: [" + numTrimmed + "]");
//                        }
//                    }
//                }

                Log.d(TAG, "Total números disponibles: " + availableNumbers.size());
                showToastInMainThread("Números disponibles: " + availableNumbers.size());

                // Notificar a los listeners sobre los números disponibles
                notifyAvailableNumbersUpdated();

            }
            else if (message.startsWith("MSG:")) {
                // Formato: MSG:remitente:mensaje
                String[] parts = message.split(":", 3);
                if (parts.length >= 3) {
                    String sender = parts[1];
                    String content = parts[2];
                    Log.d(TAG, "Mensaje recibido de " + sender + ": " + content);

                    // Guardar en el historial local
                    saveMessageToHistory(sender, content, false);
                }
            }
            else if (message.startsWith("FROM:")) {
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
                }
                else {
                    Log.w(TAG, "Mensaje con formato incorrecto o campos vacíos");
                }
            }
            else {
                Log.d(TAG, "Tipo de mensaje no reconocido: [" + message + "]");
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    private void sendMessageToServer(String message) {
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

    public void sendMessage(String to, String message) {
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

    private void saveMessageToHistory(String contact, String content, boolean isSent) {
        mainHandler.post(() -> {
            if (!chatHistory.containsKey(contact)) {
                chatHistory.put(contact, new ArrayList<>());
            }
            chatHistory.get(contact).add(new Message(content, isSent));

            // Notificar a los listeners sobre el nuevo mensaje
            notifyMessageHistoryUpdated(contact);
        });
    }

    private void notifyMessageHistoryUpdated(String contact) {
        for (MessageListener listener : messageListeners) {
            if (listener != null) {
                listener.onMessageReceived(contact, "");
            }
        }
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
//    public Map<String, List<Message>> getChatHistory() {
//        return chatHistory;
//    }
    @NonNull
    public List<Message> getChatHistory(String contact) {
        if (chatHistory.containsKey(contact)) {
            return chatHistory.get(contact);
        }
        return new ArrayList<>();
    }
    public List<String> getAllContactsWithChat() {
        // Devolver la lista de todos los contactos con historial de chat
        return new ArrayList<>(chatHistory.keySet());
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

    private void processAvailableNumbers(String numbersString) {
        Log.d(TAG, "Cadena de números recibida: [" + numbersString + "]");

        availableNumbers.clear();
        String[] numbers = numbersString.split(",");
        Log.d(TAG, "Cantidad de números recibidos: " + numbers.length);

        // Lista temporal para detectar duplicados
        List<String> processedNumbers = new ArrayList<>();

        // Obtener la lista de contactos con los que ya tenemos chat
        List<String> contactsWithChat = new ArrayList<>(chatHistory.keySet());
        Log.d(TAG, "Contactos con chat existente: " + contactsWithChat.size());

        Random random = new Random();

        // Agregar solo los números que NO son el número propio y NO tienen chat existente
        for (String number : numbers) {
            String trimmedNumber = number.trim();

            if (!trimmedNumber.isEmpty() && !trimmedNumber.equals(phoneNumber)) {
                // Verificar si ya existe un chat con este número
                if (contactsWithChat.contains(trimmedNumber)) {
                    Log.d(TAG, "Número omitido por tener chat existente: [" + trimmedNumber + "]");
                    continue; // Saltar al siguiente número
                }

                // Verificar si es un duplicado
                String uniqueNumber = trimmedNumber;
                int attempts = 0;

                // Si ya existe este número, añadirle dígitos aleatorios al final
                while (processedNumbers.contains(uniqueNumber) && attempts < 10) {
                    // Agregar un sufijo aleatorio de 3 dígitos para diferenciar
                    int suffix = random.nextInt(900) + 100; // Número entre 100 y 999
                    uniqueNumber = trimmedNumber + "-" + suffix;
                    attempts++;
                    Log.d(TAG, "Número duplicado detectado, creando variante: " + uniqueNumber);
                }

                processedNumbers.add(uniqueNumber);
                availableNumbers.add(uniqueNumber);
                Log.d(TAG, "Número agregado a la lista: [" + uniqueNumber + "]");
            } else if (trimmedNumber.equals(phoneNumber)) {
                Log.d(TAG, "Número propio ignorado: [" + trimmedNumber + "]");
            }
        }

        Log.d(TAG, "Total números disponibles (filtrados): " + availableNumbers.size());

        // Notificar a todos los listeners que la lista de números disponibles ha cambiado
        notifyAvailableNumbersUpdated();
    }

    private String validatePhoneNumber(String number) {
        Log.d(TAG, "Validando número de teléfono: " + number);

        // Verificar si el número comienza con +52 y tiene menos de 13 caracteres en total
        // (prefijo +52 + al menos 10 dígitos = mínimo 13 caracteres)
        if (number.startsWith("+52") && number.length() < 10) {
            Log.d(TAG, "Número incompleto detectado (solo prefijo o número corto)");

            // Generar un nuevo número aleatorio con prefijo +52 y 10 dígitos
            StringBuilder phoneBuilder = new StringBuilder("+52");
            Random random = new Random();

            // Generar 10 dígitos aleatorios para el número
            for (int i = 0; i < 10; i++) {
                phoneBuilder.append(random.nextInt(10));
            }

            String validNumber = phoneBuilder.toString();
            Log.d(TAG, "Número generado aleatoriamente: " + validNumber);
            return validNumber;
        }

        // Si el número no tiene prefijo +52, agrégalo
        if (!number.startsWith("+52")) {
            Log.d(TAG, "Agregando prefijo +52 al número");
            number = "+52" + number;
        }

        // Si el número ya es válido, devolverlo tal cual
        return number;
    }

    private void notifyAvailableNumbersUpdated() {
        mainHandler.post(() -> {
            for (MessageListener listener : messageListeners) {
                if (listener != null) {
                    listener.onAvailableNumbersUpdated(getAvailableNumbers());
                }
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

}