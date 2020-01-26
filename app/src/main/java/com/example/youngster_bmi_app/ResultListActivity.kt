package com.example.youngster_bmi_app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.youngster_bmi_app.impl.CentileService
import com.example.youngster_bmi_app.impl.ControlService
import java.io.File

class ResultListActivity : AppCompatActivity() {

    private lateinit var resultsRecyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var centileService: CentileService
    private lateinit var controlService: ControlService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_list)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        centileService = CentileService(applicationContext, getString(R.string.noDataForThisAgeLong))
        controlService = ControlService(centileService, getString(R.string.resultsFile), getString(R.string.noDataForThisAgeShort))
        resultsRecyclerView = findViewById(R.id.resultsRecyclerView)

        viewManager = LinearLayoutManager(this)
        viewAdapter = ControlRecyclerViewAdapter(controlService.getControlResults())

        resultsRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_list_results, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.emailIcon -> {
            composeEmail(arrayOf("maciejmpawlik@gmail.com"), "some")
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    fun composeEmail(addresses: Array<String>, subject: String) {
        val attachment = File(applicationContext.filesDir, getString(R.string.resultsFile))
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, addresses)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            //putExtra(Intent.EXTRA_STREAM, Uri.fromFile(attachment))
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }
}
