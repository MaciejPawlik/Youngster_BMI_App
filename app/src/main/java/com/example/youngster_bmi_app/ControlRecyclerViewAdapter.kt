package com.example.youngster_bmi_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.youngster_bmi_app.model.ControlCentile
import kotlinx.android.synthetic.main.fragment_control_result.view.*

class ControlRecyclerViewAdapter(
    private val results: List<ControlCentile>
) : RecyclerView.Adapter<ControlRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_control_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val control = results[position]
        holder.gender.text = control.gender
        holder.age.text = control.age
        holder.childName.text = control.childName
        holder.date.text = control.date
        holder.height.text = control.height
        holder.weight.text = control.weight
        holder.bmi.text = control.bmi
        holder.centileHeight.text = control.centileHeight
        holder.centileWeight.text = control.centileWeight
        holder.centileBmi.text = control.centileBmi
    }

    override fun getItemCount(): Int = results.size

    inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val gender: TextView = mView.controlGender
        val age: TextView = mView.controlAge
        val childName: TextView = mView.controlChildName
        val date: TextView = mView.controlDate
        val weight: TextView = mView.controlWeight
        val height: TextView = mView.controlHeight
        val bmi: TextView = mView.controlBmi
        val centileWeight: TextView = mView.controlCentileWeight
        val centileHeight: TextView = mView.controlCentileHeight
        val centileBmi: TextView = mView.controlCentileBmi
    }
}
