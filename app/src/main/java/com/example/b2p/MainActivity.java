package com.example.b2p;
import android.content.pm.ApplicationInfo;
import android.view.View;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {
    private Context mContext;
    private Activity mActivity;

    private Button Textbutton;
    private TextView mTextView;
    private TextView lTextView;

    int count=0;
    String tempstr="";
    PackageManager packageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void scrollButton(View view){
        
        mContext = getApplicationContext();
        mActivity = MainActivity.this;
    
        Textbutton = findViewById(R.id.button);
        mTextView = findViewById(R.id.text_view);
        lTextView=findViewById(R.id.textView);
        Textbutton.setVisibility(view.GONE);
        lTextView.setVisibility(view.GONE);
        mTextView.setVisibility(view.VISIBLE);
        mTextView.setMovementMethod(new ScrollingMovementMethod());

        List<String> installedPackageNames = new ArrayList<String>();
        List<String> name = new ArrayList<String>();
        PackageManager packageManager= getApplicationContext().getPackageManager();
        List<ApplicationInfo> apps = getPackageManager().getInstalledApplications(0);
        for(ApplicationInfo app : apps) {
            if ((app.flags & (ApplicationInfo.FLAG_UPDATED_SYSTEM_APP | ApplicationInfo.FLAG_SYSTEM)) > 0) {
                // It is a system app
            } else {
                installedPackageNames.add(app.packageName);
            }
        }
        TextView text = (TextView) findViewById(R.id.text_view);
        String key="";

        int count[] = new int[100];
        List<String> actualpermission = new ArrayList<String>();

        int j=0;
        int i=0;
        for(j=0;j<installedPackageNames.size();j++)
        {
        Log.i("messa",Integer.toString(getPermissionsByPackageName(installedPackageNames.get(j)).length()));
        for(i=0;i<getPermissionsByPackageName(installedPackageNames.get(j)).length();i++) {

            if (getPermissionsByPackageName(installedPackageNames.get(j)).charAt(i) != '\n' && getPermissionsByPackageName(installedPackageNames.get(j)).charAt(i) != '\0') {
                tempstr = tempstr + getPermissionsByPackageName(installedPackageNames.get(j)).charAt(i);
            } else {
                
                if (tempstr.equals("android.permission.BLUETOOTH")) {
                    tempstr = "Bluetooth";
                } else if (tempstr.equals("android.permission.RECORD_AUDIO")) {
                    tempstr = "Microphone";
                } else if (tempstr.equals("android.permission.CAMERA")) {
                    tempstr = "Camera";
                } else if (tempstr.equals("android.permission.ACCESS_COARSE_LOCATION") || tempstr.equals("android.permission.ACCESS_FINE_LOCATION")) {
                    tempstr = "Location";
                } else if (tempstr.equals("android.permission.READ_EXTERNAL_STORAGE") || tempstr.equals("android.permission.WRITE_EXTERNAL_STORAGE")) {
                    tempstr = "File storage";
                } else if (tempstr.equals("android.permission.READ_CONTACTS") || tempstr.equals("android.permission.WRITE_CONTACTS")) {
                    tempstr = "Contacts";
                } else if (tempstr.equals("android.permission.CALL_PHONE") || tempstr.equals("android.permission.READ_PHONE_STATE") || tempstr.equals("android.permission.READ_CALL_LOG") || tempstr.equals("android.permission.WRITE_CALL_LOG")) {
                    tempstr = "Telephone";
                } else if (tempstr.equals("android.permission.SEND_SMS") || tempstr.equals("android.permission.RECEIVE_SMS") || tempstr.equals("android.permission.READ_SMS") || tempstr.equals("android.permission.RECEIVE_MMS")) {
                    tempstr = "Sms";
                } else {
                    tempstr = "";
                    continue;
                }
                actualpermission.add(tempstr);
                Log.i("permis", tempstr);
                tempstr = "";
            }

        }
            getWebsite(installedPackageNames.get(j));
        }
        Log.i("ivar",Integer.toString(i));
    }


    private void getWebsite(final String str) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final StringBuilder builder = new StringBuilder();

                try {
                    Document doc = Jsoup.connect("https://play.google.com/store/apps/details?id=" + str + "&hl=en_IN").get();
                    String title = doc.title();
                  
                    Elements link= doc.select("h1[itemprop=name]");
                    builder.append(link.text()).append("\n");

                    builder.append(getPermissionsByPackageName(str)).append("\n");
                } catch (IOException e) {
                    //builder.append("Error : ").append(e.getMessage()).append("\n");
                }
                count++;
                Log.i("count",Integer.toString(count));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       mTextView.append(builder.toString() +"\n"+ tempstr );

                    }
                });

            }
        }).start();
    }
	
    // Custom method to get all installed package names
    protected HashMap<String,String> getInstalledPackages(){
		
        PackageManager packageManager = getPackageManager();
        
        Intent intent = new Intent(Intent.ACTION_MAIN,null);        
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                |Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        List<ResolveInfo> resolveInfoList = getPackageManager().queryIntentActivities(intent,0);

        HashMap<String,String> map = new HashMap<>();
        		
        for(ResolveInfo resolveInfo : resolveInfoList){            
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            String packageName = activityInfo.applicationInfo.packageName;
            String label = (String) packageManager.getApplicationLabel(activityInfo.applicationInfo);
            map.put(packageName,label);
        }
        return map;
    }


    // Custom method to get app requested and granted permissions from package name
    protected String getPermissionsByPackageName(String packageName){
  
        StringBuilder builder = new StringBuilder();

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            
            for (int i = 0; i < packageInfo.requestedPermissions.length; i++){
                if ((packageInfo.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) {
                    String permission =packageInfo.requestedPermissions[i];
                    builder.append(permission + "\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.toString();
    }
}