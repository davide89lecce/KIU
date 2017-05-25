package com.gambino_serra.KIU;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * La classe gestisce il login
 */
public class Login extends AppCompatActivity {

    private static final String TAG = "Login";
    private static final String MY_PREFERENCES = "kiuPreferences";
    private static final String IDUTENTE = "IDutente";
    private static final String PASSWORD = "password";
    private static final String TIPO_UTENTE = "tipoUtente";
    private static final String LOGGED_USER = "logged_user";
    private static final String CONV_NAME = "_conv_name";

    EditText etUsername, etPassword;
    Button btnLogin;

    /**
     * Il metodo si occupa dell'inizializzazione degli elementi della UI di accesso all'applicazione.
     * Verifica che l'utente sia connesso, se vero, allora lo indirizza nella sua Activity principale.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);

        // si verifica che le SharedPreferences contengano dati, nel caso contrario l'utente risultera non connesso.
        final SharedPreferences sharedPrefs = getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE);
        if (!sharedPrefs.getAll().isEmpty()) {
            Log.d("Utente", "Connesso");
            getStatusAndGoHome();
            }
        else {
            Log.d("Utente", "Non connesso");
            }

        btnLogin.setOnClickListener(new View.OnClickListener() {

            /**
             * Il metodo permette di acquisire i dati inseriti dall'utente, verifica che i campi
             * di testo non siano vuoti ed effettua il login.
             */
            @Override
            public void onClick(View v) {

                String username = etUsername.getText().toString();
                String psw = etPassword.getText().toString();
                if (!etUsername.getText().toString().equals("") && !etPassword.getText().toString().equals("")) {
                    signIn(username, psw);
                    }
                else {
                    Toast.makeText(getApplicationContext(), R.string.insert_name_password, Toast.LENGTH_LONG).show();
                    }
            }
        });
    }

    /**
     * Il metodo imposta il messaggio della Dialog.
     */
    //@Override
    protected void setMessage() {
        mProgressDialog.setMessage(getString(R.string.login));
        }

    /**
     * Il metodo gestisce l'evento di autenticazione.
     */
    private void signIn(String IDutente, String password) {

        showProgressDialog();

        //Lettura con volley dell'attributo disponibilità helper per settare lo switch presente nel layout
        final SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE);
        String url = "http://www.davideantonio2.altervista.org/login.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.contains("correct_login_helper")) {
                    setAllAndGoHome(etUsername.getText().toString(), etPassword.getText().toString(), "H");
                    }
                else if(response.contains("correct_login_kiuer")) {
                    setAllAndGoHome(etUsername.getText().toString(), etPassword.getText().toString(), "K");
                    }
                else if(response.contains("no_login")){
                    Toast.makeText(getApplicationContext(), "Username o password errata", Toast.LENGTH_SHORT).show();
                    }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), R.string.error_update_volley, Toast.LENGTH_SHORT).show();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("IDutente", etUsername.getText().toString());
                params.put("password", etPassword.getText().toString());
                return params;
            }
        };

        Volley.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    /**
     * Il metodo inizializza le SharedPreferences dell'applicazione.
     */
    private void setPrefs(String username, String password, String tipologia) {
        final SharedPreferences sharedPrefs = getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(IDUTENTE, username);   // identificativo utente in altervista.
        editor.putString(PASSWORD, password);   // identificativo utente in altervista.
        editor.putString(TIPO_UTENTE, tipologia); // usato come identificativo del tipo di utente.
        editor.apply();
    }

    /**
     * Il metodo verifica l'utente e lo indirizza nella sua Home Activity.
     */
    private void getStatusAndGoHome() {

        final SharedPreferences sharedPrefs = getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE);
        if (sharedPrefs.getString(TIPO_UTENTE, "").equals("H")) {
            Intent in = new Intent(Login.this, Helper_Home.class);
            startActivity(in);
            }
        else if (sharedPrefs.getString(TIPO_UTENTE, "").equals("K")) {
            Intent in = new Intent(Login.this, Kiuer_Home.class);
            startActivity(in);
            }
    }

    /**
     * Il metodo imposta le informazioni dell'utente e lo indirizza nella sua Home Activity.
     */
    private void setAllAndGoHome(String username, String password, String tipologia) {
        hideProgressDialog();
        setPrefs(username, password, tipologia);
        getStatusAndGoHome();
        }


    public ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            setMessage();
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }
}