package com.uday.android.toolkit.ui;

import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import com.uday.android.toolkit.*;
import com.uday.android.toolkit.listeners.*;
import com.uday.android.util.*;
import java.util.*;

import com.uday.android.toolkit.R;
import android.support.v7.widget.*;
import android.app.*;
import com.uday.android.toolkit.fragments.*;
import android.net.*;
import android.text.*;


public class ApkListAdapter extends ArrayAdapter<ApkListData> implements Filterable
{
	private ArrayList<ApkListData> apkList;
	private Context context;
	private int layoutRes;
	private DeleteClickListener deleter;
	private InstallClickListener installer;
	private BatchInstallerFragment fragment;
	
	public ApkListAdapter(BatchInstallerFragment fragment,int layoutRes,ArrayList<ApkListData> apkListData){
		super(fragment.context,layoutRes,apkListData);
		this.context=fragment.context;
		this.fragment=fragment;
		this.apkList=apkListData;
		this.layoutRes=layoutRes;
		deleter=new DeleteClickListener(this);
		installer=new InstallClickListener(this);
		
	}

	@Override
	public ApkListData getItem(int position)
	{
		// TODO: Implement this method
		return apkList.get(position);
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		
			LayoutInflater inflater=(LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View row=inflater.inflate(layoutRes,parent,false);
			
			final ApkListData apkListData=apkList.get(position);

			ImageView iV=(ImageView)row.findViewById(R.id.apk_icon);
			iV.setImageDrawable(apkList.get(position).ICON);

			String titleText=apkListData.NAME+" "+apkListData.VERSION_NAME,txtSearch=apkListData.txtSearch;
			if(txtSearch!=null && titleText.toLowerCase().contains(txtSearch.toLowerCase())){
				int length=txtSearch.length(),start=titleText.toLowerCase().indexOf(txtSearch.toLowerCase());
				String regx="";
				for(int i=0;i<length;i++){
					regx=regx+titleText.charAt(start+i);
				}
				titleText=titleText.replace(regx,"<font color="+'"'+"#00AEFF"+'"'+">"+regx+"</font>");
			}
			
			TextView title=(TextView)row.findViewById(R.id.apk_name);
			title.setTextColor(apkListData.titleColor);
			title.setText(Html.fromHtml(titleText));
			
			if(!apkListData.isSelectable)
				title.setTextColor(Color.RED);
				
			String apkName=apkListData.apkFile.getName();
			if(txtSearch!=null && apkName.toLowerCase().contains(txtSearch.toLowerCase())){
				int length=txtSearch.length(),start=apkName.toLowerCase().indexOf(txtSearch.toLowerCase());
				String regx="";
				for(int i=0;i<length;i++){
					regx=regx+apkName.charAt(start+i);
				}
				apkName=apkName.replace(regx,"<font color="+'"'+"#00AEFF"+'"'+">"+regx+"</font>");
			}
			((TextView)row.findViewById(R.id.apk_path)).setText(Html.fromHtml(apkListData.apkFile.getParent()+"/"+apkName));
			final CheckBox chbx=(CheckBox)row.findViewById(R.id.apk_chbx);

			chbx.setChecked(apkListData.isSelected);

			if(apkListData.isSelectable)
				row.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View p1){
							chbx.toggle();
						}
					});
			else
				chbx.setEnabled(false);

			chbx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
					@Override
					public void onCheckedChanged(CompoundButton p1,boolean p2){
						apkList.get(position).isSelected=p2;
						if(p2)fragment.onChecked();
						else fragment.onUnchecked();
						ApkListAdapter.this.onCheckedChanged();
					}
				});

			TextView chkVer=(TextView)row.findViewById(R.id.check_ver);
			if(apkListData.isOld || apkListData.isInstalled){
				chkVer.setVisibility(View.VISIBLE);
			}

			row.setOnLongClickListener(new OnLongClickListener(){
					@Override
					public boolean onLongClick(View p1){
						int twWidth=(int)(context.getResources().getDisplayMetrics().widthPixels*0.48);
						TableLayout layout=(TableLayout)((Activity)context).getLayoutInflater().inflate(R.layout.apk_details_layout,null);
						TextView verName=(TextView)layout.findViewById(R.id.apk_version_view);
						verName.setWidth(twWidth);
						verName.setText(apkListData.VERSION_NAME);
						((TextView)layout.findViewById(R.id.apk_version_code_view)).setText(apkListData.VERSION_CODE+"");
						((TextView)layout.findViewById(R.id.apk_size_view)).setText(apkListData.SIZE);
						TextView pkgName=(TextView)layout.findViewById(R.id.apk_package_view);
						pkgName.setWidth(twWidth);
						pkgName.setText(apkListData.PACKAGE_NAME);
						TextView fileName=(TextView)layout.findViewById(R.id.apk_file_view);
						fileName.setWidth((int)(context.getResources().getDisplayMetrics().widthPixels*0.45));
						fileName.setText(apkListData.apkFile.getName());
						final AlertDialog dialog=new AlertDialog.Builder(context)
							.setIcon(apkList.get(position).ICON)
							.setTitle(apkList.get(position).NAME)
							.setView(layout)
							.setPositiveButton("Install",null)
							.setNegativeButton("Market",new DialogInterface.OnClickListener(){
								@Override
								public void onClick(DialogInterface p1,int p2){
									try{
										Intent intent = new Intent(Intent.ACTION_VIEW); 
										intent.setData(Uri.parse("market://details?id="+apkListData.PACKAGE_NAME)); 
										context.startActivity(intent);

									}catch(Exception ex){
										Log.e(MainActivity.TAG,ex.toString());
										CustomToast.showFailureToast(context,"No market is found\n",Toast.LENGTH_SHORT);
									}
								}
							})
							.setNeutralButton("Delete",null)
							.create();
						dialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
						dialog.show();
						dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){
								@Override
								public void onClick(View P1){
									installer.setPosition(position);
									installer.onClick(dialog);
								}
							});
						dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener(){
								@Override
								public void onClick(View P1){
									deleter.setPosition(position,row);
									deleter.onClick(dialog);
								}
							});
						return true;
					}
				});

			TextView State=(TextView)row.findViewById(R.id.stat_inst);
			if(apkListData.isInstalled){
				State.setText("Installed");
				State.setTextColor(Color.rgb(0,255,55));

				iV.setOnClickListener(new View.OnClickListener(){
						@Override
						public void onClick(View p1){
							new AlertDialog.Builder(context)
								.setTitle(apkListData.NAME)
								.setIcon(apkListData.ICON)
								.setMessage("Do you want to launch "+apkListData.NAME+"..?")
								.setNegativeButton("cancel",null)
								.setPositiveButton("Launch",new DialogInterface.OnClickListener(){
									@Override
									public void onClick(DialogInterface p1,int p2){
										try{
											Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(apkListData.PACKAGE_NAME);
											if (launchIntent != null) { 
												context.startActivity(launchIntent);//null pointer check in case package name was not found
											}
											else
												CustomToast.showFailureToast(context,"Launch intent not found,\ncould not launch this app.",Toast.LENGTH_SHORT);
										}catch(Exception ex){
											CustomToast.showFailureToast(context,"Failed to launch "+apkListData.NAME,Toast.LENGTH_SHORT);
											Log.e(MainActivity.TAG,ex.toString());
										}
									}
								})
								.show();
						}
					});
				if(apkListData.isInstalledVer){
					chkVer.setText("Current Installed Version");
					chkVer.setTextColor(Color.rgb(0,255,55));
				}
				else if(!apkListData.isOld){
					chkVer.setText("New Version");
					chkVer.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
				}
			}
			else{
				State.setText("Not Installed");
				State.setTextColor(Color.rgb(255,15,0));
			}
			return row;
		

	}

	public void onCheckedChanged(){}
	
	@Override
	public void notifyDataSetChanged()
	{
			fragment.onAdapterNotified();
		super.notifyDataSetChanged();
	}
	
	public void filter(String charText) {
		charText = charText.toLowerCase(Locale.getDefault());
		apkList.clear();
		if (charText.length() == 0) {
			apkList.addAll(fragment.apkFilesOrig);
			for(ApkListData data:apkList){
				data.txtSearch=null;
			}
		} else {
			for (ApkListData data : fragment.apkFilesOrig) {
				if ((data.NAME+data.VERSION_NAME+data.apkFile.getName()).toLowerCase(Locale.getDefault()).contains(charText)) {
					apkList.add(data);
					data.txtSearch=charText;
				}
			}
		}
		notifyDataSetChanged();
	}
	
}
