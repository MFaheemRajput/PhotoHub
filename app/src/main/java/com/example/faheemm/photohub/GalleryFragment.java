package com.example.faheemm.photohub;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.faheem.wifidirect.FileTransferService;
import com.example.faheem.wifidirect.Utils;
import com.example.faheem.wifidirect.WiFiDirectActivity;

import java.io.File;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the

 * to handle interaction events.
 * Use the {@link GalleryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GalleryFragment extends Fragment {

    public static final String IP_SERVER = "192.168.49.1";
    public static int PORT = 8988;

    private ArrayList<WifiP2pDevice> peers=new ArrayList<WifiP2pDevice>();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    View rootView;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Cursor cursor;
    private int columnIndex;
    ImageView selectedImage;

    // CLIENT SECRET                      fd1db8e2-0e5e-446e-b11d-4a08c6113cba
    // CLIENT ID (DEVELOPMENT MODE)*      9433665bfd6d40fe9e9458a80208e2a9

    int imageIndex = 0 ;



    private Integer[] imageIDs = {
            R.drawable.sample_0,
            R.drawable.sample_1,
            R.drawable.sample_2,
            R.drawable.sample_3,
            R.drawable.sample_4,
            R.drawable.sample_5,
            R.drawable.sample_6,
            R.drawable.sample_7
    };


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GalleryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GalleryFragment newInstance(String param1, String param2) {
        GalleryFragment fragment = new GalleryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public GalleryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);

        }

        setHasOptionsMenu(true);
   }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);

        //getActivity().getMenuInflater().inflate(R.menu.menu_camera, menu);

        menu.findItem(R.id.share).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                if (menuItem.getItemId() == R.id.share) {

                    File sdDir = new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES), "PhotoHub");
                    File[] imagesArray = sdDir.listFiles();
                    File  singleFile = imagesArray[imageIndex];
                    Uri imageUri = Uri.fromFile(singleFile);




                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                    shareIntent.setType("image/jpeg");
                    startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));




                    // start the activity
                    startActivityForResult(shareIntent, 1);
                }
                return true;
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment

        rootView = inflater.inflate(R.layout.fragment_gallery, container, false);
        ;
//        peers.addAll(((CameraActivity) getActivity()).getPeers());
        Gallery gallery = (Gallery) rootView.findViewById(R.id.gallery1);
        gallery.setAdapter(new ImageAdapter(getActivity(), imageIDs, rootView));
        gallery.setSelection(0);
        gallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Toast.makeText(getActivity(), "pic" + (position + 1) + " selected",
                        Toast.LENGTH_SHORT).show();

                imageIndex = position;

                Drawable drawable = ((ImageView) view).getDrawable();

                setImageInImageView(drawable);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        Button sendButton = (Button)rootView.findViewById(R.id.sendImg);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File sdDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), "PhotoHub");
                File[] imagesArray = sdDir.listFiles();
                File  singleFile = imagesArray[imageIndex];
                Uri uri = Uri.fromFile(singleFile);


                String localIP = Utils.getLocalIPAddress();
                // Trick to find the ip in the file /proc/net/arp
                WifiP2pDevice device=((CameraActivity)getActivity()).getSelectedDevice();
                if(null==device){
                    Toast.makeText(getActivity(),"No device selected",Toast.LENGTH_SHORT).show();
                    return;
                }

//                String client_mac_fixed = new String(device.deviceAddress).replace("99", "19");
//                String clientIP = Utils.getIPFromMac(client_mac_fixed);

                // User has picked an image. Transfer it to group owner i.e peer using
                // FileTransferService.
                Toast.makeText(getActivity(),"Sending: " + uri,Toast.LENGTH_SHORT).show();

                Log.d(WiFiDirectActivity.TAG, "Intent----------- " + uri);
                Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
                serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
                serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());

//                if(localIP.equals(IP_SERVER)){
//                    serviceIntent.putExtra(FileTransferService.EXTRAS_ADDRESS, clientIP);
//                }else{
                CameraActivity activity=(CameraActivity)getActivity();
                if(activity.getWifiP2pInfo()==null){
                    return;
                }
                serviceIntent.putExtra(FileTransferService.EXTRAS_ADDRESS, activity.getWifiP2pInfo());
