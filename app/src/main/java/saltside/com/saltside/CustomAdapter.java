package saltside.com.saltside;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
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

public class CustomAdapter extends BaseAdapter {

    private final Activity activity;


    private  ArrayList<DisplayItem> displayItems;
    String [] result;
    Context context;
    int [] imageId;
    private static LayoutInflater inflater=null;


    public CustomAdapter(MainActivity mainActivity, ArrayList<DisplayItem> displayItems) {
        this.activity=mainActivity;
        inflater = ( LayoutInflater )activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.displayItems = displayItems;




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

        AsyncTask<Void,Void,Void> imageTask = new AsyncTask<Void, Void, Void>() {
            Bitmap bmp = null;

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    URL url = new URL(displayItem.imageAddr);
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

        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
            imageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

          //  imageTask.execute();
        } else {
            imageTask.execute();
        }
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




}