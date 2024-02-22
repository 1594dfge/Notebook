package com.example.notebook

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SortDataFragment  : BottomSheetDialogFragment() {

    lateinit var sortBy_updateDate: RadioButton
    lateinit var sortBy_createDate: RadioButton
    lateinit var sortBy_color: RadioButton

    lateinit var prefsSortData: SharedPreferences
    lateinit var sortDataDefault: String

    interface RadioButtonListener{
        fun sendValue(value: String)
    }

    lateinit var radioButtonListener:RadioButtonListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        radioButtonListener = context as RadioButtonListener

        prefsSortData = context.getSharedPreferences("sortData", Context.MODE_PRIVATE)
        sortDataDefault = prefsSortData.getString("sortDataDefault","sortBy_updateDate").toString()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sort_data, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sortBy_updateDate=view.findViewById(R.id.sortBy_updateDate)
        sortBy_createDate=view.findViewById(R.id.sortBy_createDate)
        sortBy_color=view.findViewById(R.id.sortBy_color)

        selectSortDataDefault(sortDataDefault)

        sortBy_updateDate.setOnClickListener {
            sortBy_updateDate.setChecked(true)
            sortBy_createDate.setChecked(false)
            sortBy_color.setChecked(false)

            val editor = prefsSortData.edit()
            editor.putString("sortDataDefault","sortBy_updateDate")
            editor.apply()

            sortDataDefault = prefsSortData.getString("sortDataDefault","sortBy_updateDate").toString()

            radioButtonListener.sendValue(sortDataDefault)
        }

        sortBy_createDate.setOnClickListener {
            sortBy_updateDate.setChecked(false)
            sortBy_createDate.setChecked(true)
            sortBy_color.setChecked(false)

            val editor = prefsSortData.edit()
            editor.putString("sortDataDefault","sortBy_createDate")
            editor.apply()

            sortDataDefault = prefsSortData.getString("sortDataDefault","sortBy_createDate").toString()

            radioButtonListener.sendValue(sortDataDefault)
        }

        sortBy_color.setOnClickListener {
            sortBy_updateDate.setChecked(false)
            sortBy_createDate.setChecked(false)
            sortBy_color.setChecked(true)

            val editor = prefsSortData.edit()
            editor.putString("sortDataDefault","sortBy_color")
            editor.apply()

            sortDataDefault = prefsSortData.getString("sortDataDefault","sortBy_color").toString()

            radioButtonListener.sendValue(sortDataDefault)
        }
    } //onViewCreated

    fun selectSortDataDefault(default: String?){
        if(default == "sortBy_updateDate"){
            Thread.sleep(50)
            sortBy_updateDate.setChecked(true)
            sortBy_createDate.setChecked(false)
            sortBy_color.setChecked(false)
        }else if(default == "sortBy_createDate"){
            Thread.sleep(50)
            sortBy_updateDate.setChecked(false)
            sortBy_createDate.setChecked(true)
            sortBy_color.setChecked(false)
        }else if(default == "sortBy_color"){
            Thread.sleep(50)
            sortBy_updateDate.setChecked(false)
            sortBy_createDate.setChecked(false)
            sortBy_color.setChecked(true)
        }
    }

}