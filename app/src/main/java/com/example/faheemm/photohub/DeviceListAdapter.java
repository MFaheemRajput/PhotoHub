package com.example.faheemm.photohub;


import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;

public class DeviceListAdapter extends BaseAdapter{
    private ArrayList<WifiP2pDevice> peers;
    private Context context;
    private LayoutInflater inflater;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        if(null==convertView){
            convertView=inflater.inflate(R.layout.device_list_item,parent,false);
            convertView.setTag(new ViewHolder(convertView));
        }
        WifiP2pDevice device=(WifiP2pDevice)getItem(position);
        ViewHolder holder=(ViewHolder)convertView.getTag();
        holder.deviceName.setText(device.deviceName);
        holder.deviceDetail.setText(device.deviceAddress);
        holder.toggleButton.setChecked(device.status==WifiP2pDevice.CONNECTED);
        return convertView;
    }

    private class  ViewHolder{
        public TextView deviceName;
        public TextView deviceDetail;
        public ToggleButton toggleButton;

        public ViewHolder(View view){
            deviceName=(TextView)view.findViewById(R.id.device_name);
            deviceDetail=(TextView)view.findViewById(R.id.device_detail);
            toggleButton=(ToggleButton)view.findViewById(R.id.toggleButton);
        }
    }
}
