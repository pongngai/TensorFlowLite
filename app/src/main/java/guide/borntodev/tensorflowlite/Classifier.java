package guide.borntodev.tensorflowlite;

import android.graphics.Bitmap;

import java.util.List;

public interface Classifier {

    class Recognition{

        private String title;

        private boolean quant;
        /**
         *logic for processing the camera input contains floating point and quantized models
         */
        private Float confidence;

        public Recognition() {

        }

        public Recognition(String title,boolean quant,Float confidence){
            this.title = title;
            this.quant = quant;
            this.confidence = confidence;
        }

        public String getTitle(){
            return title;
        }

        public Float getConfidence(){
            return confidence;
        }

        @Override
        public String toString() {
            String resultString = "";

            if (title != null) {
                resultString += title + " ";
            }

            if (confidence != null) {
                resultString += String.format("(%.1f%%) ", confidence * 100.0f);
            }

            return resultString.trim();
        }
    }

    List<Recognition> recognizeImage(Bitmap bitmap);
    void close();

}
