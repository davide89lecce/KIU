package com.gambino_serra.KIU.chat.service;

/**
 * L'interfaccia definisce il metodo che il servizio deve implementare
 * per poter comunicare con il Listener di Firebase
 */

public interface ChatService {

    /**
     * Il metodo avvisa il servizio di un nuovo messaggio.
     *
     * @param convname
     */
    public void handleNewMessage(String convname);

}
