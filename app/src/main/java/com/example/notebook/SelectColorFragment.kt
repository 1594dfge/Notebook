package com.example.notebook

import android.content.Context
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

class SelectColorFragment(context: Context) : BottomSheetDialogFragment() {

    lateinit var txv: TextView
    lateinit var edit_finish: Button
    lateinit var radioButton0: RadioButton
    lateinit var radioButton1: RadioButton
    lateinit var radioButton2: RadioButton
    lateinit var radioButton3: RadioButton
    lateinit var radioButton4: RadioButton
    lateinit var colorRadioGroup: LinearLayout //使用RadioGroup會有問題
    lateinit var colorsRecyclerView: RecyclerView

    lateinit var imm : InputMethodManager

    val prefs = context.getSharedPreferences("colors", Context.MODE_PRIVATE)
    val prefs2 = context.getSharedPreferences("radioButtonChecked", Context.MODE_PRIVATE)
    var colorsList = ArrayList<Colors>()
    var colorDefault = prefs.getString("colorDefault","allcolor")

    interface RadioButtonListener{
        fun sendValue(value: String)
    }

    lateinit var radioButtonListener:RadioButtonListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        radioButtonListener = activity as RadioButtonListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_select_color, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txv=view.findViewById(R.id.txv)
        edit_finish=view.findViewById(R.id.edit_finish)
        radioButton0=view.findViewById(R.id.radioButton0)
        radioButton1=view.findViewById(R.id.radioButton1)
        radioButton2=view.findViewById(R.id.radioButton2)
        radioButton3=view.findViewById(R.id.radioButton3)
        radioButton4=view.findViewById(R.id.radioButton4)
        colorRadioGroup=view.findViewById(R.id.colorRadioGroup)
        colorsRecyclerView=view.findViewById<RecyclerView>(R.id.colorsRecyclerView)

        imm= context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        var draw1 = context?.getDrawable(prefs.getInt("colorDraw1",R.drawable.baseline_circle_green_24))
        var draw2 = context?.getDrawable(prefs.getInt("colorDraw2",R.drawable.baseline_circle_yellow_24))
        var draw3 = context?.getDrawable(prefs.getInt("colorDraw3",R.drawable.baseline_circle_blue_24))
        var draw4 = context?.getDrawable(prefs.getInt("colorDraw4",R.drawable.baseline_circle_red_24))

        draw1?.setBounds(0,0,100,100)
        draw2?.setBounds(0,0,100,100)
        draw3?.setBounds(0,0,100,100)
        draw4?.setBounds(0,0,100,100)

        radioButton1.setText(prefs.getString("colorContent1","工作"))
        radioButton1.setCompoundDrawables(draw1,null,null,null)
        radioButton2.setText(prefs.getString("colorContent2","個人"))
        radioButton2.setCompoundDrawables(draw2,null,null,null)
        radioButton3.setText(prefs.getString("colorContent3","其他"))
        radioButton3.setCompoundDrawables(draw3,null,null,null)
        radioButton4.setText(prefs.getString("colorContent4",""))
        radioButton4.setCompoundDrawables(draw4,null,null,null)

        colorsList.add(Colors(prefs.getString("color1","green"),prefs.getInt("colorDraw1",R.drawable.baseline_circle_green_24)
            ,prefs.getString("colorContent1","工作"),4,"radioButton1"))
        colorsList.add(Colors(prefs.getString("color2","yellow"),prefs.getInt("colorDraw2",R.drawable.baseline_circle_yellow_24)
            ,prefs.getString("colorContent2","個人"),3,"radioButton2"))
        colorsList.add(Colors(prefs.getString("color3","blue"),prefs.getInt("colorDraw3",R.drawable.baseline_circle_blue_24)
            ,prefs.getString("colorContent3","其他"),2,"radioButton3"))
        colorsList.add(Colors(prefs.getString("color4","red"),prefs.getInt("colorDraw4",R.drawable.baseline_circle_red_24)
            ,prefs.getString("colorContent4",""),1,"radioButton4"))

        val editor2 = prefs2.edit()
        editor2.putString(prefs.getString("color1","green"),"radioButton1")
        editor2.putString(prefs.getString("color2","yellow"),"radioButton2")
        editor2.putString(prefs.getString("color3","blue"),"radioButton3")
        editor2.putString(prefs.getString("color4","red"),"radioButton4")
        editor2.apply()

        selectColor(colorDefault)

        val layoutManager = LinearLayoutManager(context)
        colorsRecyclerView.layoutManager = layoutManager
        val adapter = ColorsAdapter(colorsList)
        colorsRecyclerView.adapter = adapter
        val callback: ItemTouchHelper.Callback = SimpleItemTouchHelperCallback(adapter)
        val touchHelper: ItemTouchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(colorsRecyclerView)



        radioButton1.setOnClickListener {
            radioButton1.setChecked(true)
            radioButton2.setChecked(false)
            radioButton3.setChecked(false)
            radioButton4.setChecked(false)
            radioButton0.setChecked(false)
            val color = prefs.getString("color1","green")
            if (color != null) {
                radioButtonListener.sendValue(color)
            }
            val editor = prefs.edit()
            editor.putString("colorDefault",color)
            editor.apply()
            colorDefault = prefs.getString("colorDefault","allcolor")
        }

        radioButton2.setOnClickListener {
            radioButton1.setChecked(false)
            radioButton2.setChecked(true)
            radioButton3.setChecked(false)
            radioButton4.setChecked(false)
            radioButton0.setChecked(false)
            val color = prefs.getString("color2","yellow")
            if (color != null) {
                radioButtonListener.sendValue(color)
            }
            val editor = prefs.edit()
            editor.putString("colorDefault",color)
            editor.apply()
            colorDefault = prefs.getString("colorDefault","allcolor")
        }

