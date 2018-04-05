package com.uday.android.toolkit.fragments;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.text.*;
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

import com.uday.android.toolkit.R;
import com.uday.android.toolkit.ui.*;


@SuppressLint("NewApi")
public class KernelFragment extends Fragment
{

	public static final int SELECTED_BACKUP=0;
	public static final int SELECTED_RESTORE=1;
	public static final int SELECTED_INSTALL=2;
	
	public Context context;
	public AlertDialog termDialog;
	public TextView termTextView;
	public ProgressBar termProgress;
	public int mOption;
	public File file,selected;
	public String BLOCK,fileList[],isImg,line;
	public KernCommandLineListener commandLineListener;
	public FloatingActionButton Inst;
	public File DIR;
	
	private FloatingActionButton Bkup,Rstr;
	private SharedPreferences prefs;
	
	private ListView list;
	private FilePickerDialog dialog;
	private DialogProperties properties;
	private RelativeLayout rootView;
	private Dialog selector;
	private FloatingActionMenu menu;
	private Shell.Interactive rootSession;
	private Shell.OnCommandLineListener backupsListListener;
	
	
	private String confirmTxt;
	
	private DialogInterface.OnClickListener confirmClickListener;
	private AdapterView.OnItemClickListener listItemOnClick;
	private DialogSelectionListener onFileSelected;
	private KernelAction backTerm;
	public String kernVersion;

	@Override
	public void onResume()
	{
		try{
		Inst.setEnabled(context.getSharedPreferences("general",0).getBoolean("allow_kern_installation",false));
		}catch(NullPointerException ex){}
		
		if(context.getSharedPreferences("general",0).getInt("storage",0)==1){
			if(Utils.getExternalSdCard()!=null){
				DIR=new File(Utils.getExternalSdCard().getAbsolutePath()+"/ToolKit");
			}
			else{
				Toast.makeText(context,"External sdcard not found,\nuses internal storage for backups",Toast.LENGTH_SHORT).show();
				DIR=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/ToolKit");
			}
		}else{
			DIR=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/ToolKit");
		}
		((TextView)rootView.findViewById(R.id.kern_backup_dir)).setText(DIR+"/backups/Kernel");
		updateBackupsList();
		
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
	
	public KernelFragment(Context context){
		
		rootSession=MainActivity.rootSession;
		this.context=context;
		properties = new DialogProperties();
		properties.selection_mode = DialogConfigs.SINGLE_MODE;
		properties.selection_type = DialogConfigs.FILE_SELECT;
		properties.root = Environment.getExternalStorageDirectory();
		properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
		properties.offset = new File(DialogConfigs.DEFAULT_DIR);
		properties.hasStorageButton=true;
		
		initialise(context);
		
		commandLineListener=new KernCommandLineListener(this);
		
		listItemOnClick=new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int which, long id) 
			{
				selector.cancel();
				String fileChosen = (String) list.getItemAtPosition(which);
				final File selected=new File(DIR+"/backups/Kernel/"+fileChosen);
				if(new File(selected.getAbsolutePath()+"/kernel").exists() && (! selected.getAbsolutePath().contains(" "))){
				kernRestore(selected);
				}
			}
		};
		
		backTerm=new KernelAction(this);
		
