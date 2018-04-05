package com.uday.android.toolkit.fragments;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.text.*;
import android.text.method.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import com.uday.android.toolkit.*;
import com.uday.android.util.*;
import android.graphics.drawable.*;
import android.graphics.*;

@SuppressLint("NewApi")
public class AboutFragment extends Fragment {
	
	private View rootView;
	private Context context;
	private LinearLayout fabLicense,LibSuLicense,filePickerLicense;
	private Dialog qrDialog;
	
	public AboutFragment(Context context){
		this.context=context;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		if(rootView==null){
			rootView = inflater
				.inflate(R.layout.home, container, false);
			
			TextView findMe=(TextView)rootView.findViewById(R.id.find_me),
			donateMe=(TextView)rootView.findViewById(R.id.donate_me),
			donatePaytm=(TextView)rootView.findViewById(R.id.donate_me_paytm),
			xdaLink=(TextView)rootView.findViewById(R.id.xda_link);
			findMe.setClickable(true);donateMe.setClickable(true);xdaLink.setClickable(true);
			findMe.setMovementMethod(LinkMovementMethod.getInstance());
			findMe.setText(Html.fromHtml(Utils.getStringFromInputStream(context.getResources().openRawResource(R.raw.find_me))));
			donateMe.setMovementMethod(LinkMovementMethod.getInstance());
			donateMe.setText(Html.fromHtml(Utils.getStringFromInputStream(context.getResources().openRawResource(R.raw.donate_me))));
			xdaLink.setMovementMethod(LinkMovementMethod.getInstance());
			xdaLink.setText(Html.fromHtml("<a href="+'"'+"https://forum.xda-developers.com/android/apps-games/app-android-toolkit-t3772040/post76093810#post76093810"+'"'+"><b>Link to Xda official thread</b></a>"));
			
			qrDialog=new Dialog(this.context);
			qrDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			ImageView qrImage=new ImageView(context);
			qrImage.setImageResource(R.drawable.qr_code_paytm);
			qrDialog.setContentView(qrImage);
			qrDialog.getWindow().setLayout((int)(MainActivity.SCREEN_WIDTH*0.7),(int)(MainActivity.SCREEN_WIDTH*0.7));
			qrDialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
			qrDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			qrDialog.setCanceledOnTouchOutside(false);
			
			donatePaytm.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View p1){
					qrDialog.show();
				}
			});
			
			fabLicense=(LinearLayout)rootView.findViewById(R.id.fab_license);
			fabLicense.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1){
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.apache.org/licenses/LICENSE-2.0"));
						startActivity(browserIntent);
				}
			});
			
			LibSuLicense=(LinearLayout)rootView.findViewById(R.id.lib_su_license);
			LibSuLicense.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View p1){
						Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.apache.org/licenses/LICENSE-2.0"));
						startActivity(browserIntent);
					}
				});
				
			filePickerLicense=(LinearLayout)rootView.findViewById(R.id.filpicker_license);
			filePickerLicense.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View p1){
						Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.apache.org/licenses/LICENSE-2.0"));
						startActivity(browserIntent);
					}
				});
			}
			
		rootView.startAnimation(((MainActivity)context).mGrowIn);
		return rootView;
	}

}
