package com.example.notebook

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Collections

class SelectColorFragment() : BottomSheetDialogFragment() {
    lateinit var filterTxv: TextView
    lateinit var edit_finish: Button
    lateinit var topLinearLayout: LinearLayout // isActivity == "NotesActivity" 就將 topLinearLayout gone
    lateinit var radioButton0: RadioButton
    lateinit var radioButton1: RadioButton
    lateinit var radioButton2: RadioButton
    lateinit var radioButton3: RadioButton
    lateinit var radioButton4: RadioButton
    lateinit var colorRadioGroup: LinearLayout //使用RadioGroup會有問題 可能是手動跟自動設定RadioButton造成的錯誤
    lateinit var colorsRecyclerView: RecyclerView

    lateinit var imm : InputMethodManager

    var colorsList = ArrayList<Colors>()
    lateinit var prefsColors: SharedPreferences
    lateinit var prefsRadioButtons: SharedPreferences
    lateinit var colorDefaultMode: String //這裡是 選擇呈現甚麼顏色的資料 green、yellow、blue、red、allcolor(呈現所有顏色)
    lateinit var notesUpdateColor: String
    lateinit var colorDefault: String

    lateinit var isActivity: String

    private val TAG = "testsss"

    interface RadioButtonListener{
        fun sendValue(value: String)
    }

    lateinit var radioButtonListener:RadioButtonListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        radioButtonListener = context as RadioButtonListener

