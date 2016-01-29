package saltside.com.saltside;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ThreadPoolExecutor;

public class CustomAdapter extends BaseAdapter implements AbsListView.OnScrollListener{

    private final Activity activity;
    private Thread thread;
    private DownloadTask dTask;

    private  ArrayList<DisplayItem> displayItems;
    String [] result;
    Context context;
    int [] imageId;
    private static LayoutInflater inflater=null;
    private int firstVisibleItem,visibleItemCount;
    private ArrayList<AsyncTask> asyncTasks;


    public CustomAdapter(MainActivity mainActivity, ArrayList<DisplayItem> displayItems) {
        this.activity=mainActivity;
        inflater = ( LayoutInflater )activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.displayItems = displayItems;




    }


    public CustomAdapter(MainActivity mainActivity) {

        this.activity=mainActivity;
        inflater = ( LayoutInflater )activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        displayItems = new ArrayList<DisplayItem>();
        asyncTasks = new ArrayList<AsyncTask>();

        dTask = new DownloadTask();
        thread = new Thread(dTask);

        thread.start();

    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }




    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return displayItems.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }



    public class Holder
    {
        TextView tv;
        ImageView img;
        TextView titleView;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub

        View rowView = convertView;
         final Holder holder;
        if(rowView==null){
            rowView = inflater.inflate(R.layout.item, null);
            holder = new Holder();
            holder.tv=(TextView) rowView.findViewById(R.id.textView1);
            holder.img=(ImageView) rowView.findViewById(R.id.imageView1);
            holder.titleView = (TextView)rowView.findViewById(R.id.title);
            rowView.setTag(holder);
        }else{
            holder= (Holder)rowView.getTag();
        }
        final DisplayItem displayItem = displayItems.get(position);
        holder.tv.setText(displayItem.description);
        holder.titleView.setText(displayItem.title);
        displayItem.holder = holder;



     //   holder.img.setImageURI(Uri.parse(displayItems.get(position).image));

        rowView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Toast.makeText(activity, "You Clicked ", Toast.LENGTH_LONG).show();

            }
        });

        return rowView;
    }


    public class DownloadTask implements Runnable {

        @Override
        public void run() {

            URL url;
            HttpURLConnection urlConnection = null;
            String res = "";

            try {
                url = new URL("https://gist.githubusercontent.com/maclir/f715d78b49c3b4b3b77f/raw/8854ab2fe4cbe2a5919cea97d71b714ae5a4838d/items.json");
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader isw = new InputStreamReader(in);

                isw.read(); //removing first character
                int data = isw.read();
                int ctr=0;

                while (data != -1) {
                    char current = (char) data;

                    res += current;
                    if(current=='}'){
                        ctr++;
                        JSONObject jsonObj = new JSONObject(res);
                        DisplayItem displayItem = new DisplayItem();
                        displayItem.title = jsonObj.getString("title");
                        displayItem.imageAddr =  jsonObj.getString("image");
                        displayItem.description = jsonObj.getString("description");
                        displayItems.add(displayItem);
                        // CustomAdapter.this.notifyDataSetChanged();
                        Runnable updateView = new Runnable() {
                            @Override
                            public void run() {
                                CustomAdapter.this.notifyDataSetChanged();
                            }
                        };
                        activity.runOnUiThread(updateView);
                        res="";
                        isw.read(); //removing ','

                    }
                    data = isw.read();
                    if(ctr==10){
                        ctr=0;
                        synchronized (dTask) {
                            dTask.wait();
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {

                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

        }


    }




         class  ImageTask extends AsyncTask <Void,Void,Void> {

             int pos = -1;
             Bitmap bmp = null;

             public ImageTask(int pos) {
                 this.pos = pos;
             }

             @Override
             protected Void doInBackground(Void... params) {
                 try {
                     URL url = new URL(displayItems.get(pos).imageAddr);
                     bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                 } catch (IOException e) {
                     e.printStackTrace();
                 }
                 return null;

             }

             @Override
             protected void onPostExecute(Void aVoid) {
                 super.onPostExecute(aVoid);
                 displayItems.get(pos).holder.img.setImageBitmap(bmp);


             }


         };






    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {


        int i=0,j  =  0;
        int position = 0;


        if(scrollState == SCROLL_STATE_IDLE) {
            synchronized (dTask) {

                dTask.notify();
            }

                while (i<visibleItemCount ){
                     position = firstVisibleItem+i;
                    if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
                        AsyncTask<Void, Void, Void> asyncTask = new ImageTask(position);
                        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                asyncTasks.add(asyncTask);



                    } else {
                        AsyncTask<Void, Void, Void> asyncTask = new ImageTask(position);
                        asyncTask.execute();
                        asyncTasks.add(asyncTask);
                    }
                    Log.i("start",""+ i);
                    i++;
                }


        }
        else {
            j = 0;
            while (j < asyncTasks.size()) {
                asyncTasks.get(j).cancel(true);

                Log.i("stop", "" + j);
                j++;
            }
        }
    }




    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        this.firstVisibleItem = firstVisibleItem;
        this.visibleItemCount=visibleItemCount;



    }

}