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
    intent: Intent
) : RemoteViewsService.RemoteViewsFactory {
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
        val meal = getMeal()
        val mealCode = meal.code
        val mealInfoName = meal.name
        val mealInfoTime = SimpleDateFormat("MM/dd", Locale.US).format(Date())
        Intent().also { intent ->
            intent.action = DiningWidget.UPDATE_MEAL
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
            intent.putExtra("mealInfo", "$mealInfoTime - $mealInfoName")
            context.sendBroadcast(intent)
        }

        val menuCall = apiService.getMenu(context.getString(R.string.apikey), currTime, commons, mealCode)
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

    private fun getMeal(): Meal {
        var usedButton = DiningWidget.loadButtonPref(context, mAppWidgetId)
        //TODO:Remove
//        Log.d(LOG_TAG, "in getMeal, usedbutton is $usedButton")
        if(usedButton != null || usedButton == "none") {
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
        return meals[mealIndex]
    }

    override fun getViewAt(position: Int): RemoteViews {
        //TODO: Consider adding the station to the list item
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
