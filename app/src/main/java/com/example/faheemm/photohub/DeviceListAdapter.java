package com.example.faheemm.photohub;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;

public class DeviceListAdapter extends BaseAdapter{
    private ArrayList<WifiP2pDevice> peers;
    private Context context;
    private LayoutInflater inflater;
    ProgressDialog progressDialog = null;
    public DeviceListAdapter(Context context,ArrayList<WifiP2pDevice> peers){
        this.context=context;
        this.peers=peers;
        this.inflater=LayoutInflater.from(context);
    }

    public ArrayList<WifiP2pDevice> getPeers() {
        return peers;
    }

    @Override
    public int getCount() {
        return peers.size();
    }

    @Override
    public Object getItem(int position) {
        return peers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(null==convertView){
            convertView=inflater.inflate(R.layout.device_list_item,parent,false);
            convertView.setTag(new ViewHolder(convertView));
        }
        WifiP2pDevice device=(WifiP2pDevice)getItem(position);
        ViewHolder holder=(ViewHolder)convertView.getTag();
        holder.deviceName.setText(device.deviceName);
        holder.deviceDetail.setText(device.deviceAddress);
        if(device.status == WifiP2pDevice.CONNECTED){
            holder.toggleButton.setText("Connected");
        }else{
            holder.toggleButton.setText("Connect");
        }
        holder.toggleButton.setTag(position);
        holder.toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer index = (Integer) v.getTag();
                WifiP2pDevice peer = peers.get(index);
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = peer.deviceAddress;
                config.wps.setup = WpsInfo.PBC;

                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                ((CameraActivity) context).connect(config);

            }
        });
        holder.deviceName.setTag(position);
        holder.deviceName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer index=(Integer)v.getTag();
                        ((CameraActivity) context).setSelectedDevice((WifiP2pDevice)getItem(index));
            }
        });
        return convertView;
    }

    private class  ViewHolder{
        public TextView deviceName;
        public TextView deviceDetail;
        public Button toggleButton;

        public ViewHolder(View view){
            deviceName=(TextView)view.findViewById(R.id.device_name);
            deviceDetail=(TextView)view.findViewById(R.id.device_detail);
            toggleButton=(Button)view.findViewById(R.id.toggleButton);
        }
    }
}
