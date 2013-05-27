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
	private NMapView mMapView = null; 						// 네이버 맵 객체
	private NMapController mMapController = null; 			// 맵 컨트롤러
	private NMapOverlayManager mOverlayManager; 			// 지도 위에 표시되는 오버레이 객체를 관리
	private NMapLocationManager mMapLocationManager; 		// 위치 관리자
	private NMapViewerResourceProvider mMapViewerResourceProvider = null;
	LinearLayout mMapContainer; 							// 맵을 추가할 레이아웃

	
	private NMapPOIitem mFloatingPOIitem; 				// 지도 위에 표시되는 POI 아이템 클래스
	private NMapPOIdataOverlay mFloatingPOIdataOverlay; // 여러 개의 오버레이 아이템을 포함할 수 있는 오버레이 클래스
	private NGeoPoint mMyGeoPoint; 						// 지도 상의 경위도 좌표를 나타내는 클래스
	private NMapPOIdata mDestinationPoiData; 			// 지도 위에 표시되는 POI 아이템을 관리하는 클래스
	private NMapCalloutOverlay mMapCalloutOverlay;		// 지도 위의 오버레이 아이템 선택 시 표시되는 말풍선 오버레이의 추상 클래스
	private OnCalloutOverlayListener onCalloutOverlayListener;
	
	EditText edit01; // 사용자 검색어 입력 에딧 텍스트
	Button show_btn; // 검색 버튼
	String destination;
	double dstLat, dstLng;
	
	
	
	ArrayList<MyItem> myItemArrayList = new ArrayList<MyItem>(); // MyItem 형 ArrayList
	AlertDialog searchedResultDialog; // 검색 결과 띄울 다이얼로그
	AlertDialog selectionDialog;
	MyAdapter adapter = new MyAdapter(this, R.layout.map_activity //다이얼로그에 띄울 뷰와 데이터 관리를 위한 어댑터
			);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity);

		edit01 = (EditText) findViewById(R.id.edit01);
		show_btn = (Button) findViewById(R.id.show_btn);
		
		show_btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
	
				// 사용자가 입력한 주소 정보 확인
				String userInput = edit01.getText().toString();

				// 리스트 뷰 아이템 초기화
				adapter.itemClear();

				// 검색어 파싱
				parcingSerchedWord(userInput);

				// 검색결과 리스트뷰를 다이얼로그로 출력
				printParcedData();

			}
		});
		
		
		
	
		
		initializeNMap(); // 맵 초기화
		displayTouchedLocation(); // 터치된 곳 마커 표시 함수 호출
		


	}

	
	
	// 맵 초기화 함수
	private void initializeNMap() {

		mMapView = new NMapView(this); // mMapView 객체 생성
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
		


		// 좌표를 주소로 변환하는 API 호출 시 서버 응답에 대한 콜백 인터페이스 : onDataProviderListener
		// placemark 객체에는 요청한 좌표에 대한 주소 데이터를 포함
		// findPlacemarkAtLocation() : 좌표를 주소를 변환하는 서버 API를 호출
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
// Start 시에 현재 위치로 이동
	protected void onStart() {
		super.onStart();

		moveMyLocation();
	}
///////////////////////////
// 현재 내 위치로 이동하는 함수
	protected void moveMyLocation() {

		// 현재 위치 탐색 시작 : enableMyLocation (찾지 못한다면 false 반환)
		boolean isMyLocationEnabled = mMapLocationManager
				.enableMyLocation(false);
		// enableMyLocation() 인자 값이 false라면 마지막으로 탐색된 위치를 사용하지 않는다.
		if (!isMyLocationEnabled) {
			Toast.makeText(this,
					"Please enable a My Location source in system settings",
					Toast.LENGTH_LONG).show();
			Intent goToSettings = new Intent(
					Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(goToSettings);
			return;
		}

		// 현재 위치 상태 변경 시 호출되는 콜백 인터페이스
		mMapLocationManager
				.setOnLocationChangeListener(new NMapLocationManager.OnLocationChangeListener() {

					// 현재 위치 변경 시 호출 : onLocationChanged
					// myLocation 객체에 변경된 좌표가 전달된다
					public boolean onLocationChanged(
							NMapLocationManager locationManager,
							NGeoPoint myLocation) {

						mMyGeoPoint = myLocation;
						 // 현재 내 위치로 맵 중심이동
						mMapController.setMapCenter(mMyGeoPoint, 17);
						markCurrentLocation(); // 내 위치 마크로 표시 함수 호출
						stopMyLocation();
						return true;
					}

					// 정해진 시간 내에 현재 위치 탐색 실패 시 호출
					public void onLocationUnavailableArea(
							NMapLocationManager arg0, NGeoPoint arg1) {
						Toast.makeText(
								MapActivity.this,
								"Your current location is temporarily unavailable.",
								Toast.LENGTH_LONG).show();
						stopMyLocation();
					}

					// 현재 위치가 지도 상에 표시할 수 있는 범위를 벗어나는 경우에 호출
					public void onLocationUpdateTimeout(NMapLocationManager arg0) {
						Toast.makeText(MapActivity.this,
								"Your current location is unavailable area.",
								Toast.LENGTH_LONG).show();
						stopMyLocation();
					}
				});
	}
/////////////////////////
// 내 위치 찾기를 중단하는 함수
	private void stopMyLocation() {

		// disableMylocatio() : 현재 위치 탐색을 종료
		mMapLocationManager.disableMyLocation();

	}
/////////////////////////
// 현재 위치 마커 표시하는 함수
	private void markCurrentLocation() {

		NMapPOIdata poiData = new NMapPOIdata(1, mMapViewerResourceProvider);
		poiData.beginPOIdata(1);
		poiData.addPOIitem(mMyGeoPoint, "현재위치", NMapPOIflagType.FROM, 0);
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
// 내 어댑터 클래스 (텍스트 뷰 두개 포함)
	class MyAdapter extends BaseAdapter {
	

		public MyAdapter(Context context, int layoutRes) {
			
		}

		@Override
		// 관리하고 있는 아이템의 갯수
		public int getCount() {

			// TODO Auto-generated method stub
			return myItemArrayList.size();
		}

		@Override
		// 데이터값
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return myItemArrayList.get(position);
		}
		// 위도 가져오기
		public double get_lat(int position) {
			return myItemArrayList.get(position).m_lat;
		}
		// 경도 가져오기
		public double get_lng(int position) {
			return myItemArrayList.get(position).m_lng;
		}
		// 상호명 가져오기 
		public String get_title(int position) {
			return myItemArrayList.get(position).m_title;
		}

		@Override
		// 인덱스값
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		// 각각의 아이템을 보여줄 뷰를 결정
		public View getView(int position, View converView, ViewGroup parent) {
			final int index = position;
			item_list layout = new item_list(getApplicationContext());

			layout.setTitle(myItemArrayList.get(index).m_title);
			layout.setAddress(myItemArrayList.get(index).m_address);
			return layout;

		}
		// Arraylist 초기화 
		public void itemClear() {
			myItemArrayList.clear();
		}

	}
/////////////////////////
// 검색 단어 xml파싱 함수 
	private void parcingSerchedWord(String userInput) {

		GeoTransPoint beforeConverting;
		GeoTransPoint afterConverting;
		Double lat;
		Double lng;

		boolean inItem = false, inTitle = false, inAddress = false, inMapx = false, inMapy = false;
		String title = null, address = null, mapx = null, mapy = null;
		String parced_title = null;
		String query = userInput;// 입력된 문자

		try {
			// 한글 입력 가능하도록 UTF-8로 인코딩
			query = URLEncoder.encode(query, "UTF-8");
			// 요청 URL
			URL url = new URL("http://openapi.naver.com/search?"
					+ "key=15627643be13f3021fdfac57371b368f" + "&query="
					+ query 
					+ "&target=local&start=1&display=30");
			//key : 발급받은 키
			//query : 검색어
			//display : 검색결과 건수
			//target : 지역 검색으로 반드시 local로 지정
			
			
			//xmlpullparser객체를 생성하기위해 팩토리클래스의 
	        //newinstance메소드를 이용하여 newpullparset메소드를 통해 객체를 얻어옴.
			XmlPullParserFactory parserCreator = XmlPullParserFactory
					.newInstance();
			XmlPullParser parser = parserCreator.newPullParser();

			// 파싱하기 위해 스트림 오픈
			parser.setInput(url.openStream(), null);
			// 파싱할 데이터 타입을 알려줌
			int parserEvent = parser.getEventType();

			while (parserEvent != XmlPullParser.END_DOCUMENT) {
				switch (parserEvent) {
				case XmlPullParser.START_TAG: // parser가 시작 태그를 만나면 실행
					if (parser.getName().equals("item")) {
						inItem = true;
					}
					if (parser.getName().equals("title")) { 
					// <title> 만나면 내용을 받을 수 있도록 true
						inTitle = true;
					}
					if (parser.getName().equals("address")) { 
					// <address> 만나면 내용을 받을 수 있도록 true
						inAddress = true;
					}
					if (parser.getName().equals("mapx")) { 
					// <mapx> 만나면 내용을 받을 수 있도록 true
						inMapx = true;
					}
					if (parser.getName().equals("mapy")) { 
					// <mapy> 만나면 내용을 받을 수 있도록 true
						inMapy = true;
					}
					if (parser.getName().equals("message")) { 
						// message 태그를 만나면  에러 출력
						// 여기에 에러코드에 따라 다른 메세지를 출력하도록 할 수 있다.
					}
					break;

				case XmlPullParser.TEXT:// parser가 내용에 접근했을때
					if (inItem) {
						if (inTitle) { // isTitle이 true일 때 태그의 내용을 저장.
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
					if (inAddress) { // isAddress이 true일 때 태그의 내용을 저장.
						address = parser.getText();

						inAddress = false;
					}
					if (inMapx) { // isMapx이 true일 때 태그의 내용을 저장.
						mapx = parser.getText();

						inMapx = false;
					}
					if (inMapy) { // isMapy이 true일 때 태그의 내용을 저장.
						mapy = parser.getText();

						inMapy = false;
					}
					break;
				case XmlPullParser.END_TAG:
					if (parser.getName().equals("item")) {
						// 좌표변환
						//KTM(카텍좌표) -> WGS84(위도,경도 좌표)
						beforeConverting = new GeoTransPoint(
								Double.parseDouble(mapx),
								Double.parseDouble(mapy));
						afterConverting = GeoTrans.convert(GeoTrans.KATEC,
								GeoTrans.GEO, beforeConverting);
						lat = afterConverting.getX();
						lng = afterConverting.getY();

						// 파싱된 정보 저장
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
// <bn> </bn> 태그를 파싱하는 함수 
	String bParcing(String title) {
		String temp_title = null, parced_title = null;
		temp_title = title.replace("<b>", ""); //String의 replace함수 사용
		parced_title = temp_title.replace("</b>", "");
		return parced_title;
	}
/////////////////////////
// 파싱된 결과 다이얼로그로 출력하는 함수
	void printParcedData() {

		searchedResultDialog = new AlertDialog.Builder(this).setTitle("검색결과")
		// 어댑터 셋팅 (다이얼로그에 어댑터로 출력하기 위해 setAdapter)
				.setAdapter(adapter, new DialogInterface.OnClickListener() {
					@Override
					// 다이얼로그 클릭 이벤트 처리
					public void onClick(DialogInterface dialog, int which) {
						double lat = adapter.get_lat(which);
						double lng = adapter.get_lng(which);
						String location = adapter.get_title(which);

						// 선택된 곳으로 중심점 이동
						mMapController.setMapCenter(lat, lng, 11);
						// 중심점 표시
						displaySelectedLocation(lat, lng, location);
						// System.out.println(lang);

					}
				}).create();
		searchedResultDialog.show();
	}
/////////////////////////
// 검색으로 선택된 지역 마크표시하는 함수
	private void displaySelectedLocation(double lat, double lng, String title) {

		
		destination = title;
		dstLat = lat;
		dstLng = lng;
		// 이전 PoiData가 있으면 모두 지움.
		if (mDestinationPoiData != null)
			mDestinationPoiData.removeAllPOIdata();

		// 선택된 위치에 마크표시
		mDestinationPoiData = new NMapPOIdata(1, mMapViewerResourceProvider);
		mDestinationPoiData.beginPOIdata(1);
		NMapPOIitem item = mDestinationPoiData.addPOIitem(lat, lng, "도착지",
				NMapPOIflagType.TO, 0);

		// 선택된 위치의 상호명으로 말풍선 표시
		item.setTitle(title);
		if (item != null) {

			// 선택된 위치의 마커를 터치하여 이동시킬 수 있음.
			item.setFloatingMode(NMapPOIitem.FLOATING_TOUCH
					| NMapPOIitem.FLOATING_DRAG);
			// show right button on callout
			// 마커의 말풍선 버튼 추가
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
// 터치된 위치 마크표시 함수
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

	
	
	
	
	
	
	
	// 지도 상태 변경 시 호출되는 콜백 인터페이스의 오버라이딩이 필요한 함수들

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

	// 지도에서 터치 이벤트 발생 시 호출되는 콜백 인터페이스의 오버라이딩이 필요한 함수들

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
