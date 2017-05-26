package com.gambino_serra.KIU;

import android.app.DialogFragment;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.kosalgeek.android.json.JsonConverter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * La classe gestisce l'Activity relativa alle richieste di coda pendenti ricevute dall'Helper
 */
public class Helper_Notification extends AppCompatActivity implements Response.Listener<String> {

    final String TAG = this.getClass().getSimpleName();
    final private static String MY_PREFERENCES = "kiuPreferences";
    private static final String IDUTENTE = "IDutente";

    ListView lvProduct;
    SwipeRefreshLayout mSwipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        //per aggiornare la lista nella home con uno swipe
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        updateRichieste();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }
        );

        //Elimina le notifiche relative alle richieste di coda ricevute dall'Helper
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(001);

        //Lettura dal database di altervista tramite Volley delle richieste di coda pendenti
        updateRichieste();
    }

    /**
     * Metodo invocato da Volley alla ricezione della risposta.
     * Si occupa di convertire il JSON ricevuto e di valorizzare la ListView e le rispettive Dialog
     */
    @Override
    public void onResponse(String response) {
        Log.d(TAG, response);
        final Bundle bundle = new Bundle();
        final SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        final ArrayList<Json_Richiesta> productList = new JsonConverter<Json_Richiesta>().toArrayList(response, Json_Richiesta.class);
        final BindDictionary<Json_Richiesta> dictionary = new BindDictionary<>();
        dictionary.addStringField(R.id.tvText, new StringExtractor<Json_Richiesta>() {
            @Override
            public String getStringValue(Json_Richiesta product, int position) {
                return getResources().getString(R.string.hour_queue) + " " + product.orario.toString().substring(0, 5) + "\n"
                        + getResources().getString(R.string.kiuer2) + product.nome.toString() + "\n";
            }
        }).onClick(new ItemClickListener<Json_Richiesta>() {

            @Override
            public void onClick(Json_Richiesta item, int position, View view) {
                DialogFragment newFragment = new Helper_NotificationDetails();
                bundle.putString("ID", productList.get(position).ID_richiesta.toString());
                bundle.putString("text", productList.get(position).nome
                        + " " + getResources().getString(R.string.text_request_queue)
                        + " " + productList.get(position).orario.substring(0, 5)
                        + " " + getResources().getString(R.string.text_request_queue2)
                        + " " + productList.get(position).luogo);
                newFragment.setArguments(bundle);
                newFragment.show(getFragmentManager(), "Helper_NotificationDetails");
            }
        });
        FunDapter<Json_Richiesta> adapter = new FunDapter<>(getApplicationContext(), productList, R.layout.notification_layout_helper, dictionary);
        lvProduct.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateRichieste();
    }

    /**
     * Il metodo esegue la richiesta di lettura dei dati presenti nel database altervista relativi alle richieste di coda pendenti per l'aggiornamento della ListView
     */
    public void updateRichieste() {
        final SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        String url = "http://www.davideantonio2.altervista.org/helper_check_richieste.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, this, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), R.string.err_read_google, Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("IDutente", prefs.getString(IDUTENTE, "").toString());
                return params;
            }
        };
        Volley.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
        lvProduct = (ListView) findViewById(R.id.lvProduct);

    }

    /**
     * Il metodo crea il menù sull'ActionBar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.helper_notification_menu, menu);
        return true;
    }

    /**
     * Il metodo gestisce il menù sull'ActionBar
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent in;
        boolean check = false;
        final SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);

        switch (item.getItemId()) {
            case R.id.exit_helper:
                SharedPreferences.Editor editor;
                editor = prefs.edit().clear();
                editor.apply();
                in = new Intent(getApplicationContext(), Login.class);
                startActivity(in);
                check = true;
                break;
            default:
                check = super.onOptionsItemSelected(item);
        }
        return check;
    }
}