package com.example.scratchpad

/**
 * Constants used throughout the Scratchpad application.
 * 
 * @see <a href="https://github.com/HackerHomestead/scratchpad">Project Documentation</a>
 */
object Constants {
    // Package name - used for intent actions and component identification
    const val PACKAGE_NAME = "com.example.scratchpad"
    
    // Intent action names for ADB commands
    object Actions {
        const val IMPORT = "$PACKAGE_NAME.IMPORT"
        const val EXPORT = "$PACKAGE_NAME.EXPORT"
        const val CLEAR = "$PACKAGE_NAME.CLEAR"
        const val LIST = "$PACKAGE_NAME.LIST"
        const val ABOUT = "$PACKAGE_NAME.ABOUT"
        const val IMPORT_ACTION = "$PACKAGE_NAME.IMPORT_ACTION"
    }
    
    // Intent extras
    object Extras {
        const val BASE64_DATA = "base64_data"
        const val FILE_PATH = "file_path"
    }
    
    // App metadata
    const val VERSION_NAME = "1.1"
    
    // Limits
    object Limits {
        const val MAX_TITLE_LENGTH = 43  // 8.3 format + some flexibility
        const val MAX_CONTENT_LENGTH = 1_000_000  // 1MB
    }
    
    // File names
    object Files {
        const val BACKUP_PREFIX = "scratchpad_backup_"
        const val BACKUP_EXTENSION = ".json"
    }
}
