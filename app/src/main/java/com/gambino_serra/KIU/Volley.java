package com.gambino_serra.KIU;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;

/**
 * La classe modella l'entita tramite la quale Volley gestisce la sincronizzazione dei thread delle richieste
 * nella cache gestita come una coda con priorità.
 */
public class Volley {

    private static Volley mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;

    private Volley(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
        }

    public static synchronized Volley getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new Volley(context);
            }
        return mInstance;
        }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = com.android.volley.toolbox.Volley.newRequestQueue(mCtx.getApplicationContext());
            }
        return mRequestQueue;
        }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
        }
}