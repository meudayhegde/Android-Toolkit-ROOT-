package com.uday.android.toolkit.fragments;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.widget.AbsListView.*;
import com.github.clans.fab.*;
import com.uday.android.toolkit.*;
import com.uday.android.toolkit.ui.*;
import com.uday.android.util.*;
import eu.chainfire.libsuperuser.*;
import java.io.*;
import java.util.*;

import android.view.View.OnClickListener;
import com.uday.android.toolkit.R;

@SuppressLint("NewApi")
public class BuildPropFragment extends Fragment {

	public LinearLayout dialogContent;
	public EditText editPropView;
	public EditText editValView;
	public TextView PropTextView,ValTextView;
	public AlertDialog dialog;
	public Context context;
	public ArrayList<BuildProperty> buildProperties;
	public Button positive,negative,neutral;
	public int selected;
	public File BuildProp;
	
	public static ArrayList<BuildProperty> buildPropertiesOrig;
	private static Shell.Interactive rootsession;
	
	private TextView ConfirmTextView;
	private LinearLayout PropContent;
	private RelativeLayout rootView;
	private FloatingActionButton fab;
	private ListView buildListView;
	private BuildPropAdapter adapter;
	private int n;
	private OnClickListener onSaveClicked;
	private int mPreviousVisibleItem;
	private int scrollState;
	
	public static final int EDIT_TYPE=0;
	public static final int PRIMARY_TYPE=1;
	public static final int CONFIRM_TYPE=2;
	
	public static final int DELETE=3;
	public static final int SAVE=4;
	public static final int NEW=5;
	public int mOption;

	
	public BuildPropFragment(Context context){
		this.context=context;
		buildProperties=new ArrayList<BuildProperty>();
		buildPropertiesOrig=new ArrayList<BuildProperty>();
		
		rootsession=MainActivity.rootSession;
		
		dialogContent=(LinearLayout)((Activity)context).getLayoutInflater().inflate(R.layout.build_prop_edit_dialog,null);
		PropContent=(LinearLayout)dialogContent.findViewById(R.id.prop_content);
		ConfirmTextView=(TextView)dialogContent.findViewById(R.id.confirm_txt);
		editPropView=(EditText)dialogContent.findViewById(R.id.build_edit_prop);
		editValView=(EditText)dialogContent.findViewById(R.id.build_edit_value);
		PropTextView=(TextView)dialogContent.findViewById(R.id.prop_text);
		ValTextView=(TextView)dialogContent.findViewById(R.id.val_text);
		
		onSaveClicked=new OnClickListener(){
			@Override
			public void onClick(View p1){
				dialog.cancel();
				String command;
				switch(mOption){
					case DELETE:
						command=BuildPropFragment.this.context.getFilesDir()+"/common/build_prop_edit.sh "+MainActivity.TOOL+" del_prop "+buildProperties.get(selected).PROPERTY;
						buildProperties.remove(selected);
						break;
					case SAVE:
						command=BuildPropFragment.this.context.getFilesDir()+"/common/build_prop_edit.sh "+MainActivity.TOOL+" set_prop '"+buildProperties.get(selected).PROPERTY+"' '"
							+editPropView.getText().toString()+"' '"+editValView.getText().toString()+"'";
						buildProperties.get(selected).PROPERTY=editPropView.getText().toString();
						buildProperties.get(selected).VALUE=editValView.getText().toString();
						
						break;
					case NEW:
						command=BuildPropFragment.this.context.getFilesDir()+"/common/build_prop_edit.sh new_prop '"
							+editPropView.getText().toString()+"'='"+editValView.getText().toString()+"'";
							break;
					default:
						command="echo Invalid Option\nreturn 1";
				}
				rootsession.addCommand(command,mOption,new Shell.OnCommandResultListener(){
						@Override
						public void onCommandResult(int commandcode,final int resultcode,final List<String> output){
							runOnUiThread(new Runnable(){
									@Override
									public void run(){
										if(resultcode==0){
											CustomToast.showSuccessToast(getContext(),"Operation successful\n"+Utils.getString(output),Toast.LENGTH_SHORT);
										}
										else
											CustomToast.showFailureToast(getContext(),"Operation failed ..!!\n"+Utils.getString(output),Toast.LENGTH_SHORT);
										refreshProp();
									}
								});
						}
					});
			}
		};
	}

	@Override
	public void onResume()
	{
		fab.hide(false);
		new Handler().postDelayed(new Runnable(){
				@Override
				public void run(){
					fab.show(true);
				}
			},400);
		super.onResume();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.build_prop_menu,menu);
		
