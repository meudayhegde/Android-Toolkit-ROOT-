package com.uday.android.toolkit.ui;
import android.content.*;
import android.app.*;
import android.widget.*;
import com.uday.android.toolkit.R;
import android.view.*;
import android.view.View.*;
import android.graphics.*;
import android.util.*;
import android.text.*;
import com.uday.android.toolkit.*;

public class DialogUtils
{
	private Context context;
	private AlertDialog mDialog;
	private AlertDialog.Builder dialogBuilder;
	private Button mPositive,mNegative,mNeutral;
	private View mView;
	
	public DialogUtils(Context context){
		this.context=context;
		dialogBuilder=new AlertDialog.Builder(context)
					.setPositiveButton("ok",null)
					.setTitle(" ").setMessage(" ")
					.setNegativeButton("cancel",null)
					.setNeutralButton("another",null);
	}
	
	public DialogUtils create(){
		mDialog=dialogBuilder.create();
		mDialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
		return this;
	}
	
	public DialogUtils setView(View view){
		mView=view;
		dialogBuilder.setView(view);
		return this;
	}
	
	public DialogUtils setView(int layoutRes){
		mView=((Activity)context).getLayoutInflater().inflate(layoutRes,null);
		dialogBuilder.setView(mView);
		return this;
	}
	
	public Button getPositiveButton(){
		return mPositive;
	}
	
	public Button getNegativeButton(){
		return mNegative;
	}
	
	public Button getNeutralButton(){
		if(mNeutral!=null){
			mNeutral.setVisibility(View.VISIBLE);
		}
		return mNeutral;
	}
	public AlertDialog getDialog(){
		return mDialog;
	}
	
	public DialogUtils setTitle(String title){
		mDialog.setTitle(title);
		return this;
	}
	
	public DialogUtils setMessage(String message){
		mDialog.setMessage(message);
		return this;
	}
	
	public DialogUtils show(){
		mDialog.show();
		if(mPositive ==null)
			mPositive=mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
		if(mNegative==null)
			mNegative=mDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
		if(mNeutral==null)
			mNeutral=mDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
		return this;
	}
	
	public DialogUtils cancel(){
		mDialog.cancel();
		return this;
	}
	
	public DialogUtils showConfirmDialog(boolean retainView,int icRes,String title,String message,String cancelText,String confirmText,final OnClickListener confirmListener){
		if(mDialog==null)create();
		show();
		mNeutral.setVisibility(View.GONE);
		if(mView!=null && !retainView)mView.setVisibility(View.GONE);
		if(title!=null)mDialog.setTitle(title);else mDialog.setTitle("");
		if(message!=null)mDialog.setMessage(message);else mDialog.setMessage("");
		if(cancelText!=null)mNegative.setText(cancelText);else mNegative.setVisibility(View.GONE);
		if(confirmText!=null)mPositive.setText(confirmText);else mPositive.setVisibility(View.GONE);
		if(icRes!=0)mDialog.setIcon(icRes);else mDialog.setIcon(null);
		if(confirmListener!=null)mPositive.setOnClickListener(new View.OnClickListener(){
			@Override public void onClick(View p1){
				confirmListener.onClick(mDialog);
			}
		});
		
		return this;
	}
	
	public static EditText showEditTextDialog(Context context,String title,String message,String editorContent,String description,String confirmText,final DialogUtils.OnClickListener confirmListener){
		LinearLayout contentView=(LinearLayout)((Activity)context).getLayoutInflater().inflate(R.layout.edit_text,null);
		EditText edtTxt=(EditText)contentView.findViewById(R.id.edt_txt);
		edtTxt.setText(editorContent);
		edtTxt.setHint(description);
		edtTxt.setSelectAllOnFocus(true);
		edtTxt.setSingleLine(true);
		final AlertDialog dialog=new AlertDialog.Builder(context)
							.setTitle(title)
							.setMessage(message)
							.setView(contentView)
							.setPositiveButton(confirmText,null)
							.setNegativeButton("cancel",null)
							.create();
			dialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
		dialog.show();
		dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View p1){
				confirmListener.onClick(dialog);
			}
		});
		
		return edtTxt;
	}
	
	public static Object[] showTermDialog(Context context,String title,String subtitle,String positive,String negative){
		LinearLayout titleView=(LinearLayout)((Activity)context).getLayoutInflater().inflate(R.layout.term_dialog_header,null);
		((TextView)titleView.findViewById(R.id.term_title)).setText(title);
		((TextView)titleView.findViewById(R.id.term_dscrptn)).setText(subtitle);
		
		LinearLayout contentView=(LinearLayout)((Activity)context).getLayoutInflater().inflate(R.layout.term_dialog_content,null);
		contentView.findViewById(R.id.term_scroller).setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,(int)(MainActivity.SCREEN_HEIGHT*0.5)));
		AlertDialog dialog=new AlertDialog.Builder(context)
				.setCustomTitle(titleView)
				.setView(contentView)
				.setNegativeButton(negative,null)
				.setPositiveButton(positive,null)
				.create();
		dialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
		dialog.show();
		dialog.setCancelable(false);
		dialog.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.GONE);
		dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
		
		return new Object[]{dialog,contentView.findViewById(R.id.term_text),contentView.findViewById(R.id.term_progress)};
	}
	
	
	public static AlertDialog showConfirmDialog(Context context,String title,String content,Spanned spannedContent,String confirmTxt,final OnClickListener listener){
		AlertDialog.Builder dialogBuilder= new AlertDialog.Builder(context).setTitle(title)
							.setMessage(content)
							.setNegativeButton("cancel",null)
							.setPositiveButton(confirmTxt,null);
		if(spannedContent!=null){
			TextView tw=new TextView(context);
			tw.setPadding(40,15,20,0);
			tw.setText(spannedContent);
			dialogBuilder.setView(tw);
		}
		final AlertDialog dialog=dialogBuilder.create();		
		dialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
		dialog.show();
		if(listener!=null){
			dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View p1){
					listener.onClick(dialog);
				}
			});
		}
		return dialog;
	}
	
	public static interface OnClickListener{
		public void onClick(AlertDialog p1);
	}
}
