package com.example.servicebesttest;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.io.File;

public class DownloadService extends Service {
    private DownloadTask downloadTask;
    private String downloadUrl;
    private DownloadListener listener=new DownloadListener(){

        @Override
        public void onProgress(int progress) {
            getNotificationManger().notify(1,getNotification("Downloading...",progress));
        }

        @Override
        public void onSuccess() {
            downloadTask=null;
            stopForeground(true);
            getNotificationManger().notify(1,getNotification("Download Success",-1));
            Toast.makeText(DownloadService.this,"Download Success",Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onFailed() {
            downloadTask=null;
            stopForeground(true);
            getNotificationManger().notify(1,getNotification("Download Failed",-1));
            Toast.makeText(DownloadService.this,"Download Failed",Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onPaused() {
            downloadTask=null;
            Toast.makeText(DownloadService.this,"Paused",Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onCanceled() {
            downloadTask=null;
            Toast.makeText(DownloadService.this,"Cancled",Toast.LENGTH_SHORT).show();

        }
    };
    private DownloadBinder mBinder=new DownloadBinder();
   public IBinder onBind(Intent intent){
        return mBinder;
    }
    class  DownloadBinder extends Binder{
       public void startDownload(String url){
           if (downloadTask==null){
               downloadUrl=url;
               downloadTask=new DownloadTask(listener);
               downloadTask.execute(downloadUrl);
               startForeground(1,getNotification("Downloading...",0));
               Toast.makeText(DownloadService.this,"Downloading...",Toast.LENGTH_SHORT).show();
           }
       }
       public void pauseDownload(){
           if (downloadTask!=null){
               downloadTask.pauseDownload();
           }
       }
       public void cancelDownload(){
           if (downloadTask!=null){
               downloadTask.cancelDownload();
           }else {
               if (downloadUrl!=null){
                   String fileName=downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                   String directory= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                   File file=new File(directory+fileName);
                   if (file.exists()){
                       file.delete();
                   }
                   getNotificationManger().cancel(1);
                   stopForeground(true);
                   Toast.makeText(DownloadService.this,"Canceled",Toast.LENGTH_SHORT).show();
               }
           }
       }
    }



    private NotificationManager getNotificationManger() {
        return (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }
    private Notification getNotification(String title,int progress){
        Intent intent=new Intent(this,MainActivity.class);
        PendingIntent pi=PendingIntent.getActivities(this,0, new Intent[]{intent},0);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        if (progress>0){
            builder.setContentTitle(progress+"%");
            builder.setProgress(100,progress,false);
        }
        return builder.build();
    }

    public DownloadService() {
    }


}