        prefsColors = context.getSharedPreferences("colors", Context.MODE_PRIVATE)
        prefsRadioButtons = context.getSharedPreferences("radioButtons", Context.MODE_PRIVATE)
        colorDefaultMode = prefsColors.getString("colorDefaultMode","allcolor").toString()
        notesUpdateColor = prefsColors.getString("notesUpdateColor","green").toString()
        colorDefault = prefsColors.getString("colorDefault","green").toString()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_select_color, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "activity "+activity.toString())
        Log.d(TAG, "context: "+context.toString().split(".")[3].split("@")[0])
        if(context.toString().split(".")[3].split("@")[0] == "MainActivity"){
            isActivity = "MainActivity"
            Log.d(TAG, "context = $isActivity")
        }else if(context.toString().split(".")[3].split("@")[0] == "NotesActivity"){
            isActivity = "NotesActivity"
            Log.d(TAG, "context = $isActivity")
        }

        filterTxv=view.findViewById(R.id.filterTxv)
        edit_finish=view.findViewById(R.id.edit_finish)
        topLinearLayout=view.findViewById(R.id.topLinearLayout)
        radioButton0=view.findViewById(R.id.radioButton0)
        radioButton1=view.findViewById(R.id.radioButton1)
        radioButton2=view.findViewById(R.id.radioButton2)
        radioButton3=view.findViewById(R.id.radioButton3)
        radioButton4=view.findViewById(R.id.radioButton4)
        colorRadioGroup=view.findViewById(R.id.colorRadioGroup)
        colorsRecyclerView=view.findViewById(R.id.colorsRecyclerView)

        imm= context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        var draw1 = prefsColors.let { context?.getDrawable(it.getInt("colorDraw1",R.drawable.baseline_circle_green_24)) }
        var draw2 = prefsColors.let { context?.getDrawable(it.getInt("colorDraw2",R.drawable.baseline_circle_yellow_24)) }
        var draw3 = prefsColors.let { context?.getDrawable(it.getInt("colorDraw3",R.drawable.baseline_circle_blue_24)) }
        var draw4 = prefsColors.let { context?.getDrawable(it.getInt("colorDraw4",R.drawable.baseline_circle_red_24)) }

        draw1?.setBounds(0,0,100,100)
        draw2?.setBounds(0,0,100,100)
        draw3?.setBounds(0,0,100,100)
        draw4?.setBounds(0,0,100,100)

        radioButton1.setCompoundDrawables(draw1,null,null,null)
        radioButton2.setCompoundDrawables(draw2,null,null,null)
        radioButton3.setCompoundDrawables(draw3,null,null,null)
        radioButton4.setCompoundDrawables(draw4,null,null,null)

        radioButton1.setText(prefsColors.getString("colorContent1","工作"))
        radioButton2.setText(prefsColors.getString("colorContent2","個人"))
        radioButton3.setText(prefsColors.getString("colorContent3","其他"))
        radioButton4.setText(prefsColors.getString("colorContent4",""))

        // isActivity == "MainActivity"
        colorsList.add(Colors(
            prefsColors.getString("color1","green"), prefsColors.getInt("colorDraw1",R.drawable.baseline_circle_green_24)
            , prefsColors.getString("colorContent1","工作"),4,"radioButton1"))
        colorsList.add(Colors(
            prefsColors.getString("color2","yellow"), prefsColors.getInt("colorDraw2",R.drawable.baseline_circle_yellow_24)
            , prefsColors.getString("colorContent2","個人"),3,"radioButton2"))
        colorsList.add(Colors(
            prefsColors.getString("color3","blue"), prefsColors.getInt("colorDraw3",R.drawable.baseline_circle_blue_24)
            , prefsColors.getString("colorContent3","其他"),2,"radioButton3"))
        colorsList.add(Colors(
            prefsColors.getString("color4","red"), prefsColors.getInt("colorDraw4",R.drawable.baseline_circle_red_24)
            , prefsColors.getString("colorContent4",""),1,"radioButton4"))

        // isActivity == "MainActivity"
        val layoutManager = LinearLayoutManager(context)
        colorsRecyclerView.layoutManager = layoutManager
        val adapter = ColorsAdapter(colorsList)
        colorsRecyclerView.adapter = adapter
        val callback: ItemTouchHelper.Callback = SimpleItemTouchHelperCallback(adapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(colorsRecyclerView)

        val editorRadioButtons = prefsRadioButtons.edit()
        editorRadioButtons.putString(prefsColors.getString("color1","green"),"radioButton1")
        editorRadioButtons.putString(prefsColors.getString("color2","yellow"),"radioButton2")
        editorRadioButtons.putString(prefsColors.getString("color3","blue"),"radioButton3")
        editorRadioButtons.putString(prefsColors.getString("color4","red"),"radioButton4")
        editorRadioButtons.apply()

        if(isActivity == "MainActivity"){
            selectColorDefaultMode(colorDefaultMode)
        }else if(isActivity == "NotesActivity"){
            selectColorDefaultMode(notesUpdateColor)
            topLinearLayout.setVisibility(View.GONE)
            radioButton0.setVisibility(View.GONE)
        }

        radioButton1.setOnClickListener {
            radioButton1.setChecked(true)
            radioButton2.setChecked(false)
            radioButton3.setChecked(false)
            radioButton4.setChecked(false)
            radioButton0.setChecked(false)

            val color = prefsColors.getString("color1","green")
            val editor = prefsColors.edit()
            if(isActivity == "MainActivity"){
                if (editor != null) {
                    editor.putString("colorDefaultMode",color)
                    editor.apply()
                }
                colorDefaultMode = prefsColors.getString("colorDefaultMode","allcolor").toString()

                radioButtonListener.sendValue(colorDefaultMode)
            }else if(isActivity == "NotesActivity"){
                if (editor != null) {
                    editor.putString("colorDefault",color)
                    editor.apply()
                }
                colorDefault = prefsColors.getString("colorDefault","green").toString()

                radioButtonListener.sendValue(colorDefault)
            }
        }

        radioButton2.setOnClickListener {
            radioButton1.setChecked(false)
            radioButton2.setChecked(true)
            radioButton3.setChecked(false)
            radioButton4.setChecked(false)
            radioButton0.setChecked(false)

            val color = prefsColors.getString("color2","yellow")
            val editor = prefsColors.edit()
            if(isActivity == "MainActivity"){
                if (editor != null) {
                    editor.putString("colorDefaultMode",color)
                    editor.apply()
                }
                colorDefaultMode = prefsColors.getString("colorDefaultMode","allcolor").toString()

                radioButtonListener.sendValue(colorDefaultMode)
            }else if(isActivity == "NotesActivity"){
                if (editor != null) {
                    editor.putString("colorDefault",color)
                    editor.apply()
                }
                colorDefault = prefsColors.getString("colorDefault","green").toString()

                radioButtonListener.sendValue(colorDefault)
            }
        }

        radioButton3.setOnClickListener {
            radioButton1.setChecked(false)
            radioButton2.setChecked(false)
            radioButton3.setChecked(true)
            radioButton4.setChecked(false)
            radioButton0.setChecked(false)

            val color = prefsColors.getString("color3","blue")
            val editor = prefsColors.edit()
            if(isActivity == "MainActivity"){
                if (editor != null) {
                    editor.putString("colorDefaultMode",color)
                    editor.apply()
                }
                colorDefaultMode = prefsColors.getString("colorDefaultMode","allcolor").toString()

                radioButtonListener.sendValue(colorDefaultMode)
            }else if(isActivity == "NotesActivity"){
                if (editor != null) {
                    editor.putString("colorDefault",color)
                    editor.apply()
                }
                colorDefault = prefsColors.getString("colorDefault","green").toString()

                radioButtonListener.sendValue(colorDefault)
            }
        }

        radioButton4.setOnClickListener {
            radioButton1.setChecked(false)
            radioButton2.setChecked(false)
            radioButton3.setChecked(false)
            radioButton4.setChecked(true)
            radioButton0.setChecked(false)

            val color = prefsColors.getString("color4","red")
            val editor = prefsColors.edit()
            if(isActivity == "MainActivity"){
                if (editor != null) {
                    editor.putString("colorDefaultMode",color)
                    editor.apply()
                }
                colorDefaultMode = prefsColors.getString("colorDefaultMode","allcolor").toString()

                radioButtonListener.sendValue(colorDefaultMode)
            }else if(isActivity == "NotesActivity"){
                if (editor != null) {
                    editor.putString("colorDefault",color)
                    editor.apply()
                }
                colorDefault = prefsColors.getString("colorDefault","green").toString()

                radioButtonListener.sendValue(colorDefault)
            }
        }

        radioButton0.setOnClickListener {
            radioButton1.setChecked(false)
            radioButton2.setChecked(false)
            radioButton3.setChecked(false)
            radioButton4.setChecked(false)
            radioButton0.setChecked(true)

            val color = prefsColors.getString("color0","allcolor") //好像可以直接 val color = "allcolor"
            val editor = prefsColors.edit()
            if(isActivity == "MainActivity"){
                if (editor != null) {
                    editor.putString("colorDefaultMode",color)
                    editor.apply()
                }
                colorDefaultMode = prefsColors.getString("colorDefaultMode","allcolor").toString()

                radioButtonListener.sendValue(colorDefaultMode)
            }
        }



        edit_finish.setOnClickListener {
            if(edit_finish.text.toString() == "編輯"){
                colorRadioGroup.setVisibility(View.GONE)
                colorsRecyclerView.setVisibility(View.VISIBLE)
                filterTxv.setText("顏色")
                edit_finish.setText("完成")
            }else if(edit_finish.text.toString() == "完成"){
                colorRadioGroup.setVisibility(View.VISIBLE)
                colorsRecyclerView.setVisibility(View.GONE)
                filterTxv.setText("篩選")
                edit_finish.setText("編輯")

                val editorColors = prefsColors.edit()

                if (editorColors != null) {
                    editorColors.putString("color1",adapter.colorsList[0].color)
                    editorColors.putString("color2",adapter.colorsList[1].color)
                    editorColors.putString("color3",adapter.colorsList[2].color)
                    editorColors.putString("color4",adapter.colorsList[3].color)

                    adapter.colorsList[0].colorDraw?.let { it1 -> editorColors.putInt("colorDraw1", it1) }
                    adapter.colorsList[1].colorDraw?.let { it2 -> editorColors.putInt("colorDraw2", it2) }
                    adapter.colorsList[2].colorDraw?.let { it3 -> editorColors.putInt("colorDraw3", it3) }
                    adapter.colorsList[3].colorDraw?.let { it4 -> editorColors.putInt("colorDraw4", it4) }

                    editorColors.putString("colorContent1",adapter.colorsList[0].colorContent)
                    editorColors.putString("colorContent2",adapter.colorsList[1].colorContent)
                    editorColors.putString("colorContent3",adapter.colorsList[2].colorContent)
                    editorColors.putString("colorContent4",adapter.colorsList[3].colorContent)

                    editorColors.putInt(adapter.colorsList[0].color,4) //顏色 對 顏色等級 colorsRecyclerView由上到下 最上面等級最高 最下面等級最低
                    editorColors.putInt(adapter.colorsList[1].color,3)
                    editorColors.putInt(adapter.colorsList[2].color,2)
                    editorColors.putInt(adapter.colorsList[3].color,1)

                    editorColors.apply()
                }

                //val editorRadioButtons = prefsRadioButtons.edit() //改名測試(OK) editor2 -> editorRadioButtons
                if (editorRadioButtons != null) {
                    editorRadioButtons.putString(adapter.colorsList[0].color,"radioButton1")
                    editorRadioButtons.putString(adapter.colorsList[1].color,"radioButton2")
                    editorRadioButtons.putString(adapter.colorsList[2].color,"radioButton3")
                    editorRadioButtons.putString(adapter.colorsList[3].color,"radioButton4")
                    editorRadioButtons.apply()
                }

                draw1 = prefsColors.let { it1 -> context?.getDrawable(it1.getInt("colorDraw1",R.drawable.baseline_circle_green_24)) }
                draw2 = prefsColors.let { it1 -> context?.getDrawable(it1.getInt("colorDraw2",R.drawable.baseline_circle_yellow_24)) }
                draw3 = prefsColors.let { it1 -> context?.getDrawable(it1.getInt("colorDraw3",R.drawable.baseline_circle_blue_24)) }
                draw4 = prefsColors.let { it1 -> context?.getDrawable(it1.getInt("colorDraw4",R.drawable.baseline_circle_red_24)) }

                draw1?.setBounds(0,0,100,100)
                draw2?.setBounds(0,0,100,100)
                draw3?.setBounds(0,0,100,100)
                draw4?.setBounds(0,0,100,100)

                radioButton1.setCompoundDrawables(draw1,null,null,null)
                radioButton2.setCompoundDrawables(draw2,null,null,null)
                radioButton3.setCompoundDrawables(draw3,null,null,null)
                radioButton4.setCompoundDrawables(draw4,null,null,null)

                radioButton1.setText(prefsColors.getString("colorContent1","工作"))
                radioButton2.setText(prefsColors.getString("colorContent2","個人"))
                radioButton3.setText(prefsColors.getString("colorContent3","其他"))
                radioButton4.setText(prefsColors.getString("colorContent4",""))

                colorDefaultMode = prefsColors.getString("colorDefaultMode","allcolor").toString()
                selectColorDefaultMode(colorDefaultMode)

                Log.d(TAG, "green =" + (prefsColors.getInt("green",0) ?: 0) + " yellow =" + (prefsColors.getInt("yellow",0) ?: 0) +
                        " blue =" + (prefsColors.getInt("blue",0) ?: 0) + " red =" + (prefsColors.getInt("red",0) ?: 0))

                imm.hideSoftInputFromWindow(edit_finish.windowToken,0)

                radioButtonListener.sendValue("edit_color_finish")
            } //edit_finish.text.toString() == "完成"
        } //edit_finish.setOnClickListener
    } //onViewCreated

    fun selectColorDefaultMode(default: String?){ //selectColorDefault
        val radioButtonName =
            prefsRadioButtons.getString(default,"radioButton0") //顏色 對 radioButton位置
        if(radioButtonName == "radioButton1"){
            Thread.sleep(50)
            radioButton1.setChecked(true)
            radioButton2.setChecked(false)
            radioButton3.setChecked(false)
            radioButton4.setChecked(false)
            radioButton0.setChecked(false)
        }else if(radioButtonName == "radioButton2"){
            Thread.sleep(50)
            radioButton1.setChecked(false)
            radioButton2.setChecked(true)
            radioButton3.setChecked(false)
            radioButton4.setChecked(false)
            radioButton0.setChecked(false)
        }else if(radioButtonName == "radioButton3"){
            Thread.sleep(50)
            radioButton1.setChecked(false)
            radioButton2.setChecked(false)
            radioButton3.setChecked(true)
            radioButton4.setChecked(false)
            radioButton0.setChecked(false)
        }else if(radioButtonName == "radioButton4"){
            Thread.sleep(50)
            radioButton1.setChecked(false)
            radioButton2.setChecked(false)
            radioButton3.setChecked(false)
            radioButton4.setChecked(true)
            radioButton0.setChecked(false)
        }else if(radioButtonName == "radioButton0"){
            Thread.sleep(50)
            radioButton1.setChecked(false)
            radioButton2.setChecked(false)
            radioButton3.setChecked(false)
            radioButton4.setChecked(false)
            radioButton0.setChecked(true)
        }
    }

    inner class ColorsAdapter(val colorsList: List<Colors>) : RecyclerView.Adapter<ColorsAdapter.ViewHolder>(),  ItemTouchHelperAdapter{

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val colorsImageView: ImageView = view.findViewById(R.id.colorsImageView)
            val colorsEditText: EditText = view.findViewById(R.id.colorsEditText)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.colors_item, parent, false)
            val viewHolder = ViewHolder(view)

            return viewHolder
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val colors = colorsList[position]
            holder.colorsImageView.setImageDrawable(colors.colorDraw?.let { context?.getDrawable(it) })
            holder.colorsEditText.setText(colors.colorContent)

            holder.colorsEditText.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    colorsList[holder.absoluteAdapterPosition].colorContent = s.toString()
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })
        }

        override fun getItemCount() = colorsList.size



        override fun onItemMove(source: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) {
            val fromPosition = source.absoluteAdapterPosition
            val toPosition = target.absoluteAdapterPosition

            Collections.swap(colorsList, fromPosition, toPosition)

            notifyItemMoved(fromPosition, toPosition)
        }

//        override fun onItemDissmiss(source: RecyclerView.ViewHolder) {
//
//        }
//
//        override fun onItemSelect(source: RecyclerView.ViewHolder) {
//
//        }
//
//        override fun onItemClear(source: RecyclerView.ViewHolder) {
//
//        }

    }

}