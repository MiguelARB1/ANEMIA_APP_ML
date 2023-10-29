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
    private Button selectImageBtn, drawPolygonBtn, saveImageBtn, takePhotoBtn;
    private Bitmap originalBitmap;
    private Bitmap editedBitmap;
    private Bitmap originalBitmapWithoutPoints; // Agregar esta variable
    private Canvas canvas;
    private Paint paint;
    private List<Point> polygonPoints = new ArrayList<>();

    private Uri selectedImageUri;
    private static final int SELECT_PICTURE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tomar_foto);

        imageView = findViewById(R.id.imageView);
        selectImageBtn = findViewById(R.id.selectImageBtn);
        drawPolygonBtn = findViewById(R.id.drawPolygonBtn);
        saveImageBtn = findViewById(R.id.saveImageBtn);
        takePhotoBtn = findViewById(R.id.takePhotoBtn);

        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);

        selectImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        drawPolygonBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawPolygon();
            }
        });

        takePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera();
            }
        });

        saveImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performPrediction();
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, SELECT_PICTURE);
    }

    private void drawPolygon() {
        if (originalBitmapWithoutPoints != null && !polygonPoints.isEmpty()) {
            // Crear un nuevo Bitmap recortado según el Path
            Bitmap croppedBitmap = Bitmap.createBitmap(originalBitmapWithoutPoints.getWidth(), originalBitmapWithoutPoints.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas croppedCanvas = new Canvas(croppedBitmap);
            Paint croppedPaint = new Paint();
            croppedPaint.setAntiAlias(true);
            croppedCanvas.drawARGB(0, 0, 0, 0);

            // Crear un Path con los puntos del polígono
            Path path = new Path();
            path.moveTo(polygonPoints.get(0).x, polygonPoints.get(0).y);
            for (int i = 1; i < polygonPoints.size(); i++) {
                Point p = polygonPoints.get(i);
                path.lineTo(p.x, p.y);
            }
            path.close();

            // Dibuja el polígono en el nuevo Bitmap recortado
            croppedCanvas.drawPath(path, croppedPaint);

            // Recortar el área del Bitmap original sin puntos
            croppedCanvas.clipPath(path);
            croppedCanvas.drawBitmap(originalBitmapWithoutPoints, 0, 0, null);

            // Establecer el nuevo Bitmap recortado en el ImageView
            imageView.setImageBitmap(croppedBitmap);
        }
    }

    private MappedByteBuffer loadModelFile(String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private ByteBuffer preprocessImage(Bitmap imageBitmap) {
        // Normalize the image (convert pixel values to floats between 0 and 1)
        int width = imageBitmap.getWidth();
        int height = imageBitmap.getHeight();
        int channel = 3; // RGB

        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(4 * 1 * width * height * channel);
        inputBuffer.order(ByteOrder.nativeOrder());
        inputBuffer.rewind();

        int[] pixels = new int[width * height];
        imageBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

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

    private void performPrediction() {
        if (originalBitmapWithoutPoints != null) {
            try {
                // Cargar el modelo (puedes mantener esta parte si ya tienes el modelo cargado en onCreate)
                MappedByteBuffer tfliteModel = loadModelFile("anemiaV2_final_android.tflite");
                Interpreter.Options tfliteOptions = new Interpreter.Options();
                tflite = new Interpreter(tfliteModel, tfliteOptions);

                // Obtener la imagen del ImageView para la predicción
                Bitmap imageBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                imageBitmap = Bitmap.createScaledBitmap(imageBitmap, 128, 128, true);

                // Normalizar la imagen
                ByteBuffer inputBuffer = preprocessImage(imageBitmap);

                // Realizar la inferencia
                float[][] outputArray = new float[1][1];
                tflite.run(inputBuffer, outputArray);
                float probability = outputArray[0][0];

                // Definir un umbral
                float threshold = 0.5f;

                // Crear el resultado
                String result;
                if (probability > threshold) {
                    result = "La imagen indica la presencia de anemia con una probabilidad del " + (probability * 100) + "%.";
                } else {
                    result = "La imagen indica la ausencia de anemia con una probabilidad del " + ((1 - probability) * 100) + "%.";
                }

                // Mostrar el resultado en un TextView
                TextView textViewResult = findViewById(R.id.textView);
                textViewResult.setText(result);

                // Cerrar el modelo después de usarlo
                tflite.close();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error performing prediction", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        // Crear un intent para abrir la cámara
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            // Manejar el caso en el que la cámara no está disponible en el dispositivo
            Toast.makeText(this, "La cámara no está disponible", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PICTURE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data.getData();
            try {
                originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                originalBitmapWithoutPoints = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                editedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true); // Actualiza editedBitmap
                imageView.setImageBitmap(editedBitmap); // Actualiza la imagen en el ImageView
                polygonPoints.clear(); // Borra los puntos
                drawPolygon();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            originalBitmapWithoutPoints = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
            editedBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true); // Actualiza editedBitmap

            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            originalBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
            imageView.setImageBitmap(editedBitmap); // Actualiza la imagen en el ImageView
            polygonPoints.clear(); // Borra los puntos
            drawPolygon();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && editedBitmap != null) {
            float[] eventCoords = { event.getX(), event.getY() };
            Matrix invertMatrix = new Matrix();
            imageView.getImageMatrix().invert(invertMatrix);
            invertMatrix.mapPoints(eventCoords);
            float x = eventCoords[0];
            float y = eventCoords[1];
            y -= imageView.getPaddingTop();

            polygonPoints.add(new Point(Math.round(x), Math.round(y))); // Agrega el punto a la lista

            Paint greenPaint = new Paint();
            greenPaint.setColor(Color.GREEN);

            // Establece un tamaño fijo para los puntos
            float pointSize = 10.0f; // Tamaño en píxeles
            greenPaint.setStrokeWidth(pointSize);

            canvas = new Canvas(editedBitmap);
            canvas.drawPoint(x, y, greenPaint);

            imageView.setImageBitmap(editedBitmap);
        }
        return true;
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