package com.example.notebook

import android.content.ContentValues
import android.content.Context
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
import java.util.UUID

class NotesActivity : AppCompatActivity() {

    //為什麼使用List，是因為要實現 上一筆內容 下一筆內容 的功能
    var titleList = ArrayList<String>()
    val contentList = ArrayList<String>()
    lateinit var titleString : String
    lateinit var contentString : String
    lateinit var uuid : String
    lateinit var createDate : String
    lateinit var updateDate : String

    lateinit var title : EditText
    lateinit var content : EditText
    lateinit var date : TextView
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
        date = findViewById(R.id.date)
        topToolbar = findViewById(R.id.notesTopToolbar)
        bottomToolber = findViewById(R.id.notesBottomToolbar)

        //MaicActivity會有兩種方式 開啟NotesActivity
        //1.新增(按一下FloatingActionButton(@id/add_button)) uuid title content 都為 ""
        //2.更改(按一下RecyclerView裡的item)
        uuid = intent.getStringExtra("uuid").toString()
        title.setText(intent.getStringExtra("title"))
        content.setText(intent.getStringExtra("content"))
        createDate = intent.getStringExtra("createDate").toString()
        date.setText("最後編輯:"+intent.getStringExtra("updateDate"))
        updateDate = LocalDateTime.now().toString()

        imm= getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        db = dbHelper.writableDatabase

        //讓content(EditText)獲得焦點 鍵盤顯示(設定在AndroidManifest.xml android:windowSoftInputMode="stateAlwaysVisible")
        content.requestFocus()

        titleList.add(title.text.toString())
        contentList.add(content.text.toString())
        titleString = title.text.toString()
        contentString = content.text.toString()

        topToolbar.setNavigationIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_arrow_back_24))

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
            } else{
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

        topToolbar.setNavigationOnClickListener {
            //上下 返回鍵 好像可以先setResult(1000000000,intent) 在finish
            if(title.isFocused||content.isFocused){
                title.clearFocus()
                content.clearFocus()
                imm.hideSoftInputFromWindow(content.windowToken,0)
            }else{
                finish()
            }
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
            true
        }
    }

    fun db_insert_update(){
        if(titleString== titleList[titleList.size-1]&&contentString== contentList[contentList.size-1]){

        }else{
            intent.putExtra("title", title.text.toString())
            intent.putExtra("content", content.text.toString())
            intent.putExtra("updateDate", updateDate)

            if(uuid == ""){
                Log.d(TAG, "onCreate: create")
                uuid = UUID.randomUUID().toString()

                createDate = LocalDateTime.now().toString()

                val values1 = ContentValues().apply {
                    put("uuid", uuid)
                    put("title", title.text.toString())
                    put("content", content.text.toString())
                    //put("colors", )
                    put("createDate", createDate)
                    put("updateDate", updateDate)
                }
                db.insert("Notes", null, values1)
            }else{
                Log.d(TAG, "onCreate: update")

                val values1 = ContentValues().apply {
                    put("title", title.text.toString())
                    put("content", content.text.toString())
                    put("updateDate", updateDate)
                }
                db.update("Notes", values1, "uuid = ?", arrayOf(uuid))
            }
            intent.putExtra("uuid", uuid)
            intent.putExtra("createDate", createDate)
            setResult(1000000000,intent)
            Toast.makeText(this,"已儲存",Toast.LENGTH_SHORT).show()
            titleString = title.text.toString()
            contentString = content.text.toString()
        }
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
        if(titleString== titleList[titleList.size-1]&&contentString== contentList[contentList.size-1]){

        }else{
            if(uuid == ""){
                Log.d(TAG, "onCreate: create")
                uuid = UUID.randomUUID().toString()

                createDate = LocalDateTime.now().toString()

                val values1 = ContentValues().apply {
                    put("uuid", uuid)
                    put("title", title.text.toString())
                    put("content", content.text.toString())
                    put("createDate", createDate)
                    put("updateDate", updateDate)
                }
                db.insert("Notes", null, values1)
            }else{
                Log.d(TAG, "onCreate: update")

                val values1 = ContentValues().apply {
                    put("title", title.text.toString())
                    put("content", content.text.toString())
                    put("updateDate", updateDate)
                }
                db.update("Notes", values1, "uuid = ?", arrayOf(uuid))
            }
            setResult(0) //在 onPause 裡做setResult不管怎樣都會失敗 發送0 必須在finish之前發送 才會成功
            Toast.makeText(this,"已儲存",Toast.LENGTH_SHORT).show()
            titleString = title.text.toString()
            contentString = content.text.toString()
        }

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
}