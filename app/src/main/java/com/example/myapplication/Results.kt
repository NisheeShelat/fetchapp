package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ProgressBar
import android.widget.TextView
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class Results : AppCompatActivity() {

        private var currentPage = 1
        private lateinit var progressBar: ProgressBar
        private lateinit var recyclerView: RecyclerView
        private lateinit var adapter: ItemAdapter

        private val apiService: ApiService by lazy {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://fetch-hiring.s3.amazonaws.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            retrofit.create(ApiService::class.java)
        }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)
        enableEdgeToEdge()
        progressBar = findViewById(R.id.progressBar)
        recyclerView = findViewById(R.id.recyclerView)

        adapter = ItemAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                if (lastVisibleItemPosition == totalItemCount - 1 && !isDataLoading) {
                    currentPage++
                    fetchData()
                }
            }
        })

        fetchData()



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.progressBar)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private var isDataLoading = false
    private fun fetchData() {
        if (!isDataLoading) {
            isDataLoading = true
            progressBar.visibility = View.VISIBLE
            apiService.fetchItems(currentPage).enqueue(object : Callback<List<Item>> {
                override fun onResponse(call: Call<List<Item>>, response: Response<List<Item>>) {
                    val items = response.body()?.filterNotNull()
                        ?.filter { it.name?.isNotBlank() == true }
                        ?.groupBy { it.listId }
                        ?.toSortedMap()
                        ?.flatMap { (listId, itemList) ->
                            itemList.sortedBy { it.name }.map {
                                ItemWithListId(it.id, listId, it.name ?: "")
                            }
                        }

                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        if (currentPage == 1) {
                            adapter.setItems(items ?: emptyList())
                        } else {
                            adapter.addItems(items ?: emptyList())
                        }
                        isDataLoading = false
                    }
                }

                override fun onFailure(call: Call<List<Item>>, t: Throwable) {
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        // Handle error
                        isDataLoading = false
                    }
                }
            })
        }
    }

    data class Item(
        val id: Int,
        val listId: Int,
        val name: String?
    )

    data class ItemWithListId(
        val id: Int,
        val listId: Int,
        val name: String
    )

    inner class ItemAdapter : RecyclerView.Adapter<ItemViewHolder>() {
        private val items = mutableListOf<ItemWithListId>()

        fun setItems(newItems: List<ItemWithListId>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }
        fun addItems(newItems: List<ItemWithListId>) {
            val startPosition = items.size
            items.addAll(newItems)
            notifyItemRangeInserted(startPosition, newItems.size)
            items.sortBy { it.listId }
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.activity_item_layout, parent, false)
            return ItemViewHolder(view)
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val item = items[position]
            holder.bind(item)
        }

        override fun getItemCount() = items.size
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val idTextView: TextView = itemView.findViewById(R.id.idTextView)
        private val listIdTextView: TextView = itemView.findViewById(R.id.listIdTextView)
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)

        fun bind(item: ItemWithListId) {
            idTextView.text = item.id.toString()
            listIdTextView.text = item.listId.toString()
            nameTextView.text = item.name
        }
    }


    }

    interface ApiService {
        @GET("hiring.json")
        fun fetchItems(@Query("page") page: Int): Call<List<Results.Item>>
    }