		SearchView search=(SearchView)menu.findItem(R.id.action_search_build_prop).getActionView();
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
		super.onCreateOptionsMenu(menu,inflater);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		if(rootView==null){
			rootView = (RelativeLayout)inflater
				.inflate(R.layout.build_prop_fragment, container, false);
				
				
			fab=(FloatingActionButton)rootView.findViewById(R.id.add_prop);
			fab.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1){
			
					if(dialog==null){
						initDialog();
					}
					else
						dialog.show();
					mOption=NEW;
					setDialog(EDIT_TYPE);
					dialog.setTitle("Add new property");
					editPropView.setText("");editValView.setText("");
				}
			});
			
			buildListView=(ListView)rootView.findViewById(R.id.build_list_view);
			adapter=new BuildPropAdapter(this);
			buildListView.setAdapter(adapter);
			
			buildListView.setOnScrollListener(new OnScrollListener(){
					@Override
					public void onScrollStateChanged(AbsListView view, int scrollState) {
						BuildPropFragment.this.scrollState=scrollState;
						if(scrollState==OnScrollListener.SCROLL_STATE_IDLE){
							new Handler().postDelayed(new Runnable(){
									@Override
									public void run(){
										if(fab.isHidden() && BuildPropFragment.this.scrollState==OnScrollListener.SCROLL_STATE_IDLE){
											fab.show(true);
										}
									}
								},400);

						}
					}

					@Override
					public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
						if (firstVisibleItem > mPreviousVisibleItem) {
							
							fab.hide(true);
						} else if (firstVisibleItem < mPreviousVisibleItem) {
							
							fab.show(true);
						}
						mPreviousVisibleItem = firstVisibleItem;
					}
				});
			
			
			rootView.setVisibility(View.GONE);
			refreshProp();
		}
		else{
			rootView.startAnimation(((MainActivity)context).mGrowIn);
		}
		
		
		return rootView;
	}
	
	public void refreshProp(){
		n=0;
		rootsession.addCommand(context.getFilesDir().getAbsolutePath()+"/common/refresh_prop.sh "+MainActivity.TOOL,1512,new Shell.OnCommandLineListener(){
			@Override
			public void onCommandResult(int commandCode,int exitCode){
				runOnUiThread(new Runnable(){
						@Override
						public void run(){
							buildProperties.clear();
							buildProperties.addAll(buildPropertiesOrig);
							adapter.notifyDataSetChanged();
							rootView.setVisibility(View.VISIBLE);
							rootView.startAnimation(((MainActivity)context).mGrowIn);
							setHasOptionsMenu(true);
						}
					});
			}
			@Override
			public void onLine(final String line){
				if(!line.startsWith("#") && line.split("=").length==2){
					try{
						String[] str=line.split("=");
						addIntoList(new BuildProperty(str[0],str[1]));
					}catch(Exception ex){}
				}
			}
		});
	}
	
	public void runOnUiThread(Runnable action){
		((Activity)context).runOnUiThread(action);
	}
	
	public void toast(String toast){
		Toast.makeText(context,toast,Toast.LENGTH_SHORT).show();
	}
	
	private void addIntoList(final BuildProperty buildProperty){
		boolean added=false,isDuplicate=false;
		for(BuildProperty tmp :buildPropertiesOrig){
			if(tmp.PROPERTY.equalsIgnoreCase(buildProperty.PROPERTY))
				isDuplicate=true;
			}
		if(!isDuplicate){
			for(int i=0;i<n;i++){
				if(buildPropertiesOrig.get(i).PROPERTY.compareToIgnoreCase(buildProperty.PROPERTY)>=0){
					buildPropertiesOrig.add(i,buildProperty);
					added=true;
					break;
				}
			}
			if(!added)
				buildPropertiesOrig.add(buildProperty);
			n++;
		}
	}
	
	public void initDialog(){
		dialog=new AlertDialog.Builder(context)
			.setPositiveButton("Save",null)
			.setNegativeButton("Cancel",null)
			.setNeutralButton("Delete",null)
			.setView(dialogContent)
			.show();
		positive=dialog.getButton(AlertDialog.BUTTON_POSITIVE);
		negative=dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
		neutral=dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
	}
	
	public void setDialog(int type){
		switch(type){
			case EDIT_TYPE:
				PropContent.setVisibility(View.VISIBLE);
				ConfirmTextView.setVisibility(View.GONE);
				PropTextView.setVisibility(View.GONE);
				ValTextView.setVisibility(View.GONE);
				editPropView.setVisibility(View.VISIBLE);
				editValView.setVisibility(View.VISIBLE);
				
				positive.setText("Save");neutral.setVisibility(View.GONE);
				positive.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View p1){
							ConfirmTextView.setText("Are you sure you want to save changes..?");
							setDialog(CONFIRM_TYPE);
						}
					});
				break;
			case PRIMARY_TYPE:
				PropContent.setVisibility(View.VISIBLE);
				ConfirmTextView.setVisibility(View.GONE);
				PropTextView.setVisibility(View.VISIBLE);
				ValTextView.setVisibility(View.VISIBLE);
				editPropView.setVisibility(View.GONE);
				editValView.setVisibility(View.GONE);
				
				positive.setText("Edit");neutral.setText("Delete");neutral.setVisibility(View.VISIBLE);
				positive.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View p1){
							mOption=SAVE;
							setDialog(EDIT_TYPE);
						}
					});
				neutral.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View p1){
							mOption=DELETE;
							ConfirmTextView.setText("Are you sure you want to delete this property..?");
							setDialog(CONFIRM_TYPE);
						}
					});
				break;
			case CONFIRM_TYPE:
				PropContent.setVisibility(View.GONE);
				ConfirmTextView.setVisibility(View.VISIBLE);
				positive.setText("Confirm");
				positive.setOnClickListener(onSaveClicked);
				neutral.setVisibility(View.GONE);
		}
		
	}

}
