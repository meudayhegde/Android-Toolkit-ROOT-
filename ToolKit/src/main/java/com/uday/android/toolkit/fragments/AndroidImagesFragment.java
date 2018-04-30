package com.uday.android.toolkit.fragments;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import com.github.angads25.filepicker.controller.*;
import com.github.angads25.filepicker.model.*;
import com.github.angads25.filepicker.view.*;
import com.github.clans.fab.*;
import com.uday.android.toolkit.*;
import com.uday.android.toolkit.listeners.*;
import com.uday.android.toolkit.runnable.*;
import com.uday.android.util.*;
import eu.chainfire.libsuperuser.*;
import java.io.*;
import java.util.*;

import com.uday.android.toolkit.R;
import com.uday.android.toolkit.ui.*;
import android.view.inputmethod.*;

@SuppressLint("NewApi")
public class AndroidImagesFragment extends Fragment
{
	
	
	public final static int SELECTED_UNPACK=1;
	public final static int SELECTED_REPACK=0;
	public final static int SELECTED_INSTALL=2;
	public final static int SELECTED_BACKUP=4;
	public final static int SELECTED_RESTORE=3;
	public final static int SELECTED_BOOT=5;
	public final static int SELECTED_RECOVERY=6;
	public final static int SELECTED_LOGO=7;
	public static final int SELECTED_RESTORE_ITEM=8;
	
	public int mBlock,mOption;
	public File backupDir,fileChoosen;
	public String tmpstr,pName,choosen,BOOT,RECOVERY,LOGO,BLOCK,BLOCK_NAME;
	public TermListener commandLineListener;
	public Button term_finished;
	public TextView termTextView;
	public AlertDialog termDialog;
	public ProgressDialog pDialog;
	public ActionExecuter executer;
	public ArrayAdapter BlockAdapter;
	public FilePickerDialog dialog;
	public DialogProperties properties;
	public AlertDialog imagesDialog,projectDialog,confirmDialog;
	public ConfirmListener mListener;
	public DialogSelectionListener fileListener;
	public File DIR;
	public ProgressBar termProgress;
	
	private RelativeLayout rootView;
	private FloatingActionButton unpack,repack,install,backup,restore;
	
	private FloatingActionMenu menu;
	private EditText edtTxt;
	private String[] list;
	private ListView listProjectView;
	private Context context;
	private SharedPreferences prefs;
	public String[] blockNames=new String[]{"Boot image","Recovery image","Boot Logo"};

	
	private static Shell.Interactive rootSession;

	public AndroidImagesFragment(){}
	
