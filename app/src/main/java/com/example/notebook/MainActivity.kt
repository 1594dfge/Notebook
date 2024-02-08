package com.example.notebook

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {

    private var notessList = ArrayList<Notes>()
    val dbHelper = NotesDatabaseHelper(this, "NotesStore.db", 1)

    private val TAG = "testsss"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val notesRecyclerView=findViewById<RecyclerView>(R.id.notesRecyclerView)
        val add_button: FloatingActionButton = findViewById(R.id.add_button)
        val bottom_navigation: BottomNavigationView = findViewById(R.id.bottomnavigation)
        val top_navigation: BottomNavigationView = findViewById(R.id.topnavigation)

        setSupportActionBar(toolbar)

        val db = dbHelper.writableDatabase

        val layoutManager = LinearLayoutManager(this)
        notesRecyclerView.layoutManager = layoutManager
        get_Notes()
        val adapter = NotessAdapter(notessList)
        notesRecyclerView.adapter = adapter



        val notesactivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(), ActivityResultCallback<ActivityResult>(){item ->
                if(item.resultCode == Activity.RESULT_OK){
                    Log.d(TAG, "RESULT_OK")
                    val title = item.data?.getStringExtra("title")
                    val content = item.data?.getStringExtra("content")

                    val values1 = ContentValues().apply {
                        put("title", title)
                        put("content", content)
                    }
                    db.insert("Notes", null, values1)

                    notessList.add(Notes(title,content))
                    adapter.notifyDataSetChanged()
                }
            }
        )

        add_button.setOnClickListener {
            val intent = Intent(this, NotesActivity::class.java)
            notesactivityLauncher.launch(intent);
        }

        bottom_navigation.setOnItemReselectedListener {item ->
            when(item.itemId){
            }
        }

        top_navigation.setOnItemReselectedListener {item ->
            when(item.itemId){
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
                Toast.makeText(parent.context,notes.content,Toast.LENGTH_SHORT).show()
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
