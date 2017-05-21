package com.gambino_serra.KIU.chat.model;


/**
 * La classe modella l'entita' messaggio di tipo coppia chiave/valore.
 */
public class MessageEntity {

    /**
     * L'attributo key contiene la chiave che identifica univocamente il
     * messaggio all'interno del database.
     */
    public String key;

    /**
     * L'attributo messaggio contiene il valore del messaggio.
     */
    public Message message;

    /**
     * Costruttore utilizzato all'interno dell'app per creare un oggetto.
     *
     * @param key     chiave identificativa del messaggio
     * @param message messaggio
     */
    public MessageEntity(String key, Message message) {
        this.key = key;
        this.message = message;
    }
}
