package org.wetube.wetubetv;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Created by ajalal on 2/23/16.
 */

public class MediaGrabber {

    public static int ChannelId = 0;
    public static String ChannelTitle;
    public static String ChannelPath;
    public static int ChannelsCount;
    public static String MediaFile;
    public static String MediaType;
    public static String MediaExtention;
    static StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

    public static void grabChannel() {
        StrictMode.setThreadPolicy(policy);
        System.out.println(MediaFile);
        if (MediaGrabber.MediaType == "org.wetube.wetubetv.Channels") {
            try {
                URL url = new URL(MediaFile);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String line = "";
                ArrayList<String> items = new ArrayList<String>();
                while ((line = in.readLine()) != null) {
                    items.add(line);
                }
                in.close();
                System.out.println("channelid:"+ChannelId);
                String[] myArray = items.get(ChannelId).split(",");
                ChannelsCount = items.size();
                ChannelId = Integer.parseInt(myArray[0]);
                ChannelTitle = myArray[1];
                ChannelPath = myArray[2];
                if (myArray[3] != null) {
                    MediaExtention = myArray[3];
                    System.out.println(MediaExtention);
                }
                System.out.println("channel"+ChannelPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            MediaGrabber.ChannelPath = MediaGrabber.MediaFile;
        }
    }

    private static void sendPOST(String POST_URL, String POST_PARAMS) throws IOException {
        URL obj = new URL(POST_URL);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");

        // For POST only - START
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        os.write(POST_PARAMS.getBytes());
        os.flush();
        os.close();
        // For POST only - END

        int responseCode = con.getResponseCode();
        System.out.println("POST Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) { //success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
            String fine = response.toString().substring(response.toString().indexOf("|video|") );
            System.out.println(fine);
        } else {
            System.out.println("POST request not worked");
        }
    }


    public static String getSource(String urls, String host) {
        HttpURLConnection urlConnection = null;
        String fine2 = null;
        System.out.println(urls + "   " + host);
        switch (host){
            case "vidzi.tv":
                try {
                    URL url = new URL(urls);
                    URLConnection yc = url.openConnection();
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            yc.getInputStream(), "UTF-8"));
                    String inputLine;
                    StringBuilder a = new StringBuilder();
                    while ((inputLine = in.readLine()) != null)
                        a.append(inputLine);
                    in.close();
                    String fine = a.toString().substring(a.toString().indexOf("|position|") + 10);
                    if(a.toString().indexOf("|position|") > 0) {
                        System.out.println("fine" + fine);
                        fine2 = fine.substring(0, fine.indexOf("|"));
                        System.out.println(fine + "    " + fine2);
                        fine2 = urls.replace(".html", "-") + fine2 + ".m3u8";
                    }else{
                        fine2 = null;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
                break;

            case "thevideo.me":
                try {
                    URL url = new URL("https://thevideo.me/embed-"+urls+".html");
                    System.out.println(url);
                    URLConnection yc = url.openConnection();
                    yc.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            yc.getInputStream(), "UTF-8"));
                    String inputLine;
                    StringBuilder a = new StringBuilder();
                    while ((inputLine = in.readLine()) != null)
                        a.append(inputLine);
                    in.close();
                    if(a.toString().indexOf("360p") > 0) {
                        String fine = a.toString().substring(a.toString().indexOf("240p")+16);
                        System.out.println("fine111111"+fine);
                        Document doc = Jsoup.connect("https://thevideo.me/dljsv/" + urls).get();
                        String doc1 = doc.toString().substring(0, doc.toString().indexOf("|role|"));
                        doc1 = doc1.substring(doc1.indexOf("|each|")+6);
                        fine2 = fine.substring(0,fine.indexOf("\""))+"?direct=false&ua=1&vt="+doc1;
                        System.out.println("fine222222"+fine2);

                        System.out.println(doc1);
                        if (!fine2.contains("http")) {
                            fine2 = null;
                        }
                    }else{
                        fine2 = null;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
                break;
            case "gorillavid.in":
                try {
                    URL url = new URL(urls);
                    URLConnection yc = url.openConnection();
                    yc.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            yc.getInputStream(), "UTF-8"));
                    String inputLine;
                    StringBuilder a = new StringBuilder();
                    while ((inputLine = in.readLine()) != null)
                        a.append(inputLine);
                    in.close();
                    if(a.toString().indexOf("file: \"") > 0) {
                        String fine = a.toString().substring(a.toString().indexOf("file: \"") + 7);
                        System.out.println(fine + "    " + fine2);
                        fine2 = fine.substring(0, fine.indexOf("\""));
                        if (!fine2.contains("http")) {
                            fine2 = null;
                        }
                    }else{
                        fine2 = null;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
                break;
            case "streamin.to":
                try {
                    URL url = new URL(urls);
                    HttpURLConnection yc = (HttpURLConnection) url.openConnection();
                    yc.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            yc.getInputStream(), "UTF-8"));
                    String inputLine;
                    StringBuilder a = new StringBuilder();
                    while ((inputLine = in.readLine()) != null)
                        a.append(inputLine);
                    in.close();
                    if (a.toString().indexOf("file:'") > 0) {
                        String fine = a.toString().substring(a.toString().indexOf("file:'")+6);
                        System.out.println("fine" + fine);
                        fine2 = fine.substring(0, fine.indexOf("'"));
                        System.out.println(fine + "    " + fine2);
                    } else {
                        fine2 = null;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
                break;
            case "vidtodo.com":
                try {
                    URL url = new URL(urls);
                    HttpURLConnection yc = (HttpURLConnection) url.openConnection();
                    yc.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            yc.getInputStream(), "UTF-8"));
                    String inputLine;
                    StringBuilder a = new StringBuilder();
                    while ((inputLine = in.readLine()) != null)
                        a.append(inputLine);
                    in.close();
                    if (a.toString().indexOf(",{file: \"") > 0) {
                        String fine = a.toString().substring(a.toString().indexOf(",{file: \"")+9);
                        System.out.println("fine" + fine);
                        fine2 = fine.substring(0, fine.indexOf("\""));
                        System.out.println(fine + "    " + fine2);
                    } else {
                        fine2 = null;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
                break;

            case "divxme.com":
                try {
                    sendPOST(urls, "op=download1&id=d8vllye0vam0&method_free=method_free");

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
                break;
            default:
                fine2 = null;
                break;

        }
        return fine2;
    }

    public static String getLink(String url, String host) {
        URLConnection con = null;
        String finallink = null;

        System.out.println(url + "   " + host);
        switch (host) {
            case "vidzi.tv":
                try {
                    con = new URL(url).openConnection();
                    con.connect();
                    InputStream is = con.getInputStream();
                    finallink = String.valueOf(con.getURL());
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "thevideo.me":
                try {
                    con = new URL(url).openConnection();
                    con.connect();
                    InputStream is = con.getInputStream();
                    String correctLink = String.valueOf(con.getURL()).replace("https://thevideo.me/", "");
                    finallink = correctLink;

                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(finallink);
                break;
            case "gorillavid.in":
                try {
                    con = new URL(url).openConnection();
                    con.connect();
                    InputStream is = con.getInputStream();
                    String correctLink = String.valueOf(con.getURL()).replace("http://gorillavid.in/", "");
                    finallink = "http://gorillavid.in/embed-" + correctLink + ".html";

                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(finallink);
                break;
            case "streamin.to":
                try {
                    con = new URL(url).openConnection();
                    con.connect();
                    InputStream is = con.getInputStream();
                    String correctLink = String.valueOf(con.getURL()).replace("http://streamin.to/", "");
                    finallink = "http://streamin.to/embed-" + correctLink + ".html";
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(finallink);
                break;
            case "vidtodo.com":
                try {
                    con = new URL(url).openConnection();
                    con.connect();
                    InputStream is = con.getInputStream();
                    String correctLink = String.valueOf(con.getURL()).replace("http://vidtodo.com/", "");
                    finallink = "http://vidtodo.com/embed-" + correctLink + ".html";
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(finallink);
                break;
            case "divxme.com":
                try {
                    con = new URL(url).openConnection();
                    con.connect();
                    InputStream is = con.getInputStream();
                    String correctLink = String.valueOf(con.getURL()).replace("http://vidtodo.com/", "");
                    finallink = correctLink;
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(finallink);
                break;
            case "openload.co":
                try {
                    con = new URL(url).openConnection();
                    con.connect();
                    InputStream is = con.getInputStream();
                    String correctLink = String.valueOf(con.getURL());
                    finallink = correctLink;
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(finallink);





            default:
                return null;
        }
        return finallink;
    }
}