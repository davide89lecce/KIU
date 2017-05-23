package com.gambino_serra.KIU;

import android.app.DialogFragment;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
//import com.gambino_serra.KIU.chat.ConversationsActivity;
import com.kosalgeek.android.json.JsonConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * La classe gestisce l'Activity relativa alle richieste di Coda pendenti inviate agli Helper
 */
public class NotificationKiuer extends AppCompatActivity implements Response.Listener<String> {

    final String TAG = this.getClass().getSimpleName();
    final private static String MY_PREFERENCES = "kiuPreferences";
    private static final String IDUTENTE = "IDutente";

    boolean check = false;
    ListView lvProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        //Elimina le notifiche relative alle risposte degli Helper alle richieste di coda inviate dal Kiuer
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(001);
        mNotifyMgr.cancel(002);

        //Lettura dal database di altervista tramite Volley delle richieste di coda pendenti inviate agli Helper
        updateRichieste();;
    }

    /**
     * Metodo invocato da volley alla ricezione della risposta.
     * Si occupa di convertire il JSON ricevuto e di valorizzare la ListView e le rispettive Dialog
     */
    @Override
    public void onResponse(String response) {
        Log.d(TAG, response);
        final Bundle bundle = new Bundle();

        final ArrayList<JsonCheckRichiesta> productList = new JsonConverter<JsonCheckRichiesta>().toArrayList(response, JsonCheckRichiesta.class);

        final BindDictionary<JsonCheckRichiesta> dictionary = new BindDictionary<>();
        dictionary.addStringField(R.id.tvText, new StringExtractor<JsonCheckRichiesta>() {
            @Override
            public String getStringValue(JsonCheckRichiesta product, int position) {
                String info = getResources().getString(R.string.hour_start_queue) + " " + product.orario.toString().substring(0,5) + "\n" + getResources().getString(R.string.helper2) + " " + product.nome.toString() + "\n";
                String status;
                if(product.stato_richiesta.equals("1")){
                    status = getResources().getString(R.string.request_accepted);
                }else if(product.stato_richiesta.equals("2")){
                    status = getResources().getString(R.string.request_rejected);
                }else{
                    status = getResources().getString(R.string.pending_confirmation);
                }
                return info + status + "\n";
            }
        }).onClick(new ItemClickListener<JsonCheckRichiesta>() {

            @Override
            public void onClick(JsonCheckRichiesta item, int position, View view) {
                DialogFragment newFragment = new NotificationDetailsKiuer();
                bundle.putString("id", productList.get(position).ID_richiesta.toString());
                bundle.putString("nome", productList.get(position).nome);
                bundle.putString("orario",productList.get(position).orario);
                bundle.putString("luogo",productList.get(position).luogo);
                bundle.putString("stato_richiesta", productList.get(position).stato_richiesta);
                bundle.putString("descrizione", productList.get(position).descrizione);
                newFragment.setArguments(bundle);
                newFragment.show(getFragmentManager(), "NotificationDetailsKiuer");
            }
        });

        FunDapter<JsonCheckRichiesta> adapter = new FunDapter<>(getApplicationContext(), productList, R.layout.notification_layout_kiuer, dictionary);
        lvProduct.setAdapter(adapter);
    }

    @Override
    public void onResume(){
        super.onResume();
        updateRichieste();
    }

    /**
     * Il metodo esegue la richiesta di lettura dei dati presenti nel datbase di altervista relativi alle richieste di coda pendenti per l'aggiornamento della ListView
     */
    public void updateRichieste(){
        final SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        String url = "http://www.davideantonio2.altervista.org/richieste_kiuer.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, this, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), R.string.err_read_google, Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("IDutente", prefs.getString(IDUTENTE,"").toString());
                return params;
            }
        };
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
        lvProduct = (ListView) findViewById(R.id.lvProduct);
    }

    /**
     * Il metodo crea il menù sull'ActionBar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.kiuer_notification_menu, menu);
        return true;
    }

    /**
     * Il metodo gestisce il menù sull'ActionBar
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent in;
        final SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);

        switch (item.getItemId()) {
            case R.id.home_kiuer:
                in = new Intent(getApplicationContext(), KiuerHomeActivity.class);
                startActivity(in);
                check = true;
                break;
//            case R.id.chat_kiuer:
//                in = new Intent(getApplicationContext(), ConversationsActivity.class);
//                startActivity(in);
//                check = true;
//                break;
            case R.id.exit_kiuer:
                SharedPreferences.Editor editor;
                editor = prefs.edit().clear();
                editor.apply();
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
