package com.lccm.practicaapp7;

public class ChatModel {
    private String nombre;
    private String mensaje;

    // Constructor para crear un objeto Chat con el nombre de la persona y un mensaje
    public ChatModel(String nombre, String mensaje) {
        this.nombre = nombre;
        this.mensaje = mensaje;
    }

    public String getNombre() {
        return nombre;
    }

    public String getMensaje() {
        return mensaje;
    }
}
