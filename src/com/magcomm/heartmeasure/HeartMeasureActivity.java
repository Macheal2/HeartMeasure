package com.magcomm.heartmeasure;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.magcomm.adpter.DataBaseAdapter;
import com.magcomm.adpter.DataFragment;
import com.magcomm.adpter.DataPagerAdapter;
import com.magcomm.anim.CircleView;
import com.magcomm.anim.MultiScrollNumber;
import com.magcomm.data.BmpData;
import com.magcomm.heartmeasure.util.ImageProcessing;

public class HeartMeasureActivity extends FragmentActivity {

	private static final String TAG = "HeartRateMonitor";
	private static final AtomicBoolean processing = new AtomicBoolean(false);
	private static SurfaceView mPreview = null;
	private static SurfaceHolder mPreviewHolder = null;
	private static Camera mCamera = null;
	//	private static View image = null;
	//private static TextView text = null;
	//private static TextView text1 = null;
	//private static TextView text2 = null;
	private static WakeLock mWakeLock = null;
	private static int averageIndex = 0;
	private static final int averageArraySize = 1;
	private static final int[] averageArray = new int[averageArraySize];
	
	private ViewPager mViewPager;
	private FragmentManager mFragmentManager;
	//private DataRecyclerAdapter mDataAdapter;
	private DataBaseAdapter mDataAdapter;
	private List<Fragment> mList = new ArrayList<Fragment>();
	
	private List<BmpData> mDatas;
	private String[] mTitles;
	
	private static final int MAX = 8;
	public static final String STOP_ACTION="STOP_CIRCLE";
	
	LinearLayout mLinearL;

	public static enum TYPE {
		GREEN, RED
	};

	private static TYPE currentType = TYPE.GREEN;

	public static TYPE getCurrent() {
		return currentType;
	}

	private static int beatsIndex = 0;
	private static final int beatsArraySize = 3;
	private static final int[] beatsArray = new int[beatsArraySize];
	private static double beats = 0;
	private static long startTime = 0;
	
	private Button mMeasureButton;
	private Button mBackButton;
	private Button mSettingButton;
	
	private static final int UPDATE_CHART = 1;
	private static final int OPEN_CAMERA = 10;
	private static final int STOP_PREVIEW = 11;
	
	private static final int DELAY_TIME = 10 * 1000;
	
	private int mHeartBeats = 0;
	
	private MultiScrollNumber scrollNumber;
    private CirCleReceiver mReceiver;
    private CircleView mCircleView;
    private TextView mMinView;
    
    private SharedPreferences sp;
    private String[] bpms, dates, times;
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// 刷新图表
			super.handleMessage(msg);
			
