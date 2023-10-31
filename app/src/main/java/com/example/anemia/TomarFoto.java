package com.example.anemia;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class TomarFoto extends AppCompatActivity {
    private Interpreter tflite;
    private ImageView imageView;
    private Button buttonMakePrediction;
    private Button buttonSelectImage;
    private TextView textView;
    private Bitmap selectedImageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tomar_foto);

        imageView = findViewById(R.id.imageView);
        buttonMakePrediction = findViewById(R.id.buttonMakePrediction);
        buttonSelectImage = findViewById(R.id.seleccionarImg);
        textView = findViewById(R.id.textView);

        buttonSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the image selection dialog
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        try {
            tflite = new Interpreter(loadModelFile("anemiaV2_final_android.tflite"));
            Toast.makeText(this, "Modelo cargado", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al cargar el modelo", Toast.LENGTH_SHORT).show();
        }

        buttonMakePrediction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedImageBitmap != null) {
                    ByteBuffer inputBuffer = preprocessImage(selectedImageBitmap);
                    float[][] outputArray = new float[1][1];
                    tflite.run(inputBuffer, outputArray);

                    float probability = outputArray[0][0];
                    float threshold = 0.5f;
                    String result;

                    if (probability > threshold) {
                        result = "La imagen indica la presencia de anemia con una probabilidad del " + (probability * 100) + "%.";
                    } else {
                        result = "La imagen indica la ausencia de anemia con una probabilidad del " + ((1 - probability) * 100) + "%.";
                    }

                    textView.setText(result);
                } else {
                    Toast.makeText(TomarFoto.this, "Por favor, selecciona una imagen primero.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private MappedByteBuffer loadModelFile(String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    int modelInputWidth = 128;
    int modelInputHeight = 128;
    int modelInputChannels = 3;

    private ByteBuffer preprocessImage(Bitmap imageBitmap) {
        // Asegúrate de que la imagen se ajuste a las dimensiones requeridas
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(imageBitmap, modelInputWidth, modelInputHeight, true);

        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(4 * 1 * modelInputWidth * modelInputHeight * modelInputChannels);
        inputBuffer.order(ByteOrder.nativeOrder());
        inputBuffer.rewind();

        int[] pixels = new int[modelInputWidth * modelInputHeight];
        scaledBitmap.getPixels(pixels, 0, modelInputWidth, 0, 0, modelInputWidth, modelInputHeight);

        for (int pixelValue : pixels) {
            float r = ((pixelValue >> 16) & 0xFF) / 255.0f;
            float g = ((pixelValue >> 8) & 0xFF) / 255.0f;
            float b = (pixelValue & 0xFF) / 255.0f;
            inputBuffer.putFloat(r);
            inputBuffer.putFloat(g);
            inputBuffer.putFloat(b);
        }

        return inputBuffer;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            try {
                // Get the selected image
                Uri selectedImageUri = data.getData();
                selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);

                // Set the selected image to the ImageView
                imageView.setImageBitmap(selectedImageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}