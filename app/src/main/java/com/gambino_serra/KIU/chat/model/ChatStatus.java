package com.gambino_serra.KIU.chat.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * La classe modella lo stato della chat di un utente,
 * in relazione ad un altro.
 */
@IgnoreExtraProperties
public class ChatStatus {

    /**
     * L'attributo indica se una conversazione e' attiva o meno.
     */
    public boolean messaging;

    /**
     * L'attributo indica se si è ricevuto o meno un nuovo messaggio.
     */
    public boolean newmessage;

    /**
     * L'attributo contiene l'id della conversazione.
     */
    public String convname;

    /**
     * Costruttore vuoto utilizzato internamente dalla libreria firebase-database.
     */
    public ChatStatus() {
    }

    /**
     * Costruttore utilizzato all'interno dell'applicazione per creare un nuovo oggetto.
     *
     * @param messaging  stato della conversazione
     * @param newmessage nuovo messaggio nella conversazione
     * @param convname   nome della conversazione
     */
    public ChatStatus(boolean messaging, boolean newmessage, String convname) {
        this.convname = convname;
        this.newmessage = newmessage;
        this.messaging = messaging;
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
        map.put("messaging", messaging);
        map.put("newmessage", newmessage);
        map.put("convname", convname);
        return map;
    }

}
