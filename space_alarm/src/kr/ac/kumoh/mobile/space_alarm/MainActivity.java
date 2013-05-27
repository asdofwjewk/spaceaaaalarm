package kr.ac.kumoh.mobile.space_alarm;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;



public class MainActivity extends Activity {

	LocationManager locManager;
	LocationListener locationListener;
	LocationReceiver receiver;		// 브로드캐스트 리시버의 인스턴스 정의
	
	private Button mAdd;
	Context context = this;

	private ArrayList<String> item;
	private ArrayList<Double> Aitem;
	Context mContext;
	DBManager dbManager;
	AlertDialog.Builder alertDialog;

	ListView lv;
	CustomAdapter custom_adapter;
	PendingIntent proximityIntent;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		 
		
		mContext = getApplicationContext();


		lv = (ListView)findViewById(R.id.listView1);

		dbManager = new DBManager(this);
		reflashList(dbManager, lv, mContext);


		mAdd = (Button)findViewById(R.id.insert);
		mAdd.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(context, SettingActivity.class);
				startActivity(intent);
			}
		});

	}

	@Override
	protected void onRestart() {
		super.onRestart();
		reflashList(dbManager, lv, context);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	//알람 리스트를 재조회
	public void reflashList(DBManager manager, ListView lv, Context mContext) {
		Cursor cursor = manager.fetchAllLists();
		item = new ArrayList<String>();

		if(cursor.moveToFirst()){
			do{
				item.add(cursor.getString(0));
			}while(cursor.moveToNext());
		}

		custom_adapter = new CustomAdapter(mContext, R.layout.chkbox, item);
		lv.setAdapter(custom_adapter);


	}



	class CustomAdapter extends BaseAdapter {
		ArrayList<String> adapter_item;
		Context context;
		int layout;
		ViewHolder holder;
		boolean[] isChecked;
		AlertDialog dialog;


		public CustomAdapter(Context mContext, int list, ArrayList<String> item) {
			context = mContext;
			layout = list;
			adapter_item = item;
		}



		@Override
		public int getCount() {
			return adapter_item.size();
		}

		@Override
		public Object getItem(int position) {
			return adapter_item.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if(convertView == null) {
				LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(layout, parent, false);
				holder = new ViewHolder();
				holder.text = (TextView)convertView.findViewById(R.id.textView1);
				holder.check = (CheckBox)convertView.findViewById(R.id.checkBox1);
				convertView.setTag(holder);

			}
			else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.text.setText(adapter_item.get(position));

			isChecked = new boolean[adapter_item.size()];


			//체크가 변경됐을 때 리스너
			holder.check.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
					String title = getItem(position).toString();
					if(checked){
						double lat, lng, dis;
						
						
						Cursor cursor = dbManager.selectAlarm(title);
						cursor.moveToFirst();
						Aitem = new ArrayList<Double>();
						
						Aitem.add(cursor.getDouble(2));
						Aitem.add(cursor.getDouble(4));
						Aitem.add(cursor.getDouble(5));
						
						dis = Aitem.get(0);
						lat = Aitem.get(1);
						lng = Aitem.get(2);
						
						
						locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
						
						locationListener = new LocationListener(){

							@Override
							public void onLocationChanged(Location location) {
								// TODO Auto-generated method stub
								
							}

							@Override
							public void onProviderDisabled(String provider) {
								// TODO Auto-generated method stub
								
							}

							@Override
							public void onProviderEnabled(String provider) {
								// TODO Auto-generated method stub
								
							}

							@Override
							public void onStatusChanged(String provider,
									int status, Bundle extras) {
								// TODO Auto-generated method stub
								
							}
							 
						 };
						 
						 locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
									2000, 0, 
									locationListener);
						Toast.makeText(context, title + " 시작한다", Toast.LENGTH_LONG).show();
						
						setupProximityAlert(lat, lng, dis);
						
						isChecked[position] = true;
					}
					else {
				    	locManager.removeUpdates(locationListener);
				    	unregisterReceiver(receiver);
				    	locManager.removeProximityAlert(proximityIntent); 
						Toast.makeText(context, title + " 종료한다", Toast.LENGTH_LONG).show();
						isChecked[position] = false;
					}

				}
			});


			if(isChecked[position]) {
				holder.check.setChecked(true);
			}
			else {
				holder.check.setChecked(false);
			}


			holder.check.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {

					AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

					alertDialog.setTitle("삭제");
					alertDialog.setMessage("삭제할래?");
					alertDialog.setCancelable(true);


					alertDialog.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dbManager.deleteList(getItem(position).toString());
							reflashList(dbManager, lv, mContext);
						}
					});

					alertDialog.setNegativeButton(android.R.string.no,
							new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub

						}
					});

					alertDialog.setOnCancelListener(
							new DialogInterface.OnCancelListener() {

								@Override
								public void onCancel(DialogInterface dialog) {
									// TODO Auto-generated method stub

								}
							});

					alertDialog.show();

					return true;
				}
			});

			return convertView;
		}
	}
	
	
	 private void setupProximityAlert(double lat, double lng, double dis) {
	        // 브로드캐스트 리시버가 메시지를 받을 수 있도록 설정
	        // 액션이 com.androidhuman.exmple.Location인 브로드캐스트 메시지를 받도록 설정
	        receiver = new LocationReceiver();
	        IntentFilter filter = new IntentFilter("kr.ac.kumoh.mobile.space_alarm");
	        registerReceiver(receiver, filter);
	        
	        // ProximityAlert 등록
	        Intent intent = new Intent("kr.ac.kumoh.mobile.space_alarm");
	        proximityIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
	        locManager.addProximityAlert(lat, lng, (float)dis, -1, proximityIntent);
	        Toast.makeText(getApplicationContext(),"위도 : " + lat 
    					+ " 경도 : " + lng + "반경 : " + (float)dis, 1000).show();
	       
	    }
}
class ViewHolder {
	TextView text;
	CheckBox check;

}

