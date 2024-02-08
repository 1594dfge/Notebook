package com.example.notebook

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar

class NotesActivity : AppCompatActivity() {

    //為什麼使用List，是因為要實現 上一筆內容 下一筆內容 的功能
    var titleList = ArrayList<String>()
    val contentList = ArrayList<String>()
    lateinit var titleString: String
    lateinit var contentString: String
    lateinit var title: EditText
    lateinit var content: EditText
    lateinit var topToolbar: Toolbar
    lateinit var bottomToolber: Toolbar

    private val TAG="testsss"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)
        Log.d(TAG, "onCreate: ")

        title = findViewById(R.id.title)
        content = findViewById(R.id.content)
        topToolbar = findViewById(R.id.notesTopToolbar)
        bottomToolber = findViewById(R.id.notesBottomToolbar)

        titleList.add("")
        contentList.add("")
        titleString = title.text.toString()
        contentString = content.text.toString()

        topToolbar.setNavigationIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_arrow_back_24))

        title.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                titleList.add(s.toString())
                Log.d(TAG, "onTextChanged: "+ s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        content.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                contentList.add(s.toString())
                Log.d(TAG, "onTextChanged: "+ s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        topToolbar.setNavigationOnClickListener {
            finish()
        }

        bottomToolber.setOnMenuItemClickListener { item ->
            when(item.itemId){
                R.id.edit_submit -> {
                    if(titleString== titleList[titleList.size-1]&&contentString== contentList[contentList.size-1]){

                    }else{
                        intent.putExtra("title", title.text.toString())
                        intent.putExtra("content", content.text.toString())
                        setResult(RESULT_OK,intent)
                        Toast.makeText(this,"已儲存",Toast.LENGTH_SHORT).show()
                        titleString = title.text.toString()
                        contentString = content.text.toString()
                    }
                }
            }
            false
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
        // 儲存
        if(titleString== titleList[titleList.size-1]&&contentString== contentList[contentList.size-1]){
            Log.d(TAG, "onPause: if")
        }else{
            Log.d(TAG, "onPause: else start")
            intent.putExtra("title", title.text.toString())
            intent.putExtra("content", content.text.toString())
            setResult(RESULT_OK,intent)
            Toast.makeText(this,"已儲存",Toast.LENGTH_SHORT).show()
            titleString = title.text.toString()
            contentString = content.text.toString()
            //Thread.sleep(1000)
            Log.d(TAG, "onPause: else end")
        }

        super.finish()
        Log.d(TAG, "finish: ")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: ")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: ")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")

        //List clear
        titleList.clear()
        contentList.clear()
    }
}