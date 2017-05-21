package com.gambino_serra.KIU.chat.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * La classe modella il valore (coppia chiave/valore) di un messaggio.
 */
@IgnoreExtraProperties
public class Message {

    /**
     * L'attributo contiene il testo del messaggio.
     */
    public String message;

    /**
     * L'attributo contiene l'orario di invio del messaggio.
     */
    public String timestamp;

    /**
     * L'attributo contiene l'UID del mittente.
     */
    public String senderUID;

    /**
     * L'attributo indice se il destinatario ha letto o meno il messaggio.
     */
    public boolean readed;

    /**
     * Costruttore vuoto utilizzato internamente dalla libreria firebase-database.
     */
    public Message() {
    }

    /**
     * Costruttore utilizzato all'interno dell'app per creare un nuovo oggetto.
     *
     * @param message   testo del messaggio
     * @param timestamp orario di invio del messaggio
     * @param senderUID identificativo del mittente
     * @param readed    lettura da parte del destinatario
     */
    public Message(String message, String timestamp, String senderUID, boolean readed) {
        this.senderUID = senderUID;
        this.timestamp = timestamp;
        this.message = message;
        this.readed = readed;
    }

    /**
     * Il metodo toMap() crea e ritorna una HashMap contente una coppia
     * chiave-valore per ogni attributo presente nella classe.
     * Metodo rischiesto dalla libreria di firebase-database.
     *
     * @return HashMap con gli attributi della classe.
     */
    public Map<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap();
        map.put("message", message);
        map.put("timestamp", timestamp);
        map.put("senderUID", senderUID);
        map.put("readed", readed);
        return map;
    }
}