		confirmClickListener=new DialogInterface.OnClickListener(){
			@Override
			public void onClick(final DialogInterface sweet,int p2){
				sweet.cancel();
				Object[] obj;
				switch(mOption){
					case SELECTED_BACKUP:
						obj=DialogUtils.showTermDialog(KernelFragment.this.context,"kernel backup","backing up kernel please wait...","finish",null);
						termDialog=(AlertDialog)obj[0];
						termTextView=(TextView)obj[1];
						termProgress=(ProgressBar)obj[2];
						break;
					case SELECTED_RESTORE:
						obj=DialogUtils.showTermDialog(KernelFragment.this.context,"Restoring","Restoring kernel please wait...","finish",null);
						termDialog=(AlertDialog)obj[0];
						termTextView=(TextView)obj[1];
						termProgress=(ProgressBar)obj[2];
						break;
					case SELECTED_INSTALL:
						obj=DialogUtils.showTermDialog(KernelFragment.this.context,"Installing","Installing kernel please wait...","finish",null);
						termDialog=(AlertDialog)obj[0];
						termTextView=(TextView)obj[1];
						termProgress=(ProgressBar)obj[2];
						break;
				}
				termDialog.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
				termDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
				new Thread(backTerm).start();

			}
		};
		
		
		onFileSelected=new DialogSelectionListener() {
			@Override
			public void onSelectedFilePaths(String[] files) {
				file=new File(files[0]);
				String warn="Do you want to install as kernel at your own <b>knowledge and risk</b>.??";
				isImg="false";
				if(file.getName().endsWith(".img")){
					warn="If it is having valid <b>android magic</b>,<br>"+
						"kernel will be extracted from the img file you selected,"+
						"and then will be installed on your device..??";
					isImg="true";
				}
				TextView tw=new TextView(KernelFragment.this.context);
				tw.setPadding(30,30,20,0);
				tw.setTextSize(16);
				tw.setText(Html.fromHtml(
							   "Installing <b>wrong kernel</b> is extreamly harmful and it will definitely <b>hard brick</b>"+
							   " your device.<br><br>You have selected<br><br>"+"<b>"
							   +file.getAbsolutePath()+"</b><br>"+"<br>"+warn+"<br>"));
				mOption=SELECTED_INSTALL;
				new AlertDialog.Builder(KernelFragment.this.context)
					.setIcon(R.drawable.warning_red)
					.setPositiveButton("Install Anyways",confirmClickListener)
					.setNegativeButton("Don't install",null)
					.setView(tw)
					.setTitle("Kernel Installation:-")
					.show();
			}
		};
		
		backupsListListener=new Shell.OnCommandLineListener(){
			@Override
			public void onCommandResult(int commandcode,int exitcode){
				fileList=line.split("\n");
				line="";
			}
			
			@Override
			public void onLine(String line){
				KernelFragment.this.line=KernelFragment.this.line+line+"\n";
			}
		};
		
	}
	
//###############################################################################################
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		if(context.getSharedPreferences("general",0).getInt("storage",0)==1){
			if(Utils.getExternalSdCard()!=null){
				DIR=new File(Utils.getExternalSdCard().getAbsolutePath()+"/ToolKit");
			}
			else{
				Toast.makeText(context,"External sdcard not found,\nuses internal storage for backups",Toast.LENGTH_SHORT).show();
				DIR=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/ToolKit");
			}
		}else{
			DIR=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/ToolKit");
		}						 
	
		if(rootView== null){
			line="";
			rootView = (RelativeLayout)inflater
				.inflate(R.layout.kernel, container, false);
			rootView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));
			firstRun();
		}
		rootView.startAnimation(((MainActivity)context).mGrowIn);
		Inst.setEnabled(context.getSharedPreferences("general",0).getBoolean("allow_kern_installation",false));
		((TextView)rootView.findViewById(R.id.kern_backup_dir)).setText(DIR+"/backups/Kernel");
		return rootView;
	}
	
