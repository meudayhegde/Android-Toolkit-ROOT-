package com.uday.android.util;

public class DrawerListData
{
	public int icRes;
	public String title;
	public String header;
	
	public DrawerListData(String title,int icRes){
		this.icRes=icRes;
		this.title=title;
	}
	public DrawerListData(String header){
		this.header=header;
	}
}
