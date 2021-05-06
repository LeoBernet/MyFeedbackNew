package com.android.leobernet.myfeedback.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.leobernet.myfeedback.EditActivity
import com.android.leobernet.myfeedback.R

class SelectCountryAdapter(private val dialog: AlertDialog,val tvText : TextView) : RecyclerView.Adapter<SelectCountryAdapter.ItemHolder>(){
    private val mainArray = ArrayList<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val view =LayoutInflater.from(parent.context).inflate(R.layout.select_country_item,parent,false)
        return ItemHolder(view)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.setData(mainArray[position])
        holder.itemView.setOnClickListener{
            tvText.text = mainArray[position]
            dialog.dismiss()
        }
    }

    override fun getItemCount(): Int {
        return mainArray.size
    }
    class ItemHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

        lateinit var tvTitle : TextView
        fun setData(name : String){
            tvTitle = itemView.findViewById(R.id.tvCountry)
            tvTitle.text = name
        }
    }
    fun update(newList : ArrayList<String>){
        mainArray.clear()
        mainArray.addAll(newList)
        notifyDataSetChanged()
    }
}