package com.uday.android.toolkit.fragments;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.widget.LinearLayout.*;
import com.uday.android.toolkit.*;
import com.uday.android.util.*;
import eu.chainfire.libsuperuser.*;
import java.io.*;
import java.util.*;

import android.view.View.OnClickListener;
import android.support.v4.widget.*;
import com.uday.android.toolkit.ui.*;

@SuppressLint("NewApi")
public class PartitionSchemeFragment extends Fragment
{
	private Context context;
	private File DISK;
	private ListView partitionListView;
	private long DISK_SIZE;
	private PartitionListAdapter adapter;
	private RelativeLayout rootView;
	private String SECTOR_SIZE;
	private String STORAGE_MODEL;
	private String PARTITION_TABLE;
	private SwipeRefreshLayout partitionSwipeRefresh;
	private PartedParserRunnable partedParser;
	private View headerView;
	private List<String> mainBlockDevices;
	private ArrayAdapter spinnerAdapter;
	private final String blockDev0="/dev/block/mmcblk0";
	private final String blockDev1="/dev/block/mmcblk1";
	
	private static ArrayList<BlockDeviceListData> blockDevicesList;
	
	public PartitionSchemeFragment(Context context){
		this.context=context;
		partedParser=new PartedParserRunnable();
	}

	@Override
	public Context getContext()
	{
		if(Build.VERSION.SDK_INT<=Build.VERSION_CODES.M)
			return context;
		return super.getContext();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		if(rootView==null){
			rootView = (RelativeLayout)inflater
				.inflate(R.layout.partition_scheme, container, false);
			onViewFirstCreated();
		}
		
		rootView.startAnimation(((MainActivity)context).mGrowIn);
		return rootView;
	}
	
