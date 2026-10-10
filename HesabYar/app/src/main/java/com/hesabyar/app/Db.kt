package com.hesabyar.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.flow.MutableStateFlow

class Db(ctx: Context) : SQLiteOpenHelper(ctx, "hesab.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE tx (id INTEGER PRIMARY KEY AUTOINCREMENT, amount INTEGER NOT NULL, " +
                    "income INTEGER NOT NULL, category TEXT NOT NULL, note TEXT NOT NULL, " +
                    "date INTEGER NOT NULL, balance INTEGER, hash TEXT, source TEXT NOT NULL)"
        )
        db.execSQL("CREATE INDEX idx_tx_date ON tx(date)")
        db.execSQL("CREATE INDEX idx_tx_hash ON tx(hash)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    private fun cv(t: Tx, hash: String?): ContentValues {
        val v = ContentValues()
        v.put("amount", t.amount)
        v.put("income", if (t.isIncome) 1 else 0)
        v.put("category", t.category)
        v.put("note", t.note)
        v.put("date", t.date)
        val b = t.balance
        if (b == null) v.putNull("balance") else v.put("balance", b)
        if (hash != null) v.put("hash", hash)
        v.put("source", t.source)
        return v
    }

    @Synchronized
    fun insert(t: Tx, hash: String? = null) {
        writableDatabase.insert("tx", null, cv(t, hash))
    }

    @Synchronized
    fun exists(hash: String, date: Long): Boolean {
        val c = readableDatabase.rawQuery(
            "SELECT 1 FROM tx WHERE hash=? AND date BETWEEN ? AND ? LIMIT 1",
            arrayOf(hash, (date - 180_000L).toString(), (date + 180_000L).toString())
        )
        return c.use { it.moveToFirst() }
    }

    @Synchronized
    fun all(): List<Tx> {
        val out = ArrayList<Tx>()
        val c = readableDatabase.rawQuery(
            "SELECT id, amount, income, category, note, date, balance, source FROM tx ORDER BY date DESC, id DESC",
            null
        )
        c.use {
            while (it.moveToNext()) {
                out.add(
                    Tx(
                        id = it.getLong(0),
                        amount = it.getLong(1),
                        isIncome = it.getInt(2) == 1,
                        category = it.getString(3),
                        note = it.getString(4),
                        date = it.getLong(5),
                        balance = if (it.isNull(6)) null else it.getLong(6),
                        source = it.getString(7)
                    )
                )
            }
        }
        return out
    }

    @Synchronized
    fun update(t: Tx) {
        writableDatabase.update("tx", cv(t, null), "id=?", arrayOf(t.id.toString()))
    }

    @Synchronized
    fun delete(id: Long) {
        writableDatabase.delete("tx", "id=?", arrayOf(id.toString()))
    }

    @Synchronized
    fun deleteAll() {
        writableDatabase.delete("tx", null, null)
    }
}

object Repo {
    lateinit var db: Db
    val txs = MutableStateFlow<List<Tx>>(emptyList())

    @Synchronized
    fun init(ctx: Context) {
        if (!::db.isInitialized) {
            db = Db(ctx.applicationContext)
            reload()
        }
    }

    fun reload() {
        txs.value = db.all()
    }
}
