
package com.uday.android.toolkit;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.support.v4.widget.*;
import android.view.*;
import android.view.animation.*;
import android.widget.*;
import com.uday.android.toolkit.fragments.*;
import com.uday.android.toolkit.ui.*;
import com.uday.android.util.*;
import eu.chainfire.libsuperuser.*;
import java.io.*;
import android.util.*;
import java.util.*;

public class MainActivity extends Activity
{
	private static boolean isActivityAlive;
	public static String TAG="TOOLKIT";
	public static Shell.Interactive rootSession;
	public static int SCREEN_HEIGHT;
	public static int SCREEN_WIDTH;
	
	public static String TOOL;
	public AnimationSet mGrowIn;
	public static Animation mFadeIn;
	public boolean opened=false;
	
	private KernelFragment mKernel;
	private Fragment fragment;
  	private DrawerLayout mDrawerLayout;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mDrawerArray;
	private AboutFragment mAbout;
	private BatchInstallerFragment mBatch;
	private PartitionSchemeFragment mPart;
	private RawDiskImageFragment mRaw;
	private AndroidImagesFragment mAndroid;
	private AppManagerFragment mAppManager;
	
	private boolean isDuplicateActivity=false;
	private BuildPropFragment mBuildProp;
	private EnvSetup envSetup;
	private float offset;
	private boolean flipped;
	private int selected;
	private boolean doubleBackToExitPressedOnce = false;
	private DrawerLayout.SimpleDrawerListener mDrawerToggle;
	private RelativeLayout mDrawerContent;
	private DrawerArrowDrawable drawerArrow;
	private ListView mDrawerList;
	private DrawerAdapter mAdapter;
	private int selectedItem;
	private BackgroundThread backgroundThread;
	private boolean isViewCreated=false;
	public MenuItem menuReboot,menuSettings;
	public boolean backgroundThreadisRunning=false,isExcepted=false;
	
	
	
	@Override
    protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if(isActivityAlive==true){
			isDuplicateActivity=true;
			finish();
		}
		isActivityAlive=true;
		getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
		DisplayMetrics metrics= getResources().getDisplayMetrics();
		
		SCREEN_HEIGHT=metrics.heightPixels;
		SCREEN_WIDTH=metrics.widthPixels;
		
