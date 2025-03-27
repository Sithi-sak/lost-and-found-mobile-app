package com.example.lostandfound.utils

import android.content.ContentResolver
import android.content.Context

object FirebaseStorageUtils {
    private var applicationContext: Context? = null
    
    fun init(context: Context) {
        applicationContext = context.applicationContext
    }
    
    fun getContentResolver(): ContentResolver? {
        return applicationContext?.contentResolver
    }
} 