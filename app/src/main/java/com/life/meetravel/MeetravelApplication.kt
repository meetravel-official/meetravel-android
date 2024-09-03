package com.life.meetravel

import android.app.Application
import com.google.firebase.FirebaseApp

class MeetravelApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}