	private void onViewFirstCreated(){
		partitionSwipeRefresh=(SwipeRefreshLayout)rootView.findViewById(R.id.partition_swipe_refresh);
		partitionSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
			@Override public void onRefresh(){
				partitionSwipeRefresh.setRefreshing(true);
				refresh(1);
			}
		});
		partitionListView=(ListView)rootView.findViewById(R.id.partition_list_view);
		if(blockDevicesList==null){
			partitionSwipeRefresh.setRefreshing(true);
			blockDevicesList=new ArrayList<BlockDeviceListData>();
			newAdapter();
			refresh(0);
		}else newAdapter();
		
		mainBlockDevices=new ArrayList<String>();
		if(new File(blockDev0).exists())mainBlockDevices.add(blockDev0);
		if(new File(blockDev1).exists())mainBlockDevices.add(blockDev1);
		partitionListView.setAdapter(adapter);
		headerView=((Activity)getContext()).getLayoutInflater().inflate(R.layout.partition_view_header,null);
		spinnerAdapter=new ArrayAdapter(getContext(),android.R.layout.simple_list_item_1,mainBlockDevices);
		((Spinner)headerView.findViewById(R.id.part_disk_spinner)).setAdapter(spinnerAdapter);
	
		headerView.setVisibility(View.GONE);
		partitionListView.addHeaderView(headerView);
	}
	
	private void newAdapter(){
		adapter=new PartitionListAdapter(getContext(),R.layout.partition_list_item,blockDevicesList);
	}
	
	private void refresh(final int type){
		blockDevicesList.clear();
		adapter.notifyDataSetChanged();
		if(!((MainActivity)getContext()).backgroundThreadisRunning){
			runInBackground(partedParser.setType(type));
		}else{
			new Thread(partedParser).start();
		}
	}

	private void onLoadingCompleted(String partedData){
		blockDevicesList.clear();
		blockDevicesList.addAll(parseParted(partedData));
		runOnUiThread(new Runnable(){
				@Override
				public void run(){
					headerView.setVisibility(View.VISIBLE);
					adapter.notifyDataSetChanged();
					partitionSwipeRefresh.setRefreshing(false);
					setHasOptionsMenu(true);
					mainBlockDevices.remove(0);
					mainBlockDevices.add(0,blockDev0+" : "+Utils.getConventionalSize(DISK_SIZE));
					spinnerAdapter.notifyDataSetChanged();
					((TextView)headerView.findViewById(R.id.part_table)).setText(PARTITION_TABLE);
					((TextView)headerView.findViewById(R.id.disk_model)).setText(STORAGE_MODEL);
					((TextView)headerView.findViewById(R.id.sector_size)).setText(SECTOR_SIZE);
				}
			});
	}
	
	private void runInBackground(Runnable action){
		((MainActivity)getContext()).runInBackground(action);
	}
	
	private void runOnUiThread(Runnable action){
		((MainActivity)getContext()).runOnUiThread(action);
	}
	
	private void writeToFile(String data,String fileName,Context context){
		FileOutputStream outputStream;
		try{
			outputStream =context.openFileOutput(fileName, Context.MODE_PRIVATE);
			outputStream.write(data.getBytes());
			outputStream.close();
		}catch(Exception ex){
			Log.e(MainActivity.TAG,"File write failed: "+ex.toString());
		} 
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_part_unit,menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item){
		for(BlockDeviceListData data:blockDevicesList)
			data.setSizeUnit(item.getItemId());
		adapter.notifyDataSetChanged();
		return super.onOptionsItemSelected(item);
	}
	
	private ArrayList<BlockDeviceListData> parseParted(String str){
		ArrayList<BlockDeviceListData> devicesList=new ArrayList<BlockDeviceListData>();
		for(String tmp: str.split("\n"))
		{
			if(STORAGE_MODEL==null && tmp.contains("Model"))
			{
				STORAGE_MODEL=tmp.replace("Model: ","").replace("Model","");
			}
			else if(tmp.contains("Disk "))
			{
				try{
					DISK=new File(tmp.split(":")[0].split(" ")[1]);
					DISK_SIZE=Long.parseLong(tmp.split(":")[1].replace("B","").replace(" ",""));
				}catch(NullPointerException ex){
					Log.e(MainActivity.TAG,ex.toString());
				}
			}
			else if(tmp.contains("Sector size"))
			{
				try{
					SECTOR_SIZE=tmp.split(":")[1].replace(" ","");
				}catch(NullPointerException ex){
					Log.e(MainActivity.TAG,ex.toString());
				}
			}
			else if(tmp.contains("Partition Table"))
			{
				try{
					PARTITION_TABLE=tmp.split(":")[1].replace(" ","");
				}catch(NullPointerException ex){
					Log.e(MainActivity.TAG,ex.toString());
				}
			}
			else if(tmp.contains("Number "))
				for(String test : str.split(tmp)[1].split("\n"))
					if(test.length()>10){
						int i=0;
						BlockDeviceListData data=new BlockDeviceListData();
						for(String another :test.split(" "))
							if(!another.equals("")){
								switch(i)
								{
									case 0:data.setBlock(new File(DISK+"p"+Integer.parseInt(another)));break;
									case 1:data.setStart(Long.parseLong(another.split("B")[0].toString()));break;
									case 2:data.setEnd(Long.parseLong(another.split("B")[0].toString()));break;
									case 3:data.setSize(Long.parseLong(another.split("B")[0].toString()));break;
									default:try{
												if(!(data.getName()==null))
													data.setType(data.getName());
												else data.setType("");
											}catch(Exception ex){
												data.setType("");
											}
											data.setName(another);break;
								}
								i++;
							}
						if(data.getBlock()!=null)
							devicesList.add(data);
					}
		}
		return devicesList;
	}
	
	private class PartedParserRunnable implements Runnable{
		private int type=0;

		public Runnable setType(int type){
			this.type=type;
			return this;
		}

		@Override
		public void run(){
			File dataFile=new File(getContext().getFilesDir()+"/partition_scheme.info");
			String scheme="";
			if(dataFile.exists() && type==0){
				try{
					String tmp="";
					BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(dataFile)));
					while((tmp=reader.readLine())!=null){
						scheme+=tmp+"\n";
					}
					onLoadingCompleted(scheme);
				}catch(IOException ex){
					Log.e("Exception","Failed to read "+dataFile.getAbsolutePath(),ex);
				}
			}
			else{
				if(dataFile.exists())dataFile.delete();
				MainActivity.rootSession.addCommand(getContext().getFilesDir()+"/common/partition_scheme.sh "+MainActivity.TOOL+" 'b'",2323,new Shell.OnCommandResultListener(){
						@Override
						public void onCommandResult(int commandcode,int exitcode,List<String> output){
							writeToFile(Utils.getString(output),"partition_scheme.info",context);
							onLoadingCompleted(Utils.getString(output));
						}
					});		
			}
		}
	}
}

