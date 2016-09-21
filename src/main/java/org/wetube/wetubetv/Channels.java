package org.wetube.wetubetv;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class Channels extends Activity {
    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    private ListView ChannelsList;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channels);
        StrictMode.setThreadPolicy(policy);
        ChannelsList = (ListView) findViewById(R.id.listView);
        try {
            URL url = new URL("http://ahmed.wetube.org/channels.php?apikey=" + BoxSettings.APIKEY + "&mac="+Home.macAddress);
            System.out.println("url:"+url);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String line = "";
            ArrayList<String> items = new ArrayList<String>();
            while ((line = in.readLine()) != null) {
                String myArray[] = line.split(",");
                items.add(myArray[1]);
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
            ChannelsList.setAdapter(arrayAdapter);
            ChannelsList.setOnItemClickListener(new ListClickHandler());
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onBackPressed() {
        Intent intent = new Intent(getBaseContext(), Home.class);
        finish();
        startActivity(intent);
    }

    public class ListClickHandler implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
            MediaGrabber.ChannelId = position;
            MediaGrabber.MediaFile = "http://ahmed.wetube.org/channels.php?apikey=" + BoxSettings.APIKEY + "&mac="+Home.macAddress;
            MediaGrabber.MediaType = "org.wetube.wetubetv.Channels";
            Intent intent = new Intent(getBaseContext(), PlayerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long arg3) {
            System.out.println("long");
            return true;
        }
    }
}

