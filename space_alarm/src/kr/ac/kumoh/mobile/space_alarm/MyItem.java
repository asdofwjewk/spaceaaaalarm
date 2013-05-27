package kr.ac.kumoh.mobile.space_alarm;

import android.R;

public class MyItem {
	String m_title; // 상호명
	String m_address; // 주소
	Double m_lat; // 위도
	Double m_lng; // 경도 

	//생성자
	public MyItem(String title,String address, Double lat, Double lng){
		this.m_title = title;
		this.m_address = address;
		m_lat = lat;
		m_lng = lng;
	}
}
