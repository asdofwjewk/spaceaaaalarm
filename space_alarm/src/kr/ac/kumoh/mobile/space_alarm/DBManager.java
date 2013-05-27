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

		//list// �˶� ����Ʈ DB ���̺�
		//----------------------------------//
		//  title | ������| �Ÿ����� | �����ִ��� ����  //
		//----------------------------------//
		db.execSQL("create table if not exists list(title text primary key, dst text, distance int, turn int, lat double, lng double)");
		//db.execSQL("INSERT INTO list(title, dst, distance, turn) VALUES('�츮��', '�뱸', '200', '0')");
		
		//���Ҹ� & ���� ���� DB ���̺�
		db.execSQL("create table if not exists setting(bell text, biv int)");
	}
	
	
	//����Ʈ ���̺��� ������ �߰�
	public void appendList(String title, String dst, int distance, int turn, double lat, double lng) {
		String sql = "insert into list(title, dst, distance, turn, lat, lng) values('"+ title +"', '" + dst + "', '"+ distance + "', '" + turn +"','"+lat+"','"+lng+"')";
		db.execSQL(sql);
	}
	
	//����Ʈ ���̺��� ������ Ŀ���� �޾ƿ�
	public Cursor fetchAllLists() {
		Cursor cursor = null;
		cursor = db.rawQuery("select * from list", null);
		return cursor;
	}
	
	//����Ʈ ���̺��� ������ ����
	public void deleteList(String title) {
		String sql = "delete from list where title = '"+title+"'";
		db.execSQL(sql);
	}
	
	public Cursor selectAlarm(String title){
		Cursor cursor = null;
		cursor = db.rawQuery("select * from list where title = '"+title+"'", null);
		return cursor;
	}
	
	//����Ʈ ���̺��� ������ Ŀ���� �޾ƿ�
	public Cursor fetchAllSet() {
		Cursor cursor = null;
		cursor = db.rawQuery("select * from bell", null);
		return cursor;
	}
	
	//�� ���̺��� ������ ����
	public void changeSet(String bell, String biv) {
		//TODO ������ ����
		//db.execSQL(sql);
	}
	
	

}
