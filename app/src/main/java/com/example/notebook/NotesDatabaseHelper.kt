package com.example.notebook

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

class NotesDatabaseHelper(val context: Context, name: String, version: Int):SQLiteOpenHelper(context, name, null, version) {

    private val createNotes = "create table Notes (" +
            " id integer primary key autoincrement," +
            "uuid text," +
            "title text," +
            "content text," +
            "isChecked boolean default 0)"

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(createNotes)
        Toast.makeText(context, "Create succeeded", Toast.LENGTH_SHORT).show()
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }
}