package com.kehui.www.testapp.view;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.XXXX.dao.db.AssistanceDataInfoDao;
import com.google.gson.Gson;
import com.kehui.www.testapp.R;
import com.kehui.www.testapp.adpter.AssistInfoListAdapter;
import com.kehui.www.testapp.application.Constant;
import com.kehui.www.testapp.application.MyApplication;
import com.kehui.www.testapp.application.URLs;
import com.kehui.www.testapp.bean.AssistInfoReplyStatusBean;
import com.kehui.www.testapp.bean.AssistListBean;
import com.kehui.www.testapp.bean.RequestBean;
import com.kehui.www.testapp.database.AssistanceDataInfo;
import com.kehui.www.testapp.retrofit.APIService;
import com.kehui.www.testapp.ui.CLinearLayoutManager;
import com.kehui.www.testapp.ui.CustomDialog;
import com.kehui.www.testapp.util.TripleDesUtils;
import com.kehui.www.testapp.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * 协助列表页面
 */
public class AssistListActivity extends BaseActivity {

    @BindView(R.id.tv_choose_test_time)
    TextView tvChooseTestTime;
    @BindView(R.id.tv_choose_report_status)
    TextView tvChooseReportStatus;
    @BindView(R.id.tv_choose_reply_status)
    TextView tvChooseReplyStatus;
    @BindView(R.id.rv_list)
    RecyclerView rvList;
    @BindView(R.id.srl_refresh)
    SwipeRefreshLayout srlRefresh;
    @BindView(R.id.btn_initiate_assistance)
    Button btnInitiateAssistance;
    @BindView(R.id.btn_upload_data)
    Button btnUploadData;
    @BindView(R.id.btn_back)
    Button btnBack;
    @BindView(R.id.btn_search)
    Button btnSearch;
    @BindView(R.id.iv_exception)
    ImageView ivException;
    @BindView(R.id.rl_test_time)
    RelativeLayout rlTestTime;
    @BindView(R.id.rl_report_status)
    RelativeLayout rlReportStatus;
    @BindView(R.id.rl_reply_status)
    RelativeLayout rlReplyStatus;
    @BindView(R.id.tv_exception)
    TextView tvException;
    @BindView(R.id.ll_exception)
    LinearLayout llException;
    //    private LinearLayoutManager linearLayoutManager;
    private AssistInfoListAdapter assistInfoListAdapter;
    //    private List<AssistListBean.DataBean> assistList;
    private List<AssistanceDataInfo> assistList;
    public int page = 0;//代表请求第几页
    private boolean isRequest;//判断是否还有下拉数据
    private static long startTime = 0; //查询的开始时间
    private static long endTime = 0; //查询的开始时间
    public String replyStatus = ""; //回复状态
    public String reportStatus = ""; //上报状态
    private View replyView;
    private PopupWindow replyPopupWindow;
    private PopupWindow reportPopupWindow;
    public int screenWidth;
    public int screenHeight;
    private View reportView;
    private AssistanceDataInfoDao dao;
    private CustomDialog customDialog;
    private Boolean isRefreshing;
    //是否是查询状态
    private boolean isSearch;
    //线程停止的标志
    private boolean isStop;

