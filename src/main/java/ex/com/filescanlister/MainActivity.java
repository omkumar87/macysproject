package ex.com.filescanlister;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class MainActivity extends AppCompatActivity implements FileListScanService.Callbacks{
    Intent serviceIntent;
    FileListScanService fileListScanService;
    ProgressBar prgBar;
    TextView cnt;
    RecyclerView statelist;
    int permission ;
    MenuItem item;
    long mbc = 1024 * 1024;
    ArrayList<FileKrumb> feedsList = new ArrayList<FileKrumb>();
    FileKrumb title1,title2,title3;
    FileListAdapter fileListAdapter;
    Intent sendIntent = new Intent();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        statelist =  (RecyclerView) findViewById(R.id.statisLists);
        statelist.setLayoutManager(new LinearLayoutManager(this));
        title1 = new FileKrumb("Big Files",0,0,"");
        title3 = new FileKrumb("Top Used Extensions",0,0,"");
        title2 = new FileKrumb("Average File size",0,0,"");
        setSupportActionBar(toolbar);

        sendIntent.setAction(Intent.ACTION_SEND);

        sendIntent.setType("text/plain");
        //startActivity(sendIntent);
        permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        prgBar = (ProgressBar) findViewById(R.id.prgbar);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1
            );
        }
        serviceIntent = new Intent(MainActivity.this, FileListScanService.class);
    }



    private ShareActionProvider mShareActionProvider;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Locate MenuItem with ShareActionProvider
        item = menu.findItem(R.id.action_share);
        item.setEnabled(false);
        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        // Return true to display menu
        return true;
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            startActivity(sendIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    // Method to start the service
    public void startService(View view) {
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE); //Binding to the service!

        //startService(new Intent(getBaseContext(), FileListScanService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            unbindService(mConnection);
        } catch (IllegalArgumentException e){
            System.out.println("Unbinding didn't work. little surprise");
        }
    }

    // Method to stop the service
    public void stopService(View view) {
        try{
            unbindService(mConnection);
        } catch (IllegalArgumentException e){
            System.out.println("Unbinding didn't work. little surprise");
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            //Toast.makeText(MainActivity.this, "onServiceConnected called", Toast.LENGTH_SHORT).show();
            // We've binded to LocalService, cast the IBinder and get LocalService instance
            FileListScanService.LocalBinder binder = (FileListScanService.LocalBinder) service;
            fileListScanService = binder.getServiceInstance(); //Get instance of your service!
            fileListScanService.registerClient(MainActivity.this); //Activity register in the service as client for callabcks!
            fileListScanService.startFileScanning();
            prgBar.setVisibility(View.VISIBLE);
            //statelist.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            //Toast.makeText(MainActivity.this, "onServiceDisconnected called", Toast.LENGTH_SHORT).show();
            prgBar.setVisibility(View.INVISIBLE);


        }
    };

    @Override
    public void updateClient(long data, PriorityQueue<File> file,PriorityQueue<FileKrumb> extdata) {
        prgBar.setVisibility(View.INVISIBLE);
        title2.length = data/1024;
        title2.isAverage = true;
        feedsList.add(title2);
        feedsList.add(title1);
        String s = "Average File size   " + title2.length + "kb\n";
        s+= "Big Files \n";
        for(File f : file){
            feedsList.add(new FileKrumb(f.getName(),f.length()/mbc,0,""));
            s+=  f.getName() + "  " + f.length()/mbc + "mb\n";
        }
        s+= "Top Used Extensions   \n";

        feedsList.add(title3);
        for(FileKrumb f2 : extdata){
            feedsList.add(f2);
            s+=  f2.ext + "  " + f2.count + "\n";
        }

        sendIntent.putExtra(Intent.EXTRA_TEXT, s);
        fileListAdapter = new FileListAdapter(this, feedsList);
        statelist.setAdapter(fileListAdapter);
        statelist.setVisibility(View.VISIBLE);
        setShareIntent(sendIntent);
        item.setIntent(sendIntent);
        item.setEnabled(true);
    }

    @Override
    public void updateProgress(long val) {

    }
}