        radioButton3.setOnClickListener {
            radioButton1.setChecked(false)
            radioButton2.setChecked(false)
            radioButton3.setChecked(true)
            radioButton4.setChecked(false)
            radioButton0.setChecked(false)
            val color = prefs.getString("color3","blue")
            if (color != null) {
                radioButtonListener.sendValue(color)
            }
            val editor = prefs.edit()
            editor.putString("colorDefault",color)
            editor.apply()
            colorDefault = prefs.getString("colorDefault","allcolor")
        }

        radioButton4.setOnClickListener {
            radioButton1.setChecked(false)
            radioButton2.setChecked(false)
            radioButton3.setChecked(false)
            radioButton4.setChecked(true)
            radioButton0.setChecked(false)
            val color = prefs.getString("color4","red")
            if (color != null) {
                radioButtonListener.sendValue(color)
            }
            val editor = prefs.edit()
            editor.putString("colorDefault",color)
            editor.apply()
            colorDefault = prefs.getString("colorDefault","allcolor")
        }

        radioButton0.setOnClickListener {
            radioButton1.setChecked(false)
            radioButton2.setChecked(false)
            radioButton3.setChecked(false)
            radioButton4.setChecked(false)
            radioButton0.setChecked(true)
            val color = prefs.getString("color0","allcolor")
            if (color != null) {
                radioButtonListener.sendValue(color)
            }
            val editor = prefs.edit()
            editor.putString("colorDefault",color)
            editor.apply()
            colorDefault = prefs.getString("colorDefault","allcolor")
        }

        edit_finish.setOnClickListener {
            if(edit_finish.text.toString() == "編輯"){
                colorRadioGroup.setVisibility(View.GONE)
                colorsRecyclerView.setVisibility(View.VISIBLE)
                txv.setText("顏色")
                edit_finish.setText("完成")
            }else if(edit_finish.text.toString() == "完成"){
                colorRadioGroup.setVisibility(View.VISIBLE)
                colorsRecyclerView.setVisibility(View.GONE)
                txv.setText("篩選")
                edit_finish.setText("編輯")

                val editor = prefs.edit()

                editor.putString("color1",adapter.colorsList[0].color)
                editor.putString("color2",adapter.colorsList[1].color)
                editor.putString("color3",adapter.colorsList[2].color)
                editor.putString("color4",adapter.colorsList[3].color)

                editor.putString("colorContent1",adapter.colorsList[0].colorContent)
                adapter.colorsList[0].colorDraw?.let { it -> editor.putInt("colorDraw1", it) }
                editor.putString("colorContent2",adapter.colorsList[1].colorContent)
                adapter.colorsList[1].colorDraw?.let { it -> editor.putInt("colorDraw2", it) }
                editor.putString("colorContent3",adapter.colorsList[2].colorContent)
                adapter.colorsList[2].colorDraw?.let { it -> editor.putInt("colorDraw3", it) }
                editor.putString("colorContent4",adapter.colorsList[3].colorContent)
                adapter.colorsList[3].colorDraw?.let { it -> editor.putInt("colorDraw4", it) }

                editor.putInt(adapter.colorsList[0].color,4)
                editor.putInt(adapter.colorsList[1].color,3)
                editor.putInt(adapter.colorsList[2].color,2)
                editor.putInt(adapter.colorsList[3].color,1)

                editor.apply()

                val editor2 = prefs2.edit()
                editor2.putString(adapter.colorsList[0].color,"radioButton1")
                editor2.putString(adapter.colorsList[1].color,"radioButton2")
                editor2.putString(adapter.colorsList[2].color,"radioButton3")
                editor2.putString(adapter.colorsList[3].color,"radioButton4")
                editor2.apply()

                draw1 = context?.getDrawable(prefs.getInt("colorDraw1",R.drawable.baseline_circle_green_24))
                draw2 = context?.getDrawable(prefs.getInt("colorDraw2",R.drawable.baseline_circle_yellow_24))
                draw3 = context?.getDrawable(prefs.getInt("colorDraw3",R.drawable.baseline_circle_blue_24))
                draw4 = context?.getDrawable(prefs.getInt("colorDraw4",R.drawable.baseline_circle_red_24))

                draw1?.setBounds(0,0,100,100)
                draw2?.setBounds(0,0,100,100)
                draw3?.setBounds(0,0,100,100)
                draw4?.setBounds(0,0,100,100)

                radioButton1.setText(prefs.getString("colorContent1","工作"))
                radioButton1.setCompoundDrawables(draw1,null,null,null)
                radioButton2.setText(prefs.getString("colorContent2","個人"))
                radioButton2.setCompoundDrawables(draw2,null,null,null)
                radioButton3.setText(prefs.getString("colorContent3","其他"))
                radioButton3.setCompoundDrawables(draw3,null,null,null)
                radioButton4.setText(prefs.getString("colorContent4",""))
                radioButton4.setCompoundDrawables(draw4,null,null,null)

                selectColor(colorDefault)

                Log.d("testsss", "green ="+prefs.getInt("green",0)+" yellow ="+prefs.getInt("yellow",0)+" blue ="+prefs.getInt("blue",0)+" red ="+prefs.getInt("red",0))

                imm.hideSoftInputFromWindow(edit_finish.windowToken,0)
            }

        }

    }

    fun selectColor(default: String?){
        if(colorDefault == default){
            val radioButtonName = prefs2.getString(default,"radioButton0")
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