package com.uday.android.toolkit.ui;
import android.widget.ArrayAdapter;
import com.uday.android.util.BlockDeviceListData;
import android.content.Context;
import java.util.ArrayList;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;
import com.uday.android.toolkit.R;
import android.widget.ImageView;
import android.view.animation.Animation;
import com.uday.android.util.OptAnimationLoader;

public class PartitionListAdapter extends ArrayAdapter<BlockDeviceListData>
{
	private ArrayList<BlockDeviceListData> partitionList;
	private Context context;
	private int layout_res;
	
	public PartitionListAdapter(Context context,int layoutRes,ArrayList<BlockDeviceListData> partitionListData){
		super(context,layoutRes,partitionListData);
		this.partitionList=partitionListData;
		this.context=context;
		this.layout_res=layoutRes;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		
		LayoutInflater inflater=(LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View row=inflater.inflate(layout_res,parent,false);
		final BlockDeviceListData blockDevice=partitionList.get(position);
		
		((TextView)row.findViewById(R.id.part_name)).setText(blockDevice.getName());
		((TextView)row.findViewById(R.id.block_dev)).setText(blockDevice.getBlock().getAbsolutePath());
		((TextView)row.findViewById(R.id.part_size)).setText(blockDevice.getSizeStr());
		((TextView)row.findViewById(R.id.start_addr)).setText(blockDevice.getStartStr());
		((TextView)row.findViewById(R.id.end_addr)).setText(blockDevice.getEndStr());
		row.findViewById(R.id.size_expand).setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View p1){
				final View addrs=row.findViewById(R.id.part_size_addr);
				if(addrs.getVisibility()==View.GONE){
					Animation animation=OptAnimationLoader.loadAnimation(getContext(),R.anim.expand_to_down);
					addrs.setVisibility(View.VISIBLE);
					addrs.startAnimation(animation);
				}else{
					Animation animation=OptAnimationLoader.loadAnimation(getContext(),R.anim.shrink_to_top);
					animation.setAnimationListener(new Animation.AnimationListener(){
							@Override
							public void onAnimationStart(Animation p1){}
							public void onAnimationEnd(Animation p1){
								addrs.setVisibility(View.GONE);
							}
							public void onAnimationRepeat(Animation p1){}
					});
					addrs.startAnimation(animation);
				}
			}
		});
		return row;
	}
}
