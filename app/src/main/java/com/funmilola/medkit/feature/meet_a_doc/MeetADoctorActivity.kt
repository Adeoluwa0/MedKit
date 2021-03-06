package com.funmilola.medkit.feature.meet_a_doc

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.funmilola.medkit.R
import kotlinx.android.synthetic.main.activity_meet_a_doctor.*
import java.io.IOException

class MeetADoctorActivity : AppCompatActivity(), ImageRequester.ImageRequesterResponse {
    private var photosList: ArrayList<Photo> = ArrayList()
    private lateinit var imageRequester: ImageRequester
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: RecyclerAdapter
    private lateinit var gridLayoutManager: GridLayoutManager

    private val lastVisibleItemPosition: Int
        get() = if (doctors_recycler_view.layoutManager == linearLayoutManager) {
            linearLayoutManager.findLastVisibleItemPosition()
        } else {
            gridLayoutManager.findLastVisibleItemPosition()
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meet_a_doctor)
        linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        doctors_recycler_view.layoutManager = linearLayoutManager
        adapter = RecyclerAdapter(photosList)
        doctors_recycler_view.adapter = adapter
        setRecyclerViewScrollListener()
        gridLayoutManager = GridLayoutManager(this, 2)
        setRecyclerViewItemTouchListener()


        imageRequester = ImageRequester(this)

    }
    private fun changeLayoutManager() {
        if (doctors_recycler_view.layoutManager == linearLayoutManager) {
            //1
            doctors_recycler_view.layoutManager = gridLayoutManager
            //2
            if (photosList.size == 1) {
                requestPhoto()
            }
        } else {
            //3
            doctors_recycler_view.layoutManager = linearLayoutManager
        }
    }

    private fun setRecyclerViewScrollListener() {
        doctors_recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val totalItemCount = recyclerView.layoutManager!!.itemCount
                if (!imageRequester.isLoadingData && totalItemCount == lastVisibleItemPosition + 1) {
                    requestPhoto()
                }
            }
        })
    }

    private fun setRecyclerViewItemTouchListener() {

        //1
        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, viewHolder1: RecyclerView.ViewHolder): Boolean {
                //2
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                //3
                val position = viewHolder.adapterPosition
                photosList.removeAt(position)
                doctors_recycler_view.adapter!!.notifyItemRemoved(position)
            }
        }

        //4
        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(doctors_recycler_view)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_change_recycler_manager) {
            changeLayoutManager()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        if (photosList.size == 0) {
            requestPhoto()
        }

    }

    private fun requestPhoto() {
        try {
            imageRequester.getPhoto()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }


    override fun receivedNewPhoto(newPhoto: Photo) {
        runOnUiThread {
            photosList.add(newPhoto)
            adapter.notifyItemInserted(photosList.size-1)
        }
    }
}