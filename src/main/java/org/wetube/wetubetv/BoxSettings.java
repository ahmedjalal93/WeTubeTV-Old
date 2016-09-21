package org.wetube.wetubetv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by ajalal on 2/23/16.
 */

public class BoxSettings {
    public static String BoxVersion;
    public static String APIKEY;

    public static void CheckSettings() {
        try {
            URL url = new URL("http://www.wetube.org/tv/settings.php?mac=" + Home.macAddress);
            System.out.println(url);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String line = "";
            while ((line = in.readLine()) != null) {
                String mySettings[] = line.split(",");
                BoxVersion = mySettings[0];
                APIKEY = mySettings[1];
            }
        } catch (Exception e) {
            System.out.println("Check settings went wrong");
        }
    }

    public static boolean CheckNetwork() {
        try {
            if (Runtime.getRuntime().exec("/system/bin/ping -c 1 8.8.8.8").waitFor() == 0) {
                return true;
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e2) {
            e2.printStackTrace();
            return false;
        }
    }
}