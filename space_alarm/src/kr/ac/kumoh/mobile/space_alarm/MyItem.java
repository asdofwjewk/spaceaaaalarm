package kr.ac.kumoh.mobile.space_alarm;

import android.R;

public class MyItem {
	String m_title; // ��ȣ��
	String m_address; // �ּ�
	Double m_lat; // ����
	Double m_lng; // �浵 

	//������
	public MyItem(String title,String address, Double lat, Double lng){
		this.m_title = title;
		this.m_address = address;
		m_lat = lat;
		m_lng = lng;
	}
}
