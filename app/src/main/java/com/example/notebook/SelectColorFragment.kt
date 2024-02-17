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
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Collections

class SelectColorFragment(context: Context) : BottomSheetDialogFragment() {

    lateinit var radioButton1: RadioButton
    lateinit var radioButton2: RadioButton
    lateinit var radioButton3: RadioButton
    lateinit var radioButton4: RadioButton
    lateinit var txv: TextView
    lateinit var edit_finish: Button
    lateinit var colorRadioGroup: RadioGroup
    lateinit var colorsRecyclerView: RecyclerView

    lateinit var imm : InputMethodManager

    val prefs = context.getSharedPreferences("colors", Context.MODE_PRIVATE)
    var colorsList = ArrayList<Colors>()

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

        radioButton1=view.findViewById(R.id.radioButton1)
        radioButton2=view.findViewById(R.id.radioButton2)
        radioButton3=view.findViewById(R.id.radioButton3)
        radioButton4=view.findViewById(R.id.radioButton4)
        txv=view.findViewById(R.id.txv)
        edit_finish=view.findViewById(R.id.edit_finish)
        colorRadioGroup=view.findViewById(R.id.colorRadioGroup)
        colorsRecyclerView=view.findViewById<RecyclerView>(R.id.colorsRecyclerView)

        val draw1 = context?.getDrawable(prefs.getInt("11",R.drawable.baseline_circle_green_24))
        val draw2 = context?.getDrawable(prefs.getInt("22",R.drawable.baseline_circle_yellow_24))
        val draw3 = context?.getDrawable(prefs.getInt("33",R.drawable.baseline_circle_blue_24))
        val draw4 = context?.getDrawable(prefs.getInt("44",R.drawable.baseline_circle_red_24))

        draw1?.setBounds(0,0,100,100)
        draw2?.setBounds(0,0,100,100)
        draw3?.setBounds(0,0,100,100)
        draw4?.setBounds(0,0,100,100)

        radioButton1.setText(prefs.getString("1","工作"))
        radioButton1.setCompoundDrawables(draw1,null,null,null)
        radioButton2.setText(prefs.getString("2","個人"))
        radioButton2.setCompoundDrawables(draw2,null,null,null)
        radioButton3.setText(prefs.getString("3","其他"))
        radioButton3.setCompoundDrawables(draw3,null,null,null)
        radioButton4.setText(prefs.getString("4",""))
        radioButton4.setCompoundDrawables(draw4,null,null,null)

        colorsList.add(Colors(prefs.getInt("11",R.drawable.baseline_circle_green_24),prefs.getString("1","工作")))
        colorsList.add(Colors(prefs.getInt("22",R.drawable.baseline_circle_yellow_24),prefs.getString("2","個人")))
        colorsList.add(Colors(prefs.getInt("33",R.drawable.baseline_circle_blue_24),prefs.getString("3","其他")))
        colorsList.add(Colors(prefs.getInt("44",R.drawable.baseline_circle_red_24),prefs.getString("4","")))

        imm= context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        val layoutManager = LinearLayoutManager(context)
        colorsRecyclerView.layoutManager = layoutManager
        val adapter = ColorsAdapter(colorsList)
        colorsRecyclerView.adapter = adapter
        val callback: ItemTouchHelper.Callback = SimpleItemTouchHelperCallback(adapter)
        val touchHelper: ItemTouchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(colorsRecyclerView)



        radioButton1.setOnClickListener {
            radioButtonListener.sendValue("green")
        }

        radioButton2.setOnClickListener {
            radioButtonListener.sendValue("yellow")
        }

        radioButton3.setOnClickListener {
            radioButtonListener.sendValue("blue")
        }

        radioButton4.setOnClickListener {
            radioButtonListener.sendValue("red")
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
                editor.clear()
                editor.putString("1",adapter.colorsList[0].content)
                adapter.colorsList[0].color?.let { it1 -> editor.putInt("11", it1) }
                editor.putString("2",adapter.colorsList[1].content)
                adapter.colorsList[1].color?.let { it2 -> editor.putInt("22", it2) }
                editor.putString("3",adapter.colorsList[2].content)
                adapter.colorsList[2].color?.let { it3 -> editor.putInt("33", it3) }
                editor.putString("4",adapter.colorsList[3].content)
                adapter.colorsList[3].color?.let { it4 -> editor.putInt("44", it4) }
                editor.apply()

                val draw1 = context?.getDrawable(prefs.getInt("11",R.drawable.baseline_circle_green_24))
                val draw2 = context?.getDrawable(prefs.getInt("22",R.drawable.baseline_circle_yellow_24))
                val draw3 = context?.getDrawable(prefs.getInt("33",R.drawable.baseline_circle_blue_24))
                val draw4 = context?.getDrawable(prefs.getInt("44",R.drawable.baseline_circle_red_24))

                draw1?.setBounds(0,0,100,100)
                draw2?.setBounds(0,0,100,100)
                draw3?.setBounds(0,0,100,100)
                draw4?.setBounds(0,0,100,100)

                radioButton1.setText(prefs.getString("1","工作"))
                radioButton1.setCompoundDrawables(draw1,null,null,null)
                radioButton2.setText(prefs.getString("2","個人"))
                radioButton2.setCompoundDrawables(draw2,null,null,null)
                radioButton3.setText(prefs.getString("3","其他"))
                radioButton3.setCompoundDrawables(draw3,null,null,null)
                radioButton4.setText(prefs.getString("4",""))
                radioButton4.setCompoundDrawables(draw4,null,null,null)

                for(i in colorsList){
                    Log.d("testsss", "color "+i.color)
                    Log.d("testsss", "content "+i.content)
                }

                for(i in adapter.colorsList){
                    Log.d("testsss", "recyclerview color "+i.color)
                    Log.d("testsss", "recyclerview content "+i.content)
                }

                imm.hideSoftInputFromWindow(edit_finish.windowToken,0)
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
            holder.colorsImageView.setImageDrawable(colors.color?.let { context?.getDrawable(it) })
            holder.colorsEditText.setText(colors.content)

            holder.colorsEditText.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    colorsList[holder.absoluteAdapterPosition].content = s.toString()
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

        override fun onItemDissmiss(source: RecyclerView.ViewHolder) {
            Log.d("testsssss", "onItemDissmiss: ")
        }

        override fun onItemSelect(source: RecyclerView.ViewHolder) {
            source.itemView.setScaleX(1.2f)
            source.itemView.setScaleY(1.2f)
        }

        override fun onItemClear(source: RecyclerView.ViewHolder) {
            source.itemView.setScaleX(1.0f)
            source.itemView.setScaleY(1.0f)
        }

    }

}