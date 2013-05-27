package kr.ac.kumoh.mobile.space_alarm;



import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class item_list extends LinearLayout {

	
	TextView text1;
	TextView text2;
	
	public item_list(Context context) {
		super(context);
		
		init(context);
	}
	
	public item_list(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init(context);
	}
	
	private void init(Context context){
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.list_items, this, true);
		
		text1 = (TextView) findViewById(R.id.textView1);
		text2 = (TextView) findViewById(R.id.textView2);
	}
	
	public void setTitle(String data){
		text1.setText(data);
		
	}
	
	public void setAddress(String data){
		text2.setText(data);	
	}
}
