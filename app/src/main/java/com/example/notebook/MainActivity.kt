package com.example.notebook

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private val notessList = ArrayList<Notes>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val notesRecyclerView=findViewById<RecyclerView>(R.id.notesRecyclerView)
        val add_button: Button = findViewById(R.id.add_button)
        val layoutManager = LinearLayoutManager(this)
        notesRecyclerView.layoutManager = layoutManager

        val adapter = NotessAdapter(notessList)
        notesRecyclerView.adapter = adapter

        add_button.setOnClickListener {
            notessList.add(Notes("abc","123"))
            adapter.notifyDataSetChanged()
        }
//        notessList.add(Notes("a","123"))
//
//        val adapter = NotessAdapter(notessList)
//        notesRecyclerView.adapter = adapter
    }

    inner class NotessAdapter(val notessList: List<Notes>) : RecyclerView.Adapter<NotessAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val newsTitle: TextView = view.findViewById(R.id.notessTitle)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.notes_item, parent, false)
            val holder = ViewHolder(view)
            holder.itemView.setOnClickListener {
                val notes = notessList[holder.adapterPosition]
                //NewsContentActivity.actionStart(parent.context, news.title, news.content);
                Toast.makeText(parent.context,"test",Toast.LENGTH_SHORT).show()
            }
            return holder
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val news = notessList[position]
            holder.newsTitle.text = news.title
        }

        override fun getItemCount() = notessList.size

    }
}