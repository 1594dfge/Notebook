package com.example.notebook

import androidx.recyclerview.widget.RecyclerView

interface ItemTouchHelperAdapter {
    fun onItemMove(source: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder)

    fun onItemDissmiss(source: RecyclerView.ViewHolder)

    fun onItemSelect(source: RecyclerView.ViewHolder)

    fun onItemClear(source: RecyclerView.ViewHolder)
}