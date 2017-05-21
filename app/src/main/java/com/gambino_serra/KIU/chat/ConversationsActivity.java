package com.gambino_serra.KIU.chat;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.gambino_serra.KIU.MainActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

/**
 * La classe modella la schermata nella quale vengono visualizzate tutte
 * le conversazioni della chat.
 */
public class ConversationsActivity extends AppCompatActivity
        implements ConversationsFragment.ConversationFragmentCommunicatorInterface {

    /**
     * L'attributo modella il TAG della classe per il sistema di
     * Logging.
     */
    private final static String TAG = "ConversationActivity";

    /**
     * L'attributo contiene l'istanza del sistema di autenticazione.
     */
    private FirebaseAuth mAuth;

    /**
     * L'attributo contiene il listener del sistema di autenticazione.
     */
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.gambino_serra.KIU.R.layout.activity_conversations);

        //Elimina le notifiche relative alle richieste di coda ricevute dall'Helper.
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(010);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {

            /**
             * pulisce le shared e invoca la MainActivity nell'eventualità
             * in cui l'activity dovessere essere richiamata, ma non dovesse
             * esserci nessun utente connesso.
             *
             * @param firebaseAuth
             */
            @Override
            public void onAuthStateChanged(@NotNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent in = new Intent(ConversationsActivity.this, MainActivity.class);
                    startActivity(in);
                }
            }
        };

        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment fragment = new ConversationsFragment();
        transaction.add(com.gambino_serra.KIU.R.id.frame_conversation, fragment);
        transaction.commit();

    }

    /**
     * Il metodo viene invocato dal ListFragment all'interno della comunicazione IFC.
     * Con le informazioni ricevute dal ListFragment l'Activity richiede
     * MessagesActivity per la visualizzazione della chat.
     *
     * @param counterpart UID dell'utente associato alla conversazione
     * @param nome        nome dell'utente associato alla conversazione
     */
    @Override
    public void getClickedConversationInfo(String conversationName, String counterpart, String nome) {

        Intent in = new Intent(getApplicationContext(), MessagesActivity.class);
        in.putExtra("counterpartID", counterpart);
        in.putExtra("counterpartName", nome);
        in.putExtra("convname", conversationName);
        startActivity(in);
    }

    /**
     * Nel metodo viene associato un listener al sistema di autenticazione e
     * viene impostato il titolo all'interno della ActionBar.
     */
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        getSupportActionBar().setTitle(com.gambino_serra.KIU.R.string.actionbar_chat_one_title);

    }

    /**
     * Il metodo elimina, se presente, il Listener associato al sitema di autenticazione.
     */
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /**
     * Nel metodo viene creato il menu dell'Activity,
     *
     * @param menu menu
     * @return successo o fallimento
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(com.gambino_serra.KIU.R.menu.chat_menu, menu);
        return true;
    }

    /**
     * Il metodo effettua le opportune operazioni associate all'elemento selezionato nel menu.
     *
     * @param item elemento del menu selezionato
     * @return booleano
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent in;
        boolean check;
        switch (item.getItemId()) {
            case com.gambino_serra.KIU.R.id.home:
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
