package com.uday.android.util;

import java.io.File;
import com.uday.android.toolkit.R;

public class BlockDeviceListData
{
	private File BLOCK_DEV;
	private long END_ADDR;
	private String END_ADDR_STR;
	private long SIZE;
	private String SIZE_STR;
	private long START_ADDR;
	private String START_ADDR_STR;
	private String NAME;
	private String TYPE;
	private int SIZE_UNIT;
	
	public BlockDeviceListData(){
		SIZE_UNIT=R.id.unit_dynamic;
	}
	
	public BlockDeviceListData setSizeUnit(int unit){
		this.SIZE_UNIT=unit;
		setUnit();
		return this;
	}
	
	private void setUnit(){
		switch(SIZE_UNIT){
			case R.id.unit_dynamic:SIZE_STR=Utils.getConventionalSize(SIZE);
				START_ADDR_STR=Utils.getConventionalSize(START_ADDR);
				END_ADDR_STR=Utils.getConventionalSize(END_ADDR);
				break;
			case R.id.unit_byte:SIZE_STR=SIZE+" B";
				START_ADDR_STR=START_ADDR+" B";
				END_ADDR_STR=END_ADDR+" B";
				break;
			case R.id.unit_kbyte:SIZE_STR=Utils.getSizeInKb(SIZE);
				START_ADDR_STR=Utils.getSizeInKb(START_ADDR);
				END_ADDR_STR=Utils.getSizeInKb(END_ADDR);
				break;
			case R.id.unit_mbyte:SIZE_STR=Utils.getSizeInMb(SIZE);
				START_ADDR_STR=Utils.getSizeInMb(START_ADDR);
				END_ADDR_STR=Utils.getSizeInMb(END_ADDR);
				break;
			case R.id.unit_gbyte:SIZE_STR=Utils.getSizeInGb(SIZE);
				START_ADDR_STR=Utils.getSizeInGb(START_ADDR);
				END_ADDR_STR=Utils.getSizeInGb(END_ADDR);
				break;
		}
	}
	
	public BlockDeviceListData setName(String name){
		this.NAME=name;
		return this;
	}
	
	public BlockDeviceListData setType(String type){
		this.TYPE=type;
		return this;
	}
	
	public BlockDeviceListData setStart(long start){
		this.START_ADDR=start;
		START_ADDR_STR=Utils.getConventionalSize(START_ADDR);
		return this;
	}
	
	public String getSizeStr(){
		return SIZE_STR;
	}
	
	public String getStartStr(){
		return START_ADDR_STR;
	}
	
	public String getEndStr(){
		return END_ADDR_STR;
	}
	
	public BlockDeviceListData setEnd(long end){
		this.END_ADDR=end;
		END_ADDR_STR=Utils.getConventionalSize(END_ADDR);
		return this;
	}
	
	public BlockDeviceListData setSize(long size){
		this.SIZE=size;
		SIZE_STR=Utils.getConventionalSize(SIZE);
		return this;
	}
	
	public BlockDeviceListData setBlock(File file){
		this.BLOCK_DEV=file;
		return this;
	}
	
	public String getName(){
		return this.NAME;
	}

	public String getType(){
		return this.TYPE;
	}

	public long getStart(){
		return this.START_ADDR;
	}

	public long getEnd(){
		return this.END_ADDR;
	}

	public long getSize(){
		return this.SIZE;
	}

	public File getBlock(){
		return this.BLOCK_DEV;
	}
}
