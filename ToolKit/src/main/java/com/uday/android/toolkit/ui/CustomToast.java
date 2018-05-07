package com.uday.android.toolkit.ui;
import android.widget.*;
import android.content.*;
import android.app.*;
import com.uday.android.toolkit.R;
import android.graphics.*;

public class CustomToast extends Toast
{
	private Context context;
	
	public CustomToast(Context context){
		super(context);
		this.context=context;
	}
	
	public static void showSuccessToast(final Context context,final String message,final int duration){
		((Activity)context).runOnUiThread(new Runnable(){
			@Override
			public void run(){
				LinearLayout toastLayout=(LinearLayout)((Activity)context).getLayoutInflater().inflate(R.layout.toast_custom,null);
				TextView text=(TextView)toastLayout.findViewById(R.id.toast_text);
				text.setText(message);
				toastLayout.setBackground(context.getResources().getDrawable(R.drawable.toast_success));
				Toast toast=new Toast(context);
				toast.setDuration(duration);
				toast.setView(toastLayout);
				toast.show();
			}
		});
	}
	
	public static void showFailureToast(final Context context,final String message,final int duration){
		((Activity)context).runOnUiThread(new Runnable(){
				@Override
				public void run(){
					LinearLayout toastLayout=(LinearLayout)((Activity)context).getLayoutInflater().inflate(R.layout.toast_custom,null);
					TextView text=(TextView)toastLayout.findViewById(R.id.toast_text);
					text.setText(message);
					toastLayout.setBackground(context.getResources().getDrawable(R.drawable.toast_failure));
					Toast toast=new Toast(context);
					toast.setDuration(duration);
					toast.setView(toastLayout);
					toast.show();
				}
			});
	}
	
	public static void showNotifyToast(final Context context,final String message,final int duration){
		((Activity)context).runOnUiThread(new Runnable(){
				@Override
				public void run(){
					LinearLayout toastLayout=(LinearLayout)((Activity)context).getLayoutInflater().inflate(R.layout.toast_custom,null);
					TextView text=(TextView)toastLayout.findViewById(R.id.toast_text);
					text.setText(message);
					toastLayout.setBackground(context.getResources().getDrawable(R.drawable.toast_notify));
					Toast toast=new Toast(context);
					toast.setDuration(duration);
					toast.setView(toastLayout);
					toast.show();
				
				}
			});
		}
}
