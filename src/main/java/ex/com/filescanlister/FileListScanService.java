package ex.com.filescanlister;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

public class FileListScanService extends Service {
    private File root;
    NotificationManager manager;
    long avgsize = 0;
    long  count = 0;
    private ArrayList<File> fileList = new ArrayList<File>();
    FileComparator fileComparator = new FileComparator();
    private PriorityQueue<File> filePriorityQueue =  new PriorityQueue<File>(10,fileComparator);
    private HashMap<String,Integer> fileext = new HashMap<String,Integer>();
    boolean isRunning = false;
    Notification notification;
    Thread filescan = new Thread(new Runnable() {
        @Override
        public void run() {
            isRunning = true;
            root = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath());
            getfile(root);
            isRunning = false;
            stopSelf();

        }
    });
    public FileListScanService() {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


        Intent intent1 = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent;
        pendingIntent = PendingIntent.getActivity(this, 1, intent1, 0);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setAutoCancel(false);
        builder.setTicker("this is ticker text");
        builder.setContentTitle("File Scan");
        builder.setContentText("File Scan");
        builder.setSmallIcon(android.R.drawable.ic_notification_overlay);
        //builder.setContentIntent(pendingIntent);
        builder.setOngoing(true);
        builder.setSubText("File Scan");   //API level 16
        builder.setNumber(100);
        builder.build();

        notification = builder.getNotification();
        manager.notify(11, notification);

        startForeground(1, notification);
        filescan.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
        filescan.interrupt();
        stopForeground(true);
    }


    public ArrayList<File> getfile(File dir) {
        File listFile[] = dir.listFiles();
        String ext;
        if (listFile != null && listFile.length > 0) {
            for (int i = 0; i < listFile.length; i++) {

                if (listFile[i].isDirectory()) {

                    getfile(listFile[i]);

                } else {
                    filePriorityQueue.add(listFile[i]); //Max priority queue to give 10 largest file
                    avgsize+=listFile[i].length();//calculate average
                    count++;
                    int n = listFile[i].getName().lastIndexOf('.');
                    if (n > 0) {
                        ext = listFile[i].getName().substring(n+1);
                        if(fileext.containsKey(ext)){
                            int val = fileext.get(ext);
                            fileext.put(ext,++val); //Hash with extension as a key and the usage
                        }else {
                            fileext.put(ext,1);
                        }
                    }
                }

            }
        }
        return fileList;
    }

    class FileComparator implements Comparator<File>
    {
        @Override
        public int compare(File x, File y)
        {
            // Assume neither string is null. Real code should
            // probably be more robust
            // You could also just return x.length() - y.length(),
            // which would be more efficient.
            if (x.length() < y.length())
            {
                return -1;
            }
            if (x.length() > y.length())
            {
                return 1;
            }
            return 0;
        }
    }
}
