package com.example.notebook

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private var notessList = ArrayList<Notes>()
    val dbHelper = NotesDatabaseHelper(this, "NotesStore.db", 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val top_toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(top_toolbar)

        val notesRecyclerView=findViewById<RecyclerView>(R.id.notesRecyclerView)
        val add_button: Button = findViewById(R.id.add_button)

        dbHelper.writableDatabase

        val layoutManager = LinearLayoutManager(this)
        notesRecyclerView.layoutManager = layoutManager
        get_Notes()
        val adapter = NotessAdapter(notessList)
        notesRecyclerView.adapter = adapter

        add_button.setOnClickListener {
            val db = dbHelper.writableDatabase
            val values1 = ContentValues().apply {
                // 開始組裝第一條數據
                put("title", "abc")
                put("content", "123")
            }
            db.insert("Notes", null, values1) // 插入第一条数据

            notessList.add(Notes("abc","123"))
            adapter.notifyDataSetChanged()
        }

//        notessList.add(Notes("a","123"))
//        val adapter = NotessAdapter(notessList)
//        notesRecyclerView.adapter = adapter

        val bottom_navigation: BottomNavigationView = findViewById(R.id.navigation)
        bottom_navigation.setOnItemReselectedListener {item ->
            when(item.itemId){
                R.id.delete -> {
                    Log.d("testsss", "onNavigationItemSelected: ")
                }
            }
        }
    }

    fun get_Notes(){
        val db = dbHelper.writableDatabase
        // 查詢Book表中所有的數據
        val cursor = db.query("Notes", null, null, null, null, null, null)
        if (cursor.moveToFirst()) {
            do {
                // 遍歷Cursor對象，取出數據並打印
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val content = cursor.getString(cursor.getColumnIndexOrThrow("content"))
                notessList.add(Notes(title,content))
//                Log.d("MainActivity", "book name is $title")
//                Log.d("MainActivity", "book author is $content")
            } while (cursor.moveToNext())
        }
        cursor.close()
    }

    inner class NotessAdapter(val notessList: List<Notes>) : RecyclerView.Adapter<NotessAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val newsTitle: TextView = view.findViewById(R.id.notesTitle)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.notes_item, parent, false)
            val holder = ViewHolder(view)
            holder.itemView.setOnClickListener {
                val notes = notessList[holder.adapterPosition]
                //NotesContentActivity.actionStart(parent.context, news.title, news.content);
                Toast.makeText(parent.context,"test",Toast.LENGTH_SHORT).show()
            }
            return holder
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val news = notessList[position]
            holder.newsTitle.text = news.title
        }

        override fun getItemCount() = notessList.size

    }

}
