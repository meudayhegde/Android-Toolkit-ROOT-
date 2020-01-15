package com.uday.android.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.uday.android.toolkit.MainActivity
import org.tukaani.xz.XZInputStream
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


object Utils {

    //pre lolipop methods
    val externalSdCard: File?
        get() {
            var externalStorage: File? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val storage = File("/storage")

                if (storage.exists()) {
                    try {
                        val files = storage.listFiles()
                        for (file in files!!) {
                            if (file.exists() && file.canRead()) {
                                if (Environment.isExternalStorageRemovable(file)) {
                                    externalStorage = file
                                    break
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("TAG", e.toString())
                    }

                }
            }

            return externalStorage

        }

    fun getSize(file: File): String {
        return getConventionalSize(file.length().toLong())
    }

    fun getConventionalSize(size: Long): String {
        var size = size
        var count = 0
        var unit = "B"
        while (size >= 1000) {
            size = size / 1024
            count++
        }
        when (count) {
            0 -> unit = "B"
            1 -> unit = "KB"
            2 -> unit = "MB"
            3 -> unit = "GB"
            4 -> unit = "TB"
            5 -> unit = "EB"
        }
        return (Math.round(size * 100.0) / 100.0).toString() + " " + unit
    }

    internal fun getSizeInKb(size: Long): String {
        return (Math.round(size / 1024 * 100.0) / 100.0).toString() + " KB"
    }

    internal fun getSizeInMb(size: Long): String {
        return (Math.round(size / (1024 * 1024) * 100.0) / 100.0).toString() + " MB"
    }

    internal fun getSizeInGb(size: Long): String {
        return (Math.round(size / (1024 * 1024 * 1024) * 100.0) / 100.0).toString() + " GB"
    }


    fun openFolder(context: Context, directory: String) {
        val selectedUri = Uri.parse(directory)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(selectedUri, "resource/folder")

        if (intent.resolveActivityInfo(context.packageManager, 0) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(
                context,
                "install a root explorer that supports receiving intents for opening folders\neg:Es File Explorer",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    //by Ganesh varma
    fun findInPath(cmd: String): Boolean {
        val pathToTest = Objects.requireNonNull(System.getenv("PATH")).split(":".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()
        for (path in pathToTest) {
            val cmdFile = File(path, cmd)
            if (cmdFile.exists()) {
                Log.d("shell", "Found " + cmd + " at " + cmdFile.absolutePath)

                return true
            }
        }

        return false
    }

    fun checkAppInstalledByName(pm: PackageManager, packageName: String?): Boolean {
        if (packageName == null || "" == packageName)
            return false
        return try {
            pm.getApplicationInfo(
                packageName, PackageManager.GET_UNINSTALLED_PACKAGES
            )
            Log.d(MainActivity.TAG, "checkAppInstalledByName: $packageName : found")
            true
        } catch (e: Exception) {
            Log.d(MainActivity.TAG, "checkAppInstalledByName:$packageName not found")

            false
        }

    }

    fun checkAppInstalledByName(pm: PackageManager, packageName: String?, VERSION: Int): Boolean {
        if (packageName == null || "" == packageName)
            return false
        return try {
            pm.getApplicationInfo(
                packageName, PackageManager.GET_UNINSTALLED_PACKAGES
            )
            Log.d(MainActivity.TAG, "checkAppInstalledByName: $packageName : found")
            val pInfo = pm.getPackageInfo(packageName, 0)
            pInfo.versionCode == VERSION
        } catch (e: Exception) {
            Log.d(MainActivity.TAG, "checkAppInstalledByName:$packageName not found")

            false
        }

    }

    fun getString(input: List<String>): String {
        var str: StringBuilder? = null
        for (tmp in input) {
            if (str != null) {
                str.append("\n").append(tmp)
            } else
                str = StringBuilder(tmp)
        }
        assert(str != null)
        return str!!.toString()
    }

    fun getStringFromInputStream(ins: InputStream): String {

        return try {
            TextUtils.join("\n", BufferedReader(InputStreamReader(ins)).use{r->r.readLines()})
        } catch (e: IOException) {
            e.toString()
        }
    }

    fun copyAsset(assetManager: AssetManager, filename: String, Out: String) {
        var `in`: InputStream?
        val out: OutputStream
        val outDir = File(Out)
        try {
            `in` = assetManager.open(filename)

            createDir(outDir)
            val outFile = File(outDir, filename)
            out = FileOutputStream(outFile)
            copyFile(`in`!!, out)
            `in`.close()
            `in` = null
            out.flush()
            out.close()
            if (filename.endsWith(".zip")) {
                unpackZip(outFile)
                outFile.delete()
            }
            outFile.setExecutable(true, false)
        } catch (e: IOException) {
            Log.e("tag", "Failed to copy asset file: $filename", e)
        }

    }

    fun copyAssets(assetManager: AssetManager, outDir: File) {
        var files: Array<String>? = null
        try {
            files = assetManager.list("")
        } catch (e: IOException) {
            Log.e("tag", "Failed to get asset file list.", e)
        }

        assert(files != null)
        for (filename in files!!) {
            copyAsset(assetManager, filename, outDir.absolutePath)
        }
    }

    @Throws(IOException::class)
    private fun copyFile(ins: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        read = ins.read(buffer)
        while (read != -1) {
            out.write(buffer, 0, read)
            read = ins.read(buffer)
        }
    }

    @Throws(IOException::class)
    private fun createDir(dir: File) {
        if (dir.exists()) {
            if (!dir.isDirectory) {
                throw IOException("Can't create directory, a file is in the way")
            }
        } else {
            dir.mkdirs()
            if (!dir.isDirectory) {
                throw IOException("Unable to create directory")
            }
        }
    }

    fun unpackXZ(xzFile: File, keepOriginal: Boolean) {
        try {
            val fin = FileInputStream(xzFile)
            val `in` = BufferedInputStream(fin)
            val outFile = File(xzFile.parent, xzFile.name.replace(".xz", ""))
            val out = FileOutputStream(outFile)
            val xzIn = XZInputStream(`in`)
            val buffer = ByteArray(8192)
            var n = xzIn.read(buffer)
            while (-1 != n) {
                out.write(buffer, 0, n)
                n = xzIn.read(buffer)
            }
            out.close()
            xzIn.close()
            if (outFile.exists() && !keepOriginal) xzFile.delete()
        } catch (e: Exception) {
            Log.e("Decompress", "unzip", e)
        }

    }

    @SuppressLint("SetWorldReadable")
    fun unpackZip(zipFile: File): Boolean {
        File(zipFile.parent + "/" + zipFile.name.split(".zip".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])
        val zipPath: File = File(zipFile.parent + "/" + "common")
        try {
            createDir(zipPath)
        } catch (ex: IOException) {
            return false
        }

        val `is`: InputStream
        val zis: ZipInputStream
        try {
            var filename: String
            `is` = FileInputStream(zipFile)
            zis = ZipInputStream(BufferedInputStream(`is`))
            var ze: ZipEntry
            val buffer = ByteArray(1024)
            var count: Int
            ze = zis.nextEntry
            while (ze != null) {
                filename = ze.name

                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory) {
                    val fmd = File("$zipPath/$filename")
                    fmd.mkdirs()
                    continue
                }

                val fout = FileOutputStream("$zipPath/$filename")
                val f = File("$zipPath/$filename")
                f.setExecutable(true, false)
                f.setReadable(true, false)
                f.setWritable(true, true)
                count = zis.read(buffer)
                while (count!= -1) {
                    fout.write(buffer, 0, count)
                    count = zis.read(buffer)
                }

                fout.close()
                zis.closeEntry()
                ze = zis.nextEntry
            }

            zis.close()

        } catch (e: IOException) {
            return false
        }

        return true
    }
}



