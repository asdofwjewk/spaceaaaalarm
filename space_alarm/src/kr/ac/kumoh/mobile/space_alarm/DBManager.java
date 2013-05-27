package kr.ac.kumoh.mobile.space_alarm;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public class DBManager {

Context mContext;
	
	SQLiteDatabase db;
	public DBManager(Context mContext) {
		super();
		this.mContext = mContext;
		db = mContext.openOrCreateDatabase("GAlarm.db", Context.MODE_PRIVATE, null);

		//list// 알람 리스트 DB 테이블
		//----------------------------------//
		//  title | 목적지| 거리설정 | 켜져있는지 상태  //
		//----------------------------------//
		db.execSQL("create table if not exists list(title text primary key, dst text, distance int, turn int, lat double, lng double)");
		//db.execSQL("INSERT INTO list(title, dst, distance, turn) VALUES('우리집', '대구', '200', '0')");
		
		//벨소리 & 진동 설정 DB 테이블
		db.execSQL("create table if not exists setting(bell text, biv int)");
	}
	
	
	//리스트 테이블에 데이터 추가
	public void appendList(String title, String dst, int distance, int turn, double lat, double lng) {
		String sql = "insert into list(title, dst, distance, turn, lat, lng) values('"+ title +"', '" + dst + "', '"+ distance + "', '" + turn +"','"+lat+"','"+lng+"')";
		db.execSQL(sql);
	}
	
	//리스트 테이블의 내용을 커서에 받아옴
	public Cursor fetchAllLists() {
		Cursor cursor = null;
		cursor = db.rawQuery("select * from list", null);
		return cursor;
	}
	
	//리스트 테이블에 데이터 삭제
	public void deleteList(String title) {
		String sql = "delete from list where title = '"+title+"'";
		db.execSQL(sql);
	}
	
	public Cursor selectAlarm(String title){
		Cursor cursor = null;
		cursor = db.rawQuery("select * from list where title = '"+title+"'", null);
		return cursor;
	}
	
	//리스트 테이블의 내용을 커서에 받아옴
	public Cursor fetchAllSet() {
		Cursor cursor = null;
		cursor = db.rawQuery("select * from list", null);
		return cursor;
	}
	
	//셋 테이블에 데이터 수정
	public void changeSet(String bell, String biv) {
		//TODO 데이터 변경
		//db.execSQL(sql);
	}
	
	

}
