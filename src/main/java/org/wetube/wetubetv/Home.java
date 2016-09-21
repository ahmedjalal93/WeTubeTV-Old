package org.wetube.wetubetv;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Home extends Activity {
    public static String macAddress;
    public static ProgressDialog DownloadProgress;
    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    Integer[] imageIDs = {
            R.drawable.home_icon,
            R.drawable.movie_icon,
            R.drawable.television_icon,
            R.drawable.settings_icon
    };

    public static final String[] myApps = {
            "org.wetube.wetubetv.Home",
            "org.wetube.wetubetv.Movies",
            "org.wetube.wetubetv.Channels",
            "android.provider.Settings.ACTION_SETTINGS"
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        StrictMode.setThreadPolicy(policy);
        LinearLayout noConnection = (LinearLayout) findViewById(R.id.noConnection);
        Button RetryButton = (Button) findViewById(R.id.retry);
        Button WifiButton = (Button) findViewById(R.id.wifisettins);
        WifiManager wifimanager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        macAddress = wifimanager.getConnectionInfo().getMacAddress();
        if (BoxSettings.CheckNetwork()) {
            BoxSettings.CheckSettings();
            CheckUpdate();
            noConnection.setVisibility(TextView.GONE);

            GridView gridView = (GridView) findViewById(R.id.gridview);
            gridView.setAdapter(new ImageAdapter(this));

            gridView.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    try {
                        if (myApps[position] == "android.provider.Settings.ACTION_SETTINGS") {
                            startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                        } else {
                            Intent intent = null;
                            intent = new Intent(getBaseContext(), Class.forName(myApps[position]));
                            finish();
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(intent);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            noConnection.setVisibility(TextView.VISIBLE);
            RetryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getBaseContext(), Home.class);
                    finish();
                    startActivity(intent);
                }
            });
            WifiButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                    wifi.setWifiEnabled(true);
                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
            });
        }
    }

    public void CheckUpdate() {

        if (!BuildConfig.VERSION_NAME.equals(BoxSettings.BoxVersion)) {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("New updates found")
                    .setMessage("Do you want to update?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final DownloadTask downloadTask = new DownloadTask(Home.this);
                            downloadTask.execute("http://www.wetube.org/tv/app.apk");
                        }

                    })
                    .setNegativeButton("No", null)
                    .show();
        } else {
            System.out.println("Latest build");
        }
    }

    public static class DownloadTask extends AsyncTask<String, Integer, String> {
        private Context context;

        public DownloadTask(Context context) {
            this.context = context;
            DownloadProgress = new ProgressDialog(context);
            DownloadProgress.setMessage("Downloading updates");
            DownloadProgress.setIndeterminate(true);
            DownloadProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            DownloadProgress.setCancelable(true);
        }

        @Override
        protected String doInBackground(String... sUrl) {
            try {
                URL url = new URL(sUrl[0]);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.setDoOutput(true);
                c.setRequestProperty("Accept-Encoding", "identity");
                c.connect();
                int fileLength = c.getContentLength();

                String PATH = Environment.getExternalStorageDirectory() + "/download/";
                File file = new File(PATH);
                file.mkdirs();
                File outputFile = new File(file, "app.apk");
                FileOutputStream fos = new FileOutputStream(outputFile);

                InputStream is = c.getInputStream();

                byte[] buffer = new byte[1024];
                int len1 = 0;
                long total = 0;
                while ((len1 = is.read(buffer)) != -1) {
                    total += len1;
                    publishProgress((int) (total * 100 / fileLength));
                    fos.write(buffer, 0, len1);
                }
                fos.close();
                is.close();

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() +
                        "/download/" + "app.apk")), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                System.out.println("Started");

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DownloadProgress.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            DownloadProgress.setIndeterminate(false);
            DownloadProgress.setMax(100);
            DownloadProgress.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            DownloadProgress.dismiss();
            if (result != null)
                Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show();
        }
    }

    public class ImageAdapter extends BaseAdapter {
        private Context context;

        public ImageAdapter(Context c) {
            context = c;
        }

        public int getCount() {
            return imageIDs.length;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(context);
                imageView.setLayoutParams(new GridView.LayoutParams(185, 185));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(5, 5, 5, 5);
            } else {
                imageView = (ImageView) convertView;
            }
            imageView.setImageResource(imageIDs[position]);
            return imageView;
        }
    }
}