package org.wetube.wetubetv;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Movie extends Activity {
    public ProgressDialog movieLoading;
    private Bitmap mIcon_val;
    public String movieLink;
    public String movieThumb;
    public String movieTitle;
    public String movieDetails;
    public String movieRating;
    public String movieRelease;
    public String movieRuntime;
    public String movieGenres;
    public String movieDirector;
    public String movieActors;
    public static List<String> movieLinks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie);
        movieLoading = new ProgressDialog(this);
        movieLoading.setMessage("Loading movie");
        movieLoading.setIndeterminate(true);
        movieLoading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        movieLoading.setCancelable(true);
        movieLink = getIntent().getStringExtra("link");
        movieThumb = getIntent().getStringExtra("thumb");
        movieTitle = getIntent().getStringExtra("title");
        MediaGrabber.ChannelTitle = movieTitle;

        final LoadingTask LoadingTask = new LoadingTask(Movie.this);
        LoadingTask.execute(movieLink);
    }

    public void showMovie() {
        TextView textTitle = (TextView) findViewById(R.id.movietitle);
        textTitle.setText(movieTitle);
        TextView textDetails = (TextView) findViewById(R.id.moviedetails);
        textDetails.setText(movieDetails);
        TextView textRating = (TextView) findViewById(R.id.movierating);
        textRating.setText("Rating " + movieRating);
        TextView textRelease = (TextView) findViewById(R.id.movierelease);
        textRelease.setText(Html.fromHtml("<strong>Release: </strong>" + movieRelease));
        TextView textRuntime = (TextView) findViewById(R.id.movieruntime);
        textRuntime.setText(Html.fromHtml("<strong>Runtime: </strong>" + movieRuntime));
        TextView textGenres = (TextView) findViewById(R.id.moviegenres);
        textGenres.setText(Html.fromHtml("<strong>Genres: </strong>" + movieGenres));
        TextView textDirector = (TextView) findViewById(R.id.moviedirector);
        textDirector.setText(Html.fromHtml("<strong>Director: </strong>" + movieDirector));
        TextView textActors = (TextView) findViewById(R.id.movieactors);
        textActors.setText(Html.fromHtml("<strong>Actors: </strong>" + movieActors));
        ImageView thumbnail = (ImageView) findViewById(R.id.moviethumb);
        try {
            URL newurl = new URL(movieThumb);
            mIcon_val = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
            thumbnail.setImageBitmap(mIcon_val);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (movieLinks.size() > 0) {
            Button buttonPlay = (Button) findViewById(R.id.movieplay);
            buttonPlay.setVisibility(View.VISIBLE);
            buttonPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (movieLinks.size() < 1) {
                        Toast.makeText(getApplicationContext(), "No media file found, sorry :(", Toast.LENGTH_SHORT).show();
                    } else {
                        MediaGrabber.MediaFile = movieLinks.get(0);
                        MediaGrabber.MediaType = "org.wetube.wetubetv.Movies";
                        Intent intent = new Intent(getBaseContext(), PlayerActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                }
            });
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
                Document doc = Jsoup.connect("http://www.letmewatchthis.one/" + movieLink).get();
                Elements elements = doc.select(".index_container");
                movieDetails = elements.select(".movie_info > table > tbody > tr:eq(0) > td:eq(0) > p").text();
                String movieRatings[] = elements.select(".current-rating").text().replace("Currently ", "").split("/");
                movieRating = Double.parseDouble(movieRatings[0]) / (double) 20 + " / 5";
                movieRelease = elements.select(".movie_info > table > tbody > tr:eq(1) > td:eq(1)").text();
                movieRuntime = elements.select(".movie_info > table > tbody > tr:eq(2) > td:eq(1)").text();
                movieGenres = elements.select(".movie_info > table > tbody > tr:eq(3) > td:eq(1)").text();
                movieDirector = elements.select(".movie_info > table > tbody > tr:eq(5) > td:eq(1)").text();
                movieActors = elements.select(".movie_info > table > tbody > tr:eq(6) > td:eq(1)").text();
                Elements elements2 = doc.select(".movie_version");
                movieLinks.clear();
                for (Element element : elements2) {
                    if (movieLinks.size() < 1) {
                        String movieHost = element.select("tbody > tr > td:eq(2) > span").text();
                        String movieLink = element.select(".movie_version > tbody > tr > td:eq(1) > span > a").attr("href");
                        String hacklink = MediaGrabber.getLink("http://www.letmewatchthis.pl" + movieLink, movieHost);
                        String hack_source = MediaGrabber.getSource(hacklink, movieHost);
                        if (hack_source != null) {
                            System.out.println(hack_source);
                            movieLinks.add(hack_source);
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            movieLoading.show();
        }

        @Override
        protected void onPostExecute(String result) {
            movieLoading.dismiss();
            showMovie();
        }
    }
}
