package com.example.faheemm.photohub;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.faheem.wifidirect.WiFiDirectActivity;
import com.example.faheem.wifidirect.WiFiDirectBroadcastReceiver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Locale;


public class CameraActivity extends ActionBarActivity implements ActionBar.TabListener,WifiP2pManager.ChannelListener,WifiP2pManager.PeerListListener,FriendsFragment.DeviceActionListener,WifiP2pManager.ConnectionInfoListener {
    public static final int POST_REQUEST=10;
    public static final int GET_REQUEST=11;

    private WifiP2pInfo wifiP2pInfo;

    private WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;

    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;

    private  Menu mMenu;

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;

    WifiP2pDeviceList peers;
    WifiP2pDevice selectedDevice;

    public WifiP2pDevice getSelectedDevice() {
        return selectedDevice;
    }

    public WifiP2pInfo getWifiP2pInfo() {
        return wifiP2pInfo;
    }

    public void setSelectedDevice(WifiP2pDevice selectedDevice) {
        this.selectedDevice = selectedDevice;
    }

    public WifiP2pDeviceList getPeers() {
        return peers;
    }

    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);


        boolean isDirCreated = createPhotoHubDir();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
                Fragment fragment=((SectionsPagerAdapter) mViewPager.getAdapter()).getItem(position);
                if(position==2 && null!=peers){
                    onPeersAvailable(peers);
                    if(null!=fragment && fragment instanceof FriendsFragment){
                        ((FriendsFragment)fragment).onPeersAvailable(peers);
                    }
                }else if(position==1){
                    ((GalleryFragment)fragment).refreshData();
                }
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {

            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }


        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        discover();

    }

    public void discover(){
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("wifi", "onsuccess");
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.d("wifi", "onfailure");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
    }
    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        this.peers=peers;

        Fragment fragment=((SectionsPagerAdapter) mViewPager.getAdapter()).getItem(mViewPager.getCurrentItem());

        if(null!=fragment && fragment instanceof FriendsFragment){
            ((FriendsFragment)fragment).onPeersAvailable(peers);
        }

    }

    public void resetData() {
//        DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager()
//                .findFragmentById(R.id.frag_list);
//        DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) getFragmentManager()
//                .findFragmentById(R.id.frag_detail);
//        if (fragmentList != null) {
//            fragmentList.clearPeers();
//        }
//        if (fragmentDetails != null) {
//            fragmentDetails.resetViews();
//        }
    }

    @Override
    public void onChannelDisconnected() {
        // we will try once more
        if (manager != null && !retryChannel) {
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            resetData();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this,
                    "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera, menu);

        //menu.findItem(R.id.share).setOnMenuItemClickListener(on);



        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.share){

        return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

        mViewPager.setCurrentItem(tab.getPosition());

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments=new ArrayList<Fragment>();
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            fragments.add(new CameraFragment());
            fragments.add(GalleryFragment.newInstance("",""));
            fragments.add(FriendsFragment.newInstance("",""));
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return fragments.get(position);
        }


        @Override
        public int getCount() {
            // Show 3 total pages.
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_camera, container, false);

            return rootView;
        }
    }
        public Boolean createPhotoHubDir(){

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "PhotoHub");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.
        boolean isCreate = false;
        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){

            isCreate = mediaStorageDir.mkdirs();

            if (!isCreate){
                Log.d("Photo Hub", "failed to create directory");
                isCreate = false;
            }
            else {

                isCreate = true;
            }

        }
        return isCreate;
    }

    @Override
    public void showDetails(WifiP2pDevice device) {

    }

    @Override
    public void cancelDisconnect() {

    }

    @Override
    public void connect(final WifiP2pConfig config) {
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(CameraActivity.this, "Connected to:"+config.deviceAddress,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(CameraActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void disconnect() {

    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        this.wifiP2pInfo=info;
        if(info.groupFormed && info.isGroupOwner){
            new ServerAsyncTask(CameraActivity.this).execute();
        }
    }

    public class ServerAsyncTask extends AsyncTask<Void, Void, String> {


        private final Context context;


        /**
         * @param context
         */
        public ServerAsyncTask(Context context) {
            this.context = context;

        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                ServerSocket serverSocket = new ServerSocket(8988);
                Log.d(WiFiDirectActivity.TAG, "Server: Socket opened");
                Socket client = serverSocket.accept();
                Log.d(WiFiDirectActivity.TAG, "Server: connection done");
                final File f = new File(Environment.getExternalStorageDirectory() + "/PhotoHub/"+ System.currentTimeMillis()
                        + ".jpg");

                File dirs = new File(f.getParent());
                if (!dirs.exists())
                    dirs.mkdirs();
                f.createNewFile();

                Log.d(WiFiDirectActivity.TAG, "server: copying files " + f.toString());
                DataInputStream dataInputStream=new DataInputStream(client.getInputStream());
                int requestType=dataInputStream.readInt();
                if(requestType==POST_REQUEST){
                    copyFile(dataInputStream, new FileOutputStream(f));
                }else if(requestType==GET_REQUEST){

                    File sdDir = new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES), "PhotoHub");
                    File[] imagesArray = sdDir.listFiles();

                    DataOutputStream dataOutputStream=new DataOutputStream(client.getOutputStream());
                    dataOutputStream.writeInt(imagesArray.length);

                    for (File file:imagesArray){
                        dataOutputStream.writeLong(file.length());

                        FileInputStream inputStream=new FileInputStream(file);
                        byte buf[] = new byte[1024];
                        int len;
                        int theByte = 0;


                        try {
//                            while((theByte = inputStream.read()) != -1){
//                                dataOutputStream.write(theByte);
//                                Log.d("","WrittenByte"+theByte);
//                            }
//                            while ((len = inputStream.read(buf)) != -1) {
//                                dataOutputStream.write(buf, 0, len);
//
//                            }
//                            out.close();
                            Log.d("","FileSize "+file.length());
                            for(int j = 0; j < file.length(); j++){
                                dataOutputStream.write(inputStream.read());
                                Log.d("", "Current File Lenth: " + ((float)j / file.length()) * 100);
//                                Log.d("", "Current File Lenth: " + j/file.length()*100);
//                                Log.d("", "Current Character: " + j);
                            }
                            inputStream.close();

                        } catch (IOException e) {
                            Log.d(WiFiDirectActivity.TAG, e.toString());

                        }
//                        copyFile(new FileInputStream(file),dataOutputStream);
                    }
                    dataOutputStream.close();
                    dataOutputStream.flush();

                }
//
                serverSocket.close();

                return f.getAbsolutePath();
            } catch (IOException e) {
                Log.e(WiFiDirectActivity.TAG, e.getMessage());
                return null;
            }
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
//            if (result != null) {
//                statusText.setText("File copied - " + result);
//                Intent intent = new Intent();
//                intent.setAction(Intent.ACTION_VIEW);
//                intent.setDataAndType(Uri.parse("file://" + result), "image/*");
//                context.startActivity(intent);
//            }
            ((GalleryFragment)mSectionsPagerAdapter.getItem(1)).refreshData();
            new ServerAsyncTask(CameraActivity.this).execute();
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {

        }

    }

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d(WiFiDirectActivity.TAG, e.toString());
            return false;
        }
        return true;
    }
}
