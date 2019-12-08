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

package com.github.angads25.filepicker.model

import java.util.HashMap

/**
 *
 *
 * Created by Angad Singh on 11-07-2016.
 *
 */

/*  SingleTon containing <Key,Value> pair of all the selected files.
 *  Key: Directory/File path.
 *  Value: FileListItem Object.
 */
object MarkedItemList {
    private var ourInstance = HashMap<String, FileListItem>()

    val selectedPaths: Array<String>
        get() {
            return ourInstance.keys.toTypedArray()
        }

    val fileCount: Int
        get() = ourInstance.size

    fun addSelectedItem(item: FileListItem) {
        ourInstance[item.location] = item
    }

    fun removeSelectedItem(key: String) {
        ourInstance.remove(key)
    }

    fun hasItem(key: String): Boolean {
        return ourInstance.containsKey(key)
    }

    fun clearSelectionList() {
        ourInstance = HashMap()
    }

    fun addSingleFile(item: FileListItem) {
        ourInstance = HashMap()
        ourInstance[item.location] = item
    }
}

private operator fun <K, V> HashMap<K, V>.set(location: K?, value: V) {
    this[location]=value
}
