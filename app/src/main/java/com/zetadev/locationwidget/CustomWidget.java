package com.zetadev.locationwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;

import androidx.core.graphics.drawable.DrawableCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class CustomWidget extends AppWidgetProvider {

    // Definisci un'azione per il refresh
    private static final String ACTION_REFRESH = "com.zetadev.locationwidget.ACTION_REFRESH";
    private static final String ACTION_LOCATION_UPDATE = "com.zetadev.locationwidget.ACTION_LOCATION_UPDATE";
    private static final String ACTION_BLUETOOTH_UPDATE = "com.zetadev.locationwidget.ACTION_BLUETOOTH_UPDATE";
    private static final String ACTION_WIFI_UPDATE = "com.zetadev.locationwidget.ACTION_WIFI_UPDATE";
    private static final String ACTION_FAI_IL_CAZZO_DI_WORKER = "com.zetadev.locationwidget.ACTION_FAI_IL_CAZZO_DI_WORKER";
    private static final String ACTION_CHARGING_UPDATE = "com.zetadev.locationwidget.ACTION_CHARGING_UPDATE";
    private static final String ACTION_CALL_UPDATE = "com.zetadev.locationwidget.ACTION_CALL_UPDATE";

    List<Integer> widgetimagevies;


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);




       // Ottieni la lista delle app installate
        PackageManager pm = context.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> installedApps = pm.queryIntentActivities(mainIntent, 0);

        // Seleziona 3 applicazioni a caso
        Random random = new Random();
        ResolveInfo app1 = installedApps.get(random.nextInt(installedApps.size()));
        ResolveInfo app2 = installedApps.get(random.nextInt(installedApps.size()));
        ResolveInfo app3 = installedApps.get(random.nextInt(installedApps.size()));

        // Crea RemoteViews per il layout del widget
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_test_layout);

        // Imposta le icone delle applicazioni



        views.setImageViewBitmap(R.id.icon1,  drawableToBitmap(GetAppIconDrawable(context, app1.activityInfo.packageName)));
        views.setImageViewBitmap(R.id.icon2,drawableToBitmap(GetAppIconDrawable(context, app2.activityInfo.packageName)));
        views.setImageViewBitmap(R.id.icon3, drawableToBitmap(GetAppIconDrawable(context, app3.activityInfo.packageName)));

       widgetimagevies = Arrays.asList(R.id.icon1, R.id.icon2, R.id.icon3);

        // Imposta il click per avviare le app
        views.setOnClickPendingIntent(R.id.icon1, getPendingIntentForApp(context, app1));
        views.setOnClickPendingIntent(R.id.icon2, getPendingIntentForApp(context, app2));
        views.setOnClickPendingIntent(R.id.icon3, getPendingIntentForApp(context, app3));











        // Aggiorna il widget per ogni ID
        for (int appWidgetId : appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }






    }


    private void DoWorkerOneTime(Context context)
    {
        OneTimeWorkRequest wifiWork = new OneTimeWorkRequest.Builder(WifiMonitor.class)
                .build();

// Pianifica l'esecuzione del worker
        WorkManager.getInstance(context).enqueue(wifiWork);
    }

    private PendingIntent getPendingIntentForApp(Context context, ResolveInfo app) {
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(app.activityInfo.packageName);
        return PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
    }


    //gestire tasto premuto
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (ACTION_REFRESH.equals(intent.getAction())) {
            // Se riceviamo l'intent di refresh, chiediamo l'aggiornamento del widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, CustomWidget.class));
            onUpdate(context, appWidgetManager, appWidgetIds);
        }

        if(ACTION_BLUETOOTH_UPDATE.equals(intent.getAction()))
        {
            String[] apps = intent.getStringArrayExtra("apps");
            updateWidget(context, apps);
        }

        if(ACTION_WIFI_UPDATE.equals(intent.getAction()))
        {
            String[] apps = intent.getStringArrayExtra("apps");
            updateWidget(context, apps);
        }


     /*   if(ACTION_FAI_IL_CAZZO_DI_WORKER.equals(intent.getAction()))
        {
           DoWorkerOneTime(context);
        }*/ //TODO eliminare


        if(ACTION_CHARGING_UPDATE.equals(intent.getAction()))
        {
            String[] apps = intent.getStringArrayExtra("apps");
            updateWidget(context, apps);
        }

        if(ACTION_CALL_UPDATE.equals(intent.getAction()))
        {
            String[] apps = intent.getStringArrayExtra("apps");
            updateWidget(context, apps);
        }

        if( ACTION_LOCATION_UPDATE.equals(intent.getAction()))
        {
            String[] apps = intent.getStringArrayExtra("apps");
            updateWidget(context, apps);
        }






    }


    public void updateWidget(Context context, String[] apps) {

        int numberofapps = apps.length;
        widgetimagevies = Arrays.asList(R.id.icon1, R.id.icon2, R.id.icon3);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, CustomWidget.class));
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_test_layout);

        for(int i=0; i<numberofapps; i++)
        {
            views.setImageViewBitmap(widgetimagevies.get(i),  drawableToBitmap(GetAppIconDrawable(context, apps[i])));
            views.setOnClickPendingIntent(widgetimagevies.get(i), getPendingIntentForApp(context, getResolveInfoByPackageName(context, apps[i])));
        }



        int extraviews = widgetimagevies.size() - apps.length;

        if(extraviews>0)
        {
            for(int i=0; i<extraviews; i++)
            {
                views.setViewVisibility(widgetimagevies.get( (widgetimagevies.size() -1) - i), View.GONE);
            }
        }


        // Aggiorna il widget per ogni ID
        for (int appWidgetId : appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

    }



    public Drawable GetAppIconDrawable(Context context,String PackageName) {

        try
        {
            return  context.getPackageManager().getApplicationIcon(PackageName);
        }
        catch (Exception e)
        {
            return  null;
        }

    }



    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }



    public ResolveInfo getResolveInfoByPackageName(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();

        // Get a list of installed applications
        List<ApplicationInfo> apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        // Iterate through the list to find the application with the given package name
        for (ApplicationInfo appInfo : apps) {
            if (appInfo.packageName.equals(packageName)) {
                // Create a ResolveInfo object for the found application
                Intent launchIntent = packageManager.getLaunchIntentForPackage(packageName);
                if (launchIntent != null) {
                    List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(launchIntent, 0);
                    if (!resolveInfoList.isEmpty()) {
                        return resolveInfoList.get(0); // Return the first matching ResolveInfo
                    }
                }
            }
        }
        return null; // Return null if no matching application is found
    }




}
