package com.example.anemia;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class OpcionesActivity extends AppCompatActivity {

    private Button btnEscanearQR;
    private Button btnIngresarNivelHemoglobina;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opciones);

    }
    public void LLAMAR_NIVEL_HEMOGLOBINA(View view) {
        Intent intencion = new Intent(this,MainActivity.class);
        startActivity(intencion);
    }

    public void LLAMAR_QR(View view) {
        Intent intencion = new Intent(this,LeerQRActivity.class);
        startActivity(intencion);
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