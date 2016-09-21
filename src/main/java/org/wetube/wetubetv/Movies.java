package org.wetube.wetubetv;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Movies extends Activity {
    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    static List<String> moviesThumbs = new ArrayList<>();
    static List<String> moviesTitles = new ArrayList<>();
    static List<String> moviesLinks = new ArrayList<>();
    static int pageCount = 1;
    static int pageNumber = 1;
    public String words;
    public static String page = "sort=featured";
    public static ProgressDialog pageLoading;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movies);
        StrictMode.setThreadPolicy(policy);


        pageLoading = new ProgressDialog(this);
        pageLoading.setMessage("Loading movies");
        pageLoading.setIndeterminate(true);
        pageLoading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pageLoading.setCancelable(true);


        getMovies();

        TextView PopularButton = (TextView) findViewById(R.id.popularbutton);
        PopularButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageNumber = 1;
                page = "sort=views";
                getMovies();
            }
        });
        TextView RecentButton = (TextView) findViewById(R.id.recentbutton);
        RecentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageNumber = 1;
                page = "sort=release";
                getMovies();
            }
        });
        TextView RatingButton = (TextView) findViewById(R.id.ratingbutton);
        RatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageNumber = 1;
                page = "sort=ratings";
                getMovies();
            }
        });

        final SearchView searchView = (SearchView) findViewById(R.id.searchview);
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.onActionViewExpanded();
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                pageNumber = 1;
                words = query;
                page = "search_keywords="+words;
                getMovies();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    public void getMovies() {

        final LoadingTask LoadingTask = new LoadingTask(Movies.this);
        LoadingTask.execute(page);

    }

    public void listMovies(){
        GridView gridView = (GridView) findViewById(R.id.gridview);
        gridView.setAdapter(new ImageAdapter(this));

        final Button loadMore = (Button) findViewById(R.id.loadmore);
        loadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ++pageNumber;
                getMovies();
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent,
                                    View v, int position, long id) {
                Intent intent = new Intent(getBaseContext(), Movie.class);
                intent.putExtra("link", moviesLinks.get(position));
                intent.putExtra("thumb", moviesThumbs.get(position));
                intent.putExtra("title", moviesTitles.get(position));
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        gridView.setOnScrollListener(new GridView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount == totalItemCount && pageCount > pageNumber) {
                    loadMore.setVisibility(View.VISIBLE);
                } else if (loadMore.getVisibility() != View.GONE) {
                    loadMore.setVisibility(View.GONE);
                }
            }
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }
        });
    }

    public void onBackPressed() {
        Intent intent = new Intent(getBaseContext(), Home.class);
        finish();
        startActivity(intent);
    }

    public class ImageAdapter extends BaseAdapter {
        private Context context;


        public ImageAdapter(Context c) {
            context = c;
        }

        @Override
        public int getCount() {
            return moviesThumbs.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            RecordHolder holder;
            if (row == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(R.layout.gridviewer, parent, false);
                holder = new RecordHolder();
                holder.txtTitle = (TextView) row.findViewById(R.id.item_text);
                holder.imageItem = (ImageView) row.findViewById(R.id.item_image);
                row.setTag(holder);
            } else {
                holder = (RecordHolder) row.getTag();
            }
            holder.txtTitle.setText(moviesTitles.get(position));
            try {
                InputStream is = (InputStream) new URL(moviesThumbs.get(position)).getContent();
                Drawable d = Drawable.createFromStream(is, "src name");
                holder.imageItem.setImageDrawable(d);
            } catch (Exception e) {
                return null;
            }
            return row;
        }

        class RecordHolder {
            TextView txtTitle;
            ImageView imageItem;
        }
    }



    public class LoadingTask extends AsyncTask<String, Integer, String> {
        private Context context;

        public LoadingTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                moviesThumbs.clear();
                moviesTitles.clear();
                moviesLinks.clear();
                Document doc = Jsoup.connect("http://www.letmewatchthis.one/?" + params[0] + "&page=" + pageNumber).get();
                Elements elements = doc.select(".index_item > a");
                Movies.pageCount = doc.select(".paging > a").size();
                for (Element element : elements) {
                    String movieLink = element.attr("href");
                    String movieTitle = element.attr("title");
                    String movieThumb = element.select("img").attr("src");;
                    moviesLinks.add(movieLink);
                    moviesThumbs.add(movieThumb);
                    moviesTitles.add(movieTitle);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pageLoading.show();
        }

        @Override
        protected void onPostExecute(String result) {
            pageLoading.dismiss();
            listMovies();
        }
    }
}