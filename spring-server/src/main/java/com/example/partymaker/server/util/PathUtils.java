package com.example.partymaker.server.util;

import com.google.cloud.storage.BlobId;
import com.google.firebase.database.DatabaseReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * מחלקת עזר לטיפול בנתיבים
 */
public class PathUtils {
    private static final Logger logger = LoggerFactory.getLogger(PathUtils.class);
    
    /**
     * המרת הפניה לדאטאבייס לנתיב טקסטואלי
     * @param reference ההפניה לדאטאבייס
     * @return הנתיב כמחרוזת
     */
    public static String getPathFromReference(DatabaseReference reference) {
        String path = reference.toString();
        String databaseUrl = reference.getRoot().toString();
        
        // לוג מפורט לעזרה בניפוי שגיאות
        logger.debug("Converting reference to path: {}", reference.toString());
        logger.debug("Database URL: {}", databaseUrl);
        
        if (path.startsWith(databaseUrl)) {
            path = path.substring(databaseUrl.length());
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
        }
        
        logger.debug("Extracted path: {}", path);
        return path;
    }
    
    /**
     * המרת BlobId לנתיב טקסטואלי
     * @param blobId ה-BlobId של הקובץ באחסון
     * @return הנתיב כמחרוזת
     */
    public static String getPathFromBlobId(BlobId blobId) {
        String path = blobId.getName();
        
        // לוג מפורט לעזרה בניפוי שגיאות
        logger.debug("Converting blob to path: {}", blobId.toString());
        logger.debug("Storage path: {}", path);
        
        // הסרת ה-/ מתחילת הנתיב אם קיים
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        
        logger.debug("Final storage path: {}", path);
        return path;
    }
    
    /**
     * תיקון נתיב דאטאבייס
     * @param path הנתיב המקורי
     * @return הנתיב המתוקן
     */
    public static String correctDatabasePath(String path) {
        // וידוא שהנתיבים מתחילים באותיות גדולות כמו בשרת
        if (path.startsWith("groups/")) {
            String newPath = "Groups" + path.substring(6);
            logger.debug("Path corrected from groups/ to Groups/: {}", newPath);
            path = newPath;
        } else if (path.startsWith("users/")) {
            String newPath = "Users" + path.substring(5);
            logger.debug("Path corrected from users/ to Users/: {}", newPath);
            path = newPath;
        } else if (path.startsWith("messages/")) {
            String newPath = "Messages" + path.substring(8);
            logger.debug("Path corrected from messages/ to Messages/: {}", newPath);
            path = newPath;
        } else if (path.toLowerCase().startsWith("groupsmessages/")) {
            String newPath = "GroupsMessages" + path.substring(14);
            logger.debug("Path corrected from groupsmessages/ to GroupsMessages/: {}", newPath);
            path = newPath;
        }
        
        // בדיקה אם יש תווים לא חוקיים בנתיב
        if (path.contains("#") || path.contains("$") || path.contains("[") || path.contains("]")) {
            logger.warn("Path contains invalid characters: {}", path);
        }
        
        logger.debug("Final path: {}", path);
        return path;
    }
} 