package com.pedronveloso.sndinitdefy;

import android.util.Log;
import org.apache.http.util.ByteArrayBuffer;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: 6/1/11
 * Time: 6:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class DownloadHandler {


    public Boolean DownloadFromUrl(String fileURL, String fileName) {
        try {
            Utils.debugFunc("Will now download from URL: "+fileURL,Log.VERBOSE);
            URL url = new URL(fileURL);//new URL(Constants.ROM_SERVER_URL+ fileURL);
            File file = new File(fileName);

            /* Open a connection to that URL */
            URLConnection ucon = url.openConnection();

            /*
            * Define InputStreams to read from the URLConnection
            */
            InputStream is = ucon.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);

            /*
            * Read bytes to the Buffer until there is nothing more to read(-1)
            */
            ByteArrayBuffer baf = new ByteArrayBuffer(50);
            int current;
            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }

            /* Convert the Bytes read to a String */
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baf.toByteArray());
            fos.close();
            return true;

        } catch (IOException e) {
            Utils.debugFunc("Download error.", Log.ERROR);
            e.printStackTrace();
            return false;
        }

    }

    public static String downloadStringFromURL(String stringURL) {
        try {
            URL url = new URL(stringURL);

            /* Open a connection to that URL */
            URLConnection ucon = url.openConnection();

            /*
            * Define InputStreams to read from the URLConnection
            */
            InputStream is = ucon.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);

            /*
            * Read bytes to the Buffer until there is nothing more to read(-1)
            */
            ByteArrayBuffer baf = new ByteArrayBuffer(50);
            int current;
            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }

            return new String(baf.toByteArray());

        } catch (IOException e) {
            Utils.debugFunc("Download error.", Log.ERROR);
            e.printStackTrace();
        }
        return "";
    }

}

