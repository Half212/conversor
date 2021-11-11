package com.example.conversor;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.netcompss.ffmpeg4android.GeneralUtils;
import com.netcompss.ffmpeg4android.Prefs;

public class FirstScreen extends AppCompatActivity {

    protected void onResume() {
        super.onResume();
        Log.i(Prefs.TAG, "Main on resume handling log copy in case of a crash");
        String demoVideoFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/videokit/";
        String workFolder = getApplicationContext().getFilesDir() + "/";
        String vkLogPath = workFolder + "vk.log";
        GeneralUtils.copyFileToFolder(vkLogPath, demoVideoFolder);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GeneralUtils.checkForPermissionsMAndAbove(FirstScreen.this, false);

        Button simpleAct =  (Button)findViewById(R.id.btnExecutar);
        simpleAct.setOnClickListener(v -> {
            Log.i(Prefs.TAG, "run simpleAct.");
            startAct();
        });
    }

    public static String getAboutText(Context ctx) {
        return "\n" + ctx.getString(R.string.full_app_name) + "\n" +
                "Developed by: NetComps LTD\n" +
                "Version: " + GeneralUtils.getVersionName(ctx) + "\n" +
                "Support: " + ctx.getString(R.string.email) + "\n";
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("About").
                    setMessage(getAboutText(getApplicationContext())).
                    setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked OK button
                        }
                    });

            AlertDialog about = builder.create();
            about.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startAct() {
        Intent intent = new Intent(this, SecondScreen.class);
        Log.d(Prefs.TAG, "Starting act:" + SecondScreen.class);
        this.startActivity(intent);
    }
}