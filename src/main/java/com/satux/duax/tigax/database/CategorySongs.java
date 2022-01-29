package com.satux.duax.tigax.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import com.satux.duax.tigax.models.SongModel;

import java.util.ArrayList;

public class CategorySongs {

    @NonNull
    String TEXT_TYPE = " TEXT";
    @NonNull
    String COMMA_SEP = ",";
    @NonNull
    String TABLE_NAME = "category";
    @NonNull
    String COLUMN_NAME_ID = "id";
    @NonNull
    String CATEGORY = "category";
    @NonNull
    String VOTES = "rank";
    @NonNull
    String TITLE = "title";
    @NonNull
    String PATH = "path";
    @NonNull
    String ARTIST = "artist";
    @NonNull
    String ALBUM = "album";
    @NonNull
    String NAME = "name";
    @NonNull
    String ALBUMID = "albumid";
    @NonNull
    String FAKEPATH = "fakepath";
    @NonNull
    String DURATION = "duration";
    @NonNull
    String[] ALL_KEYS = new String[]
            {COLUMN_NAME_ID, CATEGORY, VOTES, TITLE, PATH, ARTIST, ALBUM, NAME, DURATION, ALBUMID, FAKEPATH};
    @NonNull
    String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
                    CATEGORY + TEXT_TYPE + COMMA_SEP +
                    VOTES + " INTEGER " + COMMA_SEP +
                    TITLE + TEXT_TYPE + COMMA_SEP +
                    PATH + TEXT_TYPE + COMMA_SEP +
                    ARTIST + TEXT_TYPE + COMMA_SEP +
                    ALBUM + TEXT_TYPE + COMMA_SEP +
                    NAME + TEXT_TYPE + COMMA_SEP +
                    DURATION + TEXT_TYPE + COMMA_SEP +
                    ALBUMID + TEXT_TYPE + COMMA_SEP +
                    FAKEPATH + TEXT_TYPE +
                    ");";
    @NonNull
    String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
    SQLiteDatabase db;
    ReaderDB myDBHelper;
    Context context;

    /*
     * Public Methods of Database
     * 1. open()
     * 2. close()
     * 3. getRows()
     * 4. addRow()
     */

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public CategorySongs(Context ctx) {
        this.context = ctx;
        myDBHelper = new ReaderDB(context);
    }

    // Open the database connection.
    @NonNull
    public CategorySongs open() {
        db = myDBHelper.getWritableDatabase();
        return this;
    }

    // Close the database connection.
    @NonNull
    public CategorySongs close() {
        myDBHelper.close();
        return this;
    }

    @NonNull
    private String dropInvalidString(String string) {
        return string.replaceAll("[^A-Za-z0-9()\\[\\]]", "");
    }

    /*
     * Add a row to the Database
     */
    public long addRow(long playlistID, SongModel hashRow) {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(TITLE, hashRow.getTitle());
        values.put(CATEGORY, Long.toString(playlistID));
        values.put(VOTES, "0");
        values.put(PATH, hashRow.getPath());
        values.put(ARTIST, hashRow.getArtist());
        values.put(ALBUM, hashRow.getAlbum());
        values.put(NAME, hashRow.getFileName());
        values.put(FAKEPATH, dropInvalidString(hashRow.getPath()));
        values.put(DURATION, hashRow.getDuration());
        values.put(ALBUMID, hashRow.getAlbumID());
        // Insert the new row, returning the primary key value of the new row
        return db.insert(TABLE_NAME, "NULL", values);
    }

    /*
     * Returns ArrayList Hashmap of all Rows
     */
    @NonNull
    public ArrayList<SongModel> getAllRows(int playlistID) {
        String where = CATEGORY + "=" + playlistID;
        Cursor c = db.query(TABLE_NAME, ALL_KEYS, where, null, null, null, VOTES + " DESC");

        ArrayList<SongModel> profilesArray = new ArrayList<>();
        if (c.moveToFirst()) {
            do {
                SongModel song = new SongModel();
                song.setTitle(c.getString(3));
                song.setPath(c.getString(4));
                song.setArtist(c.getString(5));
                song.setAlbum(c.getString(6));
                song.setFileName(c.getString(7));
                song.setDuration(c.getString(8));
                song.setAlbumID(c.getString(9));
                profilesArray.add(song);
            } while (c.moveToNext());
        }
        c.close();

        return profilesArray;
    }

    public boolean deleteRow(long rowId, long category) {
        String where = COLUMN_NAME_ID + "=" + rowId + " AND " + CATEGORY + "=" + category;
        return db.delete(TABLE_NAME, where, null) != 0;
    }

    public boolean deleteRowByPath(String path) {
        String where = PATH + "=" + path;
        try {
            return db.delete(TABLE_NAME, where, null) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    public int getCount(long playlist) {
        String countQuery = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + CATEGORY + "=" + playlist;
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

    /*
     * Delete All Rows
     */
    public boolean deleteAll(int PlaylistID) {
        try {
            Cursor c = getAllRowsCursor();
            long rowId = c.getColumnIndexOrThrow(COLUMN_NAME_ID);
            if (c.moveToFirst()) {
                do {
                    deleteRow(c.getLong((int) rowId), PlaylistID);
                } while (c.moveToNext());
            }
            c.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /*
     * Get All Rows Cursor
     */
    // Return all data in the database.
    public Cursor getAllRowsCursor() {
        String where = null;
        Cursor c = db.query(true, TABLE_NAME, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public boolean checkRow(String path) {
        String where = FAKEPATH + " = '" + dropInvalidString(path) + "';";
        Cursor c = db.query(TABLE_NAME, ALL_KEYS,
                where, null, null, null, null);
        if (c.getCount() > 0) {
            c.close();
            return true;
        } else {
            c.close();
            return false;
        }
    }

    /*
     * Update the row
     */
    // Change an existing row to be equal to new data.
    public boolean updateRow(String nameF) {
        String where = FAKEPATH + " = '" + dropInvalidString(nameF) + "';";

        Cursor c = db.query(TABLE_NAME, ALL_KEYS,
                where, null, null, null, null);
        int votes = 0;
        String title = null, path = null, artist = null, albumid = null, album = null, name = null, duration = null, playlistID = null;
        if (c != null) {
            c.moveToFirst();
            playlistID = c.getString(1);
            votes = c.getInt(2) + 1;
            title = c.getString(3);
            path = c.getString(4);
            artist = c.getString(5);
            album = c.getString(6);
            name = c.getString(7);
            duration = c.getString(8);
            albumid = c.getString(9);
        }
        if (c != null) {
            c.close();
        }
        /*
         * CHANGE 4:
         */
        // TODO: Update data in the row with new fields.
        // TODO: Also change the function's arguments to be what you need!
        // Create row's data:

        ContentValues newValues = new ContentValues();
        newValues.put(CATEGORY, playlistID);
        newValues.put(VOTES, votes);
        newValues.put("title", title);
        newValues.put("path", path);
        newValues.put("artist", artist);
        newValues.put("album", album);
        newValues.put("name", name);
        newValues.put("duration", duration);
        newValues.put("albumid", albumid);
        newValues.put(FAKEPATH, dropInvalidString(nameF));
        // Insert it into the database.
        return db.update(TABLE_NAME, newValues, where, null) != 0;
    }

    public class ReaderDB extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        static final int DATABASE_VERSION = 1;
        static final String DATABASE_NAME = "categories.db";

        ReaderDB(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }
}
