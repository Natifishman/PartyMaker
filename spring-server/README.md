# PartyMaker Spring Server

שרת Spring Boot עבור אפליקציית PartyMaker, המספק שירותי צד שרת לאפליקציית האנדרואיד.

## דרישות מקדימות

* Java 11 או גרסה חדשה יותר
* Maven 3.6.0 או גרסה חדשה יותר
* חשבון Firebase עם פרויקט מוגדר
* קובץ מפתח שירות של Firebase (`serviceAccountKey.json`)

## התקנה

1. שכפל את המאגר:
   ```bash
   git clone https://github.com/your-username/partymaker.git
   cd partymaker/spring-server
   ```

2. הצב את קובץ מפתח השירות של Firebase בנתיב:
   ```
   src/main/resources/serviceAccountKey.json
   ```

3. בנה את הפרויקט באמצעות Maven:
   ```bash
   mvn clean install
   ```

## הרצה

הרץ את השרת באמצעות הפקודה:

```bash
mvn spring-boot:run
```

השרת יפעל בכתובת `http://localhost:8080` כברירת מחדל.

## נקודות קצה של ה-API

### אימות

* `POST /api/auth/signin` - התחברות משתמש

### מסד נתונים

* `POST /api/database/set` - הגדרת ערך בנתיב מסוים
* `POST /api/database/update/{path}` - עדכון ילדים בנתיב מסוים
* `DELETE /api/database/remove/{path}` - הסרת ערך מנתיב מסוים
* `GET /api/database/health` - בדיקת בריאות שירות מסד הנתונים

### אחסון

* `POST /api/storage/upload` - העלאת קובץ
* `POST /api/storage/url` - קבלת URL להורדה
* `POST /api/storage/exists` - בדיקה אם קובץ קיים
* `GET /api/storage/health` - בדיקת בריאות שירות האחסון

### כללי

* `GET /` - נקודת כניסה ראשית
* `GET /health` - בדיקת בריאות כללית של השרת

## תיעוד API

תיעוד Swagger זמין בכתובת `http://localhost:8080/swagger-ui.html` לאחר הפעלת השרת.

## ניטור

השרת מספק נקודות קצה של Actuator לניטור:

* `GET /actuator/health` - מידע על בריאות השרת
* `GET /actuator/prometheus` - מדדי Prometheus לניטור

## רישיון

פרויקט זה מופץ תחת רישיון MIT. ראה קובץ `LICENSE` לפרטים נוספים. 