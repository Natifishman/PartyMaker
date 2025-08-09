# הוראות ל-Agent לתיקון אזהרות

## סדר ביצוע מומלץ

### Phase 1: תיקונים בטוחים ופשוטים
1. **עדכון Dependencies** (קובץ 09)
   - עדכן Firebase BOM ל-34.1.0
   - פשוט ובטוח

2. **מחיקת Unused Resources** (קובץ 02)
   - מחק layouts, drawables, colors, styles שלא בשימוש
   - זהירות: חפש בקוד לפני מחיקה

3. **תיקון Icon Densities** (קובץ 08)
   - הוסף תיקיות חסרות
   - תקן monochrome icons
   - פתר conflicts בין XML ו-bitmap

### Phase 2: תיקוני קוד בינוניים
1. **ניקוי קוד** (קובץ 07)
   - הסר imports מיותרים
   - מחק מתודות ריקות
   - הסר משתנים ופרמטרים לא בשימוש

2. **תיקון Nullability** (קובץ 06)
   - הוסף @NonNull/@Nullable annotations
   - תקן NullPointerException אפשריים
   - הסר בדיקות null מיותרות

3. **תיקון SDK ו-Deprecated** (קובץ 05)
   - תקן locale issues
   - עדכן deprecated methods
   - הסר SDK checks מיותרים

### Phase 3: תיקונים מורכבים
1. **תיקון Performance** (קובץ 03)
   - תקן RecyclerView.notifyDataSetChanged()
   - פתור Overdraw issues
   - אופטימיזציה של layouts

2. **תיקון Hardcoded Strings** (קובץ 01)
   - העבר strings ל-strings.xml
   - תהליך ארוך אבל פשוט

### Phase 4: תיקונים רגישים
1. **תיקון Security** (קובץ 04)
   - החלף custom TrustManager
   - עדכן permissions ל-Android 14
   - דורש בדיקה מעמיקה

2. **תיקון Accessibility** (קובץ 10)
   - Override performClick
   - הוסף content descriptions
   - דורש בדיקת UX

## הוראות חשובות ל-Agent:

### כללי:
1. **תמיד** הרץ `./gradlew spotlessApply` אחרי שינויים
2. **תמיד** בדוק compilation: `./gradlew assembleDebug`
3. **אל תמחק** קבצים בלי לחפש references
4. **אל תשנה** business logic - רק תקן warnings

### לכל קובץ:
1. קרא את קובץ ההוראות המתאים
2. בצע את התיקונים לפי הסדר
3. בדוק שלא נוצרו warnings חדשים
4. עדכן את ה-todo list

### בדיקות אחרי כל phase:
```bash
# בדיקת lint
./gradlew lint

# בדיקת build
./gradlew assembleDebug

# בדיקת formatting
./gradlew spotlessCheck
```

### דגשים מיוחדים:
1. **Hardcoded Strings**: יש הרבה, קח זמן
2. **Unused Resources**: בדוק לפני מחיקה
3. **Security**: היזהר מאוד, בדוק השפעות
4. **Performance**: בדוק שהאפליקציה עדיין עובדת

## מבנה העבודה:
```
1. קרא הוראות
2. חפש את הקבצים הרלוונטיים
3. בצע תיקון
4. בדוק תוצאה
5. עדכן todo list
6. המשך לתיקון הבא
```

## סימני הצלחה:
- מספר האזהרות יורד
- Build מצליח
- אפליקציה עובדת
- Lint reports נקיים יותר

## במקרה של בעיה:
1. חזור למצב קודם (git)
2. נסה גישה אחרת
3. דלג לתיקון הבא
4. תעד את הבעיה