package com.example.youngster_bmi_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.youngster_bmi_app.impl.CentileService
import com.example.youngster_bmi_app.impl.ControlService

class ResultListActivity : AppCompatActivity() {

    private lateinit var resultsRecyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var centileService: CentileService
    private lateinit var controlService: ControlService
    private lateinit var resultsHeader: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_list)
        centileService = CentileService(applicationContext)
        controlService = ControlService(centileService, getString(R.string.resultsFile))
        resultsRecyclerView = findViewById(R.id.resultsRecyclerView)

        viewManager = LinearLayoutManager(this)
        viewAdapter = ControlRecyclerViewAdapter(controlService.getControlResults())

        resultsRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }
}
