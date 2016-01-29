package saltside.com.saltside;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;

import java.io.IOException;
import java.net.URL;

/**
 * Created by lenovo on 27-Jan-16.
 */
public class DisplayItem {
    public String title;
    public String imageAddr;
    public String description;
    public CustomAdapter.Holder holder;
    private ImageTask imageTask ;


    public void startDownloadingImage(){


            imageTask = new ImageTask();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                imageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

             }       else {
             imageTask.execute();

    }
}



    public void stopDownladingImage(){
        if(imageTask!=null){
            imageTask.cancel(true);
        }
    }



    class  ImageTask extends AsyncTask<Void,Void,Void> {


        Bitmap bmp = null;



        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL(imageAddr);
                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            holder.img.setImageBitmap(bmp);


        }


    };

}
