package com.gambino_serra.KIU;

import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.gambino_serra.KIU.R;
import com.gambino_serra.KIU.chat.model.UserCard;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * La classe gestisce la registrazione dell'utente.
 */
public class RegisterActivity extends BaseActivity {

    private static final String TAG = "RegisterActivity";
    private static final String MY_PREFERENCES = "kiuPreferences";
    private static final String EMAIL = "email";
    private static final String TIPO_UTENTE = "tipoUtente";
    private static final String LOGGED_USER = "logged_user";
    private static final String CONV_NAME = "_conv_name";

    RadioButton kiuerbtn;
    RadioButton helperbtn;
    RadioGroup rg;
    Button btnConferma;
    EditText txt_nome;
    EditText txt_cognome;
    EditText txt_email;
    EditText txt_password;
    String nome;
    String cognome;
    String email;
    String password;
    String tipoUtente = "";
    String test = "ok";
    String test1 = "ok";
    String testData1 = "";
    String testData2 = "";
    int check_values = 0;
    boolean psw_len = true;
    private boolean check1 = true;
    private int count = 0;
    private int pos;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private DatabaseReference mDatabase;
    private String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {

            /**
             * Il metodo recupera un'istanza di FirebaseAuth e le associa un listener,
             * lo scopo è quello di resettare l'applicazione nel caso in cui si dovesse
             * verificare un errore con l'autenticazione.
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
                    //empty prefs and reload all

                }
            }
        };

        txt_nome = (EditText) findViewById(R.id.txt_nome);
        txt_cognome = (EditText) findViewById(R.id.txt_cognome);
        txt_email = (EditText) findViewById(R.id.email);
        txt_password = (EditText) findViewById(R.id.password);
        kiuerbtn = (RadioButton) findViewById(R.id.kiuerBtn);
        helperbtn = (RadioButton) findViewById(R.id.helperBtn);
        kiuerbtn.setChecked(false);
        btnConferma = (Button) findViewById(R.id.btnConferma);
        rg = (RadioGroup) findViewById(R.id.radioGroup);


        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            /**
             * Il metodo gestisce il tipo di utente con il quale ci si vuole registare.
             * @param group
             * @param checkedId
             */
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                pos = rg.indexOfChild(findViewById(checkedId));

                switch (pos) {
                    case 0:
                        kiuerbtn.setChecked(true);
                        tipoUtente = "K";
                        break;
                    case 1:
                        helperbtn.setChecked(true);
                        tipoUtente = "H";
                        break;
                    default:
                        break;
                }
            }
        });

        btnConferma.setOnClickListener(new View.OnClickListener() {

            /**
             * Il metodo gestisce le fasi di registrazione di un nuovo utente.
             * Si occupa inoltre del controllo di consistenza dei dati inseriti,
             * segnalando tramite messagi sulla UI gli eventuali errori sollevati.
             *
             * @param v istanza della View
             */
            @Override
            public void onClick(View v) {


                count = 0;

                check_values = checkInsertValues(txt_nome);
                if (check_values == 1) {
                    nome = txt_nome.getText().toString();
                    testData1 = "kor";
                } else {
                    check1 = false;
                    testData1 = "kob";

                }

                check_values = checkInsertValues(txt_cognome);
                if (check_values == 1) {
                    cognome = txt_cognome.getText().toString();
                    testData2 = "kor";
                } else {
                    check1 = false;
                    testData2 = "kob";
                }

                check_values = checkInsertValues(txt_email);
                if (check_values == 1) {
                    email = txt_email.getText().toString().trim();
                    test1 = "kok";
                    check1 = false;
                } else {
                    check1 = false;
                    test = "ko";
                }

                check_values = checkInsertValues(txt_password);
                if (check_values == 1 && pswlen(txt_password.getText().toString())) {
                    password = txt_password.getText().toString().trim();
                } else if (!pswlen(txt_password.getText().toString())) {
                    psw_len = false;
                } else {
                    check1 = false;
                    test = "ko";
                }

                if (testData1.contains("kob") || testData2.contains("kob")) {
                    Toast.makeText(getApplicationContext(), R.string.name_surname_insert, Toast.LENGTH_LONG).show();
                    testData1 = "ko";
                    testData2 = "ko";

                } else if (testData1.contains("kor") && testData2.contains("kor")) {
                    if (checkInsertValuesText(nome) == true && checkInsertValuesText(cognome) == true) {
                        count++;
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.name_surname_invalid, Toast.LENGTH_LONG).show();
                    }
                }


                if (check1 == false && test1.contains("kok")) {
                    if (!validateEmail(email)) {
                        Toast.makeText(getApplicationContext(), R.string.mail_invalid, Toast.LENGTH_SHORT).show();
                    } else {
                        count++;
                    }
                }
                if (check1 == false && test.contains("ko")) {
                    Toast.makeText(getApplicationContext(), R.string.empty_fields, Toast.LENGTH_SHORT).show();
                    test = "ok";
                } else if (psw_len == false) {
                    Toast.makeText(getApplicationContext(), R.string.psw_len_incorrect, Toast.LENGTH_SHORT).show();
                    psw_len = true;
                } else {
                    count++;
                }
                if (tipoUtente.isEmpty()) {
                    Toast.makeText(getApplicationContext(), R.string.type_user, Toast.LENGTH_SHORT).show();
                } else {
                    count++;
                }

                // se l'utente ha inserito correttamente i dati nei campi allora
                // si procede con la sua registrazione.
                if (count == 4) {
                    showProgressDialog();
                    createAccount(email, password);


                }

            }
        });

    }

    /**
     * Il metodo gestisce la registrazione dell'utente su database di
     * altervista.
     */
    private void setUserOnAltevista() {

        String url = "http://www.kiu.altervista.org/register.php"; //stringa di connessione al database su altervista per la registrazione

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            /**
             * Il metodo gestisce la memorizzazione dei dati utente nelle SharedPreferences
             * in caso di registrazione avvenuta con successo.
             * @param response
             */
            @Override
            public void onResponse(String response) {

                if (response.contains("ok")) {
                    final SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE);
                    SharedPreferences.Editor editor;
                    editor = prefs.edit();
                    editor.putString(EMAIL, email);
                    editor.putString(TIPO_UTENTE, tipoUtente);
                    editor.putString(LOGGED_USER, userID);
                    editor.putString(CONV_NAME, "");
                    editor.apply();

                    //Inizializzazione ed inserimento del profilo Helper
                    if (tipoUtente.equals("H")) {
                        String url = "http://www.kiu.altervista.org/register_profilo_helper.php";
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if (!response.contains("ok")) {
                                    getFailure();
                                    hideProgressDialog();
                                    Snackbar snack = Snackbar.make(findViewById(android.R.id.content), R.string.registration_problem, Snackbar.LENGTH_LONG);
                                    View view1 = snack.getView();
                                    TextView tv = (TextView) view1.findViewById(android.support.design.R.id.snackbar_text);
                                    tv.setTextColor(Color.WHITE);
                                    snack.show();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getApplicationContext(), R.string.connection_failed, Toast.LENGTH_SHORT).show();
                                hideProgressDialog();
                                getFailure();

                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<>();
                                params.put("email", email);
                                return params;
                            }
                        };

                        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
                    }
                } else {
                    getFailure();
                    hideProgressDialog();
                    Snackbar snack = Snackbar.make(findViewById(android.R.id.content), R.string.registration_problem, Snackbar.LENGTH_LONG);
                    View view1 = snack.getView();
                    TextView tv = (TextView) view1.findViewById(android.support.design.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    snack.show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), R.string.connection_failed, Toast.LENGTH_SHORT).show();
                hideProgressDialog();
                getFailure();


            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("nome", nome.replace("'", "").toString());
                params.put("cognome", cognome.replace("'", "").toString());
                params.put("email", email);
                params.put("uid", userID);
                params.put("tipo", tipoUtente);
                return params;
            }
        };

        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    /**
     * Il metodo cancella la registrazione utente su Firebase
     */
    private void getFailure() {
        mAuth.getCurrentUser().delete();
    }

    /**
     * Il metodo gestisce l'inizializzazione dell'utente creato
     * nel database di Firebase.
     */
    private void startSetting() {

        // creazione del database node
        FirebaseUser current_user = mAuth.getCurrentUser();
        if (current_user == null) {
            Log.e(TAG, "error with initial user startupping");
        } else {
            userID = current_user.getUid();
            mDatabase = FirebaseDatabase.getInstance().getReference("/chat");

            DatabaseReference mCards = mDatabase.child("cards");
            if (null == mCards) {
                mDatabase.child("cards").push();
            }

            mCards = FirebaseDatabase.getInstance().getReference("chat/cards");
            mCards.child(userID).setValue(new UserCard(userID, nome + " " + cognome, email, tipoUtente));
        }

        hideProgressDialog();

        DialogFragment newFragment = new DialogRegistrationSuccess();
        newFragment.show(getFragmentManager(), "DialogRegistrationSuccess");

    }

    /**
     * Il metodo imposta il messaggio della Dialog.
     */
    @Override
    protected void setMessage() {
        mProgressDialog.setMessage(getString(R.string.loading));
    }

    /**
     * Il metodo controlla se campo testo e' vuoto o meno.
     *
     * @param edit
     * @return check booleano
     */
    private int checkInsertValues(EditText edit) {
        int check;
        if (edit.getText().toString().isEmpty() || edit == null) {
            check = 0;
        } else {
            check = 1;
        }

        return check;
    }

    /**
     * Il metodo controlla che l'inserimento di caratteri sia valido.
     *
     * @param txt_nome
     * @return boolean
     */
    private boolean checkInsertValuesText(String txt_nome) {
        Pattern pattern;
        Matcher matcher;
        String nome_text_pattern = "^[A-Za-z ']*$";
        pattern = Pattern.compile(nome_text_pattern, Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(txt_nome);
        matcher.matches();
        return matcher.matches();

    }

    /**
     * Il metodo controlla la validita' del campo email.
     *
     * @param email
     * @return boolean
     */
    private boolean validateEmail(String email) {
        Pattern pattern;
        Matcher matcher;
        String EMAIL_PATTERN = "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /**
     * Il metodo controlla che la password sia di lunghezza non inferiore
     * ai 6 caratteri (specifiche Firebase).
     *
     * @param checkpsw
     * @return boolean
     */
    private boolean pswlen(String checkpsw) {

        boolean txtpsw = false;

        if (checkpsw.length() > 5) {
            txtpsw = true;
        }

        return txtpsw;
    }

    /**
     * Il metodo gestisce la creazione dell'utente nel sistema di Firebase.
     *
     * @param mail
     * @param password
     */
    private void createAccount(final String mail, String password) {
        Log.d(TAG, "createAccount:" + mail);

        mAuth.createUserWithEmailAndPassword(mail, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    /**
                     * Il metodo controlla se la creazione dell'utente in Firebase e' andata a buon fine.
                     * @param task
                     */
                    @Override
                    public void onComplete(@NotNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            hideProgressDialog();
                            Toast.makeText(RegisterActivity.this, R.string.registration_problem, Toast.LENGTH_SHORT).show();
                            hideProgressDialog();
                            return;
                        } else {
                            startSetting();
                            setUserOnAltevista();
                        }
                    }
                });
    }
}
