package kr.ac.kumoh.mobile.space_alarm;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapLocationManager;
import com.nhn.android.maps.NMapOverlay;
import com.nhn.android.maps.NMapOverlayItem;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.NMapActivity.OnDataProviderListener;
import com.nhn.android.maps.NMapView.OnMapStateChangeListener;
import com.nhn.android.maps.NMapView.OnMapViewTouchEventListener;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.nmapmodel.NMapError;
import com.nhn.android.maps.nmapmodel.NMapPlacemark;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPOIitem;
import com.nhn.android.mapviewer.overlay.NMapCalloutCustomOverlay;
import com.nhn.android.mapviewer.overlay.NMapCalloutOverlay;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager.OnCalloutOverlayListener;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay.OnFloatingItemChangeListener;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay.OnStateChangeListener;

import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MapActivity extends NMapActivity implements
		OnMapStateChangeListener, OnMapViewTouchEventListener {

	
	public static final String API_KEY = "35d7d1fdc18c883d9ad3bd73217564c4"; // api-key
	private NMapView mMapView = null; 						// ���̹� �� ��ü
	private NMapController mMapController = null; 			// �� ��Ʈ�ѷ�
	private NMapOverlayManager mOverlayManager; 			// ���� ���� ǥ�õǴ� �������� ��ü�� ����
	private NMapLocationManager mMapLocationManager; 		// ��ġ ������
	private NMapViewerResourceProvider mMapViewerResourceProvider = null;
	LinearLayout mMapContainer; 							// ���� �߰��� ���̾ƿ�

	
	private NMapPOIitem mFloatingPOIitem; 				// ���� ���� ǥ�õǴ� POI ������ Ŭ����
	private NMapPOIdataOverlay mFloatingPOIdataOverlay; // ���� ���� �������� �������� ������ �� �ִ� �������� Ŭ����
	private NGeoPoint mMyGeoPoint; 						// ���� ���� ������ ��ǥ�� ��Ÿ���� Ŭ����
	private NMapPOIdata mDestinationPoiData; 			// ���� ���� ǥ�õǴ� POI �������� �����ϴ� Ŭ����
	private NMapCalloutOverlay mMapCalloutOverlay;		// ���� ���� �������� ������ ���� �� ǥ�õǴ� ��ǳ�� ���������� �߻� Ŭ����
	private OnCalloutOverlayListener onCalloutOverlayListener;
	
	EditText edit01; // ����� �˻��� �Է� ���� �ؽ�Ʈ
	Button show_btn; // �˻� ��ư
	String destination;
	double dstLat, dstLng;
	
	
	
	ArrayList<MyItem> myItemArrayList = new ArrayList<MyItem>(); // MyItem �� ArrayList
	AlertDialog searchedResultDialog; // �˻� ��� ��� ���̾�α�
	AlertDialog selectionDialog;
	MyAdapter adapter = new MyAdapter(this, R.layout.map_activity //���̾�α׿� ��� ��� ������ ������ ���� �����
			);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity);

		edit01 = (EditText) findViewById(R.id.edit01);
		show_btn = (Button) findViewById(R.id.show_btn);
		
		show_btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
	
				// ����ڰ� �Է��� �ּ� ���� Ȯ��
				String userInput = edit01.getText().toString();

				// ����Ʈ �� ������ �ʱ�ȭ
				adapter.itemClear();

				// �˻��� �Ľ�
				parcingSerchedWord(userInput);

				// �˻���� ����Ʈ�並 ���̾�α׷� ���
				printParcedData();

			}
		});
		
		
		
	
		
		initializeNMap(); // �� �ʱ�ȭ
		displayTouchedLocation(); // ��ġ�� �� ��Ŀ ǥ�� �Լ� ȣ��
		


	}

	
	
	// �� �ʱ�ȭ �Լ�
	private void initializeNMap() {

		mMapView = new NMapView(this); // mMapView ��ü ����
		mMapContainer = (LinearLayout) findViewById(R.id.mMapContainer);

		mMapView.setApiKey(API_KEY);
		mMapContainer.addView(mMapView);
		mMapView.setClickable(true);
		mMapView.setEnabled(true);
		mMapView.setFocusable(true);
		mMapView.setBuiltInZoomControls(true, null);
		mMapView.setFocusableInTouchMode(true);
		mMapView.requestFocus();
		mMapView.setOnMapStateChangeListener(this);
		mMapView.setOnMapViewTouchEventListener(this);
		mMapController = mMapView.getMapController();


		mMapViewerResourceProvider = new NMapViewerResourceProvider(this);
		// create overlay manager
		mOverlayManager = new NMapOverlayManager(this, mMapView,
				mMapViewerResourceProvider);
		mMapLocationManager = new NMapLocationManager(this);
		


		// ��ǥ�� �ּҷ� ��ȯ�ϴ� API ȣ�� �� ���� ���信 ���� �ݹ� �������̽� : onDataProviderListener
		// placemark ��ü���� ��û�� ��ǥ�� ���� �ּ� �����͸� ����
		// findPlacemarkAtLocation() : ��ǥ�� �ּҸ� ��ȯ�ϴ� ���� API�� ȣ��
		super.setMapDataProviderListener(new OnDataProviderListener() {

			@Override
			public void onReverseGeocoderResponse(NMapPlacemark placeMark,
					NMapError errInfo) {
				// TODO Auto-generated method stub
				if (errInfo != null) {
					Log.e("", "Failed to findPlacemarkAtLocation: error="
							+ errInfo.toString());

					return;
				}

				Log.i("",
						"onReverseGeocoderResponse: placeMark="
								+ placeMark.toString());

				if (mFloatingPOIitem != null && mFloatingPOIdataOverlay != null) {
					mFloatingPOIdataOverlay.deselectFocusedPOIitem();

					if (placeMark != null) {
						destination = placeMark.toString();
						mFloatingPOIitem.setTitle(destination);
					}
					mFloatingPOIdataOverlay.selectPOIitemBy(
							mFloatingPOIitem.getId(), false);
				}

			}

		});

	}