		mGrowIn=(AnimationSet)OptAnimationLoader.loadAnimation(this,R.anim.activity_push_up_in);
		mFadeIn=OptAnimationLoader.loadAnimation(this,android.R.anim.fade_in);
		envSetup=new EnvSetup(this){
			@Override
			public void onStartup(){
					super.onStartup();
					createFragments();
					isViewCreated=true;
					try{
						drawerSetup();
					}catch(IllegalStateException ex){
						isExcepted=true;
				}
			}
		};
	 
	}

	@Override
	protected void onDestroy()
	{
		if(!isDuplicateActivity)
			isActivityAlive=false;
			
		super.onDestroy();
	}
	
	
	@Override
	protected void onResume()
	{
		if(isExcepted){
			drawerSetup();
			isExcepted=false;
		}
		super.onResume();
	}
	
	@Override
	public void onBackPressed() {
		if (doubleBackToExitPressedOnce) {
			new File(getFilesDir()+"/partition_scheme.info").delete();
			
			super.onBackPressed();
			return;
		}
		
		this.doubleBackToExitPressedOnce = true;
		CustomToast.showNotifyToast(this, "Please click BACK again to exit", Toast.LENGTH_SHORT);
		
		new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					doubleBackToExitPressedOnce=false;                       
				}
			}, 2000);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// TODO: Implement this method
		getMenuInflater().inflate(R.menu.menu_main,menu);
		return true;
	}

	
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		// TODO: Implement this method
		boolean drawerOpen;
		if(mDrawerLayout!=null){
				drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerContent);
			}
		else{
			drawerOpen=false;
		}
		
		menuSettings=menu.findItem(R.id.settings);
		menuSettings.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){
			@Override
			public boolean onMenuItemClick(MenuItem item){
				Intent intent=new Intent(MainActivity.this,SettingsActivity.class);
				startActivity(intent);
				return true;
			}
		});
		menuReboot=menu.findItem(R.id.reboot);
		menuReboot.setOnMenuItemClickListener(new RebootDialog(this));
		
		menuSettings.setVisible(!drawerOpen);
		menuReboot.setVisible(!drawerOpen);
		
		if((fragment==mBatch || fragment==mBuildProp) && fragment!=null){
			hideContextMenu();
		}
		return super.onPrepareOptionsMenu(menu);
	}

	public void disableMenu(){
		if(menuReboot!=null){
			menuReboot.setVisible(false);
			menuSettings.setVisible(false);
		}
	}
	
	public void hideContextMenu(){
		if(menuReboot!=null){
			menuReboot.setShowAsAction(0);
			menuSettings.setShowAsAction(0);
		}
	}
	
    @Override
    public void setTitle(CharSequence title)
	{
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		envSetup.onRequestPermissionsResult(requestCode,permissions,grantResults);   
	}
	
	
	
	private void createFragments(){
		mKernel=new KernelFragment(this);
		mAndroid=new AndroidImagesFragment(this);
		mRaw=new RawDiskImageFragment();
		mPart=new PartitionSchemeFragment(this);
		mBatch=new BatchInstallerFragment(this);
		mAbout=new AboutFragment(this);
		mAppManager=new AppManagerFragment(this);
		mBuildProp=new BuildPropFragment(this);
	}
	
	private void drawerSetup(){
		setContentView(R.layout.activity_main);
        mTitle = mDrawerTitle = getTitle();
        mDrawerArray = getResources().getStringArray(R.array.drawer_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerContent=(RelativeLayout) findViewById(R.id.drawer_content);
		mDrawerList = (ListView) findViewById(R.id.drawer_list_view);
		mAdapter=new DrawerAdapter(this);
		
		mDrawerList.setAdapter(mAdapter);
		addDrawerItems();
	
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		
        getActionBar().setDisplayHomeAsUpEnabled(true);
		
		drawerArrow=new DrawerArrowDrawable(getResources());
		drawerArrow.setStrokeColor(Color.WHITE);
		getActionBar().setHomeAsUpIndicator(drawerArrow);
		mDrawerToggle=new DrawerLayout.SimpleDrawerListener(){
			@Override
            public void onDrawerClosed(View view)
			{
                getActionBar().setTitle(mTitle);
				invalidateOptionsMenu();
				if(mAdapter.getItemViewType(selected)!=1)
					selectItem(selected);
				mDrawerList.setItemChecked(selectedItem,true);
            }

			@Override
            public void onDrawerOpened(View drawerView)
			{
                getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu();
      
            }
			@Override
			public void onDrawerSlide(View drawerView, float slideOffset){
				offset=slideOffset;
				if (slideOffset >= .995) {
					flipped = true;
					drawerArrow.setFlip(flipped);
				} else if (slideOffset <= .005) {
					flipped = false;
					drawerArrow.setFlip(flipped);
				}
				drawerArrow.setParameter(offset);
			}
		};
		
        mDrawerLayout.setDrawerListener(mDrawerToggle);
		selected=6;
		selectItem(selected);
	}
	

	private void addDrawerItems(){
		//mAdapter.addSeparatorItem("disc-ops");
		mAdapter.addItem(new DrawerListData("Kernel",R.drawable.ic_kernel));
		mAdapter.addItem(new DrawerListData("Android magic",R.drawable.ic_android));
		//mAdapter.addItem(new DrawerListData("Disk images",R.drawable.ic_disk));
		mAdapter.addItem(new DrawerListData("Partition Scheme",R.drawable.ic_partition));
		//mAdapter.addSeparatorItem("app-ops");
		//mAdapter.addItem(new DrawerListData("App Manager",R.drawable.ic_app_drawer));
		mAdapter.addItem(new DrawerListData("App Manager",R.drawable.ic_android));
		mAdapter.addItem(new DrawerListData("Apk Manager",R.drawable.ic_android));
		//mAdapter.addSeparatorItem("misc");
		//mAdapter.addItem(new DrawerListData("Text to speach",R.drawable.ic_app_drawer));
		mAdapter.addItem(new DrawerListData("Build prop",R.drawable.ic_build));
		//mAdapter.addSeparatorItem("general");
		//mAdapter.addItem(new DrawerListData("Settings",R.drawable.ic_settings));
		mAdapter.addItem(new DrawerListData("About",R.drawable.ic_about));
	}
	
	
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId()){
			case android.R.id.home:
				if(mDrawerLayout.isDrawerOpen(mDrawerContent))
					mDrawerLayout.closeDrawer(mDrawerContent);
				else
					mDrawerLayout.openDrawer(mDrawerContent);
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	
	
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener
	{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			if(mAdapter.getItemViewType(position)!=1){
         	   selected=position;
			  mDrawerLayout.closeDrawer(Gravity.START);
			}
        }
    }
	
    private void selectItem(int position)
	{
        // update the main content by replacing fragments
        fragment = null;
		switch (position) {
			case 0: //kernel
				fragment=mKernel;
				break;
			case 1://android images
				fragment=mAndroid;
				break;
		/*	case 3://Raw Disk image
				fragment=mRaw;
				break;*/
			case 2://Partition Scheme
				fragment=mPart;
				break;
			case 3:
				fragment=mAppManager;
				break;
			case 4://BatchApp
				fragment=mBatch;
				break;
			case 5://buildprop
				fragment=mBuildProp;
				break;
			case 6://about
				fragment=mAbout;
				break;
			default:
				break;
		}
		if(fragment!=null){
			selectedItem=position;
       		FragmentManager fragmentManager = getFragmentManager();
       	 	fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
			mDrawerList.setItemChecked(selectedItem,true);
    		setTitle(mAdapter.getItem(position).title);
 	 	 	mDrawerLayout.closeDrawer(Gravity.START);
		 }
    }
	
	public void refreshApkScreen(){
		BatchInstallerFragment.apkFilesOrig=null;
		mBatch=new BatchInstallerFragment(MainActivity.this);
		selectItem(4);
	}
	
	public void  runInBackground(Runnable action){
		if(backgroundThread==null){
			backgroundThread=new BackgroundThread();
		}else if(!backgroundThread.isInBackground())
			backgroundThread=new BackgroundThread();
		backgroundThread.runNewAction(action);
	}

	
	private class BackgroundThread extends Thread{
		private boolean keepAlive=true;
		private ArrayList<Runnable> actionsToRun;

		public BackgroundThread(){
			actionsToRun=new ArrayList<Runnable>();
			start();
		}

		
		public boolean isInBackground()
		{
			// TODO: Implement this method
			return keepAlive;
		}

		@Override
		public void run(){
			while(keepAlive){
				while(!actionsToRun.isEmpty()){
					try{
						backgroundThreadisRunning=true;
						actionsToRun.get(0).run();
						actionsToRun.remove(0);
					}catch(NullPointerException ex){
						Log.e(TAG,ex.toString());
					}
				}
				backgroundThreadisRunning=false;
			}

		}

		public void runNewAction(Runnable action){
			actionsToRun.add(action);
		}

		@Override
		public void destroy(){
			keepAlive=false;
		}
	}
	
}



