/*
 * Copyright (C) 2014 Chris Renke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.uday.android.toolkit.ui

import android.content.res.Resources
import android.graphics.*
import android.graphics.Color.BLACK
import android.graphics.Paint.*
import android.graphics.Paint.Cap.BUTT
import android.graphics.Paint.Cap.ROUND
import android.graphics.Paint.Style.STROKE
import android.graphics.PixelFormat.TRANSLUCENT
import android.graphics.drawable.Drawable
import java.lang.Math.sqrt

/** A drawable that rotates between a drawer icon and a back arrow based on parameter.  */
class DrawerArrowDrawable @JvmOverloads constructor(
    resources: Resources,
    private val rounded: Boolean = false
) : Drawable() {

    private val topLine: BridgingLine
    private val middleLine: BridgingLine
    private val bottomLine: BridgingLine

    //private var bounds: Rect
    private val halfStrokeWidthPixel: Float
    private val linePaint: Paint

    private var flip: Boolean = false
    private var parameter: Float = 0.toFloat()

    // Helper fields during drawing calculations.
    private var vX: Float = 0.toFloat()
    private var vY: Float = 0.toFloat()
    private var magnitude: Float = 0.toFloat()
    private var paramA: Float = 0.toFloat()
    private var paramB: Float = 0.toFloat()
    private val coordsA = floatArrayOf(0f, 0f)
    private val coordsB = floatArrayOf(0f, 0f)

    /**
     * Joins two [Path]s as if they were one where the first 50% of the path is `PathFirst` and the second 50% of the path is `pathSecond`.
     */
    private class JoinedPath(pathFirst: Path, pathSecond: Path) {

        private val measureFirst: PathMeasure
        private val measureSecond: PathMeasure
        private val lengthFirst: Float
        private val lengthSecond: Float

        init {
            measureFirst = PathMeasure(pathFirst, false)
            measureSecond = PathMeasure(pathSecond, false)
            lengthFirst = measureFirst.length
            lengthSecond = measureSecond.length
        }

        /**
         * Returns a point on this curve at the given `parameter`.
         * For `parameter` values less than .5f, the first path will drive the point.
         * For `parameter` values greater than .5f, the second path will drive the point.
         * For `parameter` equal to .5f, the point will be the point where the two
         * internal paths connect.
         */
        fun getPointOnLine(parameter: Float, coords: FloatArray) {
            var parameter = parameter
            if (parameter <= .5f) {
                parameter *= 2f
                measureFirst.getPosTan(lengthFirst * parameter, coords, null)
            } else {
                parameter -= .5f
                parameter *= 2f
                measureSecond.getPosTan(lengthSecond * parameter, coords, null)
            }
        }
    }

    /** Draws a line between two [JoinedPath]s at distance `parameter` along each path.  */
    private inner class BridgingLine(
        private val pathA: JoinedPath,
        private val pathB: JoinedPath
    ) {

        /**
         * Draw a line between the points defined on the paths backing `measureA` and
         * `measureB` at the current parameter.
         */
        fun draw(canvas: Canvas) {
            pathA.getPointOnLine(parameter, coordsA)
            pathB.getPointOnLine(parameter, coordsB)
            if (rounded) insetPointsForRoundCaps()
            canvas.drawLine(coordsA[0], coordsA[1], coordsB[0], coordsB[1], linePaint)
        }

        /**
         * Insets the end points of the current line to account for the protruding
         * ends drawn for [Cap.ROUND] style lines.
         */
        private fun insetPointsForRoundCaps() {
            vX = coordsB[0] - coordsA[0]
            vY = coordsB[1] - coordsA[1]

            magnitude = sqrt((vX * vX + vY * vY).toDouble()).toFloat()
            paramA = (magnitude - halfStrokeWidthPixel) / magnitude
            paramB = halfStrokeWidthPixel / magnitude

            coordsA[0] = coordsB[0] - vX * paramA
            coordsA[1] = coordsB[1] - vY * paramA
            coordsB[0] = coordsB[0] - vX * paramB
            coordsB[1] = coordsB[1] - vY * paramB
        }
    }

    init {
        val density = resources.displayMetrics.density
        val strokeWidthPixel = STROKE_WIDTH_DP * density
        halfStrokeWidthPixel = strokeWidthPixel / 2

        linePaint = Paint(SUBPIXEL_TEXT_FLAG or ANTI_ALIAS_FLAG)
        linePaint.strokeCap = if (rounded) ROUND else BUTT
        linePaint.color = BLACK
        linePaint.style = STROKE
        linePaint.strokeWidth = strokeWidthPixel

        val dimen = (DIMEN_DP * density).toInt()
        bounds = Rect(0, 0, dimen, dimen)

        var first: Path
        var second: Path
        var joinedA: JoinedPath
        var joinedB: JoinedPath

        // Top
        first = Path()
        first.moveTo(5.042f, 20f)
        first.rCubicTo(8.125f, -16.317f, 39.753f, -27.851f, 55.49f, -2.765f)
        second = Path()
        second.moveTo(60.531f, 17.235f)
        second.rCubicTo(11.301f, 18.015f, -3.699f, 46.083f, -23.725f, 43.456f)
        scalePath(first, density)
        scalePath(second, density)
        joinedA = JoinedPath(first, second)

        first = Path()
        first.moveTo(64.959f, 20f)
        first.rCubicTo(4.457f, 16.75f, 1.512f, 37.982f, -22.557f, 42.699f)
        second = Path()
        second.moveTo(42.402f, 62.699f)
        second.cubicTo(18.333f, 67.418f, 8.807f, 45.646f, 8.807f, 32.823f)
        scalePath(first, density)
        scalePath(second, density)
        joinedB = JoinedPath(first, second)
        topLine = BridgingLine(joinedA, joinedB)

        // Middle
        first = Path()
        first.moveTo(5.042f, 35f)
        first.cubicTo(5.042f, 20.333f, 18.625f, 6.791f, 35f, 6.791f)
        second = Path()
        second.moveTo(35f, 6.791f)
        second.rCubicTo(16.083f, 0f, 26.853f, 16.702f, 26.853f, 28.209f)
        scalePath(first, density)
        scalePath(second, density)
        joinedA = JoinedPath(first, second)

        first = Path()
        first.moveTo(64.959f, 35f)
        first.rCubicTo(0f, 10.926f, -8.709f, 26.416f, -29.958f, 26.416f)
        second = Path()
        second.moveTo(35f, 61.416f)
        second.rCubicTo(-7.5f, 0f, -23.946f, -8.211f, -23.946f, -26.416f)
        scalePath(first, density)
        scalePath(second, density)
        joinedB = JoinedPath(first, second)
        middleLine = BridgingLine(joinedA, joinedB)

        // Bottom
        first = Path()
        first.moveTo(5.042f, 50f)
        first.cubicTo(2.5f, 43.312f, 0.013f, 26.546f, 9.475f, 17.346f)
        second = Path()
        second.moveTo(9.475f, 17.346f)
        second.rCubicTo(9.462f, -9.2f, 24.188f, -10.353f, 27.326f, -8.245f)
        scalePath(first, density)
        scalePath(second, density)
        joinedA = JoinedPath(first, second)

        first = Path()
        first.moveTo(64.959f, 50f)
        first.rCubicTo(-7.021f, 10.08f, -20.584f, 19.699f, -37.361f, 12.74f)
        second = Path()
        second.moveTo(27.598f, 62.699f)
        second.rCubicTo(-15.723f, -6.521f, -18.8f, -23.543f, -18.8f, -25.642f)
        scalePath(first, density)
        scalePath(second, density)
        joinedB = JoinedPath(first, second)
        bottomLine = BridgingLine(joinedA, joinedB)
    }

    override fun getIntrinsicHeight(): Int {
        return bounds.height()
    }

    override fun getIntrinsicWidth(): Int {
        return bounds.width()
    }

    override fun draw(canvas: Canvas) {
        if (flip) {
            canvas.save()
            canvas.scale(1f, -1f, (intrinsicWidth / 2).toFloat(), (intrinsicHeight / 2).toFloat())
        }

        topLine.draw(canvas)
        middleLine.draw(canvas)
        bottomLine.draw(canvas)

        if (flip) canvas.restore()
    }

    override fun setAlpha(alpha: Int) {
        linePaint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(cf: ColorFilter?) {
        linePaint.colorFilter = cf
        invalidateSelf()
    }

    override fun getOpacity(): Int {
        return TRANSLUCENT
    }

    fun setStrokeColor(color: Int) {
        linePaint.color = color
        invalidateSelf()
    }

    /**
     * Sets the rotation of this drawable based on `parameter` between 0 and 1. Usually driven
     * via 's `slideOffset` parameter.
     */
    fun setParameter(parameter: Float) {
        if (parameter > 1 || parameter < 0) {
            throw IllegalArgumentException("Value must be between 1 and zero inclusive!")
        }
        this.parameter = parameter
        invalidateSelf()
    }

    /**
     * When false, rotates from 3 o'clock to 9 o'clock between a drawer icon and a back arrow.
     * When true, rotates from 9 o'clock to 3 o'clock between a back arrow and a drawer icon.
     */
    fun setFlip(flip: Boolean) {
        this.flip = flip
        invalidateSelf()
    }

    companion object {

        /** Paths were generated at a 3px/dp density; this is the scale factor for different densities.  */
        private val PATH_GEN_DENSITY = 3f

        /** Paths were generated with at this size for [DrawerArrowDrawable.PATH_GEN_DENSITY].  */
        private val DIMEN_DP = 23.5f

        /**
         * Paths were generated targeting this stroke width to form the arrowhead properly, modification
         * may cause the arrow to not for nicely.
         */
        private val STROKE_WIDTH_DP = 2f

        /**
         * Scales the paths to the given screen density. If the density matches the
         * [DrawerArrowDrawable.PATH_GEN_DENSITY], no scaling needs to be done.
         */
        private fun scalePath(path: Path, density: Float) {
            if (density == PATH_GEN_DENSITY) return
            val scaleMatrix = Matrix()
            scaleMatrix.setScale(density / PATH_GEN_DENSITY, density / PATH_GEN_DENSITY, 0f, 0f)
            path.transform(scaleMatrix)
        }
    }
}
