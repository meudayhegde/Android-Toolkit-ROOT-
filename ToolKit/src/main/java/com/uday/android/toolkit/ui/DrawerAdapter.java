package com.uday.android.toolkit.ui;

import android.content.*;
import android.view.*;
import android.widget.*;
import com.uday.android.toolkit.*;
import com.uday.android.util.*;
import java.util.*;

public class DrawerAdapter extends BaseAdapter {

	private static final int TYPE_ITEM = 0;
	private static final int TYPE_SEPARATOR = 1;
	private static final int TYPE_MAX_COUNT = TYPE_SEPARATOR + 1;

	private ArrayList<DrawerListData> mData = new ArrayList<DrawerListData>();
	private LayoutInflater mInflater;
	private ViewGroup parent;
	private TreeSet mSeparatorsSet = new TreeSet();

	public DrawerAdapter(Context context) {
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void addItem(DrawerListData item) {
		mData.add(item);
		notifyDataSetChanged();
	}

	public void addSeparatorItem(String item) {
		DrawerListData seperatorItem=new DrawerListData(item);
		mData.add(seperatorItem);
		// save separator position
		mSeparatorsSet.add(mData.size() - 1);
		notifyDataSetChanged();
	}

	@Override
	public int getItemViewType(int position) {
		return mSeparatorsSet.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
	}

	@Override
	public int getViewTypeCount() {
		return TYPE_MAX_COUNT;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public DrawerListData getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		this.parent=parent;
		int type = getItemViewType(position);
			holder = new ViewHolder();
			switch (type) {
				case TYPE_ITEM:
					convertView = mInflater.inflate(R.layout.drawer_list_item,parent,false);
					holder.textView = (TextView)convertView.findViewById(R.id.drawer_text);
					holder.textView.setText(mData.get(position).title);
					holder.imgView=(ImageView)convertView.findViewById(R.id.idIcon);
					holder.imgView.setImageResource(mData.get(position).icRes);
				
					
					break;
					
				case TYPE_SEPARATOR:
					convertView = mInflater.inflate(R.layout.drawer_subtitle, null);
					holder.textView = (TextView)convertView;
					holder.textView.setText(mData.get(position).header);
					convertView.setClickable(false);
					break;
			}
			convertView.setTag(holder);
			
	//	holder.textView.setText(mData.get(position));
		return convertView;
	}

	private static class ViewHolder{
		TextView textView;
		ImageView imgView;
	}
	
}

 
