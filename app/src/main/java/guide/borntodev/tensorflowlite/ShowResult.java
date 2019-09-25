package guide.borntodev.tensorflowlite;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ShowResult extends AppCompatActivity {

    private ImageView imageView2;
    private static final String MODEL_PATH = "model3_quant_false.tflite";
    private static final boolean QUANT = false;
    private static final String LABEL_PATH = "labels.txt";
    private static final int INPUT_SIZE = 224;
    private Handler handler = new Handler();
    private Executor executor = Executors.newSingleThreadExecutor();
    private Button button_path;
    private Button button_result2;
    private Button button_result3;
    private Classifier classifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initTensorFlowAndLoadModel();
        Bitmap bitmap = (Bitmap) this.getIntent().getParcelableExtra("photo");
        setContentView(R.layout.show_result);

        button_path = (Button) findViewById(R.id.button_path);
        button_result2 = (Button)findViewById(R.id.button_result2);
        button_result3 = (Button)findViewById(R.id.button_result3);
        imageView2 = (ImageView) findViewById(R.id.imageView2);
        imageView2.setImageBitmap(bitmap);
        getResultByTF(bitmap);


    }


    private void getResultByTF(Bitmap imageBitmapCamera) {

        if (classifier != null) {
            final List<Classifier.Recognition> resultsCamera = classifier.recognizeImage(imageBitmapCamera);
            button_path.setText(resultsCamera.get(0).toString());
            button_result2.setText(resultsCamera.get(1).toString());
            button_result3.setText(resultsCamera.get(2).toString());


            executor.execute(new Runnable() {
                @Override
                public void run() {
                    classifier.close();
                }
            });

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
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }
}
