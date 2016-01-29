package saltside.com.saltside;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class MainActivity extends Activity implements AbsListView.OnScrollListener{

    private ArrayList<DisplayItem> displayItems;
    private Thread thread;
    private    DownloadTask dTask;
     CustomAdapter adapter;
    private int firstVisibleItem,visibleItemCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GridView gridView = (GridView) findViewById(R.id.grid_view);
          displayItems = new ArrayList<DisplayItem>();
         adapter  = new CustomAdapter(MainActivity.this, displayItems);
         gridView.setAdapter(adapter);
        gridView.setOnScrollListener(this);
        dTask = new DownloadTask();
        thread = new Thread(dTask);

            thread.start();


        final AsyncTask<Void, Void, Void> httpTask = new AsyncTask<Void, Void, Void>() {



            @Override
            protected Void doInBackground(Void... params) {
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
                            Runnable updateView = new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            };
                            runOnUiThread(updateView);
                            res="";
                            isw.read(); //removing ','

                        }
                        data = isw.read();
                        if(ctr==10){
                            ctr=0;
                            break;
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {

                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            return null;
            }
        };

       /* if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
            httpTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            httpTask.execute();
        }*/

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
                                adapter.notifyDataSetChanged();
                            }
                        };
                        runOnUiThread(updateView);
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

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

        if(scrollState == SCROLL_STATE_IDLE) {
            synchronized (dTask) {

                dTask.notify();
            }

        }
    }




    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        this.firstVisibleItem = firstVisibleItem;
        this.visibleItemCount=visibleItemCount;
    }

}
