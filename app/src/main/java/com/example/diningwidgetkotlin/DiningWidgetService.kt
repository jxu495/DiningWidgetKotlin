package com.example.diningwidgetkotlin

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import retrofit2.Call

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

    override fun onCreate() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLoadingView(): RemoteViews {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDataSetChanged() {
        //TODO: get date
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
