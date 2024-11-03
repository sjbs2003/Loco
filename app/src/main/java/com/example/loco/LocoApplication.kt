package com.example.loco

import android.app.Application
import com.example.loco.model.AppContainer
import com.example.loco.model.AppDataContainer

class LocoApplication: Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}