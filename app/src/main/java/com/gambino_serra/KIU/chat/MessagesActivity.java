package com.gambino_serra.KIU.chat;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.gambino_serra.KIU.MainActivity;
import com.gambino_serra.KIU.R;
import com.gambino_serra.KIU.chat.model.ChatStatus;
import com.gambino_serra.KIU.chat.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.HashMap;

/**
 * La classe modella e gestisce la visualizzazione di tutti i messaggi di una conversazione
 * e ne permette l'invio di questi ultimi.
 */
public class MessagesActivity extends AppCompatActivity {

    private final static String TAG = "ConversationActivity";
    private static final String MY_PREFERENCES = "kiuPreferences";
    private static final String LOGGED_USER = "logged_user";
    private static final String CONV_NAME = "conv_name";

    /**
     * L'attributo contiene l'istanza del sistema di autenticazione.
     */
    private FirebaseAuth mAuth;

    /**
     * L'attributo contiene il listener del sistema di
     * autenticazione.
     */
    private FirebaseAuth.AuthStateListener mAuthListener;

    /**
     * L'attributo contiene il riferimento al ListFragment che
     * visualizza i messaggi.
     */
    private Fragment fragment;

    /**
     * L'attributo contiene il riferimento al bottone  utilizzato
     * per l'invio di un nuovo messaggio.
     */
    protected ImageButton sendButton;

    /**
     * L'attributo contiene il riferimento al campo di testo in cui
     * si inserisce un nuovo messaggio da inviare.
     */
    protected EditText msgEdit;

    /**
     * L'attributo contiene l'ID dell'utente attualmente connesso.
     */
    protected String myID;

    /**
     * l'attributo contiene l'ID dell'utente con il quale si sta
     * conversando.
     */
    protected String otherID;

    /**
     * l'attributo contiene il nome dell'utente con il quale si sta
     * conversando.
     */
    protected String otherName;

    /**
     * l'attributo contiene il riferimento alla posizione nel database
     * nella quale è presente lo status dell'utente attualmente connesso.
     */
    protected DatabaseReference myChatStatusRef;

    /**
     * l'attributo contiene il riferimento alla posizione nel database
     * nella quale è presente lo status dell'utente controparte.
     */
    protected DatabaseReference otherChatStatusRef;

    /**
     * l'attributo contiene il riferimento alla conversazione
     * che intercorrente tra l'utente corrente e la controparte.
     */
    protected DatabaseReference conversationRef;

    /**
     * l'attributo contiene la chiave del nodo contente la conversazione
     * tra l'utente e la controparte.
     */
    protected String conversationName;

    /**
     * Nel metodo onCreate() vengono svolte le seguenti operazioni:
     * - impostato il layout affinchè, in fase di scrittura, non venga coperto,
     *   ma sollevato dalla tastiera.
     * - il recupero di un'istanza di FirebaseAuth e l'associazione di un listener
     *   il cui scopo è resettare l'applicazione nel momento in cui dovesse verificare
     *   una situazione anomale con l'autenticazione.
     * - gestione delle informazione dell'utente corrente, e della controparte con
     *   annessa impostazione dell'Actionbar.
     * - gestione del ListFragment deputato alla visualizzazione dei messaggi.
     * - gestione dei componenti deputati alla creazione e all'invio dei nuovi
     *   messaggi.
     *
     * @param savedInstanceState bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NotNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent in = new Intent(MessagesActivity.this, MainActivity.class);
                    startActivity(in);
                }
            }
        };

        SharedPreferences sharedPref = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        myID = sharedPref.getString(LOGGED_USER, "").toString();

        Bundle extra = getIntent().getExtras();
        otherID = extra.getString("counterpartID");
        otherName = extra.getString("counterpartName");
        conversationName = extra.getString("convname");

        ActionBar bar = getSupportActionBar();
        bar.setHomeButtonEnabled(true);
        bar.setDisplayShowHomeEnabled(true);
        bar.setTitle(otherName);

        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        fragment = new MessagesViewFragment();
        if (extra != null) {
            fragment.setArguments(extra);
        }
        transaction.add(R.id.frame_messages, fragment);
        transaction.commit();

        sendButton = (ImageButton) findViewById(R.id.button_send);
        msgEdit = (EditText) findViewById(R.id.newMessage_editText);


        myChatStatusRef = FirebaseDatabase.getInstance()
                .getReference("/chat/chatstatus/" + myID + "/" +otherID);
        otherChatStatusRef = FirebaseDatabase.getInstance()
                .getReference("/chat/chatstatus/" + otherID + "/" +myID);

        conversationRef = FirebaseDatabase.getInstance().getReference("/chat/"+ conversationName);


        sendButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Nel metodo viene verificato che non si stia inviando un messaggio vuoto.
             * Viene impostato  lo stato della conversazione per entrambi gli utenti.
             * Infine viene aggiunto il messaggio alla conversazione.
             * @param v
             */
            @Override
            public void onClick(View v) {

                Message message;

                if (!(msgEdit.getText().toString().isEmpty())) {
                    message = new Message(msgEdit.getText().toString(),
                            (new Timestamp(System.currentTimeMillis()).toString()).substring(0,16), myID, false);

                    msgEdit.setText("");

                    ChatStatus status = new ChatStatus(true, false, conversationName);
                    myChatStatusRef.setValue(status);
                    status = new ChatStatus(true, true, status.convname);
                    otherChatStatusRef.setValue(status);

                    String key = conversationRef.push().getKey();
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("/" + key, message.toMap());
                    conversationRef.updateChildren(map);
                }

            }
        });


    }

    /**
     * il metodo imposta il valore con la key della conversazione, da condividere
     * con il servizio ChatService.
     */
    @Override
    protected void onStart() {
        super.onStart();

        final SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(CONV_NAME, conversationName);
        editor.apply();
        Log.d("convname-resume","Shared" + prefs.getString(CONV_NAME,""));
    }

    /**
     * Il metodo svuota il valore impostato con la key della conversazione,
     * condiviso con il servizio ChatService.
     */
    @Override
    protected void onStop() {
        super.onStop();

        ChatStatus status = new ChatStatus(true, false, conversationName);
        myChatStatusRef.setValue(status);

        Log.d("convname", "Io funziono");
        final SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(CONV_NAME, "");
        editor.apply();

        Log.d("convname-shared","Shared" + prefs.getString(CONV_NAME,""));
    }

    /**
     * Il metodo crea il menu dell'Activity,
     *
     * @param menu menu
     * @return successo o fallimento
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu, menu);
        return true;
    }

    /**
     * Il metodo effettua le opportune operazioni associate all'elemento selezionato
     * nel menu.
     *
     * @param item elemento del menu selezionato
     * @return booleano
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent in;
        boolean check;
        switch (item.getItemId()) {
            case R.id.home:
                in = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(in);
                check = true;
                break;
            default:
                check = super.onOptionsItemSelected(item);
        }
        return check;
    }
}