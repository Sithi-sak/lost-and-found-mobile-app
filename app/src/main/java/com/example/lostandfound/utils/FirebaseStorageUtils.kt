package com.example.lostandfound.utils

// Import necessary Android components for content resolver access
import android.content.ContentResolver
import android.content.Context

/**
 * Utility object for managing Firebase Storage related functionality.
 * Provides access to the application's ContentResolver for file operations.
 */
object FirebaseStorageUtils {
    // Store application context for content resolver access
    private var applicationContext: Context? = null
    
    /**
     * Initialize the utility with application context.
     * This should be called during app initialization.
     *
     * context The application context
     */
    fun init(context: Context) {
        applicationContext = context.applicationContext
    }
    
    /**
     * Get the application's ContentResolver for file operations.
     * Returns null if the utility hasn't been initialized.
     *
     * ContentResolver instance or null if not initialized
     */
    fun getContentResolver(): ContentResolver? {
        return applicationContext?.contentResolver
    }
} 