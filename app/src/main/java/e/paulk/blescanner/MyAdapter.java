package e.paulk.blescanner;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private ArrayList<BleDevice> mData;

    public MyAdapter(ArrayList<BleDevice> data) {
        this.mData = data;
    }

    public void updateData(ArrayList<BleDevice> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ble_item, viewGroup,
                false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        if (mData.get(i) != null) {
            String deviceName = mData.get(i).getDevice_name();
            String deviceUUID = mData.get(i).getDevice_UUID();
            int deviceRSSI = mData.get(i).getDevice_RSSI();
            int deviceTX = mData.get(i).getDevice_TXpower();
            String rawData = mData.get(i).getDevice_convertedRawData();
            String identifier = mData.get(i).getDevice_identifier();
            String lastUpdae = mData.get(i).getLastUpdateTime();
            //int deviceAdID = mData.get(i).getDevice_adID();

            viewHolder.mTextView_deviceName.setText("Name: " + deviceName);
            viewHolder.mTextView_deviceUUID.setText("UUID: " + deviceUUID);
            viewHolder.mTextView_deviceRSSI.setText("RSSI: " + String.valueOf(deviceRSSI));
            viewHolder.mTextView_deviceTX.setText("TX: " + String.valueOf(deviceTX));
            viewHolder.mTextView_deviceRawData.setText("Raw Data: " + rawData);
            viewHolder.mTextView_deviceIdentifier.setText("Identifier: " + identifier);
            viewHolder.mTextView_lastUpdate.setText("Last Updated: " + lastUpdae);
        }
        else {
            viewHolder.mTextView_deviceName.setText("Error");
            viewHolder.mTextView_deviceUUID.setText("Error");
            viewHolder.mTextView_deviceRSSI.setText("Error");
            viewHolder.mTextView_deviceTX.setText("Error");
            viewHolder.mTextView_deviceRawData.setText("Error");
            viewHolder.mTextView_deviceIdentifier.setText("Error");
            viewHolder.mTextView_lastUpdate.setText("Error");
        }
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView mTextView_deviceName;
        TextView mTextView_deviceUUID;
        TextView mTextView_deviceRSSI;
        TextView mTextView_deviceTX;
        TextView mTextView_deviceRawData;
        TextView mTextView_deviceIdentifier;
        TextView mTextView_lastUpdate;
        //TextView mTextView_deviceAdID;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextView_deviceName = (TextView) itemView.findViewById(R.id.ble_name);
            mTextView_deviceUUID = (TextView) itemView.findViewById(R.id.ble_uuid);
            mTextView_deviceRSSI = (TextView) itemView.findViewById(R.id.ble_RSSI);
            mTextView_deviceTX = (TextView) itemView.findViewById(R.id.ble_TX);
            mTextView_deviceRawData = (TextView) itemView.findViewById(R.id.ble_rawData);
            mTextView_deviceIdentifier = (TextView) itemView.findViewById(R.id.ble_identifier);
            mTextView_lastUpdate = (TextView) itemView.findViewById(R.id.ble_lastUpdate);
        }
    }
}
