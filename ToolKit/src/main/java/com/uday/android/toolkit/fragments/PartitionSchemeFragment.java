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

@SuppressLint("NewApi")
public class PartitionSchemeFragment extends Fragment
{
	private TextView indexText,tw[][],dgtxt;
	private TableLayout Table;
	private TableRow Row[];
	private ScrollView Scrl;
	private String scheme="",common,storageModel,Disk,SectorSize,PartitionTable,str,name[],type[];
	private HorizontalScrollView Hscrl;
	public Dialog dialog,dg;
	private int n=0,i=0,j;
	private long blockNo[],blockSize[],startAddr[],endAddr[],DiskSize;
	private Typeface TF;
	private LinearLayout rootView;
	private Context context;
	
	public PartitionSchemeFragment(Context context){
		this.context=context;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		if(rootView==null){
		rootView = (LinearLayout)inflater
			.inflate(R.layout.partition_scheme, container, false);
			setHasOptionsMenu(true);
			
		dialog=new Dialog(context){
			@Override
			public void onBackPressed(){}
		};
		dialog.setCanceledOnTouchOutside(false);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		
		dialog.setContentView(new ProgressBar(context));
		dialog.show();					 
		
		Table=new TableLayout(context);
		Table.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,TableLayout.LayoutParams.WRAP_CONTENT));
		TableRow tr=new TableRow(context);
		TextView tv[]=new TextView[6];
		for(i=0;i<=5;i++){
			tv[i]=new TextView(context);
			tv[i].setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT,i));
			tv[i].setTextColor(Color.DKGRAY);
			tr.addView(tv[i]);
		}
		tv[0].setText("  Name       ");tv[1].setText("  No.");tv[2].setText("Start Addr.");tv[3].setText("End Addr.");tv[4].setText("Size");tv[5].setText("Type");
		
		HorizontalScrollView Hscr=new HorizontalScrollView(context);
		Hscr.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
		//Hscr.addView(tr);
		
		Scrl=new ScrollView(context);
		Scrl.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
		
		Hscrl=new HorizontalScrollView(context);
		Hscrl.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
		
		indexText=new TextView(context);
		indexText.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
		//TF=Typeface.createFromAsset(getActivity().getAssets(), "fonts/Bree Serif-Regular.ttf");
		//indexText.setTypeface(TF);
		indexText.setTextColor(Color.BLACK);
		
		Scrl.addView(Table);
		rootView.addView(indexText);
		rootView.addView(tr);
		Hscrl.addView(Scrl);
		rootView.addView(Hscrl);

		ThreadExec();
		
		}
		
		rootView.startAnimation(((MainActivity)context).mGrowIn);
		return rootView;
	}
	
	private void ThreadExec(){
		
		new Thread(){
			@Override
			public void run(){
				File dataFile=new File(context.getFilesDir()+"/partition_scheme.info");
				if(dataFile.exists()){
					try{
						String tmp="";
						BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(dataFile)));
						while((tmp=reader.readLine())!=null){
							scheme+=tmp+"\n";
						}
					}catch(IOException ex){
						Log.e("Exception","Failed to read "+dataFile.getAbsolutePath(),ex);
					}
					setTableView(scheme);
				}
				else{
					MainActivity.rootSession.addCommand(context.getFilesDir()+"/common/partition_scheme.sh "+MainActivity.TOOL+" 'b'",2323,new Shell.OnCommandResultListener(){
						@Override
						public void onCommandResult(int commandcode,int exitcode,List<String> output){
							scheme=Utils.getString(output);
							writeToFile(scheme,"partition_scheme.info",context);
							setTableView(scheme);
						}
					});		
				}
				
			}
		}.start();
	}
	
	
	private void setTableView(String checkStr){
		
		str=checkStr;
		Table.post(new Runnable(){
				@Override
				public void run(){
					parseParted(str);
					indexText.setText(common);
					setValues();
					dialog.cancel();
				}
			});
	}
	
	private void writeToFile(String data,String fileName,Context context) {
		
		FileOutputStream outputStream;
		try {
			outputStream =context.openFileOutput(fileName, Context.MODE_PRIVATE);
			outputStream.write(data.getBytes());
			outputStream.close();
		} catch (Exception e) {
			Log.e("Exception", "File write failed: " + e.toString());
		} 
	}
	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.popup_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action buttons
        switch(item.getItemId()) {
			case R.id.jbyte:indexText.setText("\n  "+storageModel+"\n\n"+Disk+"   "+DiskSize+"B"+"\n\n  "+SectorSize+"\n\n  "+PartitionTable);
				for(i=0;i<n-1;i++){
					setTw(i,1,"B");
				}
				return true;
			case R.id.kbyte:indexText.setText("\n  "+storageModel+"\n\n"+Disk+"   "+DiskSize/1024+"KB"+"\n\n  "+SectorSize+"\n\n  "+PartitionTable);
				for(i=0;i<n-1;i++){
					setTw(i,1024,"KB");
					
				}
					return true;
			case R.id.mbyte:indexText.setText("\n  "+storageModel+"\n\n"+Disk+"   "+DiskSize/(1024*1024)+"MB"+"\n\n  "+SectorSize+"\n\n  "+PartitionTable);
				for(i=0;i<n-1;i++){
					
					setTw(i,1024*1024,"MB");
				}
				return true;
			case R.id.gbyte:indexText.setText("\n  "+storageModel+"\n\n"+Disk+"   "+DiskSize/(1024*1024*1024)+"GB"+"\n\n  "+SectorSize+"\n\n  "+PartitionTable);
				for(i=0;i<n-1;i++){
					setTw(i,1024*1024*1024,"GB");
				}
				return true;
   		 }
		return super.onOptionsItemSelected(item);
		
	}
	
	private void parseParted(String checkStr){
		
		String[] parts=str.split("\n");
		for(String tmp: parts)
		{
			if(tmp.contains("Model"))
			{
				storageModel=tmp;
			}
			else if(tmp.contains("Disk "))
			{
				String me="";
				Disk="";
				for(String test:tmp.split(" ")){
					Disk=(Disk+" "+me).toString();
					me=test;
					}
				DiskSize=Long.parseLong(me.split("B")[0].toString());
			}
			else if(tmp.contains("Sector size"))
			{
				SectorSize=tmp;
			}
			else if(tmp.contains("Partition Table"))
			{
				PartitionTable=tmp+"\n";
			}
			else if(tmp.contains("Number "))
			{
				Row=new TableRow[str.split(tmp)[1].split("\n").length];
				tw=new TextView[str.split(tmp)[1].split("\n").length][6];
				
				name=new String[str.split(tmp)[1].split("\n").length];
				blockNo=new long[str.split(tmp)[1].split("\n").length];
				startAddr=new long[str.split(tmp)[1].split("\n").length];
				endAddr=new long[str.split(tmp)[1].split("\n").length];
				blockSize=new long[str.split(tmp)[1].split("\n").length];
				type=new String[str.split(tmp)[1].split("\n").length];
				
				for(String test : str.split(tmp)[1].split("\n"))
				{
					if(test.length()>10)
					{
						i=0;
						for(String another :test.split(" "))
							if(!another.equals("") && !another.contains(" ") && !another.equals(null))
							{
								switch(i)
								{
									case 0:blockNo[n]=Integer.parseInt(another);break;
									case 1:startAddr[n]=Long.parseLong(another.split("B")[0].toString());
										break;
									case 2:endAddr[n]=Long.parseLong(another.split("B")[0].toString());break;
									case 3:blockSize[n]=Long.parseLong(another.split("B")[0].toString());break;
									default:try{
										if(!name[n].equals(null))
										type[n]=name[n];
										else type[n]="";
									}catch(Exception ex){
										type[n]="";
									}
										name[n]=another;break;
								}
								i++;
							}
						n++;

					}
				}
			}
			
		}
	}
	
	private void setValues(){
		
		dg=new Dialog(context);
		dgtxt=new TextView(context);
		dgtxt.setTextColor(Color.BLACK);
		dgtxt.setTextSize((float)17);
		dgtxt.setTypeface(TF);
		dg.setContentView(dgtxt);
		
		for(i=0;i<n-1;i++){
			Row[i]=new TableRow(context);
			Row[i].setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT));
		
			for(j=0;j<=5;j++){
				tw[i][j]=new TextView(context);
				tw[i][j].setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,50,j));
				tw[i][j].setTextColor(Color.BLACK);
				//tw[i][j].setTypeface(TF);
				Row[i].addView(tw[i][j]);
			}
			Row[i].setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1){
					TableRow ThisRow=(TableRow)p1;
					
					TextView text[]=new TextView[6];
					for(i=0;i<=5;i++){
					text[i]=(TextView)ThisRow.getChildAt(i);
					}
					dgtxt.setText("\n   Block dev:  /dev/block/mmcblk0p"+text[1].getText()+"\n\n   Type:  "+text[5].getText()+"\n\n   Start Address:  "+text[2].getText()+"\n\n   End Address:  "+text[3].getText()+"\n\n   Block Size:  "+text[4].getText()+"\n");
		
					dg.setTitle(" "+text[0].getText());
					dg.show();
				}
			});
			setTw(i,1,"B");
			Table.addView(Row[i]);
		}
		indexText.setText("\n  "+storageModel+"\n\n"+Disk+"   "+DiskSize+"B"+"\n\n  "+SectorSize+"\n\n  "+PartitionTable);
	}
	
	
	public void setTw(int i,long unit,String unitS){
		tw[i][0].setText("  "+name[i]+"  ");
		tw[i][1].setText(blockNo[i]+" ");
		tw[i][2].setText("  "+startAddr[i]/unit+unitS);
		tw[i][3].setText("  "+endAddr[i]/unit+unitS);
		tw[i][4].setText("  "+blockSize[i]/unit+unitS);
		tw[i][5].setText("  "+type[i]+"    ");
	}
}

