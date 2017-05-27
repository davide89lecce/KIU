package com.gambino_serra.KIU;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
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
 * La classe gestisce l'Activity relativa alle impostazioni dell'Helper.
 */
public class Helper_Settings extends AppCompatActivity {

    final private static String MY_PREFERENCES = "kiuPreferences";
    private static final String IDUTENTE = "IDutente";
    private TextView textInizioDisp;
    private TextView textFineDisp;
    private TextView textTariffaOraria;
    private Button scegliArea;
    private Button menoButton;
    private Button piuButton;
    private int oraInizio;
    private int minutiInizio;
    private int oraFine;
    private int minutiFine;
    private int setOrario;
    static final int TIME_DIALOG_ID1 = 1;
    static final int TIME_DIALOG_ID2 = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        textInizioDisp = (TextView) findViewById(R.id.orainiziodisponibilita);
        textFineDisp = (TextView) findViewById(R.id.orafinedisponibilita);
        textTariffaOraria = (TextView) findViewById(R.id.tariffa_oraria);
        scegliArea = (Button) findViewById(R.id.scegliArea);
        menoButton = (Button) findViewById(R.id.buttonMeno);
        piuButton = (Button) findViewById(R.id.buttonPiu);

        //Lettura delle impostazioni dell'Helper presenti nel database di altervista.
        final SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        String url = "http://www.davideantonio2.altervista.org/helper_letturaImpostazioni.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                //Se le impostazioni non sono state ancora inizializzate, le inizializza altrimenti legge le impostazioni dal database di altervista e avvalora i campi.
                if (!response.equals("null")) {
                    final ArrayList<Json_Helper> productList = new JsonConverter<Json_Helper>().toArrayList(response, Json_Helper.class);
                    textInizioDisp.setText(productList.get(0).disp_inizio.toString().subSequence(0,5));
                    textFineDisp.setText(productList.get(0).disp_fine.toString().subSequence(0,5));
                    textTariffaOraria.setText(productList.get(0).tariffa_oraria.toString());
                } else {
                    textInizioDisp.setText("00:00");
                    textFineDisp.setText("23:59");
                    textTariffaOraria.setText("0.0");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), R.string.err_read_google, Toast.LENGTH_SHORT).show();

            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("IDutente", prefs.getString(IDUTENTE,"").toString());
                return params;
            }
        };
        Volley.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);


        textInizioDisp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //visualizza Dialog con id 0 in questo caso
                showDialog(TIME_DIALOG_ID1);
                setOrario = 1;
            }
        });

        textFineDisp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //visualizza Dialog con id 0 in questo caso
                showDialog(TIME_DIALOG_ID2);
                setOrario = 2;
            }
        });

        scegliArea.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent mappa = new Intent(Helper_Settings.this, Helper_MapsSetting.class);
                startActivity(mappa);
            }
        });


        menoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if( Integer.parseInt(textTariffaOraria.getText().toString()) != 0) {
                    Integer tariffaUpdate = Integer.parseInt(textTariffaOraria.getText().toString()) - 1;
                    textTariffaOraria.setText(tariffaUpdate.toString());
                }
            }
        });

        piuButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Integer tariffaUpdate = Integer.parseInt(textTariffaOraria.getText().toString()) + 1;
                textTariffaOraria.setText(tariffaUpdate.toString());
            }
        });
    }

    /**
     * Il metodo aggiorna il tempo visualizzato nelle TextView.
     */
    private void updateDisplay() {
        textInizioDisp.setText(new StringBuilder().append(pad(oraInizio)).append(":").append(pad(minutiInizio)));
        textFineDisp.setText(new StringBuilder().append(pad(oraFine)).append(":").append(pad(minutiFine)));
    }

    private static String pad(int c) {
        if (c >= 10)   return String.valueOf(c);
        else     return "0" + String.valueOf(c);
    }

    /**
     * Il metodo viene invocata quando l'utente imposta il tempo nella Dialog e preme il bottone "OK".
     */
    private android.app.TimePickerDialog.OnTimeSetListener mTimeSetListener =
            new android.app.TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                   if(setOrario == 1) {
                       oraInizio = hourOfDay;
                       minutiInizio = minute;
                   }
                   else {
                       oraFine = hourOfDay;
                       minutiFine = minute;
                   }
                   updateDisplay();
                }
            };

    /**
     * Il metodo gestisce la creazione della Dialog TimePicker.
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case TIME_DIALOG_ID1:
                return new android.app.TimePickerDialog(this, mTimeSetListener, oraInizio, minutiInizio, false);
            case TIME_DIALOG_ID2:
                return new android.app.TimePickerDialog(this, mTimeSetListener, oraFine, minutiFine, false);
        }
        return null;
    }

    /**
     * Update nel database di altervista delle impostazioni dell'Helper.
     */
    private void updateDatabase(){

        final SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        String url = "http://www.davideantonio2.altervista.org/helper_aggiornaImpostazioni.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.equals("ok")) {
                    Toast.makeText(getApplicationContext(), R.string.update_setting, Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), R.string.error_update_setting, Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), R.string.err_read_google, Toast.LENGTH_SHORT).show();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("IDutente", prefs.getString(IDUTENTE,"").toString());
                params.put("disp_inizio", new StringBuilder().append(textInizioDisp.getText().toString()).append(":00").toString());
                params.put("disp_fine", new StringBuilder().append(textFineDisp.getText().toString()).append(":00").toString());
                params.put("tariffa_oraria", textTariffaOraria.getText().toString());
                return params;
            }
        };
        Volley.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    /**
     * Il metodo crea il menù sull'ActionBar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.helper_setting_menu, menu);
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
            case R.id.home_helper:
                in = new Intent(getApplicationContext(), Helper_Home.class);
                startActivity(in);
                check = true;
                break;
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

    @Override
    public void onPause(){
        super.onPause();
        updateDatabase();
    }
}