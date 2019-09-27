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

    private SQLiteDatabase db;
    private DaoSession mDaoSession;
    public static MyApplication instances;
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice bluetoothDevice;
    private BluetoothAdapter bluetoothAdapter;


    @Override
    public void onCreate() {
        super.onCreate();
        MultiLanguageUtil.init(getApplicationContext());
        instances = this;
        MultiLanguageUtil.getInstance().updateLanguage(PrefUtils.getString(MyApplication.getInstances(), AppConfig.CURRENT_LANGUAGE, "follow_sys"));

        setDatabase();
        //蓝牙通信socket
        bluetoothSocket = null;
        //远程蓝牙设备
        bluetoothDevice = null;
        //本地蓝牙设备    获得本设备的蓝牙适配器实例
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

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
        DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(this, "notes-db", null);
        db = devOpenHelper.getWritableDatabase();
        // 注意：该数据库连接属于 DaoMaster，所以多个 Session 指的是相同的数据库连接。
        DaoMaster daoMaster = new DaoMaster(db);
        mDaoSession = daoMaster.newSession();
    }

    public DaoSession getDaoSession() {
        return mDaoSession;
    }

    public SQLiteDatabase getDb() {
        return db;
    }

    public BluetoothSocket getBluetoothSocket() {
        return bluetoothSocket;
    }

    public void setBluetoothSocket(BluetoothSocket bluetoothSocket) {
        this.bluetoothSocket = bluetoothSocket;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public void setBluetoothAdapter(BluetoothAdapter bluetoothAdapter) {
        this.bluetoothAdapter = bluetoothAdapter;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

}
