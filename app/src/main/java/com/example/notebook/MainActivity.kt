package com.example.notebook

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
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
    lateinit var top_toolbar: Toolbar
    lateinit var notesRecyclerView : RecyclerView
    lateinit var add_button: FloatingActionButton
    lateinit var pagenavigation :BottomNavigationView
    lateinit var delete_button: Button

    private var notesList = ArrayList<Notes>()
    var uuidList = ArrayList<String>()
    var notesListPosition : Int = 0 //更新資料
    val dbHelper = NotesDatabaseHelper(this, "NotesStore.db", 1)
    lateinit var db : SQLiteDatabase

    lateinit var intentNotesActivity : Intent
    lateinit var notesactivityLauncher : ActivityResultLauncher<Intent>

    var inDeletionMode = false
    var checkBoxList = ArrayList<Int>()

    private val TAG = "testsss"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        top_toolbar = findViewById(R.id.topToolbar)
        notesRecyclerView=findViewById(R.id.notesRecyclerView)
        add_button = findViewById(R.id.add_button)
        pagenavigation = findViewById(R.id.pagenavigation)
        delete_button = findViewById(R.id.delete_button)

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
                    //create
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
                    uuidList.add(uuid.toString())
                    adapter.notifyDataSetChanged()
                }else if(item.resultCode == 1000000001){
                    //update
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
            notesactivityLauncher.launch(intentNotesActivity)
        }

        pagenavigation.setOnItemReselectedListener {item ->
            when(item.itemId){
            }
        }

        delete_button.setOnClickListener {
            var deleteList = ArrayList<String>()
            for(checked in checkBoxList){
                deleteList.add(uuidList[checked])
            }

            var dArray = arrayOfNulls<String>(1)
            for(delete in deleteList){
                Log.d(TAG, "delete"+delete)
                dArray[0] = delete
                db.delete("Notes", "uuid = ?", dArray)
            }

            checkBoxList.clear()

            get_Notes()

            inDeletionMode = false
            toolbar.setVisibility(View.VISIBLE)
            top_toolbar.setVisibility(View.GONE)
            add_button.setVisibility(View.VISIBLE)
            pagenavigation.setVisibility(View.VISIBLE)
            delete_button.setVisibility(View.GONE)

            adapter.notifyDataSetChanged()
        }
    }

    fun get_Notes(){
        notesList.clear()
        uuidList.clear()
        val cursor = db.query("Notes", null, null, null, null, null, null)
        if (cursor.moveToFirst()) {
            do {
                val uuid = cursor.getString(cursor.getColumnIndexOrThrow("uuid"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val content = cursor.getString(cursor.getColumnIndexOrThrow("content"))
                notesList.add(Notes(uuid,title,content))
                uuidList.add(uuid)
            } while (cursor.moveToNext())
        }
        cursor.close()
    }

    inner class NotessAdapter(val notesList: List<Notes>) : RecyclerView.Adapter<NotessAdapter.ViewHolder>() {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val notesTitle: TextView = view.findViewById(R.id.notesTitle)
            val notesCheckBox: CheckBox = view.findViewById(R.id.notesCheckBox)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.notes_item, parent, false)
            val holder = ViewHolder(view)

            holder.itemView.setOnClickListener {
                if(!inDeletionMode){
                    val notes = notesList[holder.absoluteAdapterPosition]
                    notesListPosition = holder.absoluteAdapterPosition
                    intentNotesActivity.putExtra("uuid", notes.uuid)
                    intentNotesActivity.putExtra("title", notes.title)
                    intentNotesActivity.putExtra("content", notes.content)
                    notesactivityLauncher.launch(intentNotesActivity)
                }else{
                    if(holder.notesCheckBox.isChecked){
                        holder.notesCheckBox.setChecked(false)
                    }else{
                        holder.notesCheckBox.setChecked(true)
                    }
                }
            }

            holder.itemView.setOnLongClickListener {
                if(!inDeletionMode){
                    inDeletionMode = true
                    toolbar.setVisibility(View.GONE)
                    top_toolbar.setVisibility(View.VISIBLE)
                    add_button.setVisibility(View.GONE)
                    pagenavigation.setVisibility(View.GONE)
                    delete_button.setVisibility(View.VISIBLE)
                    notifyDataSetChanged()
                }

                true
            }

            return holder
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val news = notesList[position]
            holder.notesTitle.text = news.title

            if(inDeletionMode){
                holder.notesCheckBox.setVisibility(View.VISIBLE)
                holder.notesCheckBox.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
                    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                        if(isChecked){
                            checkBoxList.add(holder.absoluteAdapterPosition)
                            Log.d(TAG, "onCheckedChanged: "+checkBoxList)
                        }else{
                            checkBoxList.remove(holder.absoluteAdapterPosition)
                            Log.d(TAG, "onCheckedChanged: "+checkBoxList)
                        }
                    }
                })
            }else{
                holder.notesCheckBox.setChecked(false) //delete後checkBox 勾勾 會亂跳 所以全部取消
                holder.notesCheckBox.setVisibility(View.GONE)
            }
        }

        override fun getItemCount() = notesList.size
    }

    override fun onDestroy() {
        super.onDestroy()
        notesList.clear()
        uuidList.clear()
        checkBoxList.clear()
    }

}
