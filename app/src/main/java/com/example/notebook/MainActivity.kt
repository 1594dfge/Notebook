package com.example.notebook

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    lateinit var toolbar: Toolbar
    lateinit var notesRecyclerView : RecyclerView
    lateinit var add_button: FloatingActionButton
    lateinit var bottom_navigation: BottomNavigationView
    lateinit var top_navigation: BottomNavigationView

    private var notesList = ArrayList<Notes>()
    var notesListPosition : Int = 0 //更新資料
    val dbHelper = NotesDatabaseHelper(this, "NotesStore.db", 1)
    lateinit var db : SQLiteDatabase

    lateinit var intentNotesActivity : Intent
    lateinit var notesactivityLauncher : ActivityResultLauncher<Intent>

    private val TAG = "testsss"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        notesRecyclerView=findViewById<RecyclerView>(R.id.notesRecyclerView)
        add_button = findViewById(R.id.add_button)
        bottom_navigation = findViewById(R.id.bottomnavigation)
        top_navigation = findViewById(R.id.topnavigation)

        db = dbHelper.writableDatabase

        intentNotesActivity = Intent(this, NotesActivity::class.java)

        setSupportActionBar(toolbar)

        val layoutManager = LinearLayoutManager(this)
        notesRecyclerView.layoutManager = layoutManager
        get_Notes()
        val adapter = NotessAdapter(notesList)
        notesRecyclerView.adapter = adapter



        notesactivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(), ActivityResultCallback<ActivityResult>(){item ->
                if(item.resultCode == 1000000000){
                    Log.d(TAG, "create")
                    val uuid = item.data?.getStringExtra("uuid")
                    val title = item.data?.getStringExtra("title")
                    val content = item.data?.getStringExtra("content")

                    val values1 = ContentValues().apply {
                        put("uuid", uuid)
                        put("title", title)
                        put("content", content)
                    }
                    db.insert("Notes", null, values1)

                    notesList.add(Notes(uuid,title,content))
                    adapter.notifyDataSetChanged()
                }else if(item.resultCode == 1000000001){
                    Log.d(TAG, "update")
                    val uuid = item.data?.getStringExtra("uuid")
                    val title = item.data?.getStringExtra("title")
                    val content = item.data?.getStringExtra("content")

                    val values1 = ContentValues().apply {
                        put("title", title)
                        put("content", content)
                    }
                    db.update("Notes", values1, "uuid = ?", arrayOf(uuid))

                    notesList.set(notesListPosition, Notes(uuid,title,content))
                    adapter.notifyDataSetChanged()
                }
            }
        )

        add_button.setOnClickListener {
            intentNotesActivity.putExtra("uuid", "")
            intentNotesActivity.putExtra("title","")
            intentNotesActivity.putExtra("content","")
            notesactivityLauncher.launch(intentNotesActivity);
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
        val cursor = db.query("Notes", null, null, null, null, null, null)
        if (cursor.moveToFirst()) {
            do {
                val uuid = cursor.getString(cursor.getColumnIndexOrThrow("uuid"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val content = cursor.getString(cursor.getColumnIndexOrThrow("content"))
                notesList.add(Notes(uuid,title,content))
            } while (cursor.moveToNext())
        }
        cursor.close()
    }

    inner class NotessAdapter(val notesList: List<Notes>) : RecyclerView.Adapter<NotessAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val notesTitle: TextView = view.findViewById(R.id.notesTitle)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.notes_item, parent, false)
            val holder = ViewHolder(view)
            holder.itemView.setOnClickListener {
                val notes = notesList[holder.absoluteAdapterPosition]
                notesListPosition = holder.absoluteAdapterPosition
                intentNotesActivity.putExtra("uuid", notes.uuid)
                intentNotesActivity.putExtra("title", notes.title)
                intentNotesActivity.putExtra("content", notes.content)
                notesactivityLauncher.launch(intentNotesActivity);
            }
            return holder
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val news = notesList[position]
            holder.notesTitle.text = news.title
        }

        override fun getItemCount() = notesList.size

    }

    override fun onDestroy() {
        super.onDestroy()
        notesList.clear()
    }

}
