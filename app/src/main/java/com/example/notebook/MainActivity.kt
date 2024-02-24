package com.example.notebook

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
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
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity(), SelectColorFragment.RadioButtonListener, SortDataFragment.RadioButtonListener {
    lateinit var toolbar : Toolbar
    lateinit var hide_toolbar : Toolbar
    lateinit var searchview : SearchView
    lateinit var notesRecyclerView : RecyclerView
    lateinit var add_button : FloatingActionButton
    lateinit var pagenavigation : BottomNavigationView
    lateinit var delete_button : Button

    var notesList = ArrayList<Notes>()
    //更新資料 旋轉螢幕 切換深淺模式 會出現BUG 所以要用SharedPreferences儲存
    lateinit var prefsNotesList : SharedPreferences
    lateinit var adapter : MainActivity.NotesAdapter

    var inDeletionMode = false
    var checkBoxList = ArrayList<Int>()
    var checkBoxStateList = ArrayList<checkBoxState>()

    lateinit var imm : InputMethodManager

    val dbHelper = NotesDatabaseHelper(this, "NotesStore.db", 1)
    lateinit var db : SQLiteDatabase

    lateinit var intentNotesActivity : Intent
    lateinit var notesactivityLauncher : ActivityResultLauncher<Intent>

    lateinit var bottomSheetFragment : BottomSheetDialogFragment

    lateinit var prefsColors : SharedPreferences
    lateinit var colorDefault : String
    lateinit var colorDefaultMode : String

    lateinit var prefsSortData : SharedPreferences

    private val TAG = "testsss"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "MainActivity_onCreate: ")

        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) //禁用深色模式

        toolbar = findViewById(R.id.toolbar)
        hide_toolbar = findViewById(R.id.hideToolbar)
        searchview = findViewById(R.id.searchview)
        notesRecyclerView=findViewById(R.id.notesRecyclerView)
        add_button = findViewById(R.id.add_button)
        pagenavigation = findViewById(R.id.pagenavigation)
        delete_button = findViewById(R.id.delete_button)

        setSupportActionBar(toolbar)

        db = dbHelper.writableDatabase

        intentNotesActivity = Intent(this, NotesActivity::class.java)

        prefsNotesList = this.getSharedPreferences("notesList",Context.MODE_PRIVATE)

        prefsColors = this.getSharedPreferences("colors", Context.MODE_PRIVATE)
        colorDefault = prefsColors.getString("colorDefault","green").toString()
        colorDefaultMode = prefsColors.getString("colorDefaultMode","allcolor").toString()

        prefsColors.getInt("green",4)
        prefsColors.getInt("yellow",3)
        prefsColors.getInt("blue",2)
        prefsColors.getInt("red",1)

        prefsSortData = this.getSharedPreferences("sortData",Context.MODE_PRIVATE)

        imm= getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

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
                    var title = item.data?.getStringExtra("title")
                    val content = item.data?.getStringExtra("content")
                    val color = item.data?.getStringExtra("color")
                    val createDate = item.data?.getStringExtra("createDate")
                    val updateDate = item.data?.getStringExtra("updateDate")
                    val mode = item.data?.getStringExtra("mode")

                    if(mode == "1"){
                        //update
                        if(prefsNotesList.getInt("notesList",-1) == -1){
                            Log.d(TAG, "prefsNotesList.getInt(\"notesList\",-1) == -1")
                        }else{
                            notesList.set(prefsNotesList.getInt("notesList",-1), Notes(uuid,title,content,false,color,prefsColors.getInt(color,0),LocalDateTime.parse(createDate),LocalDateTime.parse(updateDate)))
                            adapter.notifyItemChanged(prefsNotesList.getInt("notesList",-1))
                        }

                    }else if(mode == "2"){
                        //create
                        notesList.add(0,Notes(uuid,title,content,false,color,prefsColors.getInt(color,0),LocalDateTime.parse(createDate),LocalDateTime.parse(updateDate)))
                        checkBoxStateList.add(0,checkBoxState(false))
                        adapter.notifyItemInserted(0)
                        adapter.notifyItemRangeChanged(0,notesList.size+1)
                        notesRecyclerView.scrollToPosition(0)
                    }
                }else if(item.resultCode == 0){
                    get_Notes()
                }
            }
        )



        add_button.setOnClickListener {
            intentNotesActivity.putExtra("uuid", "")
            intentNotesActivity.putExtra("title","")
            intentNotesActivity.putExtra("content","")
            intentNotesActivity.putExtra("color", prefsColors.getString("colorDefault","green")) //預設顏色
            intentNotesActivity.putExtra("createDate","")
            intentNotesActivity.putExtra("updateDate","")
            intentNotesActivity.putExtra("mode","2")
            notesactivityLauncher.launch(intentNotesActivity)
        }

        searchview.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                searchview.clearFocus()
                imm.hideSoftInputFromWindow(searchview.windowToken,0)

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d(TAG, "onQueryTextChange: "+newText)

                if(newText == ""){
                    get_Notes()
                }else{
                    notesList.clear()
                    checkBoxStateList.clear()

                    val cursor: Cursor = db.rawQuery("select * from Notes where title like ?", arrayOf("%$newText%"))
                    if (cursor.moveToFirst()) {
                        do {
                            val uuid = cursor.getString(cursor.getColumnIndexOrThrow("uuid"))
                            val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                            val content = cursor.getString(cursor.getColumnIndexOrThrow("content"))
                            val color = cursor.getString(cursor.getColumnIndexOrThrow("color"))
                            val createDate = cursor.getString(cursor.getColumnIndexOrThrow("createDate"))
                            val updateDate = cursor.getString(cursor.getColumnIndexOrThrow("updateDate"))
                            val isChecked = cursor.getInt(cursor.getColumnIndexOrThrow("isChecked")) > 0

                            notesList.add(Notes(uuid,title,content,false,color,prefsColors.getInt(color,0),LocalDateTime.parse(createDate),LocalDateTime.parse(updateDate)))
                            checkBoxStateList.add(checkBoxState(isChecked))
                        } while (cursor.moveToNext())
                    }
                    cursor.close()

                    adapter.notifyDataSetChanged()
                }

                return false
            }
        })

        delete_button.setOnClickListener {
            //Notes資料表 刪除
            db.execSQL("delete from Notes where isChecked = ?", arrayOf(true))

            //notesList 刪除
            var notesListit: MutableIterator<Notes> = notesList.iterator()
            while(notesListit.hasNext()){
                Log.d(TAG, "hasNext "+notesListit.hasNext())
                if(notesListit.next().isChecked == true){
                    notesListit.remove()
                    Log.d(TAG, "remove ")
                }
            }

            //recyclerview notifyItem
//            for(delete in checkBoxList){
//                adapter.notifyItemRemoved(delete)
//                adapter.notifyItemRangeChanged(delete,notesList.size-1)
//            }
//            adapter.notifyItemRangeChanged(0,notesList.size)

            checkBoxList.clear()
            checkBoxStateList.clear()

            for(i in 0 until notesList.size){
                checkBoxStateList.add(checkBoxState(false))
            }

            inDeletionMode = false
            toolbar.setVisibility(View.VISIBLE)
            hide_toolbar.setVisibility(View.GONE)
            add_button.setVisibility(View.VISIBLE)
            pagenavigation.setVisibility(View.VISIBLE)
            delete_button.setVisibility(View.GONE)

            adapter.notifyDataSetChanged()
        }

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            //返回鍵
            override fun handleOnBackPressed() {
                if(inDeletionMode){
                    db.execSQL("update Notes set isChecked = ? where isChecked = ?", arrayOf(false,true))

                    for(isChecked in checkBoxList){
                        notesList[isChecked].isChecked = false
                    }

                    checkBoxList.clear()
                    checkBoxStateList.clear()

                    for(i in 0 until notesList.size){
                        checkBoxStateList.add(checkBoxState(false))
                    }

                    inDeletionMode = false
                    toolbar.setVisibility(View.VISIBLE)
                    hide_toolbar.setVisibility(View.GONE)
                    add_button.setVisibility(View.VISIBLE)
                    pagenavigation.setVisibility(View.VISIBLE)
                    delete_button.setVisibility(View.GONE)

                    //adapter.notifyItemRangeChanged(0,notesList.size)

                    adapter.notifyDataSetChanged()
                }else{
                    finish()
                }
            }
        })

        hide_toolbar.setNavigationIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_arrow_back_24))
        hide_toolbar.setNavigationOnClickListener {
            if(inDeletionMode){
                db.execSQL("update Notes set isChecked = ? where isChecked = ?", arrayOf(false,true))

                for(isChecked in checkBoxList){
                    notesList[isChecked].isChecked = false
                }

                checkBoxList.clear()
                checkBoxStateList.clear()

                for(i in 0 until notesList.size){
                    checkBoxStateList.add(checkBoxState(false))
                }

                inDeletionMode = false
                toolbar.setVisibility(View.VISIBLE)
                hide_toolbar.setVisibility(View.GONE)
                add_button.setVisibility(View.VISIBLE)
                pagenavigation.setVisibility(View.VISIBLE)
                delete_button.setVisibility(View.GONE)

                //adapter.notifyItemRangeChanged(0,notesList.size)

                adapter.notifyDataSetChanged()
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
                bottomSheetFragment = SelectColorFragment()
                bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
            }
            R.id.sortBy ->{
                bottomSheetFragment = SortDataFragment()
                bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun sendValue(value: String) {
        if(value == "green"){
            Log.d(TAG, "MainActivity sendValue: $value")
            toolbar.menu.getItem(0).setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_circle_green_24))
            get_Notes()
            Thread.sleep(50)
        }else if(value == "yellow"){
            Log.d(TAG, "MainActivity sendValue: $value")
            toolbar.menu.getItem(0).setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_circle_yellow_24))
            get_Notes()
            Thread.sleep(50)
        }else if(value == "blue"){
            Log.d(TAG, "MainActivity sendValue: $value")
            toolbar.menu.getItem(0).setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_circle_blue_24))
            get_Notes()
            Thread.sleep(50)
        }else if(value == "red"){
            Log.d(TAG, "MainActivity sendValue: $value")
            toolbar.menu.getItem(0).setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_circle_red_24))
            get_Notes()
            Thread.sleep(50)
        }else if(value == "allcolor"){
            Log.d(TAG, "MainActivity sendValue: $value")
            toolbar.menu.getItem(0).setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_color_lens_24))
            get_Notes()
            Thread.sleep(50)
        }else if(value == "edit_color_finish"){
            if(prefsSortData.getString("sortDataDefault","0") == "sortBy_color"){
                get_Notes()
                Thread.sleep(50)
            }
        } else if(value == "sortBy_updateDate"){
            get_Notes()
            Thread.sleep(50)
        }else if(value == "sortBy_createDate"){
            get_Notes()
            Thread.sleep(50)
        }else if(value == "sortBy_color"){
            get_Notes()
            Thread.sleep(50)
        }
    }

    fun get_Notes(){ //根據colorDefault的顏色呈現不同的顏色結果
        notesList.clear()
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

                notesList.add(Notes(uuid,title,content,false,color,prefsColors.getInt(color,0),LocalDateTime.parse(createDate),LocalDateTime.parse(updateDate)))
                val sortDataDefault = prefsSortData.getString("sortDataDefault","sortBy_updateDate")
                if(sortDataDefault == "sortBy_updateDate"){
                    notesList.sortByDescending { notes -> notes.updateDate  }
                }else if(sortDataDefault == "sortBy_createDate"){
                    notesList.sortByDescending { notes -> notes.createDate  }
                }else if(sortDataDefault == "sortBy_color"){
                    notesList.sortByDescending { notes -> notes.colorLevel  }
                }

                checkBoxStateList.add(checkBoxState(isChecked))
            } while (cursor.moveToNext())
        }
        cursor.close()

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

                    prefsNotesList.edit().putInt("notesList",holder.absoluteAdapterPosition).apply()

                    intentNotesActivity.putExtra("uuid", notes.uuid)
                    intentNotesActivity.putExtra("title", notes.title)
                    intentNotesActivity.putExtra("content", notes.content)
                    intentNotesActivity.putExtra("color", notes.color)
                    intentNotesActivity.putExtra("createDate",notes.createDate.toString())
                    intentNotesActivity.putExtra("updateDate",notes.updateDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                    intentNotesActivity.putExtra("mode","1")
                    notesactivityLauncher.launch(intentNotesActivity)
                }else{
                    val pos : Int = holder.notesCheckBox.getTag() as Int
                    Log.d(TAG, "pos :"+pos)
                    Log.d(TAG, "position: "+holder.absoluteAdapterPosition)

                    if (checkBoxStateList.get(pos).get_Checked()) {
                        checkBoxStateList.get(pos).set_Checked(false);
                        checkBoxList.remove(pos)
                        notesList[pos].isChecked = false
                        db.execSQL("update Notes set isChecked = ? where uuid = ?", arrayOf(false,notesList[pos].uuid))
                        holder.notesCheckBox.setChecked(false)
                    } else {
                        checkBoxStateList.get(pos).set_Checked(true);
                        checkBoxList.add(pos)
                        notesList[pos].isChecked = true
                        db.execSQL("update Notes set isChecked = ? where uuid = ?", arrayOf(true,notesList[pos].uuid))
                        holder.notesCheckBox.setChecked(true)
                    }
                    hide_toolbar.setTitle("已選${checkBoxList.size}項")
                }
            }

            holder.itemView.setOnLongClickListener {
                if(!inDeletionMode){
                    inDeletionMode = true
                    toolbar.setVisibility(View.GONE)
                    hide_toolbar.setVisibility(View.VISIBLE)
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

            holder.notesTitle.text = notes.title

            if(prefsSortData.getString("sortDataDefault","sortBy_updateDate") == "sortBy_createDate"){
                holder.notesDate.text = notes.createDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                Log.d(TAG, "onBindViewHolder: createDate"+notes.createDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
            }else{
                holder.notesDate.text = notes.updateDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                Log.d(TAG, "onBindViewHolder: updateDate")
            }

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

            hide_toolbar.setTitle("已選${checkBoxList.size}項")

            if(inDeletionMode){
                holder.notesCheckBox.setVisibility(View.VISIBLE)

                holder.notesCheckBox.setChecked(checkBoxStateList.get(position).get_Checked()) //RecyclerView+checkBox 上下滑動(好像有bug) checkBox點選打勾圖示會亂跳
                holder.notesCheckBox.setTag(position)

                holder.notesCheckBox.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View) {
                        val pos : Int = holder.notesCheckBox.getTag() as Int //pos == position
                        Log.d(TAG, "pos :"+pos)
                        Log.d(TAG, "position: "+holder.absoluteAdapterPosition)

                        if (checkBoxStateList.get(pos).get_Checked()) {
                            checkBoxStateList.get(pos).set_Checked(false);
                            checkBoxList.remove(pos)
                            notesList[pos].isChecked = false
                            db.execSQL("update Notes set isChecked = ? where uuid = ?", arrayOf(false,notesList[pos].uuid))
                        } else {
                            checkBoxStateList.get(pos).set_Checked(true);
                            checkBoxList.add(pos)
                            notesList[pos].isChecked = true
                            db.execSQL("update Notes set isChecked = ? where uuid = ?", arrayOf(true,notesList[pos].uuid))
                        }

                        hide_toolbar.setTitle("已選${checkBoxList.size}項")
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
        checkBoxList.clear()
        checkBoxStateList.clear()
    }

}
