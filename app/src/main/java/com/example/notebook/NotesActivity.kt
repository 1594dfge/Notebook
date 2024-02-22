package com.example.notebook

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class NotesActivity : AppCompatActivity(), SelectColorFragment.RadioButtonListener {
    //為什麼使用List，是因為要實現 上一筆內容 下一筆內容 的功能
    //判斷title跟content是否有做更改
    //可能情況 title 沒有 content 沒有   不儲存
    //        title 沒有 content 有    儲存
    //        title 有   content 沒有  儲存
    //        title 有   content 有    儲存
    var titleList = ArrayList<String>()
    val contentList = ArrayList<String>()
    lateinit var titleString : String
    lateinit var contentString : String

    //新增資料 創建新的uuid 更改資料 使用原本的uuid
    lateinit var uuid : String

    //使用 SharedPreferences 文件名:colors 裡的colorDefault預設顏色
    lateinit var color : String
    lateinit var prefsColors : SharedPreferences
    lateinit var colorString : String

    lateinit var createDate : String
    lateinit var updateDate : String

    lateinit var title : EditText
    lateinit var content : EditText
    lateinit var latestUpateDate : TextView
    lateinit var topToolbar : Toolbar
    lateinit var bottomToolber : Toolbar

    //控制鍵盤顯示(顯示 不顯示)
    lateinit var imm : InputMethodManager

    val dbHelper = NotesDatabaseHelper(this, "NotesStore.db", 1)
    lateinit var db : SQLiteDatabase

    private val TAG="testsss"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)
        Log.d(TAG, "onCreate: ")

        title = findViewById(R.id.title)
        content = findViewById(R.id.content)
        latestUpateDate = findViewById(R.id.latestUpateDate)
        topToolbar = findViewById(R.id.notesTopToolbar)
        bottomToolber = findViewById(R.id.notesBottomToolbar)

        //MaicActivity會有兩種方式 開啟NotesActivity
        //1.新增(按一下FloatingActionButton(@id/add_button)) uuid title content 都為 ""
        //2.更改(按一下RecyclerView裡的item)
        uuid = intent.getStringExtra("uuid").toString()
        title.setText(intent.getStringExtra("title"))
        content.setText(intent.getStringExtra("content"))
        color = intent.getStringExtra("color").toString()
        createDate = intent.getStringExtra("createDate").toString()
        updateDate = intent.getStringExtra("updateDate").toString()
        latestUpateDate.setText("最後編輯:"+updateDate)

        titleList.add(title.text.toString())
        contentList.add(content.text.toString())
        titleString = title.text.toString()
        contentString = content.text.toString()
        colorString = color

        prefsColors = this.getSharedPreferences("colors", Context.MODE_PRIVATE)
        prefsColors.edit().putString("notesUpdateColor",color).apply()

        if(color == "green"){
            topToolbar.menu.getItem(0).setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_circle_green_24))
        }else if(color == "yellow"){
            topToolbar.menu.getItem(0).setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_circle_yellow_24))
        }else if(color == "blue"){
            topToolbar.menu.getItem(0).setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_circle_blue_24))
        }else if(color == "red"){
            topToolbar.menu.getItem(0).setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_circle_red_24))
        }

        imm= getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        db = dbHelper.writableDatabase

        //讓content(EditText)獲得焦點 鍵盤顯示(設定在AndroidManifest.xml android:windowSoftInputMode="stateAlwaysVisible")
        content.requestFocus()



        title.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                bottomToolber.setVisibility(View.GONE)
            }
        }
        title.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                titleList.add(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })



        content.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                bottomToolber.setVisibility(View.VISIBLE)
                bottomToolber.menu.getItem(0).setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_check_24))
            }else{
                bottomToolber.menu.getItem(0).setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_create_24))
            }
        }
        content.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                contentList.add(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })



        topToolbar.setNavigationIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_arrow_back_24))
        topToolbar.setNavigationOnClickListener {
            //上下 返回鍵 先setResult(1000000000,intent) 在finish
            if(title.isFocused||content.isFocused){
                title.clearFocus()
                content.clearFocus()
                imm.hideSoftInputFromWindow(content.windowToken,0)
            }else{
                finish()
            }
        }
        topToolbar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.select_color -> {
                    val bottomSheetFragment = SelectColorFragment()
                    bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
                }
            }
            false
        }



        bottomToolber.setOnMenuItemClickListener { item ->
            when(item.itemId){
                R.id.edit_submit -> {
                    if(title.isFocused||content.isFocused){
                        title.clearFocus()
                        content.clearFocus()
                        imm.hideSoftInputFromWindow(content.windowToken,0)

                        db_insert_update()
                    }else{
                        content.requestFocus()
                        imm.showSoftInput(content,0)
                    }
                }
            }
            false
        }
    } //onCreate



    fun db_insert_update(){
        if(title.text.toString()== ""&&content.text.toString()== ""){
            //delete
            if(uuid == ""){

            }else{
                db.execSQL("delete from Notes where uuid = ?", arrayOf(uuid))

                setResult(0)
            }
        }else if(titleString== titleList[titleList.size-1]&&contentString== contentList[contentList.size-1]&&colorString== color){

        }else{
            intent.putExtra("title", title.text.toString())
            intent.putExtra("content", content.text.toString())
            updateDate = LocalDateTime.now().toString()
            latestUpateDate.setText("最後編輯:"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
            Log.d(TAG, "db_insert_update: "+latestUpateDate)
            intent.putExtra("updateDate", updateDate)

            if(uuid == ""){
                Log.d(TAG, "新增資料")

                uuid = UUID.randomUUID().toString()
                createDate = LocalDateTime.now().toString()
                color = prefsColors.getString("colorDefault","green").toString()
            }else{
                Log.d(TAG, "更改資料")

                val values1 = ContentValues().apply {
                    put("title", title.text.toString())
                    put("content", content.text.toString())
                    put("color", color)
                    put("updateDate", updateDate)
                }
                db.update("Notes", values1, "uuid = ?", arrayOf(uuid))
            }
            intent.putExtra("uuid", uuid)
            intent.putExtra("createDate", createDate)
            intent.putExtra("color", color) //如果沒有選擇顏色 那就使用原本發送過來的顏色, 如果有 使用notesTopToolbar選擇的顏色

            setResult(1000000000,intent)

            Toast.makeText(this,"已儲存",Toast.LENGTH_SHORT).show()
        }
        titleString = title.text.toString()
        contentString = content.text.toString()
        colorString = color
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: ")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
    }

    override fun finish() {
        db_insert_update()

        super.finish()
        Log.d(TAG, "finish: ")
    }



    override fun onPause() {
        if(title.text.toString()== ""&&content.text.toString()== ""){
            //delete
            if(uuid == ""){

            }else{
                db.execSQL("delete from Notes where uuid = ?", arrayOf(uuid))
            }
        }else if(titleString== titleList[titleList.size-1]&&contentString== contentList[contentList.size-1]&&colorString== color){

        }else{
            updateDate = LocalDateTime.now().toString()
            latestUpateDate.setText("最後編輯:"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
            if(uuid == ""){
                Log.d(TAG, "新增資料")
                uuid = UUID.randomUUID().toString()
                createDate = LocalDateTime.now().toString()
                color = prefsColors.getString("colorDefault","green").toString()

                val values1 = ContentValues().apply {
                    put("uuid", uuid)
                    put("title", title.text.toString())
                    put("content", content.text.toString())
                    put("color", color)
                    put("createDate", createDate)
                    put("updateDate", updateDate)
                }
                db.insert("Notes", null, values1)
            }else{
                Log.d(TAG, "更改資料")

                val values1 = ContentValues().apply {
                    put("title", title.text.toString())
                    put("content", content.text.toString())
                    put("color", color)
                    put("updateDate", updateDate)
                }
                db.update("Notes", values1, "uuid = ?", arrayOf(uuid))
            }
            Toast.makeText(this,"已儲存",Toast.LENGTH_SHORT).show()
        }
        setResult(0) //在 onPause 裡做setResult不管怎樣都會失敗 發送0 必須在finish之前發送 才會成功
        titleString = title.text.toString()
        contentString = content.text.toString()
        colorString = color

        super.onPause()
        Log.d(TAG, "onPause: ")
    }



    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: ")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy:")

        //List clear
        titleList.clear()
        contentList.clear()
    }

    override fun sendValue(value: String) {
        if(value == "green"){
            color = "green"
            topToolbar.menu.getItem(0).setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_circle_green_24))
        }else if(value == "yellow"){
            color = "yellow"
            topToolbar.menu.getItem(0).setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_circle_yellow_24))
        }else if(value == "blue"){
            color = "blue"
            topToolbar.menu.getItem(0).setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_circle_blue_24))
        }else if(value == "red"){
            color = "red"
            topToolbar.menu.getItem(0).setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_circle_red_24))
        }

        Log.d(TAG, "NotesActivity sendValue: "+value)
    }
}