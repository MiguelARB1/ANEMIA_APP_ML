package com.example.anemia;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;


public class LeerQRActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leer_qractivity);

        // Inicializa el escáner al hacer clic en un botón
        Button btnEscanear = findViewById(R.id.btnEscanear);
        btnEscanear.setOnClickListener(v -> startScanning());
    }

    private void startScanning() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Escanea un código QR");
        integrator.setCameraId(0);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                // Escaneo cancelado por el usuario
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_SHORT).show();
            } else {
                // Aquí obtienes el valor escaneado (result.getContents())
                handleScannedData(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleScannedData(String scannedData) {
        // Realiza una solicitud HTTP para obtener datos del servidor
        String apiUrl = "" + scannedData;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                apiUrl,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Aquí procesa la respuesta JSON del servidor
                        try {
                            // Obtiene el objeto "hemoglobina" de la respuesta JSON
                            JSONObject jsonHemoglobina = response.getJSONObject("hemoglobina");

                            // Obtiene el objeto "recomendaciones" de la respuesta JSON
                            JSONObject jsonRecomendaciones = response.getJSONObject("recomendaciones");

                            // Obtiene el resultado y las recomendaciones
                            String resultado = jsonRecomendaciones.getString("resultado");
                            String recomendaciones = jsonRecomendaciones.getString("recomendaciones");

                            // Crea un Intent para abrir la actividad de resultados
                            Intent resultadosIntent = new Intent(LeerQRActivity.this, ResultadosActivity.class);
                            // Pasa los datos como extras al Intent
                            resultadosIntent.putExtra("resultado", resultado);
                            resultadosIntent.putExtra("recomendaciones", recomendaciones);
                            // Inicia la actividad de resultados
                            startActivity(resultadosIntent);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            // Manejo de errores aquí, por ejemplo, mostrar un mensaje Toast
                            Toast.makeText(LeerQRActivity.this, "Error al procesar los datos JSON", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Maneja errores en la solicitud HTTP, por ejemplo, mostrar un mensaje Toast
                        Toast.makeText(LeerQRActivity.this, "Error en la solicitud HTTP", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Agrega la solicitud a la cola de solicitudes de Volley
        Volley.newRequestQueue(this).add(jsonObjectRequest);
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