    //处理线程ui
    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 0) {
                assistInfoListAdapter.notifyDataSetChanged();
                srlRefresh.setRefreshing(false);
            } else if (msg.what == 1) { //toast无未上报数据
                Utils.showToast(AssistListActivity.this, getString(R.string.no_report_data));
            } else if (msg.what == 2) {
                llException.setVisibility(View.VISIBLE);
                srlRefresh.setVisibility(View.GONE);
                ivException.setImageResource(R.drawable.ic_no_data);
                tvException.setText(R.string.no_data);
            } else if (msg.what == 3) {
                Utils.showToast(AssistListActivity.this, msg.obj.toString());
            } else if (msg.what == 4) {
                srlRefresh.setRefreshing(false);
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assist_list);
        ButterKnife.bind(this);

        WindowManager wm = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);
        screenWidth = wm.getDefaultDisplay().getWidth();
        screenHeight = wm.getDefaultDisplay().getHeight();
        initView();
        initData();
        if (Utils.isNetVisible(AssistListActivity.this)) {
            doRequestStatus.start();
        }
    }


    private void initData() {
        assistList = new ArrayList<>();
        assistInfoListAdapter = new AssistInfoListAdapter(AssistListActivity.this, assistList);
        dao = MyApplication.getInstances().getDaoSession().getAssistanceDataInfoDao();
        rvList.setAdapter(assistInfoListAdapter);
        page = 0;
        loadData(true);


    }


    //同步协助列表数据的方法
    private void requestAssist() {
        final RequestBean requestBean = new RequestBean();
        requestBean.infoDevId = Constant.DeviceId;
        final Gson gson = new Gson();
        String json = gson.toJson(requestBean);
        json = TripleDesUtils.encryptMode(MyApplication.keyBytes, json.getBytes());
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(Constant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(Constant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(Constant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URLs.AppUrl + URLs.AppPort)
                .addConverterFactory(ScalarsConverterFactory.create()).client(client)
                .build();
        APIService service = retrofit.create(APIService.class);
        Call<String> call = service.api("GetInfoTopList", json);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, final Response<String> response) {
                try {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            byte[] srcBytes = TripleDesUtils.decryptMode(MyApplication.keyBytes, response.body());
                            String result = new String(srcBytes);
                            AssistListBean assistListBean = gson.fromJson(result, AssistListBean.class);
                            List<AssistanceDataInfo> tempList = new ArrayList<AssistanceDataInfo>();
                            if (assistListBean.Code.equals("1")) {
                                if (assistListBean.data.size() > 0) {
                                    for (int i = 0; i < assistListBean.data.size(); i++) {
                                        AssistanceDataInfo assistanceDataInfo = new AssistanceDataInfo(null, assistListBean.data.get(i).InfoID
                                                , Utils.getTime(assistListBean.data.get(i).InfoTime), assistListBean.data.get(i).InfoUName, assistListBean.data.get(i).InfoAddress
                                                , assistListBean.data.get(i).InfoLength, assistListBean.data.get(i).InfoLineType, assistListBean.data.get(i).InfoFaultType
                                                , assistListBean.data.get(i).InfoFaultLength, assistListBean.data.get(i).InfoMemo, assistListBean.data.get(i).InfoCiChang
                                                , "1", assistListBean.data.get(i).ReplyStatus, assistListBean.data.get(i).ReplyContent, Integer.parseInt(assistListBean.data.get(i).InfoCiCangVol),
                                                Integer.parseInt(assistListBean.data.get(i).InfoShengYinVol), Integer.parseInt(assistListBean.data.get(i).InfoLvBo), assistListBean.data.get(i).InfoYuYan);
                                        tempList.add(assistanceDataInfo);
                                        dao.insert(assistanceDataInfo);
                                    }
                                    assistList.addAll(tempList);
                                    mHandler.sendEmptyMessage(0);
//                            assistInfoListAdapter.notifyDataSetChanged();
                                } else {
                                    mHandler.sendEmptyMessage(2);
//                            ivException.setVisibility(View.VISIBLE);
//                            srlRefresh.setVisibility(View.GONE);
//                            ivException.setImageResource(R.drawable.ic_no_data);

                                }
//                        srlRefresh.setRefreshing(false);
                                mHandler.sendEmptyMessage(4);
                            } else {
                                Message message = new Message();
                                message.obj = assistListBean.Message;
                                message.what = 3;
                                mHandler.sendMessage(message);
//                        Utils.showToast(AssistListActivity.this, assistListBean.Message);
//                        srlRefresh.setRefreshing(false);
                            }
                        }
                    }).start();

                } catch (Exception e) {
                    Log.e("打印-请求报异常-检查代码", "AppInfoList");
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Utils.showToast(AssistListActivity.this, getString(R.string.check_net_retry));
//                srlRefresh.setRefreshing(false);
                mHandler.sendEmptyMessage(4);
            }
        });


    }

    private void initView() {
        initReplyPopupWindow();
        initReportPopupWindow();
        srlRefresh.setColorSchemeResources(R.color.main_color);
        srlRefresh.setDistanceToTriggerSync(0);
        srlRefresh.setProgressBackgroundColor(R.color.white); // 设定下拉圆圈的背景
        srlRefresh.setSize(2); // 设置圆圈的大小
//        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvList.setItemAnimator(new DefaultItemAnimator());
//        rvList.setLayoutManager(linearLayoutManager);
        rvList.setLayoutManager(new CLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        srlRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isRefreshing = true;
                page = 0;
                loadData(false);
//                request(page + "", startTime, replyStatus, "");
            }
        });

        rvList.setOnScrollListener(new RecyclerView.OnScrollListener() {
            int lastVisibleItem;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //判断RecyclerView的状态 是空闲时，同时，是最后一个可见的ITEM时才加载

                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == assistInfoListAdapter.getItemCount() && !isRefreshing) {
                    Log.e("aaaaaaaaaaaa", isRefreshing + "");

                    //设置正在加载更多
                    assistInfoListAdapter.changeMoreStatus(assistInfoListAdapter.LOADING_MORE);
                    if (isRequest) {
                        page++;
                        loadData(false);
                    } else {
                        assistInfoListAdapter.changeMoreStatus(assistInfoListAdapter.NO_LOAD_MORE);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                //最后一个可见的ITEM
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();
            }
        });
    }

    @OnClick({R.id.rl_test_time, R.id.rl_report_status, R.id.rl_reply_status, R.id.btn_initiate_assistance, R.id.btn_upload_data, R.id.btn_back, R.id.btn_search})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            //点击选择时间
            case R.id.rl_test_time://DatePickerDialog.THEME_DEVICE_DEFAULT_LIGHT
                showDatePickerDialog(AssistListActivity.this, R.style.MyDatePickerDialogTheme, tvChooseTestTime, Calendar.getInstance());
                break;
            //点击选择上报状态
            case R.id.rl_report_status:
                showReportStatusDialog();
                break;
            //点击选择回复状态
            case R.id.rl_reply_status:
                showReplyStatusDialog();
                break;
            //发起协助
            case R.id.btn_initiate_assistance:
                Intent intent = new Intent(AssistListActivity.this, InitiateAssistanceActivity.class);
                startActivityForResult(intent, 0);
                break;
            //上传数据
            case R.id.btn_upload_data:
                //一键上传所有的未上传的协助
                uploadAllAssist();
                break;
            case R.id.btn_back:
                finish();
                break;
            //点击搜索
            case R.id.btn_search:
                page = 0;
