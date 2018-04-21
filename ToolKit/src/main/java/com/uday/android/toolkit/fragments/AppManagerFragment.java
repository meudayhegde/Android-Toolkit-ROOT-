package com.uday.android.toolkit.fragments;
import android.app.*;
import android.view.*;
import android.os.*;
import android.support.v4.widget.*;
import com.uday.android.toolkit.*;
import android.widget.*;
import android.content.*;


public class AppManagerFragment extends Fragment
{
	
	private RelativeLayout rootView;
	private SwipeRefreshLayout swipeRefreshLayout;
	private Context context;
	
	public AppManagerFragment(){}
	public AppManagerFragment(Context context){
		this.context=context;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if(rootView==null){
			rootView=(RelativeLayout)inflater.inflate(R.layout.app_fragment,container,false);
			onViewFirstCreated();
		}
		return rootView;
	}

	@Override
	public Context getContext()
	{
		if(context==null && Build.VERSION.SDK_INT>=Build.VERSION_CODES.N)
			context= super.getContext();
		return context;
	}
	
	
	
	private void onViewFirstCreated(){
		swipeRefreshLayout=(SwipeRefreshLayout)rootView.findViewById(R.id.swiperefresh);
		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
				@Override
				public void onRefresh(){
					swipeRefreshLayout.setRefreshing(false);
				}
			});
	}
	
}
