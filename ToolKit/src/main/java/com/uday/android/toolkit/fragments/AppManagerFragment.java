package com.uday.android.toolkit.fragments;
import android.app.*;
import android.view.*;
import android.os.*;
import android.support.v4.widget.*;
import com.uday.android.toolkit.*;
import android.widget.*;


public class AppManagerFragment extends Fragment
{
	
	private SwipeRefreshLayout rootView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		rootView=(SwipeRefreshLayout)inflater.inflate(R.layout.app_fragment,container,false);
		
		rootView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
			@Override
			public void onRefresh(){
				rootView.setRefreshing(false);
			}
		});
		return rootView;
	}
	
}
