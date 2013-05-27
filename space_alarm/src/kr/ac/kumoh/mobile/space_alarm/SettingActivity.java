package kr.ac.kumoh.mobile.space_alarm;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

public class SettingActivity extends Activity {
	DBManager dbManager;
	String mDst = "备固";   //格利瘤
	String mLabel; //力格
	String mBell;
	double mDstLat;
	double mDstLng;
	int mArea = 500;     
	int mTurn = 1; //积己矫 舅恩 累悼
	int mBiv;
	Context context = this;
	
	private CheckBox checkBiv;
	private EditText editLabel;
	private SeekBar seekArea;
	private int brightness = 50;
	private TextView dstText;
	boolean isChecked;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		
		dbManager = new DBManager(this);
		
		dstText = (TextView)findViewById(R.id.displayDestination);
		findViewById(R.id.cancelBtn).setOnClickListener(listener);
		findViewById(R.id.dstBtn).setOnClickListener(listener);
		findViewById(R.id.bellBtn).setOnClickListener(listener);
		editLabel = (EditText) findViewById (R.id.editText1);
		findViewById(R.id.saveBtn).setOnClickListener(listener);
		
		seekArea = (SeekBar)findViewById(R.id.seekBar1);
		seekArea.setProgress(brightness);
		seekArea.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
				mArea = (progress*10) + 1; 
				printSelected(mArea);
				
			}
			public void onStartTrackingTouch(SeekBar arg0) { }

			public void onStopTrackingTouch(SeekBar seekBar) { }

		});
	
	
		checkBiv = (CheckBox)findViewById(R.id.checkBox1);
		checkBiv.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
				if(checked){
					isChecked = true;
				}
				else {
					isChecked = false;
				}
				
			}
		});
		
		if(isChecked) {
			checkBiv.setChecked(true);
		}
		else {
			checkBiv.setChecked(false);
		}
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == 1){
			mDst = data.getStringExtra("dst");
			dstText.setText(mDst);
			mDstLat = data.getDoubleExtra("dstLat", 0);
			mDstLng = data.getDoubleExtra("dstLng", 0);
			
		}
	}

	public void printSelected(int value) {
		TextView tv = (TextView) findViewById(R.id.textView6);
		tv.setText(String.valueOf(value) + "M");
	}
	
	OnClickListener listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.saveBtn:
				mLabel = editLabel.getText().toString();
				dbManager.appendList(mLabel, mDst, mArea, mTurn,mDstLat,mDstLng);
				finish();
				break;
			case R.id.cancelBtn:
				finish();
				break;
			case R.id.dstBtn:
				Intent mapintent = new Intent(context, MapActivity.class);
				startActivityForResult(mapintent, 1);
				break;
			case R.id.bellBtn:
				break;
			
			}
			
		}
	};
	
	
	
	
}


