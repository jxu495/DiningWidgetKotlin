package com.example.diningwidgetkotlin

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

typealias Entree = DiningAPIService.Entree

class DiningWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return DiningRemoteViewsFactory(this.applicationContext, intent)
    }
}

class DiningRemoteViewsFactory(
    private val context: Context,
    intent: Intent
) : RemoteViewsService.RemoteViewsFactory {

    private var menu: List<DiningAPIService.Entree> = ArrayList()
    private val apiService = DiningAPIService.create()
    private val mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID)

    override fun onCreate() {

    }

    override fun getLoadingView(): RemoteViews? {
        return null //Returning null uses the default loading view.
    }

    override fun onDataSetChanged() {
        //TODO: get date
        val currTime = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val commons = DiningWidgetConfigureActivity.loadTitlePref(context, mAppWidgetId)
        val meal = "dinner" //TODO: PLACEHOLDER
        var menuCall = apiService.getMenu(context.getString(R.string.apikey), currTime, commons, meal)
        menuCall.enqueue(object : Callback<List<Entree>> {
            override fun onResponse(call: Call<List<Entree>>, response: Response<List<Entree>>) {
                if(response.isSuccessful()) {
                    menu = response.body()!! //isSuccessful should check for null, so this should be safe
                } else if(response.code() == 404) {
                    menu = listOf(Entree("Dining common is not serving today.", "Error"))
                } else {
                    menu = listOf(Entree("Unable to fetch menu information.", "Error"))
                }
            }
            override fun onFailure(call: Call<List<Entree>>, t: Throwable) {
                menu = listOf(Entree("Failed to fetch menu information.", "Error"))
            }
        })
    }

    override fun getViewAt(position: Int): RemoteViews {
        var rv = RemoteViews(context.packageName, R.layout.dining_widget_list_item)
        rv.setTextViewText(R.id.widget_list_item_text, menu[position].name)
        return rv
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getCount(): Int {
        return menu.size
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun onDestroy() {
        //To change body of created functions use File | Settings | File Templates.
    }
}
