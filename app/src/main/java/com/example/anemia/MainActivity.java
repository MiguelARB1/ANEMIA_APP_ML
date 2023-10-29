package com.example.anemia;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    private EditText editTextNivelHemoglobina;
    private Button buttonConsultar;
    private TextView textViewResultados;
    private TextView textViewRecomendaciones;

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextNivelHemoglobina = findViewById(R.id.editTextHemoglobina);
        buttonConsultar = findViewById(R.id.btnConsultar);

        requestQueue = Volley.newRequestQueue(this);

        buttonConsultar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                consultarNivelHemoglobina();
            }
        });
    }

    private void consultarNivelHemoglobina() {
        String nivelHemoglobina = editTextNivelHemoglobina.getText().toString().trim();

        // URL del servidor PHP (reemplaza con tu propia URL)
        String url = "http://192.168.1.43/api/consultardatos.php?nivel_hemoglobina=" + nivelHemoglobina;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Obtener datos del nivel de hemoglobina
                            JSONObject hemoglobina = response.getJSONObject("hemoglobina");
                            String resultadoHemoglobina =  hemoglobina.getString("descripcion");

                            // Obtener recomendaciones
                            JSONObject recomendaciones = response.getJSONObject("recomendaciones");
                            String recomendacionesTexto = recomendaciones.getString("recomendaciones");

                            // Crear un Intent para iniciar ResultadosActivity
                            Intent intent = new Intent(MainActivity.this, ResultadosActivity.class);
                            // Agregar los resultados como extras al Intent
                            intent.putExtra("resultado", resultadoHemoglobina);
                            intent.putExtra("recomendaciones", recomendacionesTexto);
                            // Iniciar ResultadosActivity
                            startActivity(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error Volley", error.toString());
                        textViewResultados.setText("Error al consultar el nivel de hemoglobina.");
                        textViewRecomendaciones.setText("");
                    }
                }
        );

        requestQueue.add(jsonObjectRequest);
    }
    public void llamar_opciones(View view) {
        Intent intencion = new Intent(this,OpcionesActivity.class);
        startActivity(intencion);
    }
    public void llmar_fotos(View view) {
        Intent intencion = new Intent(this,TomarFoto.class);
        startActivity(intencion);
    }

}