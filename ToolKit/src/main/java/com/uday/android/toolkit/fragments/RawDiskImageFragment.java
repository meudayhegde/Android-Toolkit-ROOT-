package com.uday.android.toolkit.fragments;

import android.annotation.*;
import android.app.*;
import android.os.*;
import android.view.*;
import com.uday.android.toolkit.*;

@SuppressLint("NewApi")
public class RawDiskImageFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View rootView = inflater
			.inflate(R.layout.android_image, container, false);
		
		return rootView;
	}

}
