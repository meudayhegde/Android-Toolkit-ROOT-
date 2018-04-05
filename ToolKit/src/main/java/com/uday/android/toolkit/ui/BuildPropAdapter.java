package com.uday.android.toolkit.ui;
import android.content.*;
import android.view.*;
import android.widget.*;
import com.uday.android.toolkit.*;
import com.uday.android.toolkit.fragments.*;
import com.uday.android.util.*;
import java.util.*;
import android.text.*;

public class BuildPropAdapter extends ArrayAdapter<BuildProperty>
{
	
	private BuildPropFragment fragment;
	
	public BuildPropAdapter(BuildPropFragment fragment){
		super(fragment.context,R.layout.build_prop_list_item,fragment.buildProperties);
		this.fragment=fragment;
		
		
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		
		LayoutInflater inflater=(LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View row=inflater.inflate(R.layout.build_prop_list_item,parent,false);
		TextView propTxt=(TextView)row.findViewById(R.id.build_property),
		valTxt=(TextView)row.findViewById(R.id.build_value);
		
		String value=fragment.buildProperties.get(position).VALUE
		,property=fragment.buildProperties.get(position).PROPERTY
		,txtSearch=fragment.buildProperties.get(position).textSearch;
		
		if(txtSearch!=null &&  value.toLowerCase().contains(txtSearch.toLowerCase())){
			String regx="";
			int start=value.toLowerCase().indexOf(txtSearch.toLowerCase()),length=txtSearch.length();
			for(int i=0;i<length;i++){
				regx=regx+value.charAt(start+i);
			}
			value=value.replace(regx,"<font color="+'"'+"#00AEFF"+'"'+">"+regx+"</font>");
		}
		
		if(txtSearch!=null &&  property.toLowerCase().contains(txtSearch.toLowerCase())){
			String regx="";
			int start=property.toLowerCase().indexOf(txtSearch.toLowerCase()),length=txtSearch.length();
			for(int i=0;i<length;i++){
				regx=regx+property.charAt(start+i);
			}
			property=property.replace(regx,"<font color="+'"'+"#00AEFF"+'"'+">"+regx+"</font>");
		}
		
		valTxt.setText(Html.fromHtml(value));
		propTxt.setText(Html.fromHtml(property));
		
		row.setOnClickListener(new OnItemClick(fragment.buildProperties.get(position),position));
		
		return row;
	}
	
	private class OnItemClick implements View.OnClickListener
	{
		private BuildProperty prop;
		private int position;
		public OnItemClick(BuildProperty prop,int position){
			this.prop=prop;
			this.position=position;
		}
		
		@Override
		public void onClick(View p1)
		{
			if(fragment.dialog==null)
				fragment.initDialog();
			else
				fragment.dialog.show();
				
			fragment.selected=position;
			fragment.setDialog(BuildPropFragment.PRIMARY_TYPE);
			fragment.editPropView.setText(prop.PROPERTY);
			fragment.editValView.setText(prop.VALUE);
			fragment.PropTextView.setText(prop.PROPERTY);
			fragment.ValTextView.setText(prop.VALUE);
			
		}
	}
	
	public void filter(String charText) {
		charText = charText.toLowerCase(Locale.getDefault());
		fragment.buildProperties.clear();
		if (charText.length() == 0) {
			fragment.buildProperties.addAll(fragment.buildPropertiesOrig);
			for(BuildProperty property : fragment.buildProperties){
				property.textSearch=null;
			}
		} else {
			for (BuildProperty data : fragment.buildPropertiesOrig) {
				if ((data.PROPERTY+data.VALUE).toLowerCase(Locale.getDefault()).contains(charText)) {
					data.textSearch=charText;
					fragment.buildProperties.add(data);
				}
			}
		}
		notifyDataSetChanged();
	}
	
}
