package com.uday.android.toolkit.fragments;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.widget.AbsListView.*;
import com.github.angads25.filepicker.controller.*;
import com.github.angads25.filepicker.model.*;
import com.github.angads25.filepicker.view.*;
import com.github.clans.fab.*;
import com.uday.android.toolkit.*;
import com.uday.android.toolkit.ui.*;
import com.uday.android.util.*;
import eu.chainfire.libsuperuser.*;
import java.io.*;
import java.util.*;

import android.view.View.OnClickListener;
import com.uday.android.toolkit.R;
import android.text.*;
import android.content.res.*;

@SuppressLint("NewApi")
public class BatchInstallerFragment extends Fragment {

//###############################################################################################
	
	public static Shell.Interactive rootSession;
	public Context context;
	public static ArrayList<ApkListData> apkFilesOrig;
	
	private AlertDialog instDialog;
	private ApkListAdapter adapter;
	private ArrayList<ApkListData> apkFiles;
	private DialogProperties properties;
	private Drawable icAppDefault;
	private FilePickerDialog filePicker;
	private FloatingActionButton instFab;
	private FloatingActionButton addCustomApk;
	private FloatingActionButton delFab;
	private FloatingActionMenu menuFab;
	private FloatingActionButton addInternal,addExternal,addCustom;
	private int n=0,i,chkdCount=0;
	private int countToInstall;
	private int countOfInstalled;
	private int mPreviousVisibleItem;
	private LinearLayout sortLayout;
	private ListView myApkListView;
	private PackageManager pm;
	private ProgressDialog instProg;
	private ProgressBar instBar;
	private ProgressBar apkLoading;
	private RelativeLayout rootView;
	private Spinner sorter;
	private TextView instMsg,apkCount,apkPercantage;
	private TextView chkdInfoTotal;
	private TextView chkdInfoSelected;
	
	public BatchInstallerFragment(Context context){
		this.context=context;
		
		rootSession=MainActivity.rootSession;

		properties = new DialogProperties();
		properties.selection_mode = DialogConfigs.MULTI_MODE;
		properties.selection_type = DialogConfigs.DIR_SELECT;
		properties.root = Environment.getExternalStorageDirectory();
		properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
		properties.offset = new File(DialogConfigs.DEFAULT_DIR);
		properties.hasStorageButton=true;
		
		pm=context.getPackageManager();
		icAppDefault=context.getResources().getDrawable(R.drawable.ic_app_default);
		
		apkFiles=new ArrayList<ApkListData>();
		if(apkFilesOrig==null)
			apkFilesOrig=new ArrayList<ApkListData>();
		else{
			setHasOptionsMenu(true);
			for(ApkListData data:apkFilesOrig){
				if(!data.apkFile.exists())
					apkFilesOrig.remove(data);
				else{
					if(!data.isSelectable)data.add();
					if(data.isSelected)chkdCount++;
				}
			}
		}
		apkFiles.addAll(apkFilesOrig);
		
		instProg=new ProgressDialog(getContext());
		instProg.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
		instProg.setTitle("Loading");
		instProg.setMessage("Searching for apk files.\nplease wait...");
		instProg.setCancelable(false);

		RelativeLayout layout=(RelativeLayout)((Activity)context).getLayoutInflater().inflate(R.layout.apk_inst_dialog,null);
		instMsg=(TextView)layout.findViewById(R.id.apk_progress_name);
		instBar=(ProgressBar)layout.findViewById(R.id.apk_progress);
		apkPercantage=(TextView)layout.findViewById(R.id.apk_percentage);
		apkCount=(TextView)layout.findViewById(R.id.apk_count);
		instDialog=new AlertDialog.Builder(context)
					.setTitle("Installing ")
					.setIcon(R.drawable.ic_app_default)
					.setView(layout)
					.create();
		instDialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
		instDialog.setCancelable(false);
	}