//                searchAssistData();
                loadData(true);
                break;
        }
    }

    private void uploadAllAssist() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<AssistanceDataInfo> tempList = queryNoReport();
                if (tempList.size() == 0) { //本地数据库查出无未上报数据
                    mHandler.sendEmptyMessage(1);
                    return;
                }
                //有未上报数据
                for (int i = 0; i < tempList.size(); i++) {
                    RequestBean requestBean = new RequestBean();
                    requestBean.infoDevId = Constant.DeviceId;
                    requestBean.InfoID = tempList.get(i).getInfoId();
                    requestBean.infoTime = Utils.formatTimeStamp(tempList.get(i).getTestTime());
                    requestBean.infoName = tempList.get(i).getTestName();
                    requestBean.infoAddress = tempList.get(i).getTestPosition();
                    requestBean.infoLength = tempList.get(i).getCableLength();
                    requestBean.infoVoltageLevel = tempList.get(i).getCableType();
                    requestBean.infoFaultType = tempList.get(i).getFaultType();
                    requestBean.infoFaultLength = tempList.get(i).getFaultLength();
                    requestBean.infoMagnetic = tempList.get(i).getDataCollection();
                    requestBean.infoMagneticGain = tempList.get(i).getMagneticFieldGain() + "";//磁场增益
                    requestBean.infoVoiceGain = tempList.get(i).getVoiceGain() + "";//声音增益
                    requestBean.infoFilter = tempList.get(i).getFilterMode() + "";//滤波模式
                    requestBean.infoLanguage = tempList.get(i).getLanguage();//语言类型
                    requestBean.InfoMemo = tempList.get(i).getShortNote();
                    uploadInfo(requestBean);
                }
            }
        }).start();
    }

    //上传服务器数据的方法
    private void uploadInfo(final RequestBean requestBean) {
        final Gson gson = new Gson();
        String json = gson.toJson(requestBean);
        json = TripleDesUtils.encryptMode(MyApplication.keyBytes, json.getBytes());
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(Constant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(Constant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(Constant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URLs.AppUrl + URLs.AppPort)
                .addConverterFactory(ScalarsConverterFactory.create()).client(client)
                .build();
        APIService service = retrofit.create(APIService.class);
        Call<String> call = service.api("UploadInfo", json);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    byte[] srcBytes = TripleDesUtils.decryptMode(MyApplication.keyBytes, response.body());
                    String result = new String(srcBytes);
                    AssistListBean responseBean = gson.fromJson(result, AssistListBean.class);
                    if (responseBean.Code.equals("1")) {
                        AssistanceDataInfo assistanceDataInfo = queryData2(requestBean.InfoID);
                        assistanceDataInfo.setReportStatus("1");
                        updateData(assistanceDataInfo);
                        mHandler.sendEmptyMessage(0);
                    } else if (responseBean.Code.equals("2")) {//数据库里有数据
                        AssistanceDataInfo assistanceDataInfo = queryData2(requestBean.InfoID);
                        assistanceDataInfo.setReportStatus("1");
                        updateData(assistanceDataInfo);
                        mHandler.sendEmptyMessage(0);
                    } else {
                        Utils.showToast(AssistListActivity.this, responseBean.Message);
                    }

                } catch (Exception e) {
                    Log.e("打印-请求报异常-检查代码", "AppInfoList");
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Utils.showToast(AssistListActivity.this, getString(R.string.check_net_retry));
            }
        });
    }

    /**
     * 弹出对话框，提示用户更新
     */
    protected void showSuccessDialog() {
        customDialog = new CustomDialog(AssistListActivity.this);
        customDialog.show();

        customDialog.setHintText(getString(R.string.upload_success));
        customDialog.setLeftButton(getString(R.string.confirm), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();

            }
        });
        customDialog.setRightGone();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //更新插入数据后返回到该页面刷新
        if (requestCode == 0 && resultCode == 1) {
            loadData(true);
        } else if (requestCode == 0 && resultCode == 10) {
            loadData(true);
        } else if (requestCode == 0 && resultCode == 100) {
            loadData(true);
        }
    }


    /**
     * 加载数据
     *
     * @param isFlag //是否显示加载转圈
     */
    private void loadData(final boolean isFlag) {

        new AsyncTask<Void, Boolean, List<AssistanceDataInfo>>() {


            @Override
            protected void onPreExecute() {
                if (isFlag) {
                    srlRefresh.setRefreshing(true);
                }
                super.onPreExecute();
            }

            @Override
            protected List<AssistanceDataInfo> doInBackground(Void... params) {

                List<AssistanceDataInfo> tempAssistList = getTwentyRec(page, startTime, endTime, replyStatus, reportStatus);
                return tempAssistList;
            }

            @Override
            protected void onPostExecute(List<AssistanceDataInfo> tempAssistList) {
                if (page == 0) {
                    assistList.clear();
                }
                assistList.addAll(tempAssistList);

                if (tempAssistList.size() < Constant.PageSize) {
                    isRequest = false;
                } else {
                    isRequest = true;
                }
                //加载数据
                if (tempAssistList.size() != 0) {
                    srlRefresh.setVisibility(View.VISIBLE);
                    llException.setVisibility(View.GONE);
                    assistInfoListAdapter.notifyDataSetChanged();
                    srlRefresh.setRefreshing(false);
                } else {

                    assistInfoListAdapter.notifyDataSetChanged();
                    llException.setVisibility(View.VISIBLE);
                    ivException.setImageResource(R.drawable.ic_no_data);
                    tvException.setText(R.string.no_data);

                }
                //请求状态
                if (!isRequest) {
                    assistInfoListAdapter.changeMoreStatus(assistInfoListAdapter.NO_LOAD_MORE);
                }
                //是否去服务器同步
                if (!isSearch && startTime == 0 && assistList.size() == 0 && page == 0) {
                    //去服务器同步20条数据
                    requestAssist();
                } else {
                    srlRefresh.setRefreshing(false);
                }
                isRefreshing = false;
                super.onPostExecute(tempAssistList);
            }
        }.execute();

    }


    /**
     * 日期选择
     *
     * @param activity
     * @param themeResId
     * @param textView
     * @param calendar
     */
    public static void showDatePickerDialog(final Context activity, int themeResId, final TextView textView, Calendar calendar) {
        // 直接创建一个DatePickerDialog对话框实例，并将它显示出来
        new DatePickerDialog(activity, themeResId, new DatePickerDialog.OnDateSetListener() {
            // 绑定监听器(How the parent is notified that the date is set.)
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // 此处得到选择的时间，可以进行你想要的操作
                String time = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth + " " + "00:00:00";
                String time2 = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth + " " + "23:59:59";
                textView.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                startTime = Utils.getTime(time);
                endTime = Utils.getTime(time2);

            }
        }
                // 设置初始日期
                , calendar.get(Calendar.YEAR)
                , calendar.get(Calendar.MONTH)
                , calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void initReportPopupWindow() {
        reportView = LayoutInflater.from(AssistListActivity.this).inflate(R.layout.dialog_select_report_content_layout, null);
        final TextView tvReported = (TextView) reportView.findViewById(R.id.tv_reported);
        final TextView tvNoReport = (TextView) reportView.findViewById(R.id.tv_no_report);
        reportPopupWindow = new PopupWindow(reportView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        // 设置以下代码，即背景颜色还有外部点击事件的处理才可以点击外部消失,
        reportPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        reportPopupWindow.setOutsideTouchable(true);
        reportPopupWindow.setWidth((int) (screenWidth / 4.8));
//        replyPopupWindow.setHeight((int) (400));
        tvReported.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSearch = true;
                reportStatus = "1";
                tvChooseReportStatus.setText(tvReported.getText());
                reportPopupWindow.dismiss();
            }
        });

        tvNoReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSearch = true;
                reportStatus = "0";
                tvChooseReportStatus.setText(tvNoReport.getText());
                reportPopupWindow.dismiss();
            }
        });
    }

    private void showReportStatusDialog() {
        reportPopupWindow.showAsDropDown(rlReportStatus);
    }

    private void initReplyPopupWindow() {
        replyView = LayoutInflater.from(AssistListActivity.this).inflate(R.layout.dialog_select_reply_content_layout, null);
        final TextView tvReplied = (TextView) replyView.findViewById(R.id.tv_replied);
        final TextView tvNoReply = (TextView) replyView.findViewById(R.id.tv_no_reply);
        replyPopupWindow = new PopupWindow(replyView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        // 设置以下代码，即背景颜色还有外部点击事件的处理才可以点击外部消失,
        replyPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        replyPopupWindow.setOutsideTouchable(true);
        replyPopupWindow.setWidth((int) (screenWidth / 5));
//        replyPopupWindow.setHeight((int) (400));
        tvReplied.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSearch = true;
                replyStatus = "1";
                tvChooseReplyStatus.setText(tvReplied.getText());
                replyPopupWindow.dismiss();
            }
        });

        tvNoReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSearch = true;
                replyStatus = "0";
                tvChooseReplyStatus.setText(tvNoReply.getText());
                replyPopupWindow.dismiss();
            }
        });
    }

    private void showReplyStatusDialog() {
        replyPopupWindow.showAsDropDown(rlReplyStatus);
    }


    public String requestInfoIds = "";

    //查询所列表中所有未回复的id
    private String getNoReplyList() {
        String requestInfoIds = "";
        for (int i = 0; i < assistList.size(); i++) {
            if (assistList.get(i).getReplyStatus().equals("0")) {
                if (assistList.size() - 1 == i) {
                    requestInfoIds = requestInfoIds + assistList.get(i).getInfoId();
                } else {
                    requestInfoIds = requestInfoIds + assistList.get(i).getInfoId() + ",";
                }
            }
        }
        this.requestInfoIds = requestInfoIds;
        Log.e("打印-请求参数", requestInfoIds);
        return requestInfoIds;
    }


    //根据条件查询
    public List<AssistanceDataInfo> getTwentyRec(int page, long startTime, long endTime, String replyStatus, String reportStatus) {
        List<AssistanceDataInfo> listMsg = null;
        if (startTime == 0 && replyStatus.equals("") && reportStatus.equals("")) {
            listMsg = dao.queryBuilder()
                    .offset(page * Constant.PageSize)
                    .orderAsc(AssistanceDataInfoDao.Properties.ReplyStatus, AssistanceDataInfoDao.Properties.ReportStatus)
                    .orderDesc(AssistanceDataInfoDao.Properties.TestTime)
                    .limit(Constant.PageSize)
                    .list();
        } else if (startTime == 0 && replyStatus.equals("") && !reportStatus.equals("")) {
            listMsg = dao.queryBuilder().where(AssistanceDataInfoDao.Properties.ReportStatus.eq(reportStatus))
                    .offset(page * Constant.PageSize)
                    .orderAsc(AssistanceDataInfoDao.Properties.ReplyStatus, AssistanceDataInfoDao.Properties.ReportStatus)
                    .orderDesc(AssistanceDataInfoDao.Properties.TestTime)
                    .limit(Constant.PageSize)
                    .list();
        } else if (startTime == 0 && !replyStatus.equals("") && reportStatus.equals("")) {
            listMsg = dao.queryBuilder().where(AssistanceDataInfoDao.Properties.ReplyStatus.eq(replyStatus))
                    .offset(page * Constant.PageSize)
                    .orderAsc(AssistanceDataInfoDao.Properties.ReplyStatus, AssistanceDataInfoDao.Properties.ReportStatus)
                    .orderDesc(AssistanceDataInfoDao.Properties.TestTime)
                    .limit(Constant.PageSize)
                    .list();
        } else if (startTime == 0 && !replyStatus.equals("") && !reportStatus.equals("")) {
            listMsg = dao.queryBuilder().where(AssistanceDataInfoDao.Properties.ReplyStatus.eq(replyStatus), AssistanceDataInfoDao.Properties.ReportStatus.eq(reportStatus))
                    .offset(page * Constant.PageSize)
                    .orderAsc(AssistanceDataInfoDao.Properties.ReplyStatus, AssistanceDataInfoDao.Properties.ReportStatus)
                    .orderDesc(AssistanceDataInfoDao.Properties.TestTime)
                    .limit(Constant.PageSize)
                    .list();
        } else if (startTime != 0 && replyStatus.equals("") && !reportStatus.equals("")) {
            listMsg = dao.queryBuilder().where(AssistanceDataInfoDao.Properties.TestTime.between(startTime, endTime), AssistanceDataInfoDao.Properties.ReportStatus.eq(reportStatus))
                    .offset(page * Constant.PageSize)
                    .orderAsc(AssistanceDataInfoDao.Properties.ReplyStatus, AssistanceDataInfoDao.Properties.ReportStatus)
                    .orderDesc(AssistanceDataInfoDao.Properties.TestTime)
                    .limit(Constant.PageSize)
                    .list();
        } else if (startTime != 0 && replyStatus.equals("") && reportStatus.equals("")) {
            listMsg = dao.queryBuilder().where(AssistanceDataInfoDao.Properties.TestTime.between(startTime, endTime))
                    .offset(page * Constant.PageSize)
                    .orderAsc(AssistanceDataInfoDao.Properties.ReplyStatus, AssistanceDataInfoDao.Properties.ReportStatus)
                    .orderDesc(AssistanceDataInfoDao.Properties.TestTime)
                    .limit(Constant.PageSize)
                    .list();
        } else if (startTime != 0 && !replyStatus.equals("") && !reportStatus.equals("")) {
            listMsg = dao.queryBuilder().where(AssistanceDataInfoDao.Properties.TestTime.between(startTime, endTime), AssistanceDataInfoDao.Properties.ReplyStatus.eq(replyStatus), AssistanceDataInfoDao.Properties.ReportStatus.eq(reportStatus))
                    .offset(page * Constant.PageSize)
                    .orderAsc(AssistanceDataInfoDao.Properties.ReplyStatus, AssistanceDataInfoDao.Properties.ReportStatus)
                    .orderDesc(AssistanceDataInfoDao.Properties.TestTime)
                    .limit(Constant.PageSize)
                    .list();
        } else if (startTime != 0 && !replyStatus.equals("") && reportStatus.equals("")) {
            listMsg = dao.queryBuilder().where(AssistanceDataInfoDao.Properties.TestTime.between(startTime, endTime), AssistanceDataInfoDao.Properties.ReplyStatus.eq(replyStatus))
                    .offset(page * Constant.PageSize)
                    .orderAsc(AssistanceDataInfoDao.Properties.ReplyStatus, AssistanceDataInfoDao.Properties.ReportStatus)
                    .orderDesc(AssistanceDataInfoDao.Properties.TestTime)
                    .limit(Constant.PageSize)
                    .list();
        }
        return listMsg;

    }

    //修改数据库字段
    private void updateData(AssistanceDataInfo assistanceDataInfo) {
        dao.update(assistanceDataInfo);
    }


    //后台去改变回复状态和回复内容线程
    private boolean isFlag;//线程循环标志
    Thread doRequestStatus = new Thread(new Runnable() {
        @Override
        public void run() {
            Log.e("ddddddddddd", "走我");
            while (!isStop && !isFlag) {
                getNoReplyList();
                isFlag = true;
                final RequestBean requestBean = new RequestBean();
                requestBean.InfoIDS = requestInfoIds;
                final Gson gson = new Gson();
                String json = gson.toJson(requestBean);
                json = TripleDesUtils.encryptMode(MyApplication.keyBytes, json.getBytes());
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(Constant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                        .writeTimeout(Constant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                        .readTimeout(Constant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                        .build();
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(URLs.AppUrl + URLs.AppPort)
                        .addConverterFactory(ScalarsConverterFactory.create()).client(client)
                        .build();
                APIService service = retrofit.create(APIService.class);
                Call<String> call = service.api("GetInfoReplyStatus", json);
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        try {
                            byte[] srcBytes = TripleDesUtils.decryptMode(MyApplication.keyBytes, response.body());
                            String result = new String(srcBytes);
                            Log.e("打印-请求成功", result);
                            AssistInfoReplyStatusBean assistInfoReplyStatusBean = gson.fromJson(result, AssistInfoReplyStatusBean.class);
                            if (assistInfoReplyStatusBean.Code.equals("1")) {
                                if (assistInfoReplyStatusBean.data.size() > 0) {
                                    for (int i = 0; i < assistInfoReplyStatusBean.data.size(); i++) {
                                        AssistanceDataInfo assistanceDataInfo = queryData2(assistInfoReplyStatusBean.data.get(i).InfoID);
                                        assistanceDataInfo.setReplyStatus("1");
                                        assistanceDataInfo.setReplyContent(assistInfoReplyStatusBean.data.get(i).ReplyContent);
                                        updateData(assistanceDataInfo);
                                        for (int j = 0; j < assistList.size(); j++) {
                                            if (assistList.get(j).getInfoId().equals(assistInfoReplyStatusBean.data.get(i).InfoID))
                                                assistList.set(j, assistanceDataInfo);
                                        }
                                        //更改完本地去刷新数据
                                        mHandler.sendEmptyMessage(0);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e("打印-请求报异常-检查代码", "GetInfoReplyStatus");
                        }

                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Utils.showToast(AssistListActivity.this, getString(R.string.check_net_retry));
                        srlRefresh.setRefreshing(false);
                    }
                });

                try {
                    Thread.sleep(20000);
                    isFlag = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    //更据InfoId找到数据库的一条数据
    private AssistanceDataInfo queryData2(String infoId) {
        List<AssistanceDataInfo> assistanceDataInfos = dao.queryBuilder().where(AssistanceDataInfoDao.Properties.InfoId.eq(infoId)).list();
        AssistanceDataInfo dataInfo = assistanceDataInfos.get(0);
        return dataInfo;
    }

    //从本地数据库查找未上报数据
    private List<AssistanceDataInfo> queryNoReport() {
        List<AssistanceDataInfo> assistanceDataInfos = dao.queryBuilder().where(AssistanceDataInfoDao.Properties.ReportStatus.eq("0")).list();
        return assistanceDataInfos;
    }

    @Override
    protected void onStop() {
        //清除状态
        startTime = 0;
        endTime = 0;
        replyStatus = "";
        reportStatus = "";
        tvChooseTestTime.setText("");
        tvChooseReplyStatus.setText("");
        tvChooseReportStatus.setText("");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        isStop = true;
        super.onDestroy();
    }
}
