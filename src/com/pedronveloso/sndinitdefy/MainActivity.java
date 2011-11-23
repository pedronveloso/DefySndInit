package com.pedronveloso.sndinitdefy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.*;

import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MainActivity extends Activity implements View.OnClickListener
{

    private boolean unzippedAssets = false;

    private static final int OPTION_ABOUT = 1;
    private static final int OPTION_INSTRUCTIONS = 2;
    private static final int OPTION_CHANGELOG = 3;

    ProgressDialog pdiag;

    public boolean onCreateOptionsMenu(Menu menu) {
	    menu.add(0, OPTION_ABOUT, 0,getString(R.string.about_title));
        menu.add(0, OPTION_CHANGELOG, 0, getString(R.string.changelog_title));
        menu.add(0, OPTION_INSTRUCTIONS, 0, getString(R.string.instructions_title));
	    return true;
	}

	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder alertbuilder = new AlertDialog.Builder(MainActivity.this);
	    switch (item.getItemId()) {
	    case OPTION_ABOUT:
            alertbuilder.setTitle(R.string.about_title);
            alertbuilder.setMessage(R.string.about);
            alertbuilder.setPositiveButton(android.R.string.ok,new DialogInterface.OnClickListener(){
               public void onClick(DialogInterface dialog, int whichButton) {
                    //nothing
               }
            });
            alertbuilder.show();
	        return true;
        case OPTION_CHANGELOG:
            alertbuilder.setTitle(R.string.changelog_title);
            alertbuilder.setMessage(R.string.changelog);
            alertbuilder.setPositiveButton(android.R.string.ok,new DialogInterface.OnClickListener(){
               public void onClick(DialogInterface dialog, int whichButton) {
                    //nothing
               }
            });
            alertbuilder.show();
	        return true;
        case OPTION_INSTRUCTIONS:
            alertbuilder.setTitle(R.string.instructions_title);
            alertbuilder.setMessage(R.string.instructions);
            alertbuilder.setPositiveButton(android.R.string.ok,new DialogInterface.OnClickListener(){
               public void onClick(DialogInterface dialog, int whichButton) {
                    //nothing
               }
            });
            alertbuilder.show();
	        return true;
	    }


	    return false;
	}

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button btn = (Button) findViewById(R.id.btn_install_sndinit);
        btn.setOnClickListener(this);

        btn = (Button) findViewById(R.id.btn_uninstall_sndinit);
        btn.setOnClickListener(this);


        PackageManager manager = this.getPackageManager();
        PackageInfo info;
        String result = "";
        try {
            info = manager.getPackageInfo(this.getPackageName(), 0);
            result = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        TextView tv1 = (TextView) findViewById(R.id.tv_version);
        tv1.setText(getString(R.string.version)+" "+result);

        tv1 = (TextView) findViewById(R.id.tv_already_installed);

        if(ShellInterface.isBootmenuInstalled())
        {
            tv1.setText(R.string.sndinstalled);
        }else{
            tv1.setText(R.string.snduninstalled);
        }

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_install_sndinit:
                //if (installRecovery()){
                  //  Toast.makeText(MainActivity.this,"2ndInit Recovery Installed!",Toast.LENGTH_LONG);
                //}
                final Dialog diag = new Dialog(MainActivity.this);
                diag.setContentView(R.layout.bootmenu_options);
                diag.setCancelable(true);
                final Button stableBM = (Button) diag.findViewById(R.id.btn_stable_bootmenu);
                final Button devBM = (Button) diag.findViewById(R.id.btn_dev_bootmenu);
                final Button ownBM = (Button) diag.findViewById(R.id.btn_own_bootmenu);

                stableBM.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        pdiag = new ProgressDialog(MainActivity.this);
                        pdiag.setMessage(getString(R.string.install_after_download));
                        pdiag.show();
                        new DownloadFileTask().execute("http://dl.dropbox.com/u/668793/bootmenu/bootmenu.v0.6.1.tar.gz");
                        diag.dismiss();
                    }
                });

                devBM.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        pdiag = new ProgressDialog(MainActivity.this);
                        pdiag.setMessage(getString(R.string.install_after_download));
                        pdiag.show();
                        new DownloadFileTask().execute("http://dl.dropbox.com/u/668793/bootmenu/bootmenu.1.0.9.tar.gz");
                        diag.dismiss();
                    }
                });


                ownBM.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        final Dialog diag_custom_url = new Dialog(MainActivity.this);
                        diag_custom_url.setContentView(R.layout.custom_url);
                        diag_custom_url.setCancelable(true);
                        diag.dismiss();

                        final Button own_url_btn = (Button) diag_custom_url.findViewById(R.id.btn_custom_url_ok);
                        final EditText own_url_et = (EditText) diag_custom_url.findViewById(R.id.et_custom_url);

                        own_url_btn.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                pdiag = new ProgressDialog(MainActivity.this);
                                pdiag.setMessage(getString(R.string.install_after_download));
                                pdiag.show();
                                new DownloadFileTask().execute(own_url_et.getText().toString());
                                diag.dismiss();
                            }
                        });

                        diag_custom_url.show();

                    }
                });

                diag.show();

                break;

            case R.id.btn_uninstall_sndinit:
                uninstallRecovery();
                break;
        }
    }



    boolean installRecovery(){

        if (Settings.System.getInt(getContentResolver(), Settings.System.ADB_ENABLED, 0)==1)
        {
            if (ShellInterface.isSuAvailable()) {
                //turn on red led
                ShellInterface.turn_on_blue_led(false);
                ShellInterface.turn_on_green_led(false);
                ShellInterface.turn_on_red_led(true);

                unzipAssets();

                String filesDir = getFilesDir().getAbsolutePath();
                String busybox = filesDir + "/busybox";
                String bootmenu = filesDir +"/"+ Constants.DOWNLOADED_BM;//"/bootmenu_v0.8.6.tar";

                 // remount system with RW permissions
                StringBuilder command = new StringBuilder();
                command.append(busybox + " mount -o remount,rw /system ; ");
                command.append(busybox + " cp " + busybox + " /system/xbin/busybox ; ");
                command.append(busybox + " cp " + busybox + " /system/bin/busybox ; ");
                command.append("cd /system/bin ; /system/bin/busybox --install /system/bin ; ");

                command.append(busybox + " mount -o remount,rw rootfs / ; ");
                command.append(busybox + " mkdir /bootmenu ; ");
                command.append(busybox + " cp " + bootmenu + " /bootmenu/"+Constants.DOWNLOADED_BM+" ; ");
                command.append(busybox + " rm -r /system/bootmenu ; ");
                command.append(busybox + " mkdir /system/bootmenu ; ");
                command.append(busybox + " mkdir /system/backups ; ");
                command.append(busybox + " tar xvpf /bootmenu/"+Constants.DOWNLOADED_BM+" -C / ; ");

                command.append(busybox + " chmod 755 /system/bootmenu/binary/* ; ");
                command.append(busybox + " chmod 755 /system/bootmenu/script/* ; ");
                command.append(busybox + " chmod 755 /system/bootmenu/recovery/sbin/* ; ");

                command.append(busybox + " chown -R 0.0 /system/bootmenu/* ; ");

                //do backups
                command.append(busybox + " cp /system/bin/bootmenu /system/backups ; ");
                command.append(busybox + " cp /system/bin/logwrapper /system/backups ; ");
                command.append(busybox + " cp /system/bin/logwrapper.bin /system/backups ; ");

                command.append(busybox + " rm /system/bin/bootmenu ; ");
                command.append(busybox + " rm /system/bin/logwrapper ; ");
                command.append(busybox + " rm /system/bin/logwrapper.bin ; ");
                command.append(busybox + " cp /system/bootmenu/binary/bootmenu /system/bin/ ; ");
                command.append(busybox + " cp /system/bootmenu/binary/logwrapper.bin /system/bin/ ; ");
                command.append(busybox + " ln -s /system/bin/bootmenu /system/bin/logwrapper ; ");
                command.append(busybox + " chmod 755 /system/bin/bootmenu ; ");
                command.append(busybox + " chmod 755 /system/bin/logwrapper.bin ; ");
                //command.append(busybox + " rm -r /bootmenu ; ");
                command.append(busybox + " sync ; ");
                command.append(busybox + " rm /bootmenu/"+Constants.DOWNLOADED_BM+" ; ");




                try{
                    //Log.v("SndInit",command.toString());
                    ShellInterface.runSuCommand(MainActivity.this,command.toString());
                    ShellInterface.turn_on_red_led(false);
                    ShellInterface.turn_on_green_led(true);
                    uiVerifyIfInstalled();

                }catch (Exception e){
                    e.printStackTrace();
                }

                Toast.makeText(MainActivity.this, R.string.success, Toast.LENGTH_LONG).show();

                return true;
            }
            else{
                Toast.makeText(MainActivity.this, R.string.su_not_present, Toast.LENGTH_LONG).show();
                return false;
            }
        }
        else{
            Toast.makeText(MainActivity.this,R.string.enable_adb, Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void uiVerifyIfInstalled(){
        //re-verify if it is installed
        TextView tv1 = (TextView) findViewById(R.id.tv_already_installed);

        if(ShellInterface.isBootmenuInstalled())
        {
            tv1.setText(R.string.sndinstalled);
        }else{
            tv1.setText(R.string.snduninstalled);
        }
    }


    private class DownloadFileTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            String urlToDown = params[0];
            DownloadHandler dh = new DownloadHandler();

            return dh.DownloadFromUrl(urlToDown,getFilesDir().getAbsolutePath()+"/"+Constants.DOWNLOADED_BM);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            try{
                pdiag.dismiss();
            }catch (Exception ignored){}

            if (result) //if download didn't got an error
                installRecovery();
            else
                Toast.makeText(MainActivity.this,R.string.download_error,Toast.LENGTH_LONG).show();
        }
    }


    boolean uninstallRecovery()
    {
        if (!ShellInterface.isBootmenuInstalled()){
            Toast.makeText(MainActivity.this, R.string.sndinit_not_installed_error, Toast.LENGTH_LONG).show();
            return true;
        }
        else
        if (ShellInterface.isSuAvailable()) {
            //turn on red led
            boolean partialUninstall=true;

            ShellInterface.turn_on_blue_led(false);
            ShellInterface.turn_on_green_led(false);
            ShellInterface.turn_on_red_led(true);

            unzipAssets();

            String filesDir = getFilesDir().getAbsolutePath();
            String busybox = filesDir + "/busybox";

             // remount system with RW permissions
            StringBuilder command = new StringBuilder();
            command.append(busybox + " mount -o remount,rw /system ; ");
            command.append(busybox + " mount -o remount,rw rootfs / ; ");

            command.append(busybox + " rm -r /system/bootmenu ; ");

            if (ShellInterface.areBackupFilesPresent())
            {
                partialUninstall=false;
                //command.append(busybox + " cp /system/bin/bootmenu /system/backups ; ");
                command.append(busybox + " cp /system/backups/logwrapper /system/bin ; ");
                command.append(busybox + " cp /system/backups/logwrapper.bin /system/bin ; ");
                command.append(busybox + " chmod 755 /system/bin/bootmenu ; ");
                command.append(busybox + " chmod 755 /system/bin/logwrapper.bin ; ");
            }
            //command.append(busybox + " rm /system/bin/bootmenu ; ");
            command.append(busybox + " sync ; ");

            try{
                ShellInterface.runSuCommand(MainActivity.this,command.toString());
                ShellInterface.turn_on_red_led(false);
                ShellInterface.turn_on_green_led(true);

                uiVerifyIfInstalled();
            }catch (Exception e){
                e.printStackTrace();
            }

            if (partialUninstall==false)
                Toast.makeText(MainActivity.this, R.string.snd_uninstalled_success, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(MainActivity.this, R.string.snd_uninstalled_success_partial, Toast.LENGTH_LONG).show();
            return true;
        }
        else{
            Toast.makeText(MainActivity.this, R.string.su_not_present, Toast.LENGTH_LONG).show();
            return false;
        }
    }





    void unzipAssets() {
        if (!unzippedAssets)
        {
            String apkPath = getPackageCodePath();
            String mAppRoot = getFilesDir().toString();
            try {
                File zipFile = new File(apkPath);
                long zipLastModified = zipFile.lastModified();
                ZipFile zip = new ZipFile(apkPath);
                Vector<ZipEntry> files = getAssets(zip);
                int zipFilterLength = ZIP_FILTER.length();

                Enumeration<?> entries = files.elements();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = (ZipEntry) entries.nextElement();
                    String path = entry.getName().substring(zipFilterLength);
                    File outputFile = new File(mAppRoot, path);
                    outputFile.getParentFile().mkdirs();

                    if (outputFile.exists() && entry.getSize() == outputFile.length() && zipLastModified < outputFile.lastModified())
                        continue;
                    FileOutputStream fos = new FileOutputStream(outputFile);
                    copyStreams(zip.getInputStream(entry), fos);
                    Runtime.getRuntime().exec("chmod 777 " + outputFile.getAbsolutePath());
                }
                unzippedAssets=true;
            } catch (IOException e) {
                Utils.debugFunc("Error: " + e.getMessage(), Log.ERROR);
                unzippedAssets=false;
                e.printStackTrace();
            }
        }
    }

    final static String ZIP_FILTER = "assets";
    static final int BUFSIZE = 5192;

    void copyStreams(InputStream is, FileOutputStream fos) {
        BufferedOutputStream os = null;
        try {
            byte data[] = new byte[BUFSIZE];
            int count;
            os = new BufferedOutputStream(fos, BUFSIZE);
            while ((count = is.read(data, 0, BUFSIZE)) != -1) {
                os.write(data, 0, count);
            }
            os.flush();
        } catch (IOException e) {
            Utils.debugFunc("Exception while copying: " + e, Log.ERROR);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e2) {
                Utils.debugFunc("Exception while closing the stream: " + e2, Log.ERROR);
            }
        }
    }

    public Vector<ZipEntry> getAssets(ZipFile zip) {
        Vector<ZipEntry> list = new Vector<ZipEntry>();
        Enumeration<?> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            if (entry.getName().startsWith(ZIP_FILTER)) {
                list.add(entry);
            }
        }
        return list;
    }



}
