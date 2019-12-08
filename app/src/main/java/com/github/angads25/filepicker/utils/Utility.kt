/*
 * Copyright (C) 2016 Angad Singh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.angads25.filepicker.utils

import android.content.Context
import android.content.pm.PackageManager

import com.github.angads25.filepicker.model.FileListItem

import java.io.File
import java.util.ArrayList
import java.util.Collections

/**
 *
 *
 * Created by Angad Singh on 11-07-2016.
 *
 */
class Utility {

    /**
     * Method checks whether the Support Library has been imported by application
     * or not.
     *
     * @return A boolean notifying value wheter support library is imported as a
     * dependency or not.
     */
    private fun hasSupportLibraryInClasspath(): Boolean {
        try {
            Class.forName("com.android.support:appcompat-v7")
            return true
        } catch (ex: ClassNotFoundException) {
            ex.printStackTrace()
        }

        return false
    }

    companion object {
        /**
         * Post Lollipop Devices require permissions on Runtime (Risky Ones), even though it has been
         * specified in the uses-permission tag of manifest. checkStorageAccessPermissions
         * method checks whether the READ EXTERNAL STORAGE permission has been granted to
         * the Application.
         * @return a boolean value notifying whether the permission is granted or not.
         */
        fun checkStorageAccessPermissions(context: Context): Boolean {   //Only for Android M and above.
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                val permission = "android.permission.READ_EXTERNAL_STORAGE"
                val res = context.checkCallingOrSelfPermission(permission)
                return res == PackageManager.PERMISSION_GRANTED
            } else {   //Pre Marshmallow can rely on Manifest defined permissions.
                return true
            }
        }

        /**
         * Prepares the list of Files and Folders inside 'inter' Directory.
         * The list can be filtered through extensions. 'filter' reference
         * is the FileFilter. A reference of ArrayList is passed, in case it
         * may contain the ListItem for parent directory. Returns the List of
         * Directories/files in the form of ArrayList.
         * @param internalList ArrayList containing parent directory.
         *
         * @param inter The present directory to look into.
         *
         * @param filter Extension filter class reference, for filtering files.
         *
         * @return ArrayList of FileListItem containing file info of current directory.
         */
        fun prepareFileListEntries(
            internalList: ArrayList<FileListItem>,
            inter: File,
            filter: ExtensionFilter
        ): ArrayList<FileListItem> {
            var internalList = internalList
            try {
                //Check for each and every directory/file in 'inter' directory.
                //Filter by extension using 'filter' reference.

                for (name in inter.listFiles(filter)!!) {
                    //If file/directory can be read by the Application
                    if (name.canRead()) {
                        //Create a row item for the directory list and define properties.
                        val item = FileListItem()
                        item.filename = name.name
                        item.isDirectory = name.isDirectory
                        item.location = name.absolutePath
                        item.time = name.lastModified()
                        //Add row to the List of directories/files
                        internalList.add(item)
                    }
                }
                //Sort the files and directories in alphabetical order.
                //See compareTo method in FileListItem class.
                Collections.sort(internalList)
            } catch (e: NullPointerException) {   //Just dont worry, it rarely occurs.
                e.printStackTrace()
                internalList = ArrayList()
            }

            return internalList
        }
    }
}
