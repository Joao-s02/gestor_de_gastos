package com.joao.gastos

import android.app.Application
import com.joao.gastos.di.AppContainer

class GastosApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
