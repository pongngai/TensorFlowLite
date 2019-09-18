package guide.borntodev.tensorflowlite;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private static final String MODEL_PATH = "model.tflite";
    private static final boolean QUANT = true;
    private static final String LABEL_PATH = "labels.txt";
    private static final int INPUT_SIZE = 224;
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 200;
    private static final int GALLERY_REQUEST_CODE = 0;
    private static final int CAMERA_REQUEST_CODE = 1;
    private Executor executor = Executors.newSingleThreadExecutor();
    private Button btn_captureImage;
    private Button btn_gallery;
    private ImageView imageView1;
    private TextView textViewResult1;
    private Classifier classifier;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView1 = (ImageView)findViewById(R.id.imageView1);
        btn_captureImage =(Button)findViewById(R.id.btn_captureImage);
        btn_gallery = (Button)findViewById(R.id.btn_gallery);
        textViewResult1 = (TextView)findViewById(R.id.textViewResult1);

        if(checkPermission() == false){
            verifyPermission();
        }

        btn_captureImage.setOnClickListener(new View.OnClickListener() {
            String permission = Manifest.permission.CAMERA;
            @Override
            public void onClick(View v) {
                Intent cameraView = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraView, CAMERA_REQUEST_CODE);
            }
        });

        btn_gallery.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent galleryView = new Intent(Intent.ACTION_GET_CONTENT);
                galleryView.setType("image/*");
                startActivityForResult(Intent.createChooser(galleryView
                        , "Select photo from"),GALLERY_REQUEST_CODE);
            }
        });
        //initTensorFlowAndLoadModel();

    }

    private boolean checkPermission(){
        String permission = Manifest.permission.CAMERA;
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),permission) != PackageManager.PERMISSION_GRANTED){
            return false;
        }
        return true;
    }

    private void verifyPermission(){
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                PERMISSION_CAMERA_REQUEST_CODE);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageBitmap = Bitmap.createScaledBitmap(imageBitmap, INPUT_SIZE, INPUT_SIZE, false);
            imageView1.setImageBitmap(imageBitmap);
            //final List<Classifier.Recognition> results = classifier.recognizeImage(imageBitmap);
            //textViewResult1.setText(results.toString());
        }
    }

    /*private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowLite.create(
                            getAssets(),
                            MODEL_PATH,
                            LABEL_PATH,
                            INPUT_SIZE,
                            QUANT);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }*/
}
