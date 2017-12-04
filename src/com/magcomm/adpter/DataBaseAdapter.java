package com.magcomm.adpter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.magcomm.data.BmpData;
import com.magcomm.heartmeasure.R;

public class DataBaseAdapter extends BaseAdapter {
	
	private List<BmpData> mData;  
    private Context mContext;
    private LayoutInflater inflater;
    
    public DataBaseAdapter(Context context, List<BmpData> data) {
    	this.mContext = context;
		this.mData = data;
		this.inflater = LayoutInflater.from(mContext);
    }

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.list_item, null);
			
			holder.bpm = (TextView)convertView.findViewById(R.id.tv_bpm);
			holder.date = (TextView)convertView.findViewById(R.id.tv_date);
			holder.time = (TextView)convertView.findViewById(R.id.tv_time);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder)convertView.getTag();
		}
		
		BmpData data = (BmpData)getItem(position);
		
		holder.bpm.setText(data.getBpm());
		holder.date.setText(data.getDate());
		holder.time.setText(data.getTime());
		
		return convertView;
	}
	
	static class ViewHolder {
		public TextView bpm;
		public TextView date;
		public TextView time;
	}

}
