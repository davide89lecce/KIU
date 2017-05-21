package com.gambino_serra.KIU;

import android.app.DialogFragment;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.StringExtractor;
import com.amigold.fundapter.interfaces.ItemClickListener;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.gambino_serra.KIU.R;
import com.gambino_serra.KIU.chat.ConversationsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kosalgeek.android.json.JsonConverter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * La classe modella l'Activity Home del Kiuer.
 */
public class KiuerHomeActivity extends AppCompatActivity
        implements Response.Listener<String>,Response.ErrorListener{

    final String TAG = this.getClass().getSimpleName();
    final private static String MY_PREFERENCES = "kiuPreferences";
    final private static String EMAIL = "email";
    final private static String TIPO_UTENTE = "tipoUtente";
    final private static String UID = "uid";
    private static final String LOGGED_USER = "logged_user";

    Intent in;
    ListView lvProduct;

   // private FirebaseAuth mAuth;
   // private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kiuer_home);

        //Elimina le notifiche per Coda Iniziata e Coda Terminata
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(003);
        mNotifyMgr.cancel(004);

      //  mAuth = FirebaseAuth.getInstance();
      //  mAuthListener = new FirebaseAuth.AuthStateListener() {
        /*
            @Override
            public void onAuthStateChanged(@NotNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent in = new Intent(KiuerHomeActivity.this, MainActivity.class);
                    startActivity(in);
                }
            }
        };
        */
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab2);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent kiuerMaps = new Intent(KiuerHomeActivity.this, KiuerMaps.class);
                startActivity(kiuerMaps);
            }
        });

        //Lettura dal DB tramite Volley delle Code richieste dal Kiuer
        updateRichieste();

        //avvio il servizio per la ricezione delle notifiche e dei messaggi
        startService();
    }

    /**
     * Il metodo crea il menù sull'ActionBar.
     *
     * @param menu
     * @return booleano
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.kiuer_menu, menu);
        return true;
    }

    /**
     * Il metodo gestisce il menù sull'ActionBar
     *
     * @param item
     * @return booleano
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        boolean check = false;

        final SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);

        switch (item.getItemId()) {

            case R.id.chat_kiuer:
                in = new Intent(getApplicationContext(), ConversationsActivity.class);
                startActivity(in);
                check = true;
                break;
            case R.id.notification_kiuer:
                in = new Intent(getApplicationContext(), NotificationKiuer.class);
                startActivity(in);
                check = true;
                break;
            case R.id.exit_kiuer:
                SharedPreferences.Editor editor;
                editor = prefs.edit().clear();
                editor.apply();
                FirebaseAuth.getInstance().signOut();
                stopService();
                in = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(in);
                check = true;
                break;
            default:
                check = super.onOptionsItemSelected(item);
        }

        return check;
    }

    @Override
    public void onResume(){
        super.onResume();
        updateRichieste();
    }

    /**
     * Il metodo esegue la richiesta di lettura dei dati presenti nel DB remoto relativi alle code richieste dal Kiuer per l'aggiornamento della ListView.
     */
    public void updateRichieste(){

        final SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        String url = "http://www.kiu.altervista.org/code_kiuer.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, this, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), R.string.error_update_volley, Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("email", prefs.getString(EMAIL,"").toString());
                return params;
            }
        };

        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
        lvProduct = (ListView) findViewById(R.id.lvProduct);
    }

    /**
     * Il metodo e' invocato alla risposta (dati ricevuti da database altervista) della richiesta di invio della informazioni.
     * Si occupa di convertire il JSON ricevuto e di valorizzare la ListView e le rispettive Dialog.
     *
     * @param response
     */
    @Override
    public void onResponse(String response) {
        Log.d(TAG, response);
        final Bundle bundle = new Bundle();
        final SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        final ArrayList<JsonCheckRichiesta> productList = new JsonConverter<JsonCheckRichiesta>().toArrayList(response, JsonCheckRichiesta.class);

        final BindDictionary<JsonCheckRichiesta> dictionary = new BindDictionary<>();
        dictionary.addStringField(R.id.tvText, new StringExtractor<JsonCheckRichiesta>() {
            @Override
            public String getStringValue(JsonCheckRichiesta product, int position) {
                String statoCoda = "";
                if(product.stato_coda.toString().equals("Non iniziata")){
                    statoCoda = getResources().getString(R.string.queue_not_started);
                }else if(product.stato_coda.toString().equals("In coda")){
                    statoCoda = getResources().getString(R.string.queue_in_progress);
                }else if(product.stato_coda.toString().equals("Coda terminata")){
                    statoCoda = getResources().getString(R.string.queue_terminated);
                }
                return getResources().getString(R.string.hour_queue) + "  " + product.orario.toString().substring(0,5) + "\n" + getResources().getString(R.string.helper2) + " " + product.nome.toString() + "\n" + getResources().getString(R.string.state) + "  " + statoCoda.toString() + "\n";
            }
        }).onClick(new ItemClickListener<JsonCheckRichiesta>() {

            @Override
            public void onClick(JsonCheckRichiesta item, int position, View view) {
                String statoCoda = "";
                if(productList.get(position).stato_coda.toString().equals("Non iniziata")){
                    statoCoda = getResources().getString(R.string.queue_not_started);
                }else if(productList.get(position).stato_coda.toString().equals("In coda")){
                    statoCoda = getResources().getString(R.string.queue_in_progress);
                }else if(productList.get(position).stato_coda.toString().equals("Coda terminata")){
                    statoCoda = getResources().getString(R.string.queue_terminated);
                }
                DialogFragment newFragment = new HelperDetails();
                bundle.putString("id", productList.get(position).ID_richiesta.toString());
                bundle.putString("text", getResources().getString(R.string.queue_assigned_to) + " " + productList.get(position).orario.substring(0,5) + "\n\n" + getResources().getString(R.string.place) + "  " + productList.get(position).luogo + "\n\n" + getResources().getString(R.string.helper2) + "  " + productList.get(position).nome );
                bundle.putString("nome", productList.get(position).nome);
                bundle.putString("stato_coda", statoCoda.toString());
                bundle.putString("orario_inizio_coda", productList.get(position).orario_inizio.toString());
                bundle.putString("orario_fine_coda", productList.get(position).orario_fine.toString());
                bundle.putString("email_helper", productList.get(position).ID_helper.toString());
                bundle.putString("email_kiuer", productList.get(position).ID_kiuer.toString());
                bundle.putInt("tariffa_oraria", productList.get(position).tariffa_oraria);
                newFragment.setArguments(bundle);
                newFragment.show(getFragmentManager(), "HelperDetails");
            }
        });

        FunDapter<JsonCheckRichiesta> adapter = new FunDapter<>(getApplicationContext(), productList, R.layout.product_layout, dictionary);
        lvProduct.setAdapter(adapter);
    }

    /**
     * Il metodo invocato da volley in caso di problemi nella ricezione della risposta.
     */
    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(getApplicationContext(), R.string.error_update_volley, Toast.LENGTH_SHORT).show();
    }

    /**
     * Avvia il servizio per la gestione delle notifiche e messaggi.
     */
    public void startService()
    {
        startService(new Intent(this,Service_check.class));
    }

    /**
     * Stop del servizio per la gestione delle notifiche e messaggi (eseguito al logout).
     */
    public void stopService()
    {
        stopService(new Intent(this,Service_check.class));
    }

}