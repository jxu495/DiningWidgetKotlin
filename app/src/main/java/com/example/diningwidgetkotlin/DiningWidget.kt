package com.example.diningwidgetkotlin

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [DiningWidgetConfigureActivity]
 */
class DiningWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            if(DEBUG) {
                Log.d(LOG_TAG, "appWidgetIds includes $appWidgetId")
            }
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            DiningWidgetConfigureActivity.deleteTitlePref(context, appWidgetId)
            deleteButtonPref(context, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }



    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        when(intent?.action) {
            R_BUTTON_CLICK -> {
                //TODO: Can replace most of these with a single function, aside from update meal
                val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                if(context != null) {
                    saveButtonPref(context, appWidgetId, "right")
                    Toast.makeText(context, R.string.fetch_menu_toast, Toast.LENGTH_SHORT).show()
                }
                // Instruct the widget manager to update the widget
                val appWidgetManager = AppWidgetManager.getInstance(context)
//                Log.d(LOG_TAG, "right click registered")
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.menuList)
            }
            L_BUTTON_CLICK -> {
                val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                if(context != null) {
                    saveButtonPref(context, appWidgetId, "left")
                    Toast.makeText(context, R.string.fetch_menu_toast, Toast.LENGTH_SHORT).show()
                }
                // Instruct the widget manager to update the widget
                val appWidgetManager = AppWidgetManager.getInstance(context)
//                Log.d(LOG_TAG, "left click registered")
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.menuList)
            }
            UPDATE_MEAL -> {
                val views = RemoteViews(context?.packageName, R.layout.dining_widget)
                val mealInfo = intent.getStringExtra("mealInfo")
                val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                views.setTextViewText(R.id.mealTime, mealInfo)
                val appWidgetManager = AppWidgetManager.getInstance(context)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
            REFRESH_MENU -> {
                val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                if(context != null) {
                    saveButtonPref(context, appWidgetId, "none")
                    Toast.makeText(context, R.string.fetch_menu_toast, Toast.LENGTH_SHORT).show()
                }
                val appWidgetManager = AppWidgetManager.getInstance(context)
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.menuList)
            }
            else -> {

            }
        }
    }

    companion object {
        //Used for shared preferences functions
        private const val PREFS_NAME = "com.example.diningwidgetkotlin.DiningWidget"
        private const val PREF_PREFIX_KEY = "appwidget_button_"
        //states used for onReceive responses
        const val R_BUTTON_CLICK = "com.example.diningwidgetkotlin.R_BUTTON_CLICK"
        const val L_BUTTON_CLICK = "com.example.diningwidgetkotlin.L_BUTTON_CLICK"
        const val REFRESH_MENU = "com.example.diningwidgetkotlin.REFRESH_MENU"
        const val UPDATE_MEAL = "com.example.diningwidgetkotlin.UPDATE_MEAL"
        //debug use
        const val LOG_TAG = "Dining Widget"
        const val DEBUG = true

        internal fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            if(DEBUG) {
                Log.d(LOG_TAG, "in updateAppWidget")
            }
            //Fetch selected dining commons from SharedPreferences, which was selected from DiningWidgetConfigureActivity.kt
            val widgetText = DiningWidgetConfigureActivity.loadTitlePref(context, appWidgetId)
            //SharedPreferences value that determines if the menu should display the next/previous meal
            saveButtonPref(context, appWidgetId, "none")
            // Construct the RemoteViews object from xml layout
            val views = RemoteViews(context.packageName, R.layout.dining_widget)
            views.setTextViewText(R.id.menuTitle, widgetText)
            //Setting button behavior
            views.setOnClickPendingIntent(R.id.leftButton, getSelfPendingIntent(context, L_BUTTON_CLICK, appWidgetId))
            views.setOnClickPendingIntent(R.id.rightButton, getSelfPendingIntent(context, R_BUTTON_CLICK, appWidgetId))
            views.setOnClickPendingIntent(R.id.menuTitle, getUpdatePendingIntent(context, appWidgetId))
            //Intent sets up listview adapter
            val intent = Intent(context, DiningWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                //Needed because Android caches this intent, since it views it as a duplicate of the original as extras
                //aren't compared.
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.menuList, intent)
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
        //Helper function for button pending intents
        private fun getSelfPendingIntent(context: Context, action: String, appWidgetId: Int): PendingIntent {
            val intent = Intent(context, DiningWidget::class.java)
            intent.action = action
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            return PendingIntent.getBroadcast(context, appWidgetId, intent, 0)
        }
        //Helper function for refreshing menu if user clicks on title
        private fun getUpdatePendingIntent(context: Context, appWidgetId: Int): PendingIntent {
            val intent = Intent(context, DiningWidget::class.java)
            intent.action = REFRESH_MENU
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            return PendingIntent.getBroadcast(context, appWidgetId, intent, 0)
        }

        private fun saveButtonPref(context: Context, appWidgetId: Int, text: String) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.putString(PREF_PREFIX_KEY + appWidgetId, text)
            prefs.apply()
        }

        // Read the prefix from the SharedPreferences object for this widget.
        // If there is no preference saved, use default "none"
        internal fun loadButtonPref(context: Context, appWidgetId: Int): String {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            val buttonValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null)
            return buttonValue ?: "none"
        }

        internal fun deleteButtonPref(context: Context, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(DiningWidget.PREFS_NAME, 0).edit()
            prefs.remove(DiningWidget.PREF_PREFIX_KEY + appWidgetId)
            prefs.apply()
        }
    }
}

