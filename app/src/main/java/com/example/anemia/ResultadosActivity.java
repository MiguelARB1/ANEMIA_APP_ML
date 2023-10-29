package com.example.anemia;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ResultadosActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultados);

        // Obtiene los datos pasados como extras
        String resultado = getIntent().getStringExtra("resultado");
        String recomendaciones = getIntent().getStringExtra("recomendaciones");

        // Muestra los datos en los TextView correspondientes
        TextView tvResultado = findViewById(R.id.tvResultado);
        tvResultado.setText(resultado);

        TextView tvRecomendaciones = findViewById(R.id.tvRecomendaciones);
        tvRecomendaciones.setText(recomendaciones);

        // Obtiene la ImageView
        ImageView imgResultado = findViewById(R.id.imgResultado);

        // Verifica el valor de "resultado" y establece la imagen correspondiente
        if (resultado.equals("Nivel de hemoglobina normal")) {
            imgResultado.setImageResource(R.drawable.saludable);
        } else if (resultado.equals("Nivel de hemoglobina bajo (anemia leve)")) {
            imgResultado.setImageResource(R.drawable.leve);
        } else if (resultado.equals("Anemia moderada")) {
            imgResultado.setImageResource(R.drawable.moredara);
        } else if (resultado.equals("Anemia severa")) {
            imgResultado.setImageResource(R.drawable.peligro);
        }
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
