package com.uday.android.toolkit.ui;

import android.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import com.uday.android.toolkit.fragments.BatchInstallerFragment;
import com.uday.android.toolkit.MainActivity;
import com.uday.android.toolkit.R;
import com.uday.android.util.ApkListData;
import java.util.ArrayList;

public class SelectedDialog implements View.OnClickListener
	{
		private ArrayList<ApkListData> selectedData;
		private ApkListAdapter selectedAdapter;
		private AlertDialog dialog;
		
		public SelectedDialog(BatchInstallerFragment fragment,final ArrayAdapter adapter){
			selectedData=new ArrayList<ApkListData>();
			selectedAdapter=new ApkListAdapter(fragment,R.layout.apk_list_item,selectedData){
				@Override
				public void onCheckedChanged(){
					adapter.notifyDataSetChanged();
				}
			};
			dialog=new AlertDialog.Builder(fragment.context)
				.setTitle("Selected Apk files")
				.setAdapter(selectedAdapter,null)
				.create();
			dialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
		}
		
		@Override
		public void onClick(View p1)
		{
			selectedData.clear();
			for(ApkListData data : BatchInstallerFragment.apkFilesOrig){
				if(data.isSelected)selectedData.add(data);
			}
			selectedAdapter.notifyDataSetChanged();
			dialog.show();
			dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT,(int)(MainActivity.SCREEN_HEIGHT*0.7));
		}
	}
