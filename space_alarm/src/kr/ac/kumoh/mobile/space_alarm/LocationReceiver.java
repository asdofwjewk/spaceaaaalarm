package kr.ac.kumoh.mobile.space_alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.widget.Toast;

public class LocationReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
	    boolean isEntering = intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, false);
	    if(isEntering)
	    	Toast.makeText(context, "��ǥ ������ ������..", Toast.LENGTH_LONG).show();
	    else
	    	Toast.makeText(context, "��ǥ �������� ����ϴ�.", Toast.LENGTH_LONG).show();
	}

}
