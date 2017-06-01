package com.gambino_serra.KIU;

import com.google.gson.annotations.SerializedName;

/**
 * La classe definisce l'oggetto Json.
 */
public class Json_Richiesta {
        @SerializedName("ID_richiesta")         public Integer ID_richiesta;
        @SerializedName("ID_kiuer")             public String ID_kiuer;
        @SerializedName("rating")               public float rating;
        @SerializedName("cont_feedback")        public Integer cont_feedback;
        @SerializedName("ID_helper")            public String ID_helper;
        @SerializedName("orario")               public String orario;
        @SerializedName("stato_richiesta")      public String stato_richiesta;
        @SerializedName("stato_notifica_richiesta") public String stato_notifica_richiesta;
        @SerializedName("nome")                 public String nome;
        @SerializedName("luogo")                public String luogo;
        @SerializedName("descrizione")          public String descrizione;
        @SerializedName("pos_latitudine")       public String pos_latitudine;
        @SerializedName("pos_longitudine")      public String pos_longitudine;
        @SerializedName("stato_coda")           public String stato_coda;
        @SerializedName("stato_notifica_coda")  public String stato_notifica_coda;
        @SerializedName("orario_inizio")        public String orario_inizio;
        @SerializedName("orario_fine")          public String orario_fine;
        @SerializedName("tariffa_oraria")       public int tariffa_oraria;
}
