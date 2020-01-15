package com.uday.android.util

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.io.File
import java.net.URISyntaxException

object PathUtil {
    fun getPath(context: Context,uri: Uri):String?{
        return if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.O){
            val id = uri.path?.split(':')
            if((id?: listOf(""))[0].contains("primary",ignoreCase = true))
                Environment.getExternalStorageDirectory().absolutePath+ File.separator + (id?: listOf("",""))[1]
            else
                Environment.getExternalStorageDirectory().absolutePath.split(File.separator)[1]+
                        File.separator+(id?: listOf(""))[0].replace(File.separator,"").replace("tree","")+
                        File.separator+(id?: listOf("",""))[1]
        }else
            getPathBellowOreo(context,uri)
    }

    @SuppressLint("NewApi")
    @Throws(URISyntaxException::class)
    fun getPathBellowOreo(context: Context, uri_final: Uri): String? {
        var uri = uri_final
        val needToCheckUri = Build.VERSION.SDK_INT >= 19
        var selection: String? = null
        var selectionArgs: Array<String>? = null
        if (needToCheckUri && DocumentsContract.isDocumentUri(context.applicationContext, uri)) {
            when {
                isExternalStorageDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
                isDownloadsDocument(uri) -> {
                    val id = DocumentsContract.getDocumentId(uri)
                    uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id)
                    )
                }
                isMediaDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    when (split[0]) {
                        "image" -> uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        "video" -> uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        "audio" -> uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    selection = "_id=?"
                    selectionArgs = arrayOf(split[1])
                }
            }
        }
        if ("content".equals(uri.scheme, ignoreCase = true)) {
            val projection =
                arrayOf(MediaStore.Images.Media.DATA)
            try {
                val cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
                val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                if (cursor?.moveToFirst() == true) {
                    val path = cursor.getString(columnIndex?:0)
                    cursor.close()
                    return path
                }
                cursor?.close()
            } catch (e: Exception) { }
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }
}