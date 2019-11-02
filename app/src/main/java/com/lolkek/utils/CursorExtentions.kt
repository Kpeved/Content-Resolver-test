package com.lolkek.utils

import android.database.Cursor

fun Cursor.getStringValue(key: String) = getString(getColumnIndex(key))

fun Cursor.getLongValue(key: String) = getLong(getColumnIndex(key))