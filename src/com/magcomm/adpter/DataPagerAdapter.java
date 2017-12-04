package com.magcomm.adpter;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class DataPagerAdapter extends FragmentPagerAdapter {
	List<Fragment> mList;
	String[] mTitle;
	
	public DataPagerAdapter(FragmentManager fm) {
		super(fm);
	}
	
	public DataPagerAdapter(FragmentManager fm, List<Fragment> list, String[] title) {
		super(fm);
		this.mList = list;
		this.mTitle = title;
	}

	@Override
	public Fragment getItem(int arg0) {
		// TODO Auto-generated method stub
		return mList.get(arg0);
	}
	
	@Override
	public CharSequence getPageTitle(int position) {
		// TODO Auto-generated method stub
		return mTitle[position];
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

}
