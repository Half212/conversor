package com.example.conversor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.netcompss.ffmpeg4android.CommandValidationException;
import com.netcompss.ffmpeg4android.GeneralUtils;
import com.netcompss.ffmpeg4android.Prefs;
import com.netcompss.loader.LoadJNI;


public class SecondScreen extends AppCompatActivity {

    String workFolder = null;
    String demoVideoFolder = null;
    String demoVideoPath = null;
    String vkLogPath = null;
    private boolean commandValidationFailedFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_screen);

            demoVideoFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/videokit/";
            demoVideoPath = demoVideoFolder + "in.mp4";

            Log.i(Prefs.TAG, getString(R.string.app_name) + " version: " + GeneralUtils.getVersionName(getApplicationContext()) );
            workFolder = getApplicationContext().getFilesDir().getAbsolutePath() + "/";
            //Log.i(Prefs.TAG, "workFolder: " + workFolder);
            vkLogPath = workFolder + "vk.log";

            GeneralUtils.copyLicenseFromAssetsToSDIfNeeded(this, workFolder);
            GeneralUtils.copyDemoVideoFromAssetsToSDIfNeeded(this, demoVideoFolder);

            Button invoke =  (Button)findViewById(R.id.invokeButton);
            invoke.setOnClickListener(v -> {
                Log.i(Prefs.TAG, "run clicked.");
                if (GeneralUtils.checkIfFileExistAndNotEmpty(demoVideoPath)) {
                    new TranscdingBackground(SecondScreen.this).execute();
                }
                else {
                    Toast.makeText(getApplicationContext(), demoVideoPath + " not found", Toast.LENGTH_LONG).show();
                }
            });

            int rc = GeneralUtils.isLicenseValid(getApplicationContext(), workFolder);
            Log.i(Prefs.TAG, "License check RC: " + rc);
        }

        public class TranscdingBackground extends AsyncTask<String, Integer, Integer>
        {

            ProgressDialog progressDialog;
            Activity _act;
            String commandStr;

            public TranscdingBackground (Activity act) {
                _act = act;
            }



            @Override
            protected void onPreExecute() {
                EditText commandText = (EditText)findViewById(R.id.cmdText);
                commandStr = commandText.getText().toString();

                progressDialog = new ProgressDialog(_act);
                progressDialog.setMessage("FFmpeg4Android Transcoding in progress...");
                progressDialog.show();

            }

            protected Integer doInBackground(String... paths) {
                Log.i(Prefs.TAG, "doInBackground started...");

                // delete previous log
                boolean isDeleted = GeneralUtils.deleteFileUtil(workFolder + "/vk.log");
                Log.i(Prefs.TAG, "vk deleted: " + isDeleted);

                PowerManager powerManager = (PowerManager)_act.getSystemService(Activity.POWER_SERVICE);
                @SuppressLint("InvalidWakeLockTag") WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "VK_LOCK");
                Log.d(Prefs.TAG, "Acquire wake lock");
                wakeLock.acquire();

                ///////////// Set Command using code (overriding the UI EditText) /////
                //commandStr = "ffmpeg -y -i /sdcard/videokit/in.mp4 -strict experimental -s 320x240 -r 30 -aspect 3:4 -ab 48000 -ac 2 -ar 22050 -vcodec mpeg4 -b 2097152 /sdcard/videokit/out.mp4";
                //String[] complexCommand = {"ffmpeg", "-y" ,"-i", "/sdcard/videokit/in.mp4","-strict","experimental","-s", "160x120","-r","25", "-vcodec", "mpeg4", "-b", "150k", "-ab","48000", "-ac", "2", "-ar", "22050", "/sdcard/videokit/out.mp4"};
                ///////////////////////////////////////////////////////////////////////


                LoadJNI vk = new LoadJNI();
                try {

                    // complex command
                    //vk.run(complexCommand, workFolder, getApplicationContext());

                    vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, getApplicationContext());

                    // running without command validation
                    //vk.run(complexCommand, workFolder, getApplicationContext(), false);

                    // copying vk.log (internal native log) to the videokit folder
                    GeneralUtils.copyFileToFolder(vkLogPath, demoVideoFolder);

                } catch (CommandValidationException e) {
                    Log.e(Prefs.TAG, "vk run exeption.", e);
                    commandValidationFailedFlag = true;

                } catch (Throwable e) {
                    Log.e(Prefs.TAG, "vk run exeption.", e);
                }
                finally {
                    if (wakeLock.isHeld())
                        wakeLock.release();
                    else{
                        Log.i(Prefs.TAG, "Wake lock is already released, doing nothing");
                    }
                }
                Log.i(Prefs.TAG, "doInBackground finished");
                return Integer.valueOf(0);
            }

            protected void onProgressUpdate(Integer... progress) {
            }

            @Override
            protected void onCancelled() {
                Log.i(Prefs.TAG, "onCancelled");
                //progressDialog.dismiss();
                super.onCancelled();
            }


            @Override
            protected void onPostExecute(Integer result) {
                Log.i(Prefs.TAG, "onPostExecute");
                progressDialog.dismiss();
                super.onPostExecute(result);

                // finished Toast
                String rc = null;
                if (commandValidationFailedFlag) {
                    rc = "Command Vaidation Failed";
                }
                else {
                    rc = GeneralUtils.getReturnCodeFromLog(vkLogPath);
                }
                final String status = rc;
                SecondScreen.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(SecondScreen.this, status, Toast.LENGTH_LONG).show();
                        if (status.equals("Transcoding Status: Failed")) {
                            Toast.makeText(SecondScreen.this, "Check: " + vkLogPath + " for more information.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
            public void passavalor(){
                EditText commandText = (EditText)findViewById(R.id.cmdText);
                commandStr = commandText.getText().toString();

            }
        }


    }