//                }
                serviceIntent.putExtra(FileTransferService.EXTRAS_REQUEST_TYPE, CameraActivity.POST_REQUEST);
                serviceIntent.putExtra(FileTransferService.EXTRAS_PORT, PORT);
                getActivity().startService(serviceIntent);

            }

        });
        Button receiveButton = (Button)rootView.findViewById(R.id.receiveButton);

        receiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File sdDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), "PhotoHub");
                File[] imagesArray = sdDir.listFiles();
                File  singleFile = imagesArray[imageIndex];
                Uri uri = Uri.fromFile(singleFile);


                String localIP = Utils.getLocalIPAddress();
                Log.d(WiFiDirectActivity.TAG, "LocalAddress----------- " + localIP);
                // Trick to find the ip in the file /proc/net/arp
//                WifiP2pDevice device=((CameraActivity)getActivity()).getSelectedDevice();
//                if(null==device){
//                    Toast.makeText(getActivity(),"No device selected",Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                String client_mac_fixed = new String(device.deviceAddress).replace("99", "19");
//                String clientIP = Utils.getIPFromMac(client_mac_fixed);

                // User has picked an image. Transfer it to group owner i.e peer using
                // FileTransferService.
                Toast.makeText(getActivity(),"Sending: " + uri,Toast.LENGTH_SHORT).show();

                Log.d(WiFiDirectActivity.TAG, "Intent----------- " + uri);
                Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
                serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
                serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());

//                if(localIP.equals(IP_SERVER)){
//                    serviceIntent.putExtra(FileTransferService.EXTRAS_ADDRESS, clientIP);
//                }else{
                CameraActivity activity=(CameraActivity)getActivity();
                if(activity.getWifiP2pInfo()==null){
                    Toast.makeText(getActivity(),"WIFI INfo is not available",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(activity.getWifiP2pInfo().groupOwnerAddress.getHostAddress().equals(localIP)){
                    Toast.makeText(getActivity(),"This Machne is Server",Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d(WiFiDirectActivity.TAG, "ServerAddress----------- " + activity.getWifiP2pInfo().groupOwnerAddress.getHostAddress());
                serviceIntent.putExtra(FileTransferService.EXTRAS_ADDRESS, activity.getWifiP2pInfo().groupOwnerAddress.getHostAddress());
//                }
                serviceIntent.putExtra(FileTransferService.EXTRAS_REQUEST_TYPE, CameraActivity.GET_REQUEST);
                serviceIntent.putExtra(FileTransferService.EXTRAS_PORT, PORT);
                getActivity().startService(serviceIntent);

            }

        });

        Button editButton = (Button)rootView.findViewById(R.id.editImg);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


//                File sdDir = new File(Environment.getExternalStoragePublicDirectory(
//                        Environment.DIRECTORY_PICTURES), "PhotoHub");
//                File[] imagesArray = sdDir.listFiles();
//                File  singleFile = imagesArray[imageIndex];
//                Uri imageUri = Uri.fromFile(singleFile);
//                Intent newIntent = new AviaryIntent.Builder(getActivity())
//                        .setData(imageUri) // input image src
//                        .withOutput(Uri.parse("file://" + singleFile)) // output file
//                        .withOutputFormat(Bitmap.CompressFormat.JPEG) // output format
//                        .withOutputSize(MegaPixels.Mp5) // output size
//                        .withOutputQuality(90) // output quality
//                        .build();
//
//                // start the activity
//                startActivityForResult(newIntent, 1);
            }

        });

//        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//
//
//
//            }
//        });

        return rootView;

    }

    //TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {

    }

    public void refreshData(){
        Gallery gallery = (Gallery) rootView.findViewById(R.id.gallery1);
        gallery.setAdapter(new ImageAdapter(getActivity(), imageIDs, rootView));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    public  void  setImageInImageView(Drawable drawable){

//        File singleFile = sdDirFilesParam[postion];

        ImageView imageView = (ImageView) rootView.findViewById(R.id.image1);

//        final BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize = 8;
//        Bitmap bm = BitmapFactory.decodeFile(singleFile.getAbsolutePath(),options);
//        imageView.setImageDrawable(bm);
        imageView.setImageDrawable(drawable);
        //imageView.setImageBitmap(BitmapFactory.decodeFile(singleFile.getAbsolutePath()));

    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */


}

