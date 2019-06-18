package com.cry.opengldemo5.render

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent

interface DealTouchEvent {
    fun onTouchEvent(e: MotionEvent)
}

class FluidSimulatorSurfaceView(context: Context): GLSurfaceView(context) {
    var delegate: DealTouchEvent? = null
    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_MOVE,
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_UP -> {
                queueEvent {
                    delegate!!.onTouchEvent(e)
                }
            }
        }

        return true
    }
}