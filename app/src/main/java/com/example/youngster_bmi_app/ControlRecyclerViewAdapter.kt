package com.example.youngster_bmi_app

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.youngster_bmi_app.model.Control
import kotlinx.android.synthetic.main.activity_control_result.view.*

class ControlRecyclerViewAdapter(
    private val results: List<Control>
) : RecyclerView.Adapter<ControlRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_control_result, parent, false)
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
    }

    override fun getItemCount(): Int = results.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val gender: TextView = mView.controlGender
        val age: TextView = mView.controlAge
        val childName: TextView = mView.controlChildName
        val date: TextView = mView.controlDate
        val weight: TextView = mView.controlWeight
        val height: TextView = mView.controlHeight
    }
}
