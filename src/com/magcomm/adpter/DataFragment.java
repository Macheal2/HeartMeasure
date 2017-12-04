package com.magcomm.adpter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.magcomm.heartmeasure.R;

public class DataFragment extends Fragment {
	
	private Context mContext;
	private DataBaseAdapter mDataAdapter;
	private ListView mListView;
	//private LinearLayoutManager mLayoutManager;
	//private RecyclerView mRecycler;
	//private DataRecyclerAdapter mDataAdapter;
	
	public DataFragment(Context context, DataBaseAdapter adapter) {
		this.mContext = context;
		this.mDataAdapter = adapter;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = super.onCreateView(inflater, container, savedInstanceState);
		view = inflater.inflate(R.layout.fragment_list, container, false);
		mListView = (ListView) view.findViewById(R.id.list);
		mListView.setAdapter(mDataAdapter);
		
		/*mRecycler = (RecyclerView) view.findViewById(R.id.recycler);
		
		mLayoutManager = new LinearLayoutManager(mContext);
		mLayoutManager.setOrientation(OrientationHelper.VERTICAL);
		
		mRecycler.setLayoutManager(mLayoutManager);
		mRecycler.setAdapter(mDataAdapter);*/
		
		return view;
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
	}

}
