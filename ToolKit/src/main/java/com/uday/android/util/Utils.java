package com.uday.android.util;

import android.annotation.*;
import android.content.*;
import android.content.pm.*;
import android.content.res.*;
import android.database.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.util.*;
import android.widget.*;
import com.uday.android.toolkit.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;
import org.tukaani.xz.*;


public class Utils
{
	public static String getSize(File file){
		int count=0;
		double size=file.length();
		String unit="B";
		while(size >= 1000){
			size=size/1024;
			count++;
		}
		switch(count){
			case 0:unit="B";
				break;
			case 1:unit="KB";
				break;
			case 2:unit="MB";
				break;
			case 3:unit="GB";
				break;
			case 4:unit="TB";
				break;
			case 5:unit="EB";
		}
		return Math.round(size*100.0)/100.0+" "+unit;
	}
	

	public static void openFolder(Context context,String directory){
		Uri selectedUri = Uri.parse(directory);
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(selectedUri, "resource/folder");

		if (intent.resolveActivityInfo(context.getPackageManager(), 0) != null){
			context.startActivity(intent);
		}
		else{
			Toast.makeText(context,"install a root explorer that supports recieving intents for opening folders\neg:Es File Explorer",Toast.LENGTH_LONG).show();
		}
	}
	
	
	public static File getExternalSdCard() {
		File externalStorage = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			File storage = new File("/storage");

			if(storage.exists()) {
				try{
					File[] files = storage.listFiles();
					for (File file : files) {
						if (file.exists() && file.canRead()) {
							if (Environment.isExternalStorageRemovable(file)) {
								externalStorage = file;
								break;
							} 
						}
					}
				}catch (Exception e) {
					Log.e("TAG", e.toString());
				}
			}
		} else {
			//pre lolipop methods
		}
		return externalStorage;

	}
	
	//by Ganesh varma
	public static boolean findInPath(String cmd) {
        String[] pathToTest = System.getenv("PATH").split(":");
		boolean DEBUG=true;
        for (String path : pathToTest) {
            File cmdFile = new File(path, cmd);
            if (cmdFile.exists()) {
                if (DEBUG) {
                    Log.d("shell", "Found " + cmd + " at " + cmdFile.getAbsolutePath());
                }

                return true;
            }
        }

        return false;
    }
	
	public static boolean checkAppInstalledByName(PackageManager pm, String packageName) {
	    if (packageName == null || "".equals(packageName))  
	        return false;  
	    try { 
	        ApplicationInfo info = pm.getApplicationInfo(  
				packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
	        Log.d(MainActivity.TAG,  "checkAppInstalledByName: "+packageName+" : found");
	        return true;  
	    } catch (Exception e) {  
	        Log.d(MainActivity.TAG,  "checkAppInstalledByName:"+packageName+" not found");

	        return false;  
	    }  
	}
	
	public static boolean checkAppInstalledByName(PackageManager pm, String packageName,int VERSION) {
	    if (packageName == null || "".equals(packageName))  
	        return false;  
	    try { 
	        ApplicationInfo info = pm.getApplicationInfo(  
				packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
	        Log.d(MainActivity.TAG,  "checkAppInstalledByName: "+packageName+" : found");
	        PackageInfo pInfo=pm.getPackageInfo(packageName,0);
			if(pInfo.versionCode==VERSION){
				return true;
			}
			else return false;
	    } catch (Exception e) {  
	        Log.d(MainActivity.TAG,  "checkAppInstalledByName:"+packageName+" not found");

	        return false;  
	    }  
	}
	
	public static String getString(List<String> input){
		String str=null;
		for(String tmp:input){
			if(str!=null){
				str=str+"\n"+tmp;
			}
			else
				str=tmp;
		}
		return str;
	}
	
	public static String getStringFromInputStream(InputStream is) {

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line+"\n");
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sb.toString();
	}
	
	public static void copyAsset(AssetManager assetManager,String filename,String Out){
		InputStream in = null;
		OutputStream out = null;
		File outDir=new File(Out);
		try
		{
			in = assetManager.open(filename);

			createDir(outDir);
			File outFile = new File(outDir, filename);
			out = new FileOutputStream(outFile);
			copyFile(in, out);
			in.close();
			in = null;
			out.flush();
			out.close();
			out = null;
			if(filename.endsWith(".zip")){
				unpackZip(outFile);
				outFile.delete();
				}
			outFile.setExecutable(true,false);
		}
		catch(IOException e)
		{
			Log.e("tag", "Failed to copy asset file: " + filename, e);
		}
	}
	
	public static void copyAssets(AssetManager assetManager,File outDir)
	{
		String[] files = null;
		try
		{
			files = assetManager.list("");
		}
		catch (IOException e)
		{
			Log.e("tag", "Failed to get asset file list.", e);
		}
		for(String filename : files)
		{
			  copyAsset(assetManager,filename,outDir.getAbsolutePath());
		}
	}
	
	private static void copyFile(InputStream in, OutputStream out) throws IOException
	{
        byte[] buffer = new byte[1024];
		int read;
		while((read = in.read(buffer)) != -1)
		{
			out.write(buffer, 0, read);
		}
	}

	public static void createDir(File dir) throws IOException
	{
		if (dir.exists())
		{
			if (!dir.isDirectory())
			{
				throw new IOException("Can't create directory, a file is in the way");
			}
		}
		else
		{
			dir.mkdirs();
			if (!dir.isDirectory())
			{
				throw new IOException("Unable to create directory");
			}
		}
	}
	
	public static void unpackXZ(File xzFile,boolean keepOriginal){
		try { 
			FileInputStream fin = new FileInputStream(xzFile);
			BufferedInputStream in = new BufferedInputStream(fin);
			File outFile=new File(xzFile.getParent(),xzFile.getName().replace(".xz",""));
			FileOutputStream out = new FileOutputStream(outFile);
			XZInputStream xzIn = new XZInputStream(in);
			final byte[] buffer = new byte[8192];
			int n = 0;
			while (-1 != (n = xzIn.read(buffer))) {
				out.write(buffer, 0, n);
			} 
			out.close();
			xzIn.close();
			if(outFile.exists() && !keepOriginal)xzFile.delete();
		}
		catch(Exception e) { 
			Log.e("Decompress", "unzip", e); 
		}
	}

	public static boolean unpackZip(File zipFile)
	{     
	
		File zipPath=new File(zipFile.getParent()+"/"+zipFile.getName().split(".zip")[0]);
		zipPath=new File(zipFile.getParent()+"/"+"common");
		try{
		createDir(zipPath);
		}catch(IOException ex){return false;}
		
		InputStream is;
		ZipInputStream zis;
		try 
		{
			String filename;
			is = new FileInputStream(zipFile);
			zis = new ZipInputStream(new BufferedInputStream(is));          
			ZipEntry ze;
			byte[] buffer = new byte[1024];
			int count;

			while ((ze = zis.getNextEntry()) != null) 
			{
				filename = ze.getName();

				// Need to create directories if not exists, or
				// it will generate an Exception...
				if (ze.isDirectory()) {
					File fmd = new File(zipPath+"/" + filename);
					fmd.mkdirs();
					continue;
				}

				FileOutputStream fout = new FileOutputStream(zipPath+"/"+filename);
				File f=new File(zipPath+"/"+filename);
				f.setExecutable(true,false); f.setReadable(true,false); f.setWritable(true,true);
				
				while ((count = zis.read(buffer)) != -1) 
				{
					fout.write(buffer, 0, count);             
				}

				fout.close();               
				zis.closeEntry();
			}

			zis.close();
			
		} 
		catch(IOException e)
		{
			return false;
		}
		return true;
	}
}



