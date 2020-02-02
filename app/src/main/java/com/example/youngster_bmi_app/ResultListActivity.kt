package com.example.youngster_bmi_app

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider.getUriForFile
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

        //todo
        /*when (intent?.action) {
            Intent.ACTION_SEND -> {
                if ("text/csv" == intent.type) {
                    handleSendResults(intent)
                }
            }
        }*/
    }

    private fun handleSendResults(intent: Intent) {
        (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? List<String>)?.let {
            viewAdapter = ControlRecyclerViewAdapter(controlService.readResultsFromInput(it))
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_list_results, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.emailIcon -> {
            composeEmail()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun composeEmail() {
        val attachment = File(applicationContext.filesDir, getString(R.string.resultsFile))
        val uri = getUriForFile(applicationContext, "com.example.youngster_bmi_app.fileprovider", attachment)
        val intent = Intent(Intent.ACTION_SEND).apply {
            data = uri
            putExtra(Intent.EXTRA_STREAM, uri)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }
}
