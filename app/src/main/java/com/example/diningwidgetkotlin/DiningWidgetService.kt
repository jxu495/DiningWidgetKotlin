package com.example.diningwidgetkotlin

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import retrofit2.Call
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DiningWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return DiningRemoteViewsFactory(this.applicationContext, intent)
    }
}

class DiningRemoteViewsFactory(
    private val context: Context,
    intent: Intent
) : RemoteViewsService.RemoteViewsFactory {

    private var menu: ArrayList<DiningAPIService.Entree> = ArrayList()
    private val APIService = DiningAPIService.create()
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
        //Call<List<DiningAPIService.Entree>> call = APIService.getMenu(R.string.apikey, )
    }

    override fun getViewAt(position: Int): RemoteViews {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getCount(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getViewTypeCount(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDestroy() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
