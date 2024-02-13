package com.example.notebook

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    lateinit var toolbar: Toolbar
    lateinit var top_toolbar: Toolbar
    lateinit var notesRecyclerView : RecyclerView
    lateinit var add_button: FloatingActionButton
    lateinit var pagenavigation :BottomNavigationView
    lateinit var delete_button: Button

    var notesList = ArrayList<Notes>()
    var uuidList = ArrayList<String>()
    var notesListPosition by Delegates.notNull<Int>() //更新資料
    val dbHelper = NotesDatabaseHelper(this, "NotesStore.db", 1)
    lateinit var db : SQLiteDatabase

    lateinit var intentNotesActivity : Intent
    lateinit var notesactivityLauncher : ActivityResultLauncher<Intent>

    var inDeletionMode = false
    var checkBoxList = ArrayList<Int>()
    var checkBoxStateList = ArrayList<checkBoxState>()

    private val TAG = "testsss"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "MainActivity_onCreate: ")

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
        val adapter = NotessAdapter(notesList, checkBoxStateList)
        notesRecyclerView.adapter = adapter

        notesactivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(), ActivityResultCallback<ActivityResult>(){ item ->
                Log.d(TAG, "onCreate: resultCode"+item.resultCode)
                if(item.resultCode == 1000000000){
                    val uuid = item.data?.getStringExtra("uuid")
                    val title = item.data?.getStringExtra("title")
                    val content = item.data?.getStringExtra("content")
                    val updateDate = item.data?.getStringExtra("updateDate")

                    if(uuidList.contains(uuid)){
                        //update
                        notesList.set(notesListPosition, Notes(uuid,title,content,LocalDateTime.parse(updateDate)))
                        adapter.notifyDataSetChanged()
                    }else{
                        //create
                        notesList.add(Notes(uuid,title,content,LocalDateTime.parse(updateDate)))
                        uuidList.add(uuid.toString())
                        checkBoxStateList.add(checkBoxState(false))
                        adapter.notifyDataSetChanged()
                    }
                }else if(item.resultCode == 0){
                    get_Notes()
                    adapter.notifyDataSetChanged()
                }
            }
        )

        add_button.setOnClickListener {
            intentNotesActivity.putExtra("uuid", "")
            intentNotesActivity.putExtra("title","")
            intentNotesActivity.putExtra("content","")
            intentNotesActivity.putExtra("updateDate","")
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

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            //返回鍵
            override fun handleOnBackPressed() {
                if(inDeletionMode){
                    checkBoxList.clear()

                    get_Notes()

                    inDeletionMode = false
                    toolbar.setVisibility(View.VISIBLE)
                    top_toolbar.setVisibility(View.GONE)
                    add_button.setVisibility(View.VISIBLE)
                    pagenavigation.setVisibility(View.VISIBLE)
                    delete_button.setVisibility(View.GONE)

                    adapter.notifyDataSetChanged()
                }else{
                    finish()
                }
            }
        })
    }

    fun get_Notes(){
        notesList.clear()
        uuidList.clear()
        checkBoxStateList.clear()
        val cursor = db.query("Notes", null, null, null, null, null, null)
        if (cursor.moveToFirst()) {
            do {
                val uuid = cursor.getString(cursor.getColumnIndexOrThrow("uuid"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val content = cursor.getString(cursor.getColumnIndexOrThrow("content"))
                val updateDate = cursor.getString(cursor.getColumnIndexOrThrow("updateDate"))
                val isChecked = cursor.getInt(cursor.getColumnIndexOrThrow("isChecked")) > 0
                notesList.add(Notes(uuid,title,content,LocalDateTime.parse(updateDate)))
                uuidList.add(uuid)
                checkBoxStateList.add(checkBoxState(isChecked))
            } while (cursor.moveToNext())
        }
        cursor.close()
    }

    inner class NotessAdapter(val notesList: List<Notes>, val checkBoxStateList: ArrayList<checkBoxState>) : RecyclerView.Adapter<NotessAdapter.ViewHolder>() {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val notesTitle: TextView = view.findViewById(R.id.notesTitle)
            val notesDate: TextView = view.findViewById(R.id.notesDate)
            val notesContent: TextView = view.findViewById(R.id.notesContent)
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
                    intentNotesActivity.putExtra("updateDate",notes.updateDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                    notesactivityLauncher.launch(intentNotesActivity)
                }else{
                    val pos : Int = holder.notesCheckBox.getTag() as Int
                    Log.d(TAG, "onClick: pos2"+pos)

                    if (checkBoxStateList.get(pos).get_Checked()) {
                        checkBoxStateList.get(pos).set_Checked(false);
                        checkBoxList.remove(holder.absoluteAdapterPosition)
                        holder.notesCheckBox.setChecked(false)
                    } else {
                        checkBoxStateList.get(pos).set_Checked(true);
                        checkBoxList.add(holder.absoluteAdapterPosition)
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
            val notes = notesList[position]
            holder.notesTitle.text = notes.title
            holder.notesDate.text = notes.updateDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            holder.notesContent.text = notes.content

            if(inDeletionMode){
                holder.notesCheckBox.setVisibility(View.VISIBLE)

                holder.notesCheckBox.setChecked(checkBoxStateList.get(position).get_Checked()) //RecyclerView+checkBox 上下滑動實(好像有bug) checkBox點選打勾圖示會亂跳
                holder.notesCheckBox.setTag(position)

                holder.notesCheckBox.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View) {
                        val pos : Int = holder.notesCheckBox.getTag() as Int //pos == position
                        Log.d(TAG, "onClick: pos1"+pos)

                        if (checkBoxStateList.get(pos).get_Checked()) {
                            checkBoxStateList.get(pos).set_Checked(false);
                            checkBoxList.remove(holder.absoluteAdapterPosition)
                        } else {
                            checkBoxStateList.get(pos).set_Checked(true);
                            checkBoxList.add(holder.absoluteAdapterPosition)
                        }
                    }
                })
            }else{
                holder.notesCheckBox.setChecked(false) //RecyclerView+checkBox(好像有bug) checkBox點選打勾圖示會亂跳 所以全部取消
                holder.notesCheckBox.setVisibility(View.GONE)
            }
        }

        override fun getItemCount() = notesList.size
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "MainActivity_onStart: ")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "MainActivity_onResume: ")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "MainActivity_onPause: ")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "MainActivity_onStop: ")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "MainActivity_onDestroy: ")

        notesList.clear()
        uuidList.clear()
        checkBoxList.clear()
        checkBoxStateList.clear()
    }
}