	@Override
	public Context getContext()
	{
		if(context==null)
			context= super.getContext();
		return context;
	}
	
	@Override
	public void onResume()
	{
		if(!menuFab.isOpened() && !menuFab.isMenuButtonHidden()){
			menuFab.hideMenuButton(false);
			new Handler().postDelayed(new Runnable(){
					@Override
					public void run(){
						menuFab.showMenuButton(true);
					}
				},400);
		}
		
		super.onResume();
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		if(rootView==null){
			rootView = (RelativeLayout)inflater
				.inflate(R.layout.batch_installer, container, false);
			firstRun();
		}
		rootView.startAnimation(((MainActivity)context).mGrowIn);
		return rootView;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		inflater.inflate(R.menu.check_all, menu);

		SearchView search=(SearchView)menu.findItem(R.id.action_search).getActionView();
		search.setQueryHint("type to search...");
		search.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
				@Override
				public boolean onQueryTextChange(String newText){
					adapter.filter(newText);
					return true;
				}

				@Override
				public boolean onQueryTextSubmit(String txt){
					return false;
				}
			});

		CheckBox allToggle=(CheckBox)menu.findItem(R.id.check_all_chbx).getActionView();

		int states[][] = {{android.R.attr.state_checked}, {}};
		int colors[] = {Color.WHITE, Color.WHITE};
		allToggle.setButtonTintList(new ColorStateList(states, colors));
		allToggle.setPadding(10,0,10,0);

		allToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton p1,boolean p2){

					for(ApkListData apkFile:apkFiles){
						if(apkFile.isSelectable)
							apkFile.isSelected=p2;
					}
					adapter.notifyDataSetChanged();
				}
			});


		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId()){
			case R.id.toggle_all_chbx:
				for(ApkListData apkFile:apkFiles){
					if(apkFile.isSelectable)
						apkFile.isSelected=!apkFile.isSelected;
				}
				adapter.notifyDataSetChanged();
				break;
			case R.id.check_not_installed:
				for(ApkListData apkFile:apkFiles){
					if(apkFile.isSelectable){
						if(!apkFile.isInstalled)
							apkFile.isSelected=true;
						else apkFile.isSelected=false;
					}
				}
				adapter.notifyDataSetChanged();
				break;

			case R.id.check_old:
				for(ApkListData apkFile:apkFiles){
					if(apkFile.isOld)
						apkFile.isSelected=true;
					else
						apkFile.isSelected=false;
				}
				adapter.notifyDataSetChanged();
				break;
			case R.id.check_updatable:
				for(ApkListData apkFile:apkFiles){
					if(apkFile.isInstalled && !apkFile.isInstalledVer && !apkFile.isOld)
						apkFile.isSelected=true;
					else
						apkFile.isSelected=false;
				}
				adapter.notifyDataSetChanged();
				break;
			case R.id.clrscr:
				((MainActivity)context).refreshApkScreen();
				break;
		}
		return true;
	}
	
	private void firstRun(){

		sortLayout=(LinearLayout)rootView.findViewById(R.id.layout_sort);
		sorter=(Spinner)rootView.findViewById(R.id.sort_spinner);
		sorter.setAdapter(new ArrayAdapter(getContext(),android.R.layout.simple_list_item_1,new String[]{"app name","file name","size","date modified"}));
		sorter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> p1,View p2,int p3,long p4){
				sort(p3);
			}
			@Override
			public void onNothingSelected(AdapterView<?> p1){
				
			}
		});
		
		
		myApkListView=(ListView)rootView.findViewById(R.id.apk_list_view);

		menuFab=(FloatingActionMenu)rootView.findViewById(R.id.menu_batch_app);
		chkdInfoTotal=(TextView)rootView.findViewById(R.id.batch_info_total);
		chkdInfoSelected=(TextView)rootView.findViewById(R.id.batch_info_selected);
		menuFab.setClosedOnTouchOutside(true);
		adapter=new ApkListAdapter(this,R.layout.apk_list_item,apkFiles);
		myApkListView.setAdapter(adapter);
		myApkListView.setOnScrollListener(new OnScrollListener(){
				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {
				}

				@Override
				public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
					if (firstVisibleItem > mPreviousVisibleItem) {
						menuFab.hideMenuButton(true);
					} else if (firstVisibleItem < mPreviousVisibleItem) {
						menuFab.showMenuButton(true);
					}
					mPreviousVisibleItem = firstVisibleItem;
				}
			});
		
		apkLoading=(ProgressBar)rootView.findViewById(R.id.apk_loading_progress);
		
		apkLoading.setPadding((int)(MainActivity.SCREEN_WIDTH*0.2),0,0,0);
		addInternal=(FloatingActionButton)menuFab.findViewById(R.id.internal);
		addExternal=(FloatingActionButton)menuFab.findViewById(R.id.external);
		addCustom=(FloatingActionButton)menuFab.findViewById(R.id.custom);
		addCustomApk=(FloatingActionButton)menuFab.findViewById(R.id.custom_apk);

		addInternal.setOnClickListener(new InstClickListener());
		addExternal.setOnClickListener(new ExtClickListener());
		addCustom.setOnClickListener(new CustomClickListener());
		addCustomApk.setOnClickListener(new customApk());
		
		chkdInfoSelected.setOnClickListener(new SelectedDialog());
		if(!apkFiles.isEmpty()){
			chkdInfoTotal.setText(Html.fromHtml("Total :  <b><font color="+'"'+"blue"+'"'+">"+apkFiles.size()+"</font></b>"));
			sortLayout.setVisibility(View.VISIBLE);
			chkdInfoSelected.setVisibility(View.VISIBLE);
		}
	}
	
	private class SelectedDialog implements View.OnClickListener
	{
		private ArrayList<ApkListData> selectedData;
		private ApkListAdapter selectedAdapter;
		private AlertDialog dialog;
		
		public SelectedDialog(){
			selectedData=new ArrayList<ApkListData>();
			selectedAdapter=new ApkListAdapter(BatchInstallerFragment.this,R.layout.apk_list_item,selectedData){
				@Override
				public void onCheckedChanged(){
					adapter.notifyDataSetChanged();
				}
			};
			dialog=new AlertDialog.Builder(context)
				.setTitle("Selected Apk files")
				.setAdapter(selectedAdapter,null)
				.create();
			dialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
		}
		
		@Override
		public void onClick(View p1)
		{
			selectedData.clear();
			for(ApkListData data : apkFilesOrig){
				if(data.isSelected)selectedData.add(data);
			}
			selectedAdapter.notifyDataSetChanged();
			dialog.show();
			dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT,(int)(MainActivity.SCREEN_HEIGHT*0.7));
		}
	}
	
	
	private void searchForApks(File mDir){
		try{
		File[] samp=mDir.listFiles();
		
		for(File tmp: samp)
			if(!tmp.isDirectory() && tmp.getName().endsWith(".apk"))
					addIntoList(new ApkListData(context,tmp,pm,icAppDefault));
		for(File tmp: samp)
			if(tmp.isDirectory())
				searchForApks(tmp);
		}catch(NullPointerException ex){
			
		}
	}
	
	
	private void addIntoList(final ApkListData apkListData){
		boolean isDuplicate=false;
		for(ApkListData tmp :apkFilesOrig){
			if(tmp.apkFile.equals(apkListData.apkFile)){
				isDuplicate=true;
				break;
			}
		}
		if(!isDuplicate){
			apkListData.add();
			apkFilesOrig.add(apkListData);
			n++;
		}
		
	}

	private void sort(int method){
		switch(method){
			case 0:for(i=0;i<apkFilesOrig.size()-1;i++){
						for(int j=i+1;j<apkFilesOrig.size();j++){
							if((apkFilesOrig.get(i).NAME+apkFilesOrig.get(i).VERSION_NAME).compareToIgnoreCase(apkFilesOrig.get(j).NAME+apkFilesOrig.get(j).VERSION_NAME) >0){
								ApkListData tmp=apkFilesOrig.get(i);apkFilesOrig.remove(i);
								apkFilesOrig.add(i,apkFilesOrig.get(j-1));
								apkFilesOrig.remove(j);apkFilesOrig.add(j,tmp);
							}
						}
					}
				for(i=0;i<apkFiles.size()-1;i++){
					for(int j=i+1;j<apkFiles.size();j++){
						if((apkFiles.get(i).NAME+apkFiles.get(i).VERSION_NAME).compareToIgnoreCase(apkFiles.get(j).NAME+apkFiles.get(j).VERSION_NAME) >0){
							ApkListData tmp=apkFiles.get(i);apkFiles.remove(i);
							apkFiles.add(i,apkFiles.get(j-1));
							apkFiles.remove(j);apkFiles.add(j,tmp);
						}
					}
				}
				break;
			case 1:for(i=0;i<apkFilesOrig.size()-1;i++){
					for(int j=i+1;j<apkFilesOrig.size();j++){
						if(apkFilesOrig.get(i).apkFile.getName().compareToIgnoreCase(apkFilesOrig.get(j).apkFile.getName()) >0){
							ApkListData tmp=apkFilesOrig.get(i);apkFilesOrig.remove(i);
							apkFilesOrig.add(i,apkFilesOrig.get(j-1));
							apkFilesOrig.remove(j);apkFilesOrig.add(j,tmp);
						}
					}
				}
				for(i=0;i<apkFiles.size()-1;i++){
					for(int j=i+1;j<apkFiles.size();j++){
						if(apkFiles.get(i).apkFile.getName().compareToIgnoreCase(apkFiles.get(j).apkFile.getName()) >0){
							ApkListData tmp=apkFiles.get(i);apkFiles.remove(i);
							apkFiles.add(i,apkFiles.get(j-1));
							apkFiles.remove(j);apkFiles.add(j,tmp);
						}
					}
				}
				break;
			case 2:for(i=0;i<apkFilesOrig.size()-1;i++){
					for(int j=i+1;j<apkFilesOrig.size();j++){
						if(apkFilesOrig.get(i).apkFile.length()>apkFilesOrig.get(j).apkFile.length()){
							ApkListData tmp=apkFilesOrig.get(i);apkFilesOrig.remove(i);
							apkFilesOrig.add(i,apkFilesOrig.get(j-1));
							apkFilesOrig.remove(j);apkFilesOrig.add(j,tmp);
						}
					}
				}
				for(i=0;i<apkFiles.size()-1;i++){
					for(int j=i+1;j<apkFiles.size();j++){
						if(apkFiles.get(i).apkFile.length()>apkFiles.get(j).apkFile.length()){
							ApkListData tmp=apkFiles.get(i);apkFiles.remove(i);
							apkFiles.add(i,apkFiles.get(j-1));
							apkFiles.remove(j);apkFiles.add(j,tmp);
						}
					}
				}
				break;
			case 3:for(i=0;i<apkFilesOrig.size()-1;i++){
					for(int j=i+1;j<apkFilesOrig.size();j++){
						if(apkFilesOrig.get(j).apkFile.lastModified()>apkFilesOrig.get(i).apkFile.lastModified()){
							ApkListData tmp=apkFilesOrig.get(i);apkFilesOrig.remove(i);
							apkFilesOrig.add(i,apkFilesOrig.get(j-1));
							apkFilesOrig.remove(j);apkFilesOrig.add(j,tmp);
						}
					}
				}
				for(i=0;i<apkFiles.size()-1;i++){
					for(int j=i+1;j<apkFiles.size();j++){
						if(apkFiles.get(j).apkFile.lastModified()>apkFiles.get(i).apkFile.lastModified()){
							ApkListData tmp=apkFiles.get(i);apkFiles.remove(i);
							apkFiles.add(i,apkFiles.get(j-1));
							apkFiles.remove(j);apkFiles.add(j,tmp);
						}
					}
				}
				break;
		}
		runOnUiThread(new Runnable(){
			@Override public void run(){adapter.notifyDataSetChanged();}
		});
	}
	
	private void setApkStatus(){
		for(ApkListData apkListData :apkFilesOrig)
			for(i=0;i<n;i++)
				if(apkFilesOrig.get(i).PACKAGE_NAME.compareToIgnoreCase(apkListData.PACKAGE_NAME)==0 && !apkListData.isInstalled && apkFilesOrig.get(i).VERSION_CODE>apkListData.VERSION_CODE)
					apkListData.isOld=true;
	}
	
	
	
	
	private void install(int position){
		try{
			if(apkFilesOrig.get(position).isSelected){
			
				if(!instDialog.isShowing()){
					instBar.setMax(countToInstall);
					instDialog.show();
				}
				countOfInstalled++;
				instDialog.setIcon(apkFiles.get(position).ICON);
				instBar.setProgress(countOfInstalled);
				instMsg.setText("Installing "+apkFiles.get(position).NAME+" "+apkFiles.get(position).VERSION_NAME);
				apkCount.setText(countOfInstalled+" / "+countToInstall);
				apkPercantage.setText(countOfInstalled*100/countToInstall+" %");
				
				rootSession.addCommand("pm install -rd "+'"'+apkFiles.get(position).PATH+'"',position,new Shell.OnCommandResultListener(){
						@Override
						public void onCommandResult(final int comandcode,final int exitcode,final List<String> output){
							final String outStr=apkFiles.get(comandcode).NAME+"_"+apkFiles.get(comandcode).VERSION_NAME+" : "+Utils.getString(output);
							Log.d(MainActivity.TAG,outStr);
							runOnUiThread(new Runnable(){
									@Override
									public void run(){
										if(exitcode==0){
											CustomToast.showSuccessToast(context,outStr,Toast.LENGTH_SHORT);
											apkFiles.get(comandcode).isInstalled=true;
											apkFiles.get(comandcode).titleColor=Color.rgb(0,202,0);
											apkFiles.get(comandcode).isInstalledVer=true;
											for(ApkListData data:apkFiles){
												if(data.PACKAGE_NAME.equalsIgnoreCase(apkFiles.get(comandcode).PACKAGE_NAME) && data.VERSION_CODE<apkFiles.get(comandcode).VERSION_CODE){
											
													data.isInstalledVer=false; data.isOld=true;
												}
											}
										}
										else{
											CustomToast.showFailureToast(context,outStr,Toast.LENGTH_SHORT);
											apkFiles.get(comandcode).isInstalled=false;
											apkFiles.get(comandcode).titleColor=Color.rgb(255,25,0);
										}
										install(comandcode+1);
									}
								});
						}
					});
			}
			else install(position+1);
		}
		catch(Exception ex){
			instDialog.cancel();
			setHasOptionsMenu(true);
			adapter.notifyDataSetChanged();
		}
	}
	
	
	public void runOnUiThread(Runnable run){
		((Activity)context).runOnUiThread(run);
	}
	


	private void beforeApkSearch(){
		setHasOptionsMenu(false);
		menuFab.close(true);
		menuFab.hideMenuButton(true);
		instProg.show();
		
	}
	
	
	private void delApk(){
		final List<ApkListData> delList=new ArrayList<ApkListData>();
		for(ApkListData apkFile:apkFilesOrig){
			if(apkFile.isSelected){
				delList.add(apkFile);
			}
		}
		if(delList.size()==0){
			CustomToast.showNotifyToast(context,"no apk file selected for deletion",Toast.LENGTH_SHORT);
		}
		else{
			new AlertDialog.Builder(context)
					.setTitle("Delete apk files")
					.setMessage("This will delete all the selected apk files ("+delList.size()+") from the storage.\n"
								+"and this action can not be undone")
					.setNegativeButton("cancel",new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface p1,int p2){
							p1.cancel();
						}
					})
					.setPositiveButton("delete",new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface p1,int p2){
						String dellist="";
						for(ApkListData listData:delList){
							dellist=dellist+" "+'"'+listData.PATH+'"';
						}
						rootSession.addCommand(MainActivity.TOOL+" rm "+dellist,4323,new Shell.OnCommandResultListener(){
							@Override
							public void onCommandResult(int commandcode,int resultcode,List<String> output){
								runOnUiThread(new Runnable(){
									@Override
									public void run(){
										for(ApkListData list:delList){
											if(!list.apkFile.exists()){
												apkFiles.remove(list);
												apkFilesOrig.remove(list);
											}
										}
										adapter.notifyDataSetChanged();
									}
								});
							}
						});
					}
				})
				.show();
					
		}
	}
	
	private void OnApkSearchCompleted(){
		apkLoading.setVisibility(View.VISIBLE);
		sortLayout.setVisibility(View.GONE);
		chkdInfoTotal.setText(Html.fromHtml("Total :  <b><font color="+'"'+"blue"+'"'+">"+apkFilesOrig.size()+"</font></b>"));
		new Thread(){
			@Override
			public void run(){
				while(((MainActivity)context).backgroundThreadisRunning){
					try
					{
						Thread.sleep(1000);
					}
					catch (InterruptedException e)
					{}
					finally{
						runOnUiThread(new Runnable(){
								@Override
								public void run(){
									adapter.notifyDataSetChanged();
								}
							});
					}
				}
				runOnUiThread(new Runnable(){
					@Override
					public void run(){
						apkFiles.clear();
						setApkStatus();
						sort(0);
						apkLoading.setVisibility(View.GONE);
						sortLayout.setVisibility(View.VISIBLE);
						apkFiles.addAll(apkFilesOrig);
					}
				});
			}
		}.start();
		
		
		
		setHasOptionsMenu(true);
		menuFab.showMenuButton(false);
		apkFiles.addAll(apkFilesOrig);
		adapter.notifyDataSetChanged();
		instProg.cancel();
		
		if(instFab==null && !apkFiles.isEmpty()){
			instFab=new FloatingActionButton(context);
			instFab.setButtonSize(FloatingActionButton.SIZE_MINI);
			instFab.setColorNormalResId(R.color.greenFabNormal);
			instFab.setColorPressedResId(R.color.greenFabPressed);
			instFab.setColorRippleResId(R.color.greenFabRipple);
			instFab.setImageResource(R.drawable.ic_install);
			instFab.setLabelText("Install selected apks");
			instFab.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View p1){
						countToInstall=0;
						menuFab.close(true);
						for(ApkListData tmp :apkFilesOrig)
							if(tmp.isSelected)
								countToInstall++;
						if(countToInstall>0){
							countOfInstalled=0;
							menuFab.hideMenuButton(true);
							setHasOptionsMenu(false);
							install(0);
						}
						else
							CustomToast.showNotifyToast(context,"no apk file is selected for installation",Toast.LENGTH_SHORT);
					}
				});
			menuFab.addMenuButton(instFab);
			
			
			delFab=new FloatingActionButton(context);
			delFab.setButtonSize(FloatingActionButton.SIZE_MINI);
			delFab.setImageResource(R.drawable.ic_del);
			delFab.setLabelText("delete selected apk files");
			delFab.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View p1){
						delApk();
						menuFab.close(true);
					}
				});
			menuFab.addMenuButton(delFab);
			
			
		}
	}
	
	public void onChecked(){
		chkdCount++;
		chkdInfoSelected.setText(Html.fromHtml("selected :  <b><font color="+'"'+"blue"+'"'+">"+chkdCount+"</font></b>"));
		
	}
	
	public void onUnchecked(){
		chkdCount--;
		chkdInfoSelected.setText(Html.fromHtml("selected :  <b><font color="+'"'+"blue"+'"'+">"+chkdCount+"</font></b>"));
	}
	
	public void onAdapterNotified(){
		if(!((MainActivity)context).backgroundThreadisRunning){
			chkdInfoTotal.setText(Html.fromHtml("Total :  <b><font color="+'"'+"blue"+'"'+">"+apkFiles.size()+"</font></b>"));
			chkdCount=0;
			for(ApkListData data:apkFilesOrig){
				if(data.isSelected)chkdCount++;
			}
			chkdInfoSelected.setText(Html.fromHtml("selected :  <b><font color="+'"'+"blue"+'"'+">"+chkdCount+"</font></b>"));
		}
	}
	
