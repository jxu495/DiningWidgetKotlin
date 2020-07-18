package com.example.diningwidgetkotlin

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews

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
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            DiningWidgetConfigureActivity.deleteTitlePref(context, appWidgetId)
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
                val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                val views = RemoteViews(context?.packageName, R.layout.dining_widget)
                val lvIntent = Intent(context, DiningWidgetService::class.java).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    putExtra("Button", "right")
                    //Needed because Android caches this intent, since it views it as a duplicate of the original as extras
                    //aren't compared.
                    data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
                }
                views.setRemoteAdapter(R.id.menuList, lvIntent)
                // Instruct the widget manager to update the widget
                val appWidgetManager = AppWidgetManager.getInstance(context)
                //TODO: Remove
                Log.d(LOG_TAG, "right click registered")
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.menuList)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
            L_BUTTON_CLICK -> {
                val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                val views = RemoteViews(context?.packageName, R.layout.dining_widget)
                val lvIntent = Intent(context, DiningWidgetService::class.java).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    putExtra("Button", "left")
                    //Needed because Android caches this intent, since it views it as a duplicate of the original as extras
                    //aren't compared.
                    data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
                }
                views.setRemoteAdapter(R.id.menuList, lvIntent)
                // Instruct the widget manager to update the widget
                val appWidgetManager = AppWidgetManager.getInstance(context)
                //TODO: Remove
                Log.d(LOG_TAG, "left click registered")
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.menuList)
                appWidgetManager.updateAppWidget(appWidgetId, views)
//                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.menuList)
            }
            UPDATE_MEAL -> {
                val views = RemoteViews(context?.packageName, R.layout.dining_widget)
                val mealString = intent.getStringExtra("Meal")
                val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                views.setTextViewText(R.id.mealTime, mealString)
                val appWidgetManager = AppWidgetManager.getInstance(context)
                appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
            }
            else -> {

            }
        }
    }

    companion object {

        const val R_BUTTON_CLICK = "rButtonClick"
        const val L_BUTTON_CLICK = "lButtonClick"
        const val UPDATE_MEAL = "updateMeal"

        const val LOG_TAG = "Dining Widget"

        internal fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val widgetText = DiningWidgetConfigureActivity.loadTitlePref(context, appWidgetId)
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.dining_widget)
            views.setTextViewText(R.id.menuTitle, widgetText)
            views.setOnClickPendingIntent(R.id.leftButton, getSelfPendingIntent(context, L_BUTTON_CLICK, appWidgetId))
            views.setOnClickPendingIntent(R.id.rightButton, getSelfPendingIntent(context, R_BUTTON_CLICK, appWidgetId))
            //TODO: update mealInfo text in onReceive, use partial update, send widgetID in broadcast
            //TODO: Make sure buttons send pending intent w/ appWidgetID request codes to make them distinct - send intent to widget activity
            val intent = Intent(context, DiningWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                putExtra("Button", "none")
                //Needed because Android caches this intent, since it views it as a duplicate of the original as extras
                //aren't compared.
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.menuList, intent)
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun getSelfPendingIntent(context: Context, action: String, appWidgetId: Int): PendingIntent {
            val intent = Intent(context, DiningWidget::class.java)
            intent.action = action
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            return PendingIntent.getBroadcast(context, appWidgetId, intent, 0)
        }

    }
}

