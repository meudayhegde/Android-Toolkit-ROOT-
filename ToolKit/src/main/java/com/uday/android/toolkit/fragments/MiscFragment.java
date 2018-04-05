package com.uday.android.toolkit.fragments;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.support.v7.widget.*;
import android.text.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import com.uday.android.toolkit.*;
import com.uday.android.util.*;
import java.io.*;

import com.uday.android.toolkit.R;

@SuppressLint("NewApi")
public class MiscFragment extends Fragment
{

	private CardView miscCard;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.android_image, container, false);
	//	miscCard=(CardView)rootView.findViewById(R.id.misc_tts);
		//First run actions end
		miscCard.setOnClickListener(new OnClickListener(){
			public void onClick(View v1){
			//	InputStream is=getResources().openRawResource(R.raw.crack_zip);
			//	PyExec(Utils.getStringFromInputStream(is));
			}
		});

		return rootView;
	}
	
	
	private final int SCRIPT_EXEC_PY = 40001;
	private final String extPlgPlusName = "org.qpython.qpy3";
	
	public void PyExec(String code){

		if(Utils.checkAppInstalledByName(getContext().getPackageManager(),extPlgPlusName)) {
	        Intent intent = new Intent();
	        intent.setClassName(extPlgPlusName, "org.qpython.qpylib.MPyApi");
	        intent.setAction(extPlgPlusName + ".action.MPyApi");

	        Bundle mBundle = new Bundle(); 
	        mBundle.putString("app", "AndroidToolkit");
	        mBundle.putString("act", "onPyApi");
	        mBundle.putString("flag", "onQPyExec");            // any String flag you may use in your context
	        mBundle.putString("param", "");
	        mBundle.putString("pycode", code);
			
			
	        intent.putExtras(mBundle);
	        startActivityForResult(intent, SCRIPT_EXEC_PY);
	    } else {
			
			/*
	        new SweetAlertDialog(getContext(),SweetAlertDialog.WARNING_TYPE)
							.setTitleText("Dependency:-")
							.setContentSpanned(Html.fromHtml("This action requires <b>QPython3</b> to be installed.<br>Do you want install QPython3..??"))
							.setConfirmText("Install")
							.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener(){
								@Override
								public void onClick(SweetAlertDialog v1){
									v1.cancel();
									try {
										Uri uLink = Uri.parse("market://details?id="+extPlgPlusName);
										Intent intent = new Intent( Intent.ACTION_VIEW, uLink );
										startActivity(intent);
									} catch (Exception e) {
										Uri uLink = Uri.parse("http://qpython.com");
										Intent intent = new Intent( Intent.ACTION_VIEW, uLink );
										startActivity(intent);
									}
								}
							})
							.show();
	    	*/
	    }
	}
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {  
	    if (requestCode == SCRIPT_EXEC_PY) {
	    	if (data!=null) {
		        Bundle bundle = data.getExtras();
		        String flag = bundle.getString("flag"); // flag you set
		        String param = bundle.getString("param"); // param you set 
		        String result = bundle.getString("result"); // Result your Pycode generate
		        Toast.makeText(getContext(), "onQPyExec: return ("+result+")", Toast.LENGTH_SHORT).show();
	    	} else {
		        Toast.makeText(getContext(), "onQPyExec: data is null", Toast.LENGTH_SHORT).show();

	    	}
	    }
	}

}
