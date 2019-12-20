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

import com.github.angads25.filepicker.model.DialogConfigs
import com.github.angads25.filepicker.model.DialogProperties

import java.io.File
import java.io.FileFilter
import java.util.Locale

/**
 *
 *
 * Created by Angad Singh on 11-07-2016.
 *
 */

/*  Class to filter the list of files.
 */
class ExtensionFilter(private val properties: DialogProperties) : FileFilter {
    private val validExtensions: Array<String>

    init {
        if (properties.extensions != null) {
            this.validExtensions = properties.extensions!!
        } else {
            this.validExtensions = arrayOf("")
        }
    }

    /**Function to filter files based on defined rules.
     */
    override fun accept(file: File): Boolean {
        //All directories are added in the least that can be read by the Application
        if (file.isDirectory && file.canRead()) {
            return true
        } else if (properties.selection_type == DialogConfigs.DIR_SELECT) {   /*  True for files, If the selection type is Directory type, ie.
             *  Only directory has to be selected from the list, then all files are
             *  ignored.
             */
            return false
        } else {   /*  Check whether name of the file ends with the extension. Added if it
             *  does.
             */
            val name = file.name.toLowerCase(Locale.getDefault())
            for (ext in validExtensions) {
                if (name.endsWith(ext)) {
                    return true
                }
            }
        }
        return false
    }
}
