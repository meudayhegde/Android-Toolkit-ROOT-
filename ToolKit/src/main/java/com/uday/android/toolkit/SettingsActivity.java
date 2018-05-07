package com.uday.android.toolkit;
import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import com.uday.android.util.*;
import java.io.*;
import com.uday.android.toolkit.ui.*;
import android.view.animation.*;
import com.github.angads25.filepicker.model.*;
import com.github.angads25.filepicker.view.*;
import com.github.angads25.filepicker.controller.*;

public class SettingsActivity extends Activity
{
	
	private static final int SELECTED_BOOT=0;
	private static final int SELECTED_RECOVERY=1;
	private static final int SELECTED_LOGO=2;
	
	private int mOption;
	private TextView bootBlock;
	private TextView recoveryBlock;
	private TextView logoBlock;
	private LinearLayout bootEdit,recEdit,logoEdit,AdvancedLayout;
	private SharedPreferences prefs1,prefs2;
	private SharedPreferences.Editor editor1,editor2;
	private String Storages[];
	private EditText edtTxt;
	private AlertDialog dialog;
	private Switch AdvancedSwitch;
	private DialogProperties properties;
	private FilePickerDialog filePickerDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		
		setContentView(R.layout.settings);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		prefs1=getSharedPreferences("block_devs",0);
		editor1=prefs1.edit();

		prefs2=getSharedPreferences("general",0);
		editor2=prefs2.edit();
		
		properties = new DialogProperties();
		properties.selection_mode = DialogConfigs.SINGLE_MODE;
		properties.selection_type = DialogConfigs.DIR_SELECT;
		properties.root = Environment.getExternalStorageDirectory();
		properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
		properties.offset = new File(DialogConfigs.DEFAULT_DIR);
		properties.hasStorageButton=true;
		filePickerDialog=new FilePickerDialog(this,properties,R.style.AppTheme);
		filePickerDialog.setTitle("Choose apps backup directory");
		filePickerDialog.setDialogSelectionListener(new DialogSelectionListener(){
			@Override
			public void onSelectedFilePaths(String[] files){
				editor2.putString("app_backup_dir",files[0]);
				editor2.apply();
				setBackupDir();
			}
		});
		filePickerDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimFade;
		
		//reboot dialog
		
		Switch rebootConfirm=(Switch)findViewById(R.id.confirm_reboot),softReboot=(Switch)findViewById(R.id.allow_soft_reboot);
		rebootConfirm.setChecked(prefs2.getBoolean("show_reboot_confirm_dialog",true));
		softReboot.setChecked(prefs2.getBoolean("allow_soft_reboot",false));
		
