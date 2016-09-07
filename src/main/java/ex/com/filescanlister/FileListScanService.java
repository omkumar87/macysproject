package ex.com.filescanlister;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Set;

public class FileListScanService extends Service {
    private File root;
    NotificationManager manager;
    long avgsize = 0;
    long  count = 0;
    private ArrayList<File> fileList = new ArrayList<File>();
    FileComparator fileComparator = new FileComparator();
    FilePriorityComparator filePriorityComparator = new FilePriorityComparator();
    long mbc = 1024 * 1024;
    private PriorityQueue<File> filePriorityQueue =  new PriorityQueue<File>(10,fileComparator);
    private PriorityQueue<FileKrumb> extPriorityQueue =  new PriorityQueue<FileKrumb>(5,filePriorityComparator);

    private HashMap<String,Integer> fileext = new HashMap<String,Integer>();
    boolean isRunning = false;
    private final IBinder mBinder = new LocalBinder();
    Callbacks activity;

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    Notification notification;
    LongOperation fileTask;
    Thread filescan = new Thread(new Runnable() {
        @Override
        public void run() {


            //getfile(root);
            isRunning = false;
            //for(int i = 0 i < )
            //stopSelf();

        }
    });
    public FileListScanService() {
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        //Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        /*Intent intent1 = new Intent(this, MainActivity.class);

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
        manager.notify(11, notification);*/

        //startForeground(1, notification);
        //filescan.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
        if(fileTask != null && !fileTask.isCancelled())
            fileTask.cancel(true);
       // filescan.interrupt();
       // stopForeground(true);
    }


    class FileComparator implements Comparator<File>
    {
        @Override
        public int compare(File f1, File f2)
        {


           if(f1.length() > f2.length())
               return 1;
            if(f1.length() < f2.length())
               return -1;
            return 0;

        }
    }

    class FilePriorityComparator implements Comparator<FileKrumb>
    {
        @Override
        public int compare(FileKrumb f1, FileKrumb f2)
        {

            if(f1.count> f2.count)
                return 1;
            if(f1.count < f2.count)
                return -1;
            return 0;

        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    //returns the instance of the service
    public class LocalBinder extends Binder {
        public FileListScanService getServiceInstance(){
            return FileListScanService.this;
        }
    }

    //Here Activity register to the service as Callbacks client
    public void registerClient(Activity activity){
        this.activity = (Callbacks)activity;
    }

    public void startFileScanning(){
        fileTask = new LongOperation();
        root = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath());
        fileTask.execute(root);
        Toast.makeText(this, root.getName(), Toast.LENGTH_SHORT).show();

    }

    public interface Callbacks{
        public void updateClient(long data,PriorityQueue<File> file,PriorityQueue<FileKrumb> extdata);
        public void updateProgress(long val);
    }
    private class LongOperation extends AsyncTask<File, Long, Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Set<String> str = fileext.keySet();

            Log.v("Filer Result", Long.toString(avgsize / (count * 1024)) + "  " + filePriorityQueue.size() + " " + str.size());

          /* // Log.v("Filer Result",Long.toString(avgsize/(count *1024)));
            for(File f: filePriorityQueue){
                Log.v("Filer Result Q",f.getName() + " " + f.length());

            }


            for(FileKrumb f: extPriorityQueue){
                Log.v("Filer Result ext",f.ext + " " + f.count);

            }*/
            activity.updateClient(avgsize / count, filePriorityQueue, extPriorityQueue);
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            super.onProgressUpdate(values);
            System.out.println("onProgressUpdate");

            activity.updateProgress(values[0]);
        }

        @Override
        protected Void doInBackground(File... params) {


            isRunning = true;
            System.out.println("doInBackground");

            getfile(params[0]);
            Set<String> str = fileext.keySet();
            for(String f: str){
                extPriorityQueue.add(new FileKrumb("",0,fileext.get(f),f));
                if (extPriorityQueue.size() > 5)
                    extPriorityQueue.poll();

            }
            return null;
        }


        @Override
        protected void onPreExecute() {
            Log.v("Filer", "Started");
            System.out.println("onPreExecute");

        }



        public ArrayList<File> getfile(File dir) {
            //System.out.println("getfile");
            File listFile[] = dir.listFiles();
            String ext = "";
            if (listFile != null && listFile.length > 0) {
                for (int i = 0; i < listFile.length; i++) {
                    //publishProgress(count);
                    if (listFile[i].isDirectory()) {
                        getfile(listFile[i]);
                    } else {
                        filePriorityQueue.add(listFile[i]); //Max priority queue to give 10 largest file
                        if (filePriorityQueue.size() > 10)
                            filePriorityQueue.poll();
                        avgsize+=listFile[i].length();//calculate average
                        count++;
                        int n = listFile[i].getName().lastIndexOf('.');
                        ext = listFile[i].getName().substring(n+1);


                        if (n > 0 && listFile[i].getName().length() - listFile[i].getName().replace(".", "").length() == 1
                               && !ext.matches("\\d+") && ext.length() < 10) {
                            //FileKrumb fk = new FileKrumb(listFile[i].length(),listFile[i].getName(),ext);
                            if(fileext.containsKey(ext.toLowerCase())){
                                int val = fileext.get(ext.toLowerCase());
                                fileext.put(ext.toLowerCase(),++val); //Hash with extension as a key and the usage
                            }else {
                                fileext.put(ext.toLowerCase(),1);
                            }
                        }

                    }
                    //Log.v("Filer", ext + " " + listFile[i].length());

                }
            }
            return fileList;
        }
    }
}
