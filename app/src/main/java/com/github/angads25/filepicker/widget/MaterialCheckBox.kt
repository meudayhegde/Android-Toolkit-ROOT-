/*
 * Copyright (C) 2017 Angad Singh
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

package com.github.angads25.filepicker.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.view.View

import com.uday.android.toolkit.R

/**
 *
 *
 * Created by Angad on 19-05-2017.
 *
 */

class MaterialCheckbox : View {
    private var minDim: Int = 0
    private var paint: Paint? = null
    private var bounds: RectF? = null
    private var checked: Boolean = false
    private var onCheckedChangeListener: OnCheckedChangeListener? = null
    private var tick: Path? = null

    var isChecked: Boolean
        get() = checked
        set(checked) {
            this.checked = checked
            invalidate()
        }

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(context)
    }

    fun initView(context: Context) {
        checked = false
        tick = Path()
        paint = Paint()
        bounds = RectF()
        val onClickListener = OnClickListener {
            isChecked = !checked
            onCheckedChangeListener!!.onCheckedChanged(this@MaterialCheckbox, isChecked)
        }

        setOnClickListener(onClickListener)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isChecked) {
            paint!!.reset()
            paint!!.isAntiAlias = true
            bounds!!.set(
                (minDim / 10).toFloat(),
                (minDim / 10).toFloat(),
                (minDim - minDim / 10).toFloat(),
                (minDim - minDim / 10).toFloat()
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                paint!!.color = resources.getColor(R.color.colorAccent, context!!.theme)
            } else {
                paint!!.color = resources.getColor(R.color.colorAccent)
            }
            canvas.drawRoundRect(bounds!!, (minDim / 8).toFloat(), (minDim / 8).toFloat(), paint!!)

            paint!!.color = Color.parseColor("#FFFFFF")
            paint!!.strokeWidth = (minDim / 10).toFloat()
            paint!!.style = Paint.Style.STROKE
            paint!!.strokeJoin = Paint.Join.BEVEL
            canvas.drawPath(tick!!, paint!!)
        } else {
            paint!!.reset()
            paint!!.isAntiAlias = true
            bounds!!.set(
                (minDim / 10).toFloat(),
                (minDim / 10).toFloat(),
                (minDim - minDim / 10).toFloat(),
                (minDim - minDim / 10).toFloat()
            )
            paint!!.color = Color.parseColor("#C1C1C1")
            canvas.drawRoundRect(bounds!!, (minDim / 8).toFloat(), (minDim / 8).toFloat(), paint!!)

            bounds!!.set(
                (minDim / 5).toFloat(),
                (minDim / 5).toFloat(),
                (minDim - minDim / 5).toFloat(),
                (minDim - minDim / 5).toFloat()
            )
            paint!!.color = Color.parseColor("#FFFFFF")
            canvas.drawRect(bounds!!, paint!!)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val height = measuredHeight
        val width = measuredWidth
        minDim = Math.min(width, height)
        bounds!!.set(
            (minDim / 10).toFloat(),
            (minDim / 10).toFloat(),
            (minDim - minDim / 10).toFloat(),
            (minDim - minDim / 10).toFloat()
        )
        tick!!.moveTo((minDim / 4).toFloat(), (minDim / 2).toFloat())
        tick!!.lineTo(minDim / 2.5f, (minDim - minDim / 3).toFloat())

        tick!!.moveTo(minDim / 2.75f, minDim - minDim / 3.25f)
        tick!!.lineTo((minDim - minDim / 4).toFloat(), (minDim / 3).toFloat())
        setMeasuredDimension(width, height)
    }

    fun setOnCheckedChangedListener(onCheckedChangeListener: OnCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener
    }
}
