package lab.wifiscanner;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by namtr on 22/07/2016.
 */
public class ListViewAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ArrayList<ScanResult> data;

    public ListViewAdapter(Context context, ArrayList<ScanResult> data) {
        this.mInflater = LayoutInflater.from(context);
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.row, parent, false);
            holder = new ViewHolder();
            holder.SSID = (TextView) convertView.findViewById(R.id.ssid_tv);
            holder.level = (TextView) convertView.findViewById(R.id.level_tv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.SSID.setText(data.get(position).SSID);
        holder.level.setText(data.get(position).capabilities);
        return convertView;
    }

    static class ViewHolder {
        TextView SSID;
        TextView level;
    }
}