	public Context getContext()
	{
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N && context==null){
			context= super.getContext();
		}
		return context;
	}
	
	@Override
	public void onResume()
	{
		if(rootView!=null)
			refreshList();
		if(getContext().getSharedPreferences("general",0).getInt("storage",0)==1){
			if(Utils.getExternalSdCard()!=null){
				DIR=new File(Utils.getExternalSdCard().getAbsolutePath()+"/ToolKit");
			}
			else{
				CustomToast.showFailureToast(getContext(),"External sdcard not found,\nuses internal storage for backups",Toast.LENGTH_SHORT);
				DIR=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/ToolKit");
			}
		}else{
			DIR=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/ToolKit");
		}
		((TextView)rootView.findViewById(R.id.backup_dir_android)).setText(DIR+"/backups");
		
		if(!menu.isOpened()){
			menu.hideMenuButton(false);
			new Handler().postDelayed(new Runnable(){
					@Override
					public void run(){
						menu.showMenuButton(true);
					}
				},400);
			
		}
		
		super.onResume();
	}
	
	
	public AndroidImagesFragment(Context context){
		
		rootSession=MainActivity.rootSession;
		
		this.context=context;
		properties = new DialogProperties();
		properties.selection_mode = DialogConfigs.SINGLE_MODE;
		properties.selection_type = DialogConfigs.FILE_SELECT;
		properties.root = Environment.getExternalStorageDirectory();
		properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
		properties.offset = new File(DialogConfigs.DEFAULT_DIR);
		properties.extensions=new String[]{".img",".bin",".win"};
		properties.hasStorageButton=true;

		//============================================================
		
		prefs=context.getSharedPreferences("block_devs",0);
		BOOT=prefs.getString("boot",null);
		RECOVERY=prefs.getString("recovery",null);
		LOGO=prefs.getString("logo",null);

		commandLineListener=new TermListener(this);
		executer=new ActionExecuter(this);
		mListener=new ConfirmListener(this);

		fileListener=new DialogSelectionListener() {
			@Override
			public void onSelectedFilePaths(String[] files) {
				fileChoosen=new File(files[0]);
				pName=fileChoosen.getName().replace(' ','_').replace('.','_');
				switch(mOption){
				case SELECTED_UNPACK:
						edtTxt=DialogUtils.showEditTextDialog(getContext(),"Enter project name...","(Any name without space)\nNote: This will overwrite the existing project with same name if exists..!"
									,pName,"project name","confirm",new DialogUtils.OnClickListener(){
										@Override
										public void onClick(AlertDialog p1){
											if(edtTxt.getText().toString().equals("") || edtTxt.getText() ==null || edtTxt.getText().equals(null)){
												CustomToast.showNotifyToast(getContext(),"Please enter project name...",Toast.LENGTH_SHORT);
											}
											else if(edtTxt.getText().toString().contains(" ") || edtTxt.getText().toString().contains("\n")){
												CustomToast.showNotifyToast(getContext(),"project name should not contain any spaces,\nplease enter a new name",Toast.LENGTH_SHORT);
											}
											else{
												try{
							
													pName=edtTxt.getText().toString();
													((InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
														.hideSoftInputFromWindow(edtTxt.getWindowToken(), 0);
													p1.cancel();
													Object[] obj=DialogUtils.showTermDialog(getContext(),"Unpack img","unpacking please wait...","open folder","finish");
													termDialog=(AlertDialog)obj[0];
													termTextView=(TextView)obj[1];
													termProgress=(ProgressBar)obj[2];
													new Thread(executer).start();
												}catch(Exception ex){
													Log.e(MainActivity.TAG,ex.toString()+ex.getMessage());
													CustomToast.showFailureToast(getContext(),ex.toString(),Toast.LENGTH_SHORT);
												}
											}
										}
									});
					break;
				case SELECTED_INSTALL:
						DialogUtils.showConfirmDialog(getContext(),"Install "+choosen,null,Html.fromHtml("Are you sure you want to install <br><b>"+fileChoosen.getAbsolutePath()+"</b><br> as <b>"+choosen+"</b>..??"),"confirm",mListener);
				}
			}
		};
		
		//============================================================
		list=new String[]{"please wait a second"};
		initialise();
		
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState){
								 
		if(getContext().getSharedPreferences("general",0).getInt("storage",0)==1){
			if(Utils.getExternalSdCard()!=null){
				DIR=new File(Utils.getExternalSdCard().getAbsolutePath()+"/ToolKit");
			}
			else{
				Toast.makeText(getContext(),"External sdcard not found,\nuses internal storage for backups",Toast.LENGTH_SHORT).show();
				DIR=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/ToolKit");
			}
		}else{
			DIR=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/ToolKit");
		}
		
	if(rootView==null){
		rootView = (RelativeLayout) inflater
			.inflate(R.layout.android_image, container, false);
			firstOpen();
		}
		rootView.startAnimation(MainActivity.mFadeIn);
		((TextView)rootView.findViewById(R.id.backup_dir_android)).setText(DIR+"/backups");
	return rootView;
	}
	

	
	private void firstOpen(){
		rootView.findViewById(R.id.path_unpack_dir)
		.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View p1){
				Utils.openFolder(getContext(),Environment.getDataDirectory().getAbsolutePath()+"/local/ToolKit/");
			}
		});
		
		rootView.findViewById(R.id.magic_path_backup_dir)
			.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View p1){
					Utils.openFolder(getContext(),DIR.getAbsolutePath()+"/backups/");
				}
			});
		list=new String[]{""};
		menu=(FloatingActionMenu)rootView.findViewById(R.id.menu_img);
		menu.setClosedOnTouchOutside(true);

		//============================================================
		
		refreshList();

		unpack=(FloatingActionButton)rootView.findViewById(R.id.unpack);
		unpack.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v1){
					mOption=SELECTED_UNPACK;
					properties.extensions=new String[]{".img"};
					dialog = new FilePickerDialog(getContext(),properties,R.style.AppTheme);
					dialog.setTitle("Select an img file to unpack");
					dialog.setDialogSelectionListener(fileListener);
					dialog.show();
				}
			});
		repack=(FloatingActionButton)rootView.findViewById(R.id.repack);
		repack.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v1){
					projectDialog.show();
				}
			});
		install=(FloatingActionButton)rootView.findViewById(R.id.install);
		install.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v1){
					mOption=SELECTED_INSTALL;
					imagesDialog.show();
				}
			});
		backup=(FloatingActionButton)rootView.findViewById(R.id.backup);
		backup.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v1){
					mOption=SELECTED_BACKUP;
					imagesDialog.show();
				}
			});
		restore=(FloatingActionButton)rootView.findViewById(R.id.restore);
		restore.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v1){
					mOption=SELECTED_RESTORE;
					imagesDialog.show();
				}
			});
	}
	

	public void refreshList(){
		try{
			rootSession.addCommand((MainActivity.TOOL+" mkdir -p "+Environment.getDataDirectory().getAbsolutePath()+"/local/ToolKit\n"
									+MainActivity.TOOL+" ls /data/local/ToolKit/ | while read file ; do\n"
									+"if [ ! -f /data/local/ToolKit/$file ] && [ -f /data/local/ToolKit/$file/boot_orig.img ] && [ -f /data/local/ToolKit/$file/kernel ] ; then\n"
									+MainActivity.TOOL+" echo $file\n"
									+"fi\n"
									+"done").split("\n"),1234,new Shell.OnCommandResultListener(){
									@Override
									public void onCommandResult(int commandCode, int exitCode,final List<String> output) {
										
										if (exitCode != 0) {
										Log.e(MainActivity.TAG,"error Obtaining backups list"+exitCode);
										} else {
											runOnUiThread(new Runnable(){
												public void run(){
													try{
														list=Utils.getString(output).split("\n");
														projectDialog.setTitle("select project to unpack");
														listProjectView.setAdapter(new ArrayAdapter(getContext(),
																		  android.R.layout.simple_list_item_1, list) {
																			 @Override public View getView(int pos, View view, ViewGroup parent) {
																				 view = super.getView(pos, view, parent);
																				 ((TextView) view).setSingleLine(false);
																 return view;
															}
														 });
													
													}catch(Exception ex){
														list=null;
														projectDialog.setTitle("no project found..!!");
													}
												}
											});
										}
									}
								});
		}catch(Exception ex){
			Log.e(MainActivity.TAG,ex.toString());
		}
			
	}
	
	private void initialise(){
		RadioGroup group=(RadioGroup)((Activity)getContext()).getLayoutInflater().inflate(R.layout.partition_selection_dialog,null);
		group.check(R.id.recovery_selection);
		imagesDialog=new AlertDialog.Builder(getContext())
					.setView(group)
					.setTitle("select partition")
					.setNegativeButton("cancel",null)
					.setPositiveButton("confirm",new AndImgListListener(this,group))
					.create();
		listProjectView=new ListView(getContext());
		listProjectView.setPadding(30,20,20,0);
		projectDialog=new AlertDialog.Builder(getContext())
						.setView(listProjectView)
						.setNegativeButton("cancel",null)
						.create();
		listProjectView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> p1,View p2,int p3,long p4){
				projectDialog.cancel();
				choosen=((TextView)p2).getText().toString();
				mOption=SELECTED_REPACK;
				DialogUtils.showConfirmDialog(getContext(),"Repack project",null,Html.fromHtml("Selected project : <b>"+choosen+"</b>"),"confirm",mListener);
			}
		});
	}
	
	public void runOnUiThread(Runnable action){
		((Activity)getContext()).runOnUiThread(action);
	}

}
