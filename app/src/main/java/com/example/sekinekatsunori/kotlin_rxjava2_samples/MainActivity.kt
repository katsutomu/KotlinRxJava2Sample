package com.example.sekinekatsunori.kotlin_rxjava2_samples

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import io.reactivex.FlowableSubscriber
import io.reactivex.processors.PublishProcessor
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import org.reactivestreams.Subscription

class MainActivity : AppCompatActivity() {
    val paginator = Paginater()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val nextRequest = PublishProcessor.create<Int>()
        nextRequest.onBackpressureDrop().subscribe(paginator)
        findViewById<Button>(R.id.button).setOnClickListener {
            Log.d("Paginater", "clicked")
            nextRequest.onNext(1)
        }
    }

    class Paginater : FlowableSubscriber<Int> {
        private var subscription: Subscription? = null
        private var count = 0
        override fun onError(t: Throwable?) {
        }

        override fun onSubscribe(s: Subscription) {
            subscription = s
            subscription?.request(1)
        }

        override fun onComplete() {
        }

        override fun onNext(t: Int?) {
            Log.d("Paginater", "onNext")

            launch (UI, CoroutineStart.UNDISPATCHED) {
                Log.d("Paginater", "FetchStart")
                delay(2000)
                subscription?.request(1)
                count++
                if (count == 5) {
                    subscription?.cancel()
                }
                Log.d("Paginater", "FetchEnd")
            }
        }
    }
}
