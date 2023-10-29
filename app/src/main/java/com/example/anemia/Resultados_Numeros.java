package com.example.anemia;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

// Resultados_Numeros.java
public class Resultados_Numeros extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultados_numeros);

        // Obtiene los datos pasados como extras
        String resultado = getIntent().getStringExtra("resultado");
        String recomendaciones = getIntent().getStringExtra("recomendaciones");

        // Muestra los datos en los TextView correspondientes
        TextView tvResultado = findViewById(R.id.tvResultado);
        tvResultado.setText("Resultado: " + resultado);

        TextView tvRecomendaciones = findViewById(R.id.tvRecomendaciones);
        tvRecomendaciones.setText("Recomendaciones: " + recomendaciones);
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
