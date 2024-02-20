package com.example.notebook

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity(), SelectColorFragment.RadioButtonListener {

    lateinit var toolbar : Toolbar
    lateinit var top_toolbar : Toolbar
    lateinit var notesRecyclerView : RecyclerView
    lateinit var add_button : FloatingActionButton
    lateinit var pagenavigation : BottomNavigationView
    lateinit var delete_button : Button

    var notesList = ArrayList<Notes>()
    var uuidList = ArrayList<String>()
    var notesListPosition by Delegates.notNull<Int>() //更新資料 旋轉螢幕 切換深淺模式 會出現BUG 所以要用SharedPreferences儲存
    val dbHelper = NotesDatabaseHelper(this, "NotesStore.db", 1)
    lateinit var db : SQLiteDatabase

    lateinit var intentNotesActivity : Intent
    lateinit var notesactivityLauncher : ActivityResultLauncher<Intent>

    var inDeletionMode = false
    var checkBoxList = ArrayList<Int>()
    var checkBoxStateList = ArrayList<checkBoxState>()

    lateinit var prefsColors : SharedPreferences
    lateinit var colorDefault : String
    lateinit var colorDefaultMode : String

    lateinit var adapter : MainActivity.NotesAdapter

    private val TAG = "testsss"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "MainActivity_onCreate: ")

        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) //禁用深色模式

        toolbar = findViewById(R.id.toolbar)
        top_toolbar = findViewById(R.id.topToolbar)
        notesRecyclerView=findViewById(R.id.notesRecyclerView)
        add_button = findViewById(R.id.add_button)
        pagenavigation = findViewById(R.id.pagenavigation)
        delete_button = findViewById(R.id.delete_button)

        db = dbHelper.writableDatabase

        intentNotesActivity = Intent(this, NotesActivity::class.java)

        setSupportActionBar(toolbar)

        prefsColors = this.getSharedPreferences("colors", Context.MODE_PRIVATE)
        colorDefault = prefsColors.getString("colorDefault","green").toString()
        colorDefaultMode = prefsColors.getString("colorDefaultMode","allcolor").toString()

        val editor = prefsColors.edit()
        editor.putInt("green",4)
        editor.putInt("yellow",3)
        editor.putInt("blue",2)
        editor.putInt("red",1)
        editor.apply()

        val layoutManager = LinearLayoutManager(this)
        notesRecyclerView.layoutManager = layoutManager
        adapter = NotesAdapter(notesList, checkBoxStateList)
        notesRecyclerView.adapter = adapter
        get_Notes()

        notesactivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(), ActivityResultCallback<ActivityResult>(){ item ->
                Log.d(TAG, "onCreate: resultCode"+item.resultCode)
                if(item.resultCode == 1000000000){
                    val uuid = item.data?.getStringExtra("uuid")
                    val title = item.data?.getStringExtra("title")
                    val content = item.data?.getStringExtra("content")
                    val color = item.data?.getStringExtra("color")
                    val createDate = item.data?.getStringExtra("createDate")
                    val updateDate = item.data?.getStringExtra("updateDate")

                    if(uuidList.contains(uuid)){
                        //update
                        notesList.set(notesListPosition, Notes(uuid,title,content,color,prefsColors.getInt(color,0),LocalDateTime.parse(createDate),LocalDateTime.parse(updateDate)))
                        adapter.notifyDataSetChanged() //要更改
                    }else{
                        //create
                        notesList.add(Notes(uuid,title,content,color,prefsColors.getInt(color,0),LocalDateTime.parse(createDate),LocalDateTime.parse(updateDate)))
                        uuidList.add(uuid.toString())
                        checkBoxStateList.add(checkBoxState(false))
                        adapter.notifyDataSetChanged() //要更改
                    }
                }else if(item.resultCode == 0){
                    get_Notes()
                    //adapter.notifyDataSetChanged() //要更改
                }
            }
        )

        add_button.setOnClickListener {
            intentNotesActivity.putExtra("uuid", "")
            intentNotesActivity.putExtra("title","")
            intentNotesActivity.putExtra("content","")
            colorDefault = prefsColors.getString("colorDefault","green").toString()
            intentNotesActivity.putExtra("color", colorDefault) //預設顏色
            intentNotesActivity.putExtra("createDate","")
            intentNotesActivity.putExtra("updateDate","")
            notesactivityLauncher.launch(intentNotesActivity)
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

            get_Notes() //要更改

            inDeletionMode = false
            toolbar.setVisibility(View.VISIBLE)
            top_toolbar.setVisibility(View.GONE)
            add_button.setVisibility(View.VISIBLE)
            pagenavigation.setVisibility(View.VISIBLE)
            delete_button.setVisibility(View.GONE)

            //adapter.notifyDataSetChanged() //要更改
        }

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            //返回鍵
            override fun handleOnBackPressed() {
                if(inDeletionMode){
                    checkBoxList.clear()

                    get_Notes() //要更改

                    inDeletionMode = false
                    toolbar.setVisibility(View.VISIBLE)
                    top_toolbar.setVisibility(View.GONE)
                    add_button.setVisibility(View.VISIBLE)
                    pagenavigation.setVisibility(View.VISIBLE)
                    delete_button.setVisibility(View.GONE)

                    //adapter.notifyDataSetChanged() //要更改
                }else{
                    finish()
                }
            }
        })


        top_toolbar.setNavigationIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_arrow_back_24))
        top_toolbar.setNavigationOnClickListener {
            if(inDeletionMode){
                checkBoxList.clear()

                get_Notes() //要更改

                inDeletionMode = false
                toolbar.setVisibility(View.VISIBLE)
                top_toolbar.setVisibility(View.GONE)
                add_button.setVisibility(View.VISIBLE)
                pagenavigation.setVisibility(View.VISIBLE)
                delete_button.setVisibility(View.GONE)

                //adapter.notifyDataSetChanged() //要更改
            }else{
                finish()
            }
        }

    } //onCreate

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.mainactivity_top_menu, menu)

        colorDefaultMode = prefsColors.getString("colorDefaultMode","allcolor").toString()
        if(colorDefaultMode == "green"){
            toolbar.menu.getItem(0).setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_circle_green_24))
        }else if(colorDefaultMode == "yellow"){
            toolbar.menu.getItem(0).setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_circle_yellow_24))
        }else if(colorDefaultMode == "blue"){
            toolbar.menu.getItem(0).setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_circle_blue_24))
        }else if(colorDefaultMode == "red"){
            toolbar.menu.getItem(0).setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_circle_red_24))
        }else if(colorDefaultMode == "allcolor"){
            toolbar.menu.getItem(0).setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_color_lens_24))
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.select_color ->{
                val bottomSheetFragment = SelectColorFragment(this)
                bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
            }
            R.id.sortBy ->{

            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun sendValue(value: String) {
        if(value == "green"){
            Log.d(TAG, "MainActivity sendValue: "+value)
            toolbar.menu.getItem(0).setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_circle_green_24))
            get_Notes()
            Thread.sleep(50)
        }else if(value == "yellow"){
            Log.d(TAG, "MainActivity sendValue: "+value)
            toolbar.menu.getItem(0).setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_circle_yellow_24))
            get_Notes()
            Thread.sleep(50)
        }else if(value == "blue"){
            Log.d(TAG, "MainActivity sendValue: "+value)
            toolbar.menu.getItem(0).setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_circle_blue_24))
            get_Notes()
            Thread.sleep(50)
        }else if(value == "red"){
            Log.d(TAG, "MainActivity sendValue: "+value)
            toolbar.menu.getItem(0).setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_circle_red_24))
            get_Notes()
            Thread.sleep(50)
        }else if(value == "allcolor"){
            Log.d(TAG, "MainActivity sendValue: "+value)
            toolbar.menu.getItem(0).setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_color_lens_24))
            get_Notes()
            Thread.sleep(50)
        }

    }

    fun get_Notes(){ //根據colorDefault的顏色呈現不同的顏色結果
        notesList.clear()
        uuidList.clear()
        checkBoxStateList.clear()
        colorDefaultMode = prefsColors.getString("colorDefaultMode","allcolor").toString()
        //val cursor = db.query("Notes", null, null, null, null, null, null)
        var cursor: Cursor
        if(colorDefaultMode == "allcolor"){
            cursor = db.rawQuery("select * from Notes", null)
            Log.d(TAG, "get_Notes: allcolor")
        }else{
            cursor = db.rawQuery("select * from Notes where color=?", arrayOf(colorDefaultMode))
            Log.d(TAG, "get_Notes: "+colorDefaultMode)
        }

        if (cursor.moveToFirst()) {
            do {
                val uuid = cursor.getString(cursor.getColumnIndexOrThrow("uuid"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val content = cursor.getString(cursor.getColumnIndexOrThrow("content"))
                val color = cursor.getString(cursor.getColumnIndexOrThrow("color"))
                val createDate = cursor.getString(cursor.getColumnIndexOrThrow("createDate"))
                val updateDate = cursor.getString(cursor.getColumnIndexOrThrow("updateDate"))
                val isChecked = cursor.getInt(cursor.getColumnIndexOrThrow("isChecked")) > 0

                notesList.add(Notes(uuid,title,content,color,prefsColors.getInt(color,0),LocalDateTime.parse(createDate),LocalDateTime.parse(updateDate)))
                uuidList.add(uuid)
                checkBoxStateList.add(checkBoxState(isChecked))
            } while (cursor.moveToNext())
        }
        cursor.close()

        for(notes in notesList){
            Log.d(TAG, "notesList "+notes.color)
        }

        adapter.notifyDataSetChanged()
    }

    inner class NotesAdapter(val notesList: List<Notes>, val checkBoxStateList: ArrayList<checkBoxState>) : RecyclerView.Adapter<NotesAdapter.ViewHolder>() {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val notesTitle: TextView = view.findViewById(R.id.notesTitle)
            val notesDate: TextView = view.findViewById(R.id.notesDate)
            val notesContent: TextView = view.findViewById(R.id.notesContent)
            val notesCheckBox: CheckBox = view.findViewById(R.id.notesCheckBox)
            val notesColor: TextView = view.findViewById(R.id.notesColor)
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
                    intentNotesActivity.putExtra("color", notes.color)
                    intentNotesActivity.putExtra("createDate",notes.createDate.toString())
                    intentNotesActivity.putExtra("updateDate",notes.updateDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                    notesactivityLauncher.launch(intentNotesActivity)
                }else{
                    val pos : Int = holder.notesCheckBox.getTag() as Int

                    if (checkBoxStateList.get(pos).get_Checked()) {
                        checkBoxStateList.get(pos).set_Checked(false);
                        checkBoxList.remove(holder.absoluteAdapterPosition)
                        holder.notesCheckBox.setChecked(false)
                    } else {
                        checkBoxStateList.get(pos).set_Checked(true);
                        checkBoxList.add(holder.absoluteAdapterPosition)
                        holder.notesCheckBox.setChecked(true)
                    }
                    top_toolbar.setTitle("已選${checkBoxList.size}項")
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

                false
            }

            return holder
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val notes = notesList[position]

            if(notes.title==""){
                holder.notesTitle.text = notes.content
            }else{
                holder.notesTitle.text = notes.title
            }

            holder.notesDate.text = notes.updateDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            holder.notesContent.text = notes.content

            if(notes.color == "green"){
                holder.notesColor.setBackground(getDrawable(R.drawable.baseline_circle_green_24))
            }else if(notes.color == "yellow"){
                holder.notesColor.setBackground(getDrawable(R.drawable.baseline_circle_yellow_24))
            }else if(notes.color == "blue"){
                holder.notesColor.setBackground(getDrawable(R.drawable.baseline_circle_blue_24))
            }else if(notes.color == "red"){
                holder.notesColor.setBackground(getDrawable(R.drawable.baseline_circle_red_24))
            }

            top_toolbar.setTitle("已選${checkBoxList.size}項")

            //Log.d(TAG, "onBindViewHolder: "+notes.color)

            if(inDeletionMode){
                holder.notesCheckBox.setVisibility(View.VISIBLE)

                holder.notesCheckBox.setChecked(checkBoxStateList.get(position).get_Checked()) //RecyclerView+checkBox 上下滑動實(好像有bug) checkBox點選打勾圖示會亂跳
                holder.notesCheckBox.setTag(position)

                holder.notesCheckBox.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View) {
                        val pos : Int = holder.notesCheckBox.getTag() as Int //pos == position

                        if (checkBoxStateList.get(pos).get_Checked()) {
                            checkBoxStateList.get(pos).set_Checked(false);
                            checkBoxList.remove(holder.absoluteAdapterPosition)
                        } else {
                            checkBoxStateList.get(pos).set_Checked(true);
                            checkBoxList.add(holder.absoluteAdapterPosition)
                        }

                        top_toolbar.setTitle("已選${checkBoxList.size}項")
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