//###############################################################################################
	
	private class InstClickListener implements View.OnClickListener{
		@Override
		public void onClick(View p1){
			beforeApkSearch();
			new Thread(new Runnable(){
					@Override
					public void run(){
						searchForApks(Environment.getExternalStorageDirectory());
						runOnUiThread(new Runnable(){
								@Override
								public void run(){
									menuFab.removeMenuButton(addInternal);
									OnApkSearchCompleted();
								}
							});

					}
				}).start();
		}
	}

	private class ExtClickListener implements View.OnClickListener{
		@Override
		public void onClick(View p1){
			if(Utils.getExternalSdCard()==null)
				Toast.makeText(context,"External storage not inserted...!!",Toast.LENGTH_SHORT).show();
			else{

				beforeApkSearch();

				new Thread(){
				@Override
					public void run(){
						searchForApks(Utils.getExternalSdCard());
						runOnUiThread(new Runnable(){
								@Override
								public void run(){
									menuFab.removeMenuButton(addExternal);
									new Handler().postAtTime(new Runnable(){
											@Override
											public void run(){
												OnApkSearchCompleted();
											}
										},500);
								}
							});
					}
				}.start();
			}
		}
	}

	private class CustomClickListener implements View.OnClickListener{
		@Override
		public void onClick(View p1){
			menuFab.close(true);
			properties.selection_type = DialogConfigs.DIR_SELECT;
			filePicker=new FilePickerDialog(context,properties,R.style.AppTheme);
			filePicker.setDialogSelectionListener(new DialogSelectionListener(){
					@Override
					public void onSelectedFilePaths(final String[] paths){
						beforeApkSearch();
						menuFab.hideMenuButton(false);
						new Thread(){
							@Override
							public void run(){
								for(final String tmp:paths){
									searchForApks(new File(tmp));
								}
								runOnUiThread(new Runnable(){
										@Override
										public void run(){
											OnApkSearchCompleted();
										}
									});
							}
						}.start();
					}
				});
			filePicker.show();
			filePicker.getWindow().setLayout(WindowManager.LayoutParams.FILL_PARENT,WindowManager.LayoutParams.FILL_PARENT);
		}
	}
	
	private class customApk implements View.OnClickListener{
		@Override
		public void onClick(View p1){
			menuFab.close(true);
			properties.extensions=new String[]{".apk"};
			properties.selection_mode = DialogConfigs.MULTI_MODE;
			properties.selection_type = DialogConfigs.FILE_SELECT;
			FilePickerDialog fpDiag=new FilePickerDialog(context,properties,R.style.AppTheme);
			fpDiag.setDialogSelectionListener(new DialogSelectionListener(){
					@Override
					public void onSelectedFilePaths(String[] Files){
						beforeApkSearch();
						for(String apk:Files){
							addIntoList(new ApkListData(context,new File(apk),pm,icAppDefault).add().setOnAddedListener(new ApkListData.OnAddedListener(){
													@Override
													public void onAdded(){
														adapter.notifyDataSetChanged();
													}
												}));
						}
						OnApkSearchCompleted();
					}
				});
			fpDiag.show();
		}
	}
	
}
