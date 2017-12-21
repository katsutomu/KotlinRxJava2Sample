package com.example.sekinekatsunori.kotlin_rxjava2_samples

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.ListView
import io.reactivex.Flowable
import io.reactivex.FlowableSubscriber
import io.reactivex.processors.PublishProcessor
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import org.reactivestreams.Subscription
import java.util.*

class MainActivity : AppCompatActivity() {
    val paginator = Paginater(CatsRepository())
    val cats = mutableListOf("Abyssinian",
            "Aegean",
            "American Curl",
            "American Bobtail",
            "American Shorthair",
            "American Wirehair",
            "Arabian Mau",
            "Australian Mist",
            "Asian",
            "Asian Semi-longhair",
            "Balinese",
            "Bambino",
            "Bengal",
            "Birman",
            "Bombay",
            "Brazilian Shorthair",
            "British Semi-longhair",
            "British Shorthair",
            "British Longhair",
            "Burmese",
            "Burmilla")
    val adapter by lazy {  ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, cats) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val scrolledProcessor = PublishProcessor.create<Triple<Int,Int,Int>>()
        scrolledProcessor.onBackpressureDrop().subscribe(paginator)

        val listView = findViewById<ListView>(R.id.list_view)
        listView.adapter = adapter
        listView.setOnScrollListener(object : AbsListView.OnScrollListener{
            override fun onScrollStateChanged(p0: AbsListView?, p1: Int) {}

            override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                if (totalItemCount == firstVisibleItem + visibleItemCount) {
                    scrolledProcessor.onNext(Triple(totalItemCount, firstVisibleItem, visibleItemCount))
                }
            }
        })

        val footer = layoutInflater.inflate(R.layout.progress, null, false)
        listView.addFooterView(footer)
        paginator.fetched.subscribe {
            launch (UI) {
                val (list, finished) = it
                adapter.addAll(list)
                adapter.notifyDataSetChanged()
                if (finished) {
                    listView.removeFooterView(footer)
                }
            }
        }
    }

    class CatsRepository {
        val dummyData = PublishSubject.just("California Spangled",
                "Chantilly-Tiffany",
                "Chartreux",
                "Chausie",
                "Cheetoh",
                "Colorpoint Shorthair",
                "Cornish Rex",
                "Cymric or Manx Longhair",
                "Cyprus",
                "Devon Rex")
    }
    class Paginater(private val catsRepository: CatsRepository) : FlowableSubscriber<Triple<Int,Int,Int>> {
        private var subscription: Subscription? = null
        private var count = 0
        var fetched: PublishSubject<Pair<List<String>, Boolean>> = PublishSubject.create()

        override fun onSubscribe(s: Subscription) {
            subscription = s
            subscription?.request(1)
        }

        override fun onNext(t: Triple<Int,Int,Int>?) {
            launch (UI, CoroutineStart.UNDISPATCHED) {
                Log.d("Paginater", "FetchStart")
                delay(2000)
                subscription?.request(1)
                val nextPageList = catsRepository.dummyData.skip(count * 5L).take(5).toList().blockingGet()
                var finished = false
                count++
                if (count == 2) {
                    subscription?.cancel()
                    finished = true
                }
                fetched.onNext(Pair<List<String>, Boolean>(nextPageList, finished))
                Log.d("Paginater", "FetchEnd")
            }
        }

        override fun onComplete() {}
        override fun onError(t: Throwable?) {}
    }
}
