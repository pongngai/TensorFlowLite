package guide.borntodev.tensorflowlite;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import android.os.Handler;
import android.net.Uri;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import com.soundcloud.android.crop.Crop;


public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private static final String MODEL_PATH = "mobilenet_v1_1.0_224_quant.tflite";
    private static final boolean QUANT = true;
    private static final String LABEL_PATH = "labels.txt";
    private static final int INPUT_SIZE = 224;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int GALLERY_REQUEST_CODE = 0;
    private static final int CAMERA_REQUEST_CODE = 1;
    private List<String> listPermissionsNeeded = new ArrayList<>();
    private Handler handler = new Handler();
    private Executor executor = Executors.newSingleThreadExecutor();
    private Button btn_captureImage;
    private Button btn_gallery;
    private ImageView imageView1;
    private TextView textViewResult1;
    private Classifier classifier;
    private Uri imageUri;


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
            @Override
            public void onClick(View v) {
                initTensorFlowAndLoadModel();
                //Intent cameraView = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //startActivityForResult(cameraView, CAMERA_REQUEST_CODE);
                //openCameraIntent();
                openCameraIntent();
            }
        });

        btn_gallery.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                initTensorFlowAndLoadModel();
                Intent galleryView = new Intent(Intent.ACTION_PICK);
                galleryView.setType("image/*");
                startActivityForResult(Intent.createChooser(galleryView
                        , "Select photo from"),GALLERY_REQUEST_CODE);
            }
        });
    }

    private boolean checkPermission(){
        String[]permission = {Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,
                              Manifest.permission.ACCESS_FINE_LOCATION};
        //List<String> listPermissionsNeeded = new ArrayList<>();
        for(String perNeed : permission) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), perNeed) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(perNeed);
            }

        }
        if(!listPermissionsNeeded.isEmpty()){
            return false;
        }
        return true;
    }

    private void verifyPermission(){
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_REQUEST_CODE);
    }

    private void openCameraIntent(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        // tell camera where to store the resulting picture
        imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // start camera, and wait for it to finish
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    private void TakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /**
         * return list of classifier
         * name resultsCamera and resultGallery
         */
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
                Uri source_uri = imageUri;
                Uri dest_uri = Uri.fromFile(new File(getCacheDir(), "cropped"));
                Crop.of(source_uri, dest_uri).asSquare().start(MainActivity.this);
        }else if(requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri pickedImage = data.getData();
            Uri dest_uri = Uri.fromFile(new File(getCacheDir(), "cropped"));
            Crop.of(pickedImage, dest_uri).asSquare().start(MainActivity.this);

        } else if(requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK){
            imageUri = Crop.getOutput(data);
            try {
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageBitmap = Bitmap.createScaledBitmap(imageBitmap, INPUT_SIZE, INPUT_SIZE, false);
                imageView1.setImageBitmap(imageBitmap);
                final List<Classifier.Recognition> resultsGallery = classifier.recognizeImage(imageBitmap);
                textViewResult1.setText(resultsGallery.toString());
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        classifier.close();
                    }
                });
                //Intent i = new Intent(MainActivity.this, ShowResult.class);
                //startActivity(i);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initTensorFlowAndLoadModel() {
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
                    //makeButtonCameraVisible();
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }
}
