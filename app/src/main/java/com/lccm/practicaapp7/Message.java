package com.lccm.practicaapp7;

public class Message {
    private final String contenido;
    private final boolean enviadoPorMi;

    public Message(String contenido, boolean enviadoPorMi) {
        this.contenido = contenido;
        this.enviadoPorMi = enviadoPorMi;
    }

    public String getContenido() {
        return contenido;
    }

    public boolean isEnviadoPorMi() {
        return enviadoPorMi;
    }
}
