package com.android.calculator.services

import android.content.Intent
import android.service.quicksettings.TileService
import com.android.calculator.activities.MainActivity

class MyTileService : TileService() {

    override fun onClick() {
        super.onClick()
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        startActivityAndCollapse(intent)
    }
}