		rebootConfirm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton p1,boolean p2){
					editor2.putBoolean("show_reboot_confirm_dialog",p2);
					editor2.apply();
				}
			});
			
		softReboot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton p1,boolean p2){
					editor2.putBoolean("allow_soft_reboot",p2);
					editor2.apply();
				}
			});
		
		//kernel
		
		Switch kernSwitch=(Switch)findViewById(R.id.allow_kern_inst);
		kernSwitch.setChecked(prefs2.getBoolean("allow_kern_installation",false));
		kernSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton p1,boolean p2){
				
				editor2.putBoolean("allow_kern_installation",p2);
				editor2.apply();
			}
		});
		
		//advanced
		
		AdvancedSwitch=(Switch)findViewById(R.id.advanced_allow);
		AdvancedLayout=(LinearLayout)findViewById(R.id.advanced_layout);
		
		AdvancedSwitch.setChecked(prefs2.getBoolean("allow_advanced",false));
		if(AdvancedSwitch.isChecked())
			AdvancedLayout.setVisibility(View.VISIBLE);
		else
			AdvancedLayout.setVisibility(View.GONE);
		
		AdvancedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton p1,boolean p2){
				editor2.putBoolean("allow_advanced",p2);
				editor2.apply();
				if(p2){
					AdvancedLayout.setVisibility(View.VISIBLE);
					AdvancedLayout.startAnimation(OptAnimationLoader.loadAnimation(SettingsActivity.this,R.anim.expand_to_down));
				}
				else{
					Animation anim=OptAnimationLoader.loadAnimation(SettingsActivity.this,R.anim.shrink_to_top);
					anim.setAnimationListener(new Animation.AnimationListener(){
						@Override
						public void onAnimationEnd(Animation p1){
							AdvancedLayout.setVisibility(View.GONE);
						}
						@Override public void onAnimationRepeat(Animation p1){}
						@Override public void onAnimationStart(Animation p1){}
					});
					AdvancedLayout.startAnimation(anim);
				}
			}
		});
		
		setBackupDir();
		findViewById(R.id.set_app_bkp_layout).setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View p1){
				filePickerDialog.show();
			}
		});
		
		prefs1=getSharedPreferences("block_devs",0);
		
		bootBlock=(TextView)findViewById(R.id.boot_block_setting);
		bootEdit=(LinearLayout)findViewById(R.id.boot_part_edit);
		
		recoveryBlock=(TextView)findViewById(R.id.recovery_block_setting);
		recEdit=(LinearLayout)findViewById(R.id.rec_part_edit);
		
		logoBlock=(TextView)findViewById(R.id.logo_block_setting);
		logoEdit=(LinearLayout)findViewById(R.id.logo_part_edit);
		
		
		bootBlock.setText(prefs1.getString("boot","please set this manually"));
		recoveryBlock.setText(prefs1.getString("recovery","please set this manually"));
		logoBlock.setText(prefs1.getString("logo","please set this manually"));
		
	
		bootEdit.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View p1){
				mOption=SELECTED_BOOT;
				showDialog("boot block device");
				edtTxt.setText(bootBlock.getText());
			}
		});
		
		recEdit.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1){
					mOption=SELECTED_RECOVERY;
					showDialog("recovery block device");
					edtTxt.setText(recoveryBlock.getText());
				}
			});
			
		logoEdit.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1){
					mOption=SELECTED_LOGO;
					showDialog("boot logo block device");
					edtTxt.setText(logoBlock.getText());
				}
			});
		
		
		super.onCreate(savedInstanceState);
	}

	private void showDialog(String title)
	{
		if(dialog==null){
			edtTxt=DialogUtils.showEditTextDialog(this,title,"It may even lead to hard brick if wrong. do this only if you are so sure..!!",null,null,"save",new DialogUtils.OnClickListener(){
				@Override
				public void onClick(AlertDialog p1){
					dialog=p1;
					if(new File(edtTxt.getText().toString()).exists()){
						switch(mOption){
							case SELECTED_BOOT:
								if(!bootBlock.getText().toString().equalsIgnoreCase(edtTxt.getText().toString())){
									editor1.putString("boot",edtTxt.getText().toString());
									bootBlock.setText(edtTxt.getText());
									CustomToast.showSuccessToast(SettingsActivity.this,"successfully saved dlock device detail",Toast.LENGTH_SHORT);
									dialog.cancel();
								}
								break;
							case SELECTED_RECOVERY:
								if(!recoveryBlock.getText().toString().equalsIgnoreCase(edtTxt.getText().toString())){
									editor1.putString("recovery",edtTxt.getText().toString());
									recoveryBlock.setText(edtTxt.getText());
									CustomToast.showSuccessToast(SettingsActivity.this,"successfully saved dlock device detail",Toast.LENGTH_SHORT);
									dialog.cancel();
								}
								break;
							case SELECTED_LOGO:
								if(!logoBlock.getText().toString().equalsIgnoreCase(edtTxt.getText().toString())){
									editor1.putString("logo",edtTxt.getText().toString());
									logoBlock.setText(edtTxt.getText());
									CustomToast.showSuccessToast(SettingsActivity.this,"successfully saved dlock device detail",Toast.LENGTH_SHORT);
									dialog.cancel();
								}
								break;
						}
					}
					else{
						CustomToast.showFailureToast(SettingsActivity.this,"this block device does not exists",Toast.LENGTH_SHORT);
					}
				}
			});
		}
		else{
			dialog.setTitle(title);
			dialog.show();
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId()){
			case android.R.id.home:
				finish();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume()
	{
		// TODO: Implement this method
		
		//backups
		Storages=new String[]{"Internal Storage"};
		if(Utils.getExternalSdCard()!=null){
			Storages=new String[]{"Internal Storage","External Storage"};
		}
		Spinner storageChoice=(Spinner)findViewById(R.id.settings_storage_choice);
		ArrayAdapter spinnerAdapter=new ArrayAdapter(SettingsActivity.this,android.R.layout.simple_spinner_dropdown_item,Storages);
		storageChoice.setAdapter(spinnerAdapter);
		if(prefs2.getInt("storage",0)==1){
			if(Utils.getExternalSdCard()!=null)
				storageChoice.setSelection(1);
			else{
				storageChoice.setSelection(0);
				CustomToast.showFailureToast(this,"External storage not found\nseting internal storage as backup location",Toast.LENGTH_SHORT);
			}
		}
		else{
			storageChoice.setSelection(0);
		}

		storageChoice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
				@Override
				public void onItemSelected(AdapterView<?> p1,View p2,int p3,long p4){
					editor2.putInt("storage",p3);
					editor2.apply();
				}
				public void onNothingSelected(AdapterView<?> p1){}
			});
		
		super.onResume();
	}
	
	private void setBackupDir(){
		((TextView)findViewById(R.id.app_backup_dir)).setText(prefs2.getString("app_backup_dir",Environment.getExternalStorageDirectory().getAbsolutePath()+"/ToolKit/backups/apps"));
	}
	
	
}