///////////////////////////
// Start �ÿ� ���� ��ġ�� �̵�
	protected void onStart() {
		super.onStart();

		moveMyLocation();
	}
///////////////////////////
// ���� �� ��ġ�� �̵��ϴ� �Լ�
	protected void moveMyLocation() {

		// ���� ��ġ Ž�� ���� : enableMyLocation (ã�� ���Ѵٸ� false ��ȯ)
		boolean isMyLocationEnabled = mMapLocationManager
				.enableMyLocation(false);
		// enableMyLocation() ���� ���� false��� ���������� Ž���� ��ġ�� ������� �ʴ´�.
		if (!isMyLocationEnabled) {
			Toast.makeText(this,
					"Please enable a My Location source in system settings",
					Toast.LENGTH_LONG).show();
			Intent goToSettings = new Intent(
					Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(goToSettings);
			return;
		}

		// ���� ��ġ ���� ���� �� ȣ��Ǵ� �ݹ� �������̽�
		mMapLocationManager
				.setOnLocationChangeListener(new NMapLocationManager.OnLocationChangeListener() {

					// ���� ��ġ ���� �� ȣ�� : onLocationChanged
					// myLocation ��ü�� ����� ��ǥ�� ���޵ȴ�
					public boolean onLocationChanged(
							NMapLocationManager locationManager,
							NGeoPoint myLocation) {

						mMyGeoPoint = myLocation;
						 // ���� �� ��ġ�� �� �߽��̵�
						mMapController.setMapCenter(mMyGeoPoint, 17);
						markCurrentLocation(); // �� ��ġ ��ũ�� ǥ�� �Լ� ȣ��
						stopMyLocation();
						return true;
					}

					// ������ �ð� ���� ���� ��ġ Ž�� ���� �� ȣ��
					public void onLocationUnavailableArea(
							NMapLocationManager arg0, NGeoPoint arg1) {
						Toast.makeText(
								MapActivity.this,
								"Your current location is temporarily unavailable.",
								Toast.LENGTH_LONG).show();
						stopMyLocation();
					}

					// ���� ��ġ�� ���� �� ǥ���� �� �ִ� ������ ����� ��쿡 ȣ��
					public void onLocationUpdateTimeout(NMapLocationManager arg0) {
						Toast.makeText(MapActivity.this,
								"Your current location is unavailable area.",
								Toast.LENGTH_LONG).show();
						stopMyLocation();
					}
				});
	}
/////////////////////////
// �� ��ġ ã�⸦ �ߴ��ϴ� �Լ�
	private void stopMyLocation() {

		// disableMylocatio() : ���� ��ġ Ž���� ����
		mMapLocationManager.disableMyLocation();

	}
/////////////////////////
// ���� ��ġ ��Ŀ ǥ���ϴ� �Լ�
	private void markCurrentLocation() {

		NMapPOIdata poiData = new NMapPOIdata(1, mMapViewerResourceProvider);
		poiData.beginPOIdata(1);
		poiData.addPOIitem(mMyGeoPoint, "������ġ", NMapPOIflagType.FROM, 0);
		poiData.endPOIdata();

		NMapPOIdataOverlay poiDataOverlay = mOverlayManager
				.createPOIdataOverlay(poiData, null);
		poiDataOverlay.showAllPOIdata(0);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
/////////////////////////
// �� ����� Ŭ���� (�ؽ�Ʈ �� �ΰ� ����)
	class MyAdapter extends BaseAdapter {
	

		public MyAdapter(Context context, int layoutRes) {
			
		}

		@Override
		// �����ϰ� �ִ� �������� ����
		public int getCount() {

			// TODO Auto-generated method stub
			return myItemArrayList.size();
		}

		@Override
		// �����Ͱ�
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return myItemArrayList.get(position);
		}
		// ���� ��������
		public double get_lat(int position) {
			return myItemArrayList.get(position).m_lat;
		}
		// �浵 ��������
		public double get_lng(int position) {
			return myItemArrayList.get(position).m_lng;
		}
		// ��ȣ�� �������� 
		public String get_title(int position) {
			return myItemArrayList.get(position).m_title;
		}

		@Override
		// �ε�����
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		// ������ �������� ������ �並 ����
		public View getView(int position, View converView, ViewGroup parent) {
			final int index = position;
			item_list layout = new item_list(getApplicationContext());

			layout.setTitle(myItemArrayList.get(index).m_title);
			layout.setAddress(myItemArrayList.get(index).m_address);
			return layout;

		}
		// Arraylist �ʱ�ȭ 
		public void itemClear() {
			myItemArrayList.clear();
		}

	}
/////////////////////////
// �˻� �ܾ� xml�Ľ� �Լ� 
	private void parcingSerchedWord(String userInput) {

		GeoTransPoint beforeConverting;
		GeoTransPoint afterConverting;
		Double lat;
		Double lng;

		boolean inItem = false, inTitle = false, inAddress = false, inMapx = false, inMapy = false;
		String title = null, address = null, mapx = null, mapy = null;
		String parced_title = null;
		String query = userInput;// �Էµ� ����

		try {
			// �ѱ� �Է� �����ϵ��� UTF-8�� ���ڵ�
			query = URLEncoder.encode(query, "UTF-8");
			// ��û URL
			URL url = new URL("http://openapi.naver.com/search?"
					+ "key=15627643be13f3021fdfac57371b368f" + "&query="
					+ query 
					+ "&target=local&start=1&display=30");
			//key : �߱޹��� Ű
			//query : �˻���
			//display : �˻���� �Ǽ�
			//target : ���� �˻����� �ݵ�� local�� ����
			
			
			//xmlpullparser��ü�� �����ϱ����� ���丮Ŭ������ 
	        //newinstance�޼ҵ带 �̿��Ͽ� newpullparset�޼ҵ带 ���� ��ü�� ����.
			XmlPullParserFactory parserCreator = XmlPullParserFactory
					.newInstance();
			XmlPullParser parser = parserCreator.newPullParser();

			// �Ľ��ϱ� ���� ��Ʈ�� ����
			parser.setInput(url.openStream(), null);
			// �Ľ��� ������ Ÿ���� �˷���
			int parserEvent = parser.getEventType();

			while (parserEvent != XmlPullParser.END_DOCUMENT) {
				switch (parserEvent) {
				case XmlPullParser.START_TAG: // parser�� ���� �±׸� ������ ����
					if (parser.getName().equals("item")) {
						inItem = true;
					}
					if (parser.getName().equals("title")) { 
					// <title> ������ ������ ���� �� �ֵ��� true
						inTitle = true;
					}
					if (parser.getName().equals("address")) { 
					// <address> ������ ������ ���� �� �ֵ��� true
						inAddress = true;
					}
					if (parser.getName().equals("mapx")) { 
					// <mapx> ������ ������ ���� �� �ֵ��� true
						inMapx = true;
					}
					if (parser.getName().equals("mapy")) { 
					// <mapy> ������ ������ ���� �� �ֵ��� true
						inMapy = true;
					}
					if (parser.getName().equals("message")) { 
						// message �±׸� ������  ���� ���
						// ���⿡ �����ڵ忡 ���� �ٸ� �޼����� ����ϵ��� �� �� �ִ�.
					}
					break;

				case XmlPullParser.TEXT:// parser�� ���뿡 ����������
					if (inItem) {
						if (inTitle) { // isTitle�� true�� �� �±��� ������ ����.
							title = parser.getText();
							for (int i = 0; i < title.length(); i++) {
								if (title.charAt(i) == '<') {
									parced_title = bParcing(title);
									break;
								} else if (i == (title.length() - 1)) {
									parced_title = title;
								}
							}

							inTitle = false;
							inItem = false;
						}
					}
					if (inAddress) { // isAddress�� true�� �� �±��� ������ ����.
						address = parser.getText();

						inAddress = false;
					}
					if (inMapx) { // isMapx�� true�� �� �±��� ������ ����.
						mapx = parser.getText();

						inMapx = false;
					}
					if (inMapy) { // isMapy�� true�� �� �±��� ������ ����.
						mapy = parser.getText();

						inMapy = false;
					}
					break;
				case XmlPullParser.END_TAG:
					if (parser.getName().equals("item")) {
						// ��ǥ��ȯ
						//KTM(ī����ǥ) -> WGS84(����,�浵 ��ǥ)
						beforeConverting = new GeoTransPoint(
								Double.parseDouble(mapx),
								Double.parseDouble(mapy));
						afterConverting = GeoTrans.convert(GeoTrans.KATEC,
								GeoTrans.GEO, beforeConverting);
						lat = afterConverting.getX();
						lng = afterConverting.getY();

						// �Ľ̵� ���� ����
						myItemArrayList.add(new MyItem(parced_title, address,
								lat, lng));

						inItem = false;
					}
					break;
				}
				parserEvent = parser.next();
			}

		} catch (Exception e) {
			
		}
	}
/////////////////////////	
// <bn> </bn> �±׸� �Ľ��ϴ� �Լ� 
	String bParcing(String title) {
		String temp_title = null, parced_title = null;
		temp_title = title.replace("<b>", ""); //String�� replace�Լ� ���
		parced_title = temp_title.replace("</b>", "");
		return parced_title;
	}
/////////////////////////
// �Ľ̵� ��� ���̾�α׷� ����ϴ� �Լ�
	void printParcedData() {

		searchedResultDialog = new AlertDialog.Builder(this).setTitle("�˻����")
		// ����� ���� (���̾�α׿� ����ͷ� ����ϱ� ���� setAdapter)
				.setAdapter(adapter, new DialogInterface.OnClickListener() {
					@Override
					// ���̾�α� Ŭ�� �̺�Ʈ ó��
					public void onClick(DialogInterface dialog, int which) {
						double lat = adapter.get_lat(which);
						double lng = adapter.get_lng(which);
						String location = adapter.get_title(which);

						// ���õ� ������ �߽��� �̵�
						mMapController.setMapCenter(lat, lng, 11);
						// �߽��� ǥ��
						displaySelectedLocation(lat, lng, location);
						// System.out.println(lang);

					}
				}).create();
		searchedResultDialog.show();
	}
/////////////////////////
// �˻����� ���õ� ���� ��ũǥ���ϴ� �Լ�
	private void displaySelectedLocation(double lat, double lng, String title) {

		
		destination = title;
		dstLat = lat;
		dstLng = lng;
		// ���� PoiData�� ������ ��� ����.
		if (mDestinationPoiData != null)
			mDestinationPoiData.removeAllPOIdata();

		// ���õ� ��ġ�� ��ũǥ��
		mDestinationPoiData = new NMapPOIdata(1, mMapViewerResourceProvider);
		mDestinationPoiData.beginPOIdata(1);
		NMapPOIitem item = mDestinationPoiData.addPOIitem(lat, lng, "������",
				NMapPOIflagType.TO, 0);

		// ���õ� ��ġ�� ��ȣ������ ��ǳ�� ǥ��
		item.setTitle(title);
		if (item != null) {

			// ���õ� ��ġ�� ��Ŀ�� ��ġ�Ͽ� �̵���ų �� ����.
			item.setFloatingMode(NMapPOIitem.FLOATING_TOUCH
					| NMapPOIitem.FLOATING_DRAG);
			// show right button on callout
			// ��Ŀ�� ��ǳ�� ��ư �߰�
			item.setRightButton(true);
			mFloatingPOIitem = item;

		}
		mDestinationPoiData.endPOIdata();

		NMapPOIdataOverlay poiDataOverlay = mOverlayManager
				.createPOIdataOverlay(mDestinationPoiData, null);

		if (poiDataOverlay != null) {
			// poiDataOverlay.setOnFloatingItemChangeListener(onPOIdataFloatingItemChangeListener);

			// set event listener to the overlay
			poiDataOverlay.setOnStateChangeListener(onstatechanglistener);
			poiDataOverlay.deselectFocusedPOIitem();
			poiDataOverlay.selectPOIitem(0, false);

			mFloatingPOIdataOverlay = poiDataOverlay;
			if (mFloatingPOIitem != null && mFloatingPOIdataOverlay != null) {
				mFloatingPOIdataOverlay.deselectFocusedPOIitem();
			}
		}

	}

/////////////////////////
// ��ġ�� ��ġ ��ũǥ�� �Լ�
	public void displayTouchedLocation() {
		
		mDestinationPoiData = new NMapPOIdata(1, mMapViewerResourceProvider);
		mDestinationPoiData.beginPOIdata(1);
		NMapPOIitem item = mDestinationPoiData.addPOIitem(null,
				"Touch & Drag to Move", NMapPOIflagType.TO, 0);
		// initialize location to the center of the map view.
		if (item != null) {

			item.setPoint(mMapController.getMapCenter());
			item.setFloatingMode(NMapPOIitem.FLOATING_TOUCH
					| NMapPOIitem.FLOATING_DRAG);
			item.setRightButton(true);
			mFloatingPOIitem = item;
		}
		mDestinationPoiData.endPOIdata();

		NMapPOIdataOverlay poiDataOverlay = mOverlayManager
				.createPOIdataOverlay(mDestinationPoiData, null);
		if (poiDataOverlay != null) {
			// poiDataOverlay.setOnFloatingItemChangeListener(onPOIdataFloatingItemChangeListener);

			// set event listener to the overlay
			// poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);

			poiDataOverlay.selectPOIitem(0, false);

			mFloatingPOIdataOverlay = poiDataOverlay;
		}
		poiDataOverlay.setOnStateChangeListener(onstatechanglistener);
		
		
		poiDataOverlay
				.setOnFloatingItemChangeListener(new OnFloatingItemChangeListener() {

					@Override
					public void onPointChanged(
							NMapPOIdataOverlay poiDataOverlay, NMapPOIitem item) {
						// TODO Auto-generated method stub

						NGeoPoint point = item.getPoint();
						dstLat = point.latitude;
						dstLng = point.longitude;
						findPlacemarkAtLocation(dstLng,dstLat);
						Log.i("pointchanged",
								"onPointChanged: point=" + point.toString());
					}
				});
	}

	
	OnStateChangeListener onstatechanglistener = new OnStateChangeListener() {
		
		public void onFocusChanged(NMapPOIdataOverlay arg0, NMapPOIitem arg1) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onCalloutClick(NMapPOIdataOverlay poiDataOverlay, NMapPOIitem poiItem) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(MapActivity.this, SettingActivity.class);
			intent.putExtra("dst", destination);
			intent.putExtra("dstLat", dstLat);
			intent.putExtra("dstLng", dstLng);
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	};

	
	
	
	
	
	
	
	// ���� ���� ���� �� ȣ��Ǵ� �ݹ� �������̽��� �������̵��� �ʿ��� �Լ���

	@Override
	public void onAnimationStateChange(NMapView arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMapCenterChange(NMapView arg0, NGeoPoint arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMapCenterChangeFine(NMapView arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMapInitHandler(NMapView arg0, NMapError arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onZoomLevelChange(NMapView arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	// �������� ��ġ �̺�Ʈ �߻� �� ȣ��Ǵ� �ݹ� �������̽��� �������̵��� �ʿ��� �Լ���

	@Override
	public void onLongPress(NMapView arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLongPressCanceled(NMapView arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScroll(NMapView arg0, MotionEvent arg1, MotionEvent arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSingleTapUp(NMapView arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTouchDown(NMapView arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTouchUp(NMapView arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub

	}

}
