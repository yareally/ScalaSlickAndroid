package com.cc.AndroidSlick

import android.app.Activity
import android.os.{Environment, Bundle}
import android.util.Log

/**
 * Requires Android 4.0.3 because the SQLite
 * library I'm using requires it.
 *
 * @author Wes Lanning
 * @version 2015-05-02
 */
class SlickTest extends Activity {

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)
    // val dbStrPath = "/storage/emulated/legacy/databases"
    // manually defining the db path here, but application.conf should also work
    val dbStrPath = s"${Environment.getExternalStorageDirectory }/databases"
    val dbName = "test.db"
    val dbDir = new java.io.File(dbStrPath)

    Log.d(getClass.getName, s"Path to db: jdbc:sqlite:$dbStrPath/$dbName")

    if (!dbDir.exists) {
      dbDir.mkdirs()
    }
    DbHelpers.init(dbStrPath, dbName)
  }
}
