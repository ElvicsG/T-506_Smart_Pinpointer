package com.kehui.www.testapp.application;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;

import com.XXXX.dao.db.DaoMaster;
import com.XXXX.dao.db.DaoSession;
import com.kehui.www.testapp.util.TripleDesUtils;
import com.kehui.www.testapp.util.MultiLanguageUtil;
import com.kehui.www.testapp.util.PrefUtils;

import java.util.Locale;

/**
 * @author 29062
 * @date 2016/11/09
 */
public class MyApplication extends Application {

    /**
     * 秘钥
     */
    public static final String key = TripleDesUtils.md5Encode("KH_Key_*", "").substring(3, 27).toUpperCase();

    /**
     * 24位密钥
     */
    public static final byte[] keyBytes = TripleDesUtils.hexToBytes(TripleDesUtils.byte2hex(key.getBytes()));
    private DaoMaster.DevOpenHelper mHelper;
    private SQLiteDatabase db;
    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;
    public static MyApplication instances;
    private BluetoothSocket _socket;
    private BluetoothDevice _device;
    private BluetoothAdapter _bluetooth; // 获取本地蓝牙适配器，即蓝牙设备


    @Override
    public void onCreate() {
        super.onCreate();
        MultiLanguageUtil.init(getApplicationContext());
        instances = this;
        MultiLanguageUtil.getInstance().updateLanguage(PrefUtils.getString(MyApplication.getInstances(), AppConfig.CURRENT_LANGUAGE, "follow_sys"));
//        switchLanguage(PrefUtils.getString(MyApplication.getInstances(), AppConfig.CURRENT_LANGUAGE, "follow_sys"));

        setDatabase();
        _socket = null;
        _device = null; // 蓝牙设备
        _bluetooth = BluetoothAdapter.getDefaultAdapter(); // 获取本地蓝牙适配器，即蓝牙设备// ...
        // Do it on main process
//        BlockCanary.install(this, new AppBlockCanaryContext()).start();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        MultiLanguageUtil.getInstance().updateLanguage(PrefUtils.getString(MyApplication.getInstances(), AppConfig.CURRENT_LANGUAGE, "follow_sys"));
//        switchLanguage(PrefUtils.getString(MyApplication.getInstances(), AppConfig.CURRENT_LANGUAGE, "follow_sys"));
    }

    public static MyApplication getInstances() {
        return instances;
    }

    /**
     * 设置greenDao
     */
    private void setDatabase() {
        // 通过 DaoMaster 的内部类 DevOpenHelper，你可以得到一个便利的 SQLiteOpenHelper 对象。
        // 可能你已经注意到了，你并不需要去编写「CREATE TABLE」这样的 SQL 语句，因为 greenDAO 已经帮你做了。
        // 注意：默认的 DaoMaster.DevOpenHelper 会在数据库升级时，删除所有的表，意味着这将导致数据的丢失。
        // 所以，在正式的项目中，你还应该做一层封装，来实现数据库的安全升级。
        mHelper = new DaoMaster.DevOpenHelper(this, "notes-db", null);
        db = mHelper.getWritableDatabase();
        // 注意：该数据库连接属于 DaoMaster，所以多个 Session 指的是相同的数据库连接。
        mDaoMaster = new DaoMaster(db);
        mDaoSession = mDaoMaster.newSession();
    }

    public DaoSession getDaoSession() {
        return mDaoSession;
    }

    public SQLiteDatabase getDb() {
        return db;
    }

    public BluetoothSocket get_socket() {
        return _socket;
    }

    public void set_socket(BluetoothSocket _socket) {
        this._socket = _socket;
    }

    public BluetoothAdapter get_bluetooth() {
        return _bluetooth;
    }

    public void set_bluetooth(BluetoothAdapter _bluetooth) {
        this._bluetooth = _bluetooth;
    }

    public BluetoothDevice get_device() {
        return _device;
    }

    public void set_device(BluetoothDevice _device) {
        this._device = _device;
    }



}
