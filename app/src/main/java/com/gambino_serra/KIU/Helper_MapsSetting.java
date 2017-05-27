package com.gambino_serra.KIU;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kosalgeek.android.json.JsonConverter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 * La classe gestisce la relativa posizione del Helper mediante l'uso delle mappe e l'utilizzo delle GoogleApiClient.
 */
@RuntimePermissions
public class Helper_MapsSetting extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener,
        OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private MapFragment mapFragment;
    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 60000;  /* 60 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 secs */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    View view;
    ImageView checkButton;
    ImageView closeButton;
    final private static String MY_PREFERENCES = "kiuPreferences";
    final private static String IDUTENTE = "IDutente";
    final Bundle bundle = new Bundle();
    Location location;
    LatLng ltln;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        checkButton = (ImageView) findViewById(R.id.checkButton);
        closeButton = (ImageView) findViewById(R.id.closeButton);

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.helperpositionmap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
            }
        else {
            Toast.makeText(this, R.string.error_loading_maps, Toast.LENGTH_SHORT).show();
            }
    }

    /**
     * Il metodo permette di caricare le mappe di Google.
     */
    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;
        if (map != null) {
            Helper_MapsSettingPermissionsDispatcher.getMyLocationWithCheck(this);
            map.setOnMapClickListener(this);
            }
        else {
            Toast.makeText(this, R.string.error_loading_maps, Toast.LENGTH_SHORT).show();
            }
    }

    @SuppressWarnings("all")
    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void getMyLocation() {
        if (map != null) {
            //Adesso che la mappa e' caricata puo' ricevere la psosizione
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
            connectClient();
        }
    }

    /**
     * Il metodo permette la connessione al Client dei servizi di Google delle mappe.
     */
    protected void connectClient() {
        // Connette il Client.
        if (isGooglePlayServicesAvailable() && mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Il metodo e' chiamato quando l'Activity torna visibile (foreground).
     */
    @Override
    protected void onStart() {
        super.onStart();
        connectClient();
        }

    /**
     * Il metodo e' chiamato quando l'Activity perde la visibilita'.
     */
    @Override
    protected void onStop() {
        // Disconnette il Client
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
            }
        super.onStop();
        }

    /**
     * Il metodo gestisce i risultati ritornati dal FragmentActivity dei Google Play services.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Decide cosa fare in base al codice di richiesta originale.
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
                //Se il risultato del codice e' Activity.RESULT_OK, prova a riconnettersi.
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        mGoogleApiClient.connect();
                        break;
                }
        }
    }

    /**
     * Il metodo verifica che il servizi di Google siano disponibili, in caso contrario una Dialog viene visualizzata al'utente.
     */
    private boolean isGooglePlayServicesAvailable() {

        // Verifica disponibilita' dei servizi di Google
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // Se Google Play services sono disponibili
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates", "Google Play services is available.");
            return true;
            }
        else {
            // Ricevo la Error Dialog dai servizi Google Play.
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // Se Google Play services puo' fornire una Error Dialog
            if (errorDialog != null) {
                // Creazione di un DialogFragment
                Helper_MapsSetting.ErrorDialogFragment errorFragment = new Helper_MapsSetting.ErrorDialogFragment();
                errorFragment.setDialog(errorDialog);
                errorFragment.show(getFragmentManager(), "Location Updates");
                }
            return false;
            }
    }

    /**
     * Il metodo viene invocato dal Location Services quando la richiesta di connessione al client
     * e' avvenuta con successo. In questo momento si puo' richiedere la posizione corrente.
     */
    @Override
    public void onConnected(Bundle dataBundle) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
            }

        location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null) { }
        else {
            Toast.makeText(this, R.string.enable_gps, Toast.LENGTH_SHORT).show();
            }
        startLocationUpdates();
    }

    /**
     * Il metodo permette di aggiornare la posizione.
     */
    protected void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }
    public void onLocationChanged(Location location) { }

    /**
     * Il metodo e' invocato dal Location Services se la connessione con il client si interrrompe a causa di un errore.
     */
    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(this, R.string.disconnected, Toast.LENGTH_SHORT).show();
            }
        else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(this, R.string.network_lost, Toast.LENGTH_SHORT).show();
            }
        }

    /**
     * Il metodo viene invocato dal Location Services se lo stesso servizio fallisce
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        // Se il problema di connessione e' risolvibile (da Google) allora viene
        // inviato un Intent all'Activity predisposta a risolvere il problema.
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                } //L'eccezione e' sollevata nel caso in cui l'Intent viene eliminato.
            catch (IntentSender.SendIntentException e) { e.printStackTrace(); }
            }
        else {
            Toast.makeText(getApplicationContext(), R.string.location_service_not_available, Toast.LENGTH_LONG).show();
            }
    }

    /**
     * Il metodo gestisce la comunicazione, tramite Dialog, degli errori che possono verificarsi.
     */
    public static class ErrorDialogFragment extends DialogFragment {

        private Dialog mDialog;

        public ErrorDialogFragment() {
            super();
            mDialog = null;
            }

        public void setDialog(Dialog dialog) {
            mDialog = dialog;
            }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
            }
    }

    /**
     * Il metodo permette di caricare e visualizzare la mappa nella UI dell'applicazione
     * e le sue componeti invocando il metodo loadMap(map).
     */
    @Override
    public void onMapReady(GoogleMap maps) {

        map = maps;

        loadMap(map);

        Snackbar snack = Snackbar.make(findViewById(android.R.id.content), R.string.sign_your_position, Snackbar.LENGTH_LONG);
        View view1 = snack.getView();
        TextView tv = (TextView) view1.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        snack.show();

        //Lettura della coordinate del Helper.
        final SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        String url = "http://www.davideantonio2.altervista.org/helper_letturaImpostazioni.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if (!response.equals("null")) {
                    final ArrayList<Json_Helper> productList = new JsonConverter<Json_Helper>().toArrayList(response, Json_Helper.class);

                    // se le coordinate sono già presenti nel database, recupero i dati e setto il marker
                    if(!productList.get(0).pos_latitudine.toString().equals("0.0") && !productList.get(0).pos_longitudine.toString().equals("0.0")){

                        ltln = new LatLng(productList.get(0).pos_latitudine, productList.get(0).pos_longitudine);

                        try {
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(ltln, 17);
                            map.animateCamera(cameraUpdate);
                            map.addMarker(new MarkerOptions().position(ltln).title("Io"));
                            }
                        catch (SecurityException ex) { Log.e("MAPPA!", "problema con la localizzazione"); }

                        //scelta la posizione invio i dati al database di altervista.
                        checkButton.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {

                                Intent setting = new Intent(Helper_MapsSetting.this, Helper_Settings.class);
                                startActivity(setting);

                                final Double latitude = ltln.latitude;
                                final Double longitude = ltln.longitude;

                                //Invio delle nuove coordinate al database di altervista
                                String url = "http://www.davideantonio2.altervista.org/helper_aggiornaCoordinate.php";
                                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        if (response.equals("ok")) {
                                            Toast.makeText(getApplicationContext(),R.string.update_coordinates, Toast.LENGTH_SHORT).show();
                                            }
                                        else {
                                            Toast.makeText(getApplicationContext(), R.string.error_update_coordinates, Toast.LENGTH_SHORT).show();
                                            }
                                    }
                                }, new Response.ErrorListener()
                                {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Toast.makeText(getApplicationContext(), R.string.err_read_google, Toast.LENGTH_SHORT).show();
                                        }
                                })
                                {
                                    @Override
                                    protected Map<String, String> getParams() throws AuthFailureError {
                                        Map<String, String> params = new HashMap<>();
                                        params.put("IDutente", prefs.getString(IDUTENTE, "").toString());
                                        params.put("pos_latitudine", latitude.toString());
                                        params.put("pos_longitudine", longitude.toString());
                                        return params;
                                        }
                                };
                                Volley.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
                            }
                        });

                        //chiude l'ActivityMap e visualizza Helper_Settings
                        closeButton.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                Intent setting = new Intent(Helper_MapsSetting.this, Helper_Settings.class);
                                startActivity(setting);
                                }
                        });
                    //se le coordinate non sono mai state inserite dal Helper nel database allora imposto la posizione attraverso geolocalizzazione
                    }
                    else{
                        try {
                            if (location == null) {
                                //se geolocalizzazione non disponibile carico coordinate di default
                                ltln = new LatLng(40.3551668814170, 18.17488800734281);
                                }
                            else {
                                //altrimenti carico posizione corrente del dispositivo
                                ltln = new LatLng(location.getLatitude(), location.getLongitude());
                                }
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(ltln, 17);
                            map.animateCamera(cameraUpdate);
                            map.addMarker(new MarkerOptions().position(ltln).title("Io"));
                        } catch (SecurityException ex) { Log.e("MAPPA!", "problema con la localizzazione"); }

                        //scelta la posizione invio i dati al database di altervista.
                        checkButton.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {

                                Intent setting = new Intent(Helper_MapsSetting.this, Helper_Settings.class);
                                startActivity(setting);

                                final Double latitude = ltln.latitude;
                                final Double longitude = ltln.longitude;

                                //Invio delle nuove coordinate al database di altervista
                                String url = "http://www.davideantonio2.altervista.org/helper_aggiornaCoordinate.php";
                                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        if (response.equals("ok")) {
                                            Toast.makeText(getApplicationContext(), R.string.update_coordinates, Toast.LENGTH_SHORT).show();
                                            }
                                        else {
                                            Toast.makeText(getApplicationContext(), R.string.error_update_coordinates, Toast.LENGTH_SHORT).show();
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
                                        params.put("IDutente", prefs.getString(IDUTENTE, "").toString());
                                        params.put("pos_latitudine", latitude.toString());
                                        params.put("pos_longitudine", longitude.toString());
                                        return params;
                                    }
                                };
                                Volley.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
                            }
                        });
                        //chiude l'ActivityMap e visualizza Helper_Settings
                        closeButton.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                Intent setting = new Intent(Helper_MapsSetting.this, Helper_Settings.class);
                                startActivity(setting);
                                }
                            });
                    }

                }
                else{
                    Toast.makeText(getApplicationContext(), R.string.error_read_coordinates, Toast.LENGTH_SHORT).show();
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
                params.put("IDutente", prefs.getString(IDUTENTE, "").toString());
                return params;
                }
        };
        Volley.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    /**
     * Il metodo permette di posizionare il marker sulla mappa.
     */
    @Override
    public void onMapClick(LatLng latLng) {
        map.clear();
        ltln = latLng;
        map.addMarker(new MarkerOptions().position(latLng).title("La tua posizione"));
        }
}