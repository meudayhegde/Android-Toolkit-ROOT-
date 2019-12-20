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

/**
 *
 *
 * Created by Angad Singh on 11-07-2016.
 *
 */

/*  Helper class for setting properties of Dialog.
 */
object DialogConfigs {
    /*  SELECTION_MODES*/

    /*  SINGLE_MODE specifies that a single File/Directory has to be selected
     *  from the list of Files/Directories. It is the default Selection Mode.
     */
    val SINGLE_MODE = 0

    /*  MULTI_MODE specifies that multiple Files/Directories has to be selected
     *  from the list of Files/Directories.
     */
    val MULTI_MODE = 1

    /*  SELECTION_TYPES*/

    /*  FILE_SELECT specifies that from list of Files/Directories a File has to
     *  be selected. It is the default Selection Type.
     */
    val FILE_SELECT = 0

    /*  DIR_SELECT specifies that from list of Files/Directories a Directory has to
     *  be selected.
     */
    val DIR_SELECT = 1

    /*  FILE_AND_DIR_SELECT specifies that from list of Files/Directories both
     *  can be selected.
     */
    val FILE_AND_DIR_SELECT = 2

    /*  PARENT_DIRECTORY*/
    val DIRECTORY_SEPERATOR = "/"
    val STORAGE_DIR = "mnt"

    /*  DEFAULT_DIR is the default mount point of the SDCARD. It is the default
     *  mount point.
     */
    val DEFAULT_DIR = DIRECTORY_SEPERATOR + STORAGE_DIR
}