			switch (msg.what) {
			case UPDATE_CHART:
				//updateChart();
				break;
			case OPEN_CAMERA:
				mPreviewHolder = mPreview.getHolder();
				mPreviewHolder.addCallback(mSurfaceCallback);
				mPreviewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
				
				android.util.Log.i("zhangya", "mHandler mSurfaceCallback = " + mSurfaceCallback);
				break;
			case STOP_PREVIEW:
				if (isPreview && mCamera != null) {
					mCamera.stopPreview();
					isPreview = false;
					//text.setText(getString(R.string.measure_beats_title) + mHeartBeats);
					if (mHeartBeats > 30) {
						addBPM(mHeartBeats);
					} else {
						Toast.makeText(getApplicationContext(), R.string.retry_again, Toast.LENGTH_LONG).show();
					}
					reset();
				}
				mMeasureButton.setText(isPreview ? R.string.stop_measure : R.string.start_measure);
			}
		}
	};
	
	private void reset() {
        
		mHeartBeats = 0;
		for (int i = 0; i < beatsArray.length; i++) {
			beatsArray[i] = 0;
		}
		
		for (int i = 0; i < averageArray.length; i++) {
			averageArray[i] = 0;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.heart_measure);
		
		initConfig();
	}

	boolean isPreview = false;
	
	private void initConfig() {
		RelativeLayout layout = (RelativeLayout)findViewById(R.id.heartlayout);
		
		mLinearL = (LinearLayout) findViewById(R.id.measure_layout);
		mLinearL.setBackgroundResource(R.color.blue);
		
		mMinView = (TextView) findViewById(R.id.min);
		
		mBackButton = (Button) findViewById(R.id.btn_back);
		mBackButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		mSettingButton = (Button) findViewById(R.id.btn_settings);
		mSettingButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showEditDialog();
			}
		});
		
		mMeasureButton = (Button) findViewById(R.id.measure_btn);
		mMeasureButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//mHandler.sendEmptyMessage(OPEN_CAMERA);
				android.util.Log.e("zhangya", "onClick isPreview = " + isPreview);
				
				if (!isPreview && mCamera != null) {
					Camera.Parameters parameters = mCamera.getParameters();
					parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
					mCamera.setParameters(parameters);
					mCamera.startPreview();
					isPreview = true;
					//mHandler.sendEmptyMessageDelayed(STOP_PREVIEW, DELAY_TIME);
					
					scrollNumber.setVisibility(View.INVISIBLE);
	                mCircleView.setStart();
	                mMinView.setVisibility(View.GONE);
					
				} else if (mCamera != null) {
					mCamera.stopPreview();
					isPreview = false;
					reset();
					mCircleView.setStop();
					setBackground(0);
				}
				mMeasureButton.setText(isPreview ? R.string.stop_measure : R.string.start_measure);
			}
		});

		mPreview = (SurfaceView) findViewById(R.id.preview);
		mPreviewHolder = mPreview.getHolder();
		mPreviewHolder.addCallback(mSurfaceCallback);
		mPreviewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		//text = (TextView) findViewById(R.id.text);
		//text1 = (TextView) findViewById(R.id.text1);
		//text2 = (TextView) findViewById(R.id.text2);
		
		initData();
		mFragmentManager = getSupportFragmentManager();
		mViewPager = (ViewPager) findViewById(R.id.pager);
		//mDataAdapter = new DataRecyclerAdapter(this, mData);
		
		mViewPager.setAdapter(new DataPagerAdapter(mFragmentManager, mList, mTitles));
		
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");
		
		scrollNumber = (MultiScrollNumber) findViewById(R.id.scroll_number);
        mCircleView= (CircleView) findViewById(R.id.anim_circle);
        scrollNumber.setVisibility(View.VISIBLE);
        scrollNumber.setNumber(0);
        scrollNumber.setTextSize(36);
        scrollNumber.setScrollVelocity(20);
        mReceiver=new CirCleReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction(STOP_ACTION);
        registerReceiver(mReceiver,filter);
	}
	
	private void initData() {
		sp = getSharedPreferences("heart_beats_data", Context.MODE_PRIVATE);
        bpms = getResources().getStringArray(R.array.bpms);
        dates = getResources().getStringArray(R.array.dates);
        times = getResources().getStringArray(R.array.times);
        mDatas = getFromSp();
		
		/*mDatas = new ArrayList<BmpData>();
        for ( int i = 0; i < 7; i++) {
        	BmpData data = new BmpData();
        	data.setBpm("item" + i);
        	data.setDate("2017.12." + i);
        	data.setTime("13:" + i);
        	mDatas.add(data);
        }*/
        
        /*mTitles = new ArrayList<String>();
        for (int i = 0; i < 2; i++) {
        	mTitles.add("tile " + i);
        }*/
        mTitles = getResources().getStringArray(R.array.titles);
        
        mDataAdapter = new DataBaseAdapter(this, mDatas);
        mList.add(new DataFragment(this, mDataAdapter));
		//mList.add(new Fragment());
	}
	
	private void showEditDialog() {  
        TipDialogFragment dialog = new TipDialogFragment();  
        dialog.show(getFragmentManager(), "TipDialogFragment");
    }
	
	private void addBPM(int beats) {
		String bpm = "" + beats;
		BmpData data = new BmpData();
		data.setBpm(bpm);
		data.setDate(getDate());
		data.setTime(getTime());
		
		int count = mDatas.size();
		if (count >= MAX) {
			mDatas.remove(count - 1);
			Log.i("zhangya", "1. count = " + count);
		}
		mDatas.add(0, data);
		addToSp(mDatas);
		
		Log.i("zhangya", "count = " + mDatas.size());
		mDataAdapter.notifyDataSetChanged();
	}
	
	private void addToSp(int position, BmpData data) {
		Editor editor = sp.edit();
		editor.putString(bpms[position], data.getBpm());
		editor.putString(dates[position], data.getDate());
		editor.putString(times[position], data.getTime());
		editor.commit();
	}
	
	private void addToSp(List<BmpData> datas) {
		int size = datas.size();
		Editor editor = sp.edit();
		for (int i = 0; i < size; i++) {
			BmpData data = datas.get(i);
			editor.putString(bpms[i], data.getBpm());
			editor.putString(dates[i], data.getDate());
			editor.putString(times[i], data.getTime());
		}
		editor.commit();
	}
	
	private List<BmpData> getFromSp() {
		List<BmpData> datas = new ArrayList<BmpData>();
		for (int i = 0; i < bpms.length; i++) {
			String bpm = sp.getString(bpms[i], null);
			String date = sp.getString(dates[i], null);
			String time = sp.getString(times[i], null);
			if (bpm != null) {
				BmpData data = new BmpData();
				data.setBpm(bpm);
				data.setDate(date);
				data.setTime(time);
				datas.add(data);
			} else {
				break;
			}
		}
		return datas;
	}
	
	private String getDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");    
		String date = sdf.format(new java.util.Date());
		return date;
	}
	
	private String getTime() {
		SimpleDateFormat sDateFormat = new SimpleDateFormat("HH:mm");       
		String time = sDateFormat.format(new java.util.Date());
		return time;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mWakeLock.acquire();
		startTime = System.currentTimeMillis();
	}

	@Override
	public void onPause() {
		super.onPause();
		mWakeLock.release();
		
		if (mCamera != null) {
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
	};

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	private PreviewCallback mPreviewCallback = new PreviewCallback() {

		public void onPreviewFrame(byte[] data, Camera cam) {
			calcHeartBeats(data, cam);
		}
	};
	
	private void calcHeartBeats(byte[] data, Camera cam) {
		android.util.Log.e("zhangya", "1. calcHeartBeats data = " + data);
		if (data == null) {
			throw new NullPointerException();
		}
		Camera.Size size = cam.getParameters().getPreviewSize();
		
		android.util.Log.e("zhangya", "1. size = " + size);
		if (size == null) {
			throw new NullPointerException();
		}
		
		android.util.Log.e("zhangya", "1. processing = " + processing.get());
		if (!processing.compareAndSet(false, true)) {
			return;
		}
		int width = size.width;
		int height = size.height;
		
		int imgAvg = ImageProcessing.decodeYUV420SPtoRedAvg(data.clone(),height,width);
		//text1.setText(getString(R.string.average_pixel) + String.valueOf(imgAvg));
		android.util.Log.e("zhangya", " width = " + width + ", height = " + height + ", processing = " + processing.get() + ", imgAvg = " + imgAvg);
		if (imgAvg == 0 || imgAvg == 255) {
			processing.set(false);
			return;
		}

		int averageArrayAvg = 0;
		int averageArrayCnt = 0;
		for (int i = 0; i < averageArray.length; i++) {
			if (averageArray[i] > 0) {
				averageArrayAvg += averageArray[i];
				averageArrayCnt++;
				
				android.util.Log.e("zhangya", " averageArray[" + i + "] = " + averageArray[i] + ", imgAvg = " + imgAvg);
			}
		}

		int rollingAverage = (averageArrayCnt > 0)?(averageArrayAvg/averageArrayCnt):0;
		TYPE newType = currentType;
		if (imgAvg < rollingAverage) {
			newType = TYPE.RED;
			if (newType != currentType) {
				beats++;
				//text2.setText(getString(R.string.pulse_frequency)+String.valueOf(beats));
			}
		} else if (imgAvg > rollingAverage) {
			newType = TYPE.GREEN;
		}

		if (averageIndex == averageArraySize) {
			averageIndex = 0;
		}
		averageArray[averageIndex] = imgAvg;
		averageIndex++;
		android.util.Log.e("zhangya", " averageIndex = " + averageIndex + ", rollingAverage = " + rollingAverage + ", beats = " + beats);
		// Transitioned from one state to another to the same
		if (newType != currentType) {
			currentType = newType;
			//image.postInvalidate();
		}
		//获取系统结束时间（ms）
		long endTime = System.currentTimeMillis();
		double totalTimeInSecs = (endTime - startTime) / 1000d;
		android.util.Log.e("zhangya", " totalTimeInSecs = " + totalTimeInSecs);
		if (totalTimeInSecs >= 2) {
			double bps = (beats / totalTimeInSecs);
			int dpm = (int) (bps * 60d);
			android.util.Log.e("zhangya", "1. totalTimeInSecs = " + totalTimeInSecs + ",  beats = " + beats + ", dpm = " + dpm);
			if (dpm < 30 || dpm > 180||imgAvg<200) {
				//获取系统开始时间（ms）
				startTime = System.currentTimeMillis();
				//beats心跳总数
				beats = 0;
				processing.set(false);
				return;
			}
			android.util.Log.e("zhangya", "2. totalTimeInSecs = " + totalTimeInSecs + ",  beats = "+ beats + ", dpm = " + dpm);
			if (beatsIndex == beatsArraySize) {
				beatsIndex = 0;
			}
			
			beatsArray[beatsIndex] = dpm;
			beatsIndex++;
			int beatsArrayAvg = 0;
			int beatsArrayCnt = 0;
			for (int i = 0; i < beatsArray.length; i++) {
				if (beatsArray[i] > 0) {
					beatsArrayAvg += beatsArray[i];
					beatsArrayCnt++;
				}
			}
			int beatsAvg = (beatsArrayAvg / beatsArrayCnt);
			mHeartBeats = beatsArrayAvg / beatsArrayCnt;

			startTime = System.currentTimeMillis();
			beats = 0;
		}
		processing.set(false);
	}

	private SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {

		public void surfaceCreated(SurfaceHolder holder) {
			try {
				mCamera = Camera.open();
				mCamera.setPreviewDisplay(mPreviewHolder);
				mCamera.setPreviewCallback(mPreviewCallback);
			} catch (Throwable t) {
				//Log.e("PreviewDemo-surfaceCallback","Exception in setPreviewDisplay()", t);
				
				android.util.Log.e("zhangya", " t -> " + t);
			}
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			Camera.Parameters parameters = mCamera.getParameters();
			//parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			Camera.Size size = getSmallestPreviewSize(width, height, parameters);
			if (size != null) {
				parameters.setPreviewSize(size.width, size.height);
				//Log.d(TAG, "Using width=" + size.width + " height="	+ size.height);
			}
			mCamera.setParameters(parameters);
			//mCamera.startPreview();
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// Ignore
		}
	};

	private static Camera.Size getSmallestPreviewSize(int width, int height,
			Camera.Parameters parameters) {
		Camera.Size result = null;
		for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
			if (size.width <= width && size.height <= height) {
				if (result == null) {
					result = size;
				} else {
					int resultArea = result.width * result.height;
					int newArea = size.width * size.height;
					if (newArea < resultArea)
						result = size;
				}
			}
		}
		return result;
	}
	
	private void setBackground(int beats) {
		int id = 0;
        
		mMinView.setVisibility(View.VISIBLE);
		
        if (beats >= 100) {
        	id = R.color.red;
        } else if (beats <= 60 && beats > 30) {
        	id = R.color.yellow;
        } else if (beats <= 30) {
        	id = R.color.blue;
        } else {
        	id = R.color.green;
        }
        if (id != 0) {
        	mLinearL.setBackgroundResource(id);
        }
	}
	
	class CirCleReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().toString().equals(STOP_ACTION)){
                scrollNumber.setVisibility(View.VISIBLE);
                scrollNumber.setNumber(mHeartBeats);
                mHandler.sendEmptyMessage(STOP_PREVIEW);
               
                setBackground(mHeartBeats);
            }
        }
    }
}