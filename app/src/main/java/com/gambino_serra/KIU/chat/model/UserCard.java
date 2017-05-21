package com.gambino_serra.KIU.chat.model;

/**
 * La classe modella il profilo dell'utente.
 */
public class UserCard {

    /**
     * L'attributo contiene l'UID asssegnato all'utente da firebase-auth
     * durante la fase di registrazione.
     */
    public String userID;

    /**
     * L'attributo contiene il nome e il cognome dell'utente.
     */
    public String user_name;

    /**
     * L'attributo contiene la mail dell'utente
     */
    public String mail;

    /**
     * L'attributo indica se l'utente è un Kiuer o un Helper
     */
    public String user_type;

    /**
     * Costruttore vuoto utilizzato internamente dalla libreria firebase-database.
     */
    public UserCard() {
    }

    /**
     * Costruttore utilizzato all'interno dell'applicazione per creare un nuovo oggetto.
     *
     * @param userID    UID dell'utente
     * @param name      nome dell'utente
     * @param mail      mail dell'utente
     * @param user_type tipo di utenza associata all'utente
     */
    public UserCard(String userID, String name, String mail, String user_type) {
        this.userID = userID;
        this.user_name = name;
        this.mail = mail;
        this.user_type = user_type;
    }

}