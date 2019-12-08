package com.uday.android.util

class DrawerListData {
    var icRes: Int = 0
    var title: String? = null
    var header: String? = null

    constructor(title: String, icRes: Int) {
        this.icRes = icRes
        this.title = title
    }

    constructor(header: String) {
        this.header = header
    }
}