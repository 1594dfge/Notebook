package com.example.notebook

class checkBoxState(val state: Boolean) {
    var isChecked = state

    fun get_Checked() :Boolean{
        return isChecked
    }

    fun set_Checked(selected : Boolean){
        isChecked = selected
    }
}