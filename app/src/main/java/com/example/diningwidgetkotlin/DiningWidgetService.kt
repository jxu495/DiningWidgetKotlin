package com.example.diningwidgetkotlin

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

typealias Entree = DiningAPIService.Entree
typealias Meal = DiningAPIService.Meal

class DiningWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return DiningRemoteViewsFactory(this.applicationContext, intent)
    }
}

class DiningRemoteViewsFactory(
    private val context: Context,
    val intent: Intent
) : RemoteViewsService.RemoteViewsFactory {
    //TODO: Figure out how to get around caching
    private var menu: List<Entree> = ArrayList()
    private var meals: List<Meal> = ArrayList()
    private var mealIndex = 0
    private val apiService = DiningAPIService.create()
    private val mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID)

    override fun onCreate() {

    }

    override fun getLoadingView(): RemoteViews? {
        return null //Returning null uses the default loading view.
    }

    override fun onDataSetChanged() {
        val currTime = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        //toLowerCase and replacing spaces with dashes needed to convert user friendly name to api friendly name.
        val commons = DiningWidgetConfigureActivity.loadTitlePref(context, mAppWidgetId)
            .toLowerCase()
            .replace(' ', '-')
        val mealCall = apiService.getMeals(context.getString(R.string.apikey), currTime, commons)
        try {
            val response = mealCall.execute()
            if(response.isSuccessful) {
                meals = response.body()!!
            } else if(response.code() == 404) {
                menu = listOf(Entree("Dining common is not serving today.", "Error"))
                return
            } else {
                menu = listOf(Entree("Unable to fetch menu information.", "Error"))
                return
            }
        } catch (e: Exception){
            menu = listOf(Entree("Failed to fetch menu information.", "Error"))
            e.printStackTrace()
            return
        }
        var meal = getMeal()
        //TODO: Use meals[index] here instead, account for looping when out of bounds
        val menuCall = apiService.getMenu(context.getString(R.string.apikey), currTime, commons, meal)
        try {
            val response = menuCall.execute()
            if(response.isSuccessful) {
                menu = response.body()!!
            } else if(response.code() == 404) {
                menu = listOf(Entree("Dining common is not serving today.", "Error"))
            } else {
                menu = listOf(Entree("Unable to fetch menu information.", "Error"))
            }
        } catch (e: Exception){
            menu = listOf(Entree("Failed to fetch menu information.", "Error"))
            e.printStackTrace()
        }
    }

    private fun getMeal(): String {
        var usedButton = intent.getStringExtra("Button")
        //TODO:Remove
        Log.d(LOG_TAG, "in getMeal, usedbutton is $usedButton")
        if(usedButton != null) {
            when(usedButton) {
                "left" -> {
                    if(mealIndex - 1 < 0) {
                        mealIndex = meals.lastIndex
                    } else {
                        mealIndex--;
                    }
                }
                "right" -> {
                    if(mealIndex + 1 > meals.lastIndex) {
                        mealIndex = 0
                    } else {
                        mealIndex++
                    }
                }
                else -> {
                    Log.d("Widget Service",
                        "usedButton string extra was not a valid option: $usedButton"
                    )
                }
            }
        }
        return meals[mealIndex].code
    }

    override fun getViewAt(position: Int): RemoteViews {
        var rv = RemoteViews(context.packageName, R.layout.dining_widget_list_item)
        rv.setTextViewText(R.id.widget_list_item_text, menu[position].name)
        return rv
        //To change body of created functions use File | Settings | File Templates.
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

    }
    companion object {
        const val LOG_TAG = "Widget Factory"
    }
}