//###############################################################################################
	
	private void firstRun(){
		menu=(FloatingActionMenu)rootView.findViewById(R.id.menu_kernel);
		menu.setClosedOnTouchOutside(true);
		prefs=context.getSharedPreferences("block_devs",0);
		BLOCK=prefs.getString("boot",null);

		rootView.findViewById(R.id.kernel_backup_location)
		.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View p1){
				Utils.openFolder(context,DIR.getAbsolutePath()+"/backups/kernel");
			}
		});
		
		fileList="".split(" ");
		kernVersion=System.getProperty("os.version");
		updateBackupsList();
		((TextView)rootView.findViewById(R.id.current_kernel)).setText(kernVersion);

		/********************************************/

		Inst=(FloatingActionButton)rootView.findViewById(R.id.kern_install);
		Inst.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1){
					if(dialog==null){
						dialog = new FilePickerDialog(context,properties,R.style.AppTheme);
						dialog.setTitle("Select kernel");
						dialog.setDialogSelectionListener(onFileSelected);
					}
					dialog.show();
				}
			});

		Bkup=(FloatingActionButton)rootView.findViewById(R.id.kern_backup);
		Bkup.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1){
					kernBackup();
				}
			});

		Rstr=(FloatingActionButton)rootView.findViewById(R.id.kern_restore);
		Rstr.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1){
					list.setAdapter(new ArrayAdapter(context, 
													 android.R.layout.simple_list_item_1, fileList) {
							@Override public View getView(int pos, View view, ViewGroup parent) {
								view = super.getView(pos, view, parent);
								((TextView) view).setSingleLine(false);
								return view;
							}
						});
					selector.show();
					list.setDivider(new ColorDrawable(Color.parseColor("#00AAFF")));
					list.setDividerHeight(3);
					selector.getWindow().setLayout(WindowManager.LayoutParams.FILL_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);

					list.setOnItemClickListener(listItemOnClick);

				}
			});
		
	}
	
//###############################################################################################
	
	private void kernBackup(){
		confirmTxt="Do you want to backup current kernel..??";
		if(new File(DIR+"/backups/Kernel/"+kernVersion).exists())
			confirmTxt="Backup of current kernel already <b>exists</b>.<br>Backing up again will <b>overwrite</b> the existing backup.\nDo you want to backup..?";
		TextView tw=new TextView(KernelFragment.this.context);
		tw.setText(Html.fromHtml(confirmTxt));
		tw.setPadding(30,30,20,0);
		tw.setTextSize(16);
		mOption=SELECTED_BACKUP;
		new AlertDialog.Builder(KernelFragment.this.context)
			.setIcon(R.drawable.backup_coloured)
			.setPositiveButton("backup",confirmClickListener)
			.setNegativeButton("cancel",null)
			.setView(tw)
			.setTitle("Kernel Backup:-")
			.show();
		
	}
	
//###############################################################################################

	public void kernRestore(File selected){
		mOption=SELECTED_RESTORE;
		this.selected=selected;		
		TextView tw=new TextView(KernelFragment.this.context);
		tw.setText(Html.fromHtml("Do you want restore<br><b>"+selected.getName()+"</b>..??"));
		tw.setPadding(30,30,20,0);
		tw.setTextSize(16);
		mOption=SELECTED_RESTORE;
		new AlertDialog.Builder(KernelFragment.this.context)
			.setIcon(R.drawable.restore_coloured)
			.setPositiveButton("Restore",confirmClickListener)
			.setNegativeButton("cancel",null)
			.setView(tw)
			.setTitle("Kernel Restore:-")
			.show();
			
	}
	
	private void updateBackupsList(){
		rootSession.addCommand(context.getFilesDir()+"/common/get_backups.sh "+MainActivity.TOOL+" "+DIR+"/backups/Kernel",1432,backupsListListener);
	
	}
	
	
	private void initialise(Context context){
		selector=new Dialog(context)
		{
			@Override
			public void onBackPressed(){
				updateBackupsList();
				selector.dismiss();
			}
		};
		selector.requestWindowFeature(Window.FEATURE_NO_TITLE);
		selector.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		selector.setContentView(R.layout.dialog_list_items);
		((TextView)selector.findViewById(R.id.dg_list_title)).setText("Select desired backup to restore");
		ImageView icon=(ImageView)selector.findViewById(R.id.dg_icon);
		icon.setVisibility(View.VISIBLE);
		icon.setImageResource(R.drawable.restore_coloured);
		list=(ListView)selector.findViewById(R.id.dg_list_view);
	}
	
//###############################################################################################

	public void runOnUiThread(Runnable action){
		((Activity)context).runOnUiThread(action);
	}
	
//###############################################################################################
	
}
