package com.kehui.www.testapp.view;

import android.annotation.SuppressLint;
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
 * ??????????????????
 * @author Gong
 * @date 2019/07/22
 */
public class AssistListActivity extends BaseActivity {

    private static final String TAG = "AssistListActivity";

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

    private AssistInfoListAdapter assistInfoListAdapter;
    private List<AssistanceDataInfo> assistList;
    private AssistanceDataInfoDao dao;
    public int page = 0;
    private static long startTime = 0;
    private static long endTime = 0;
    public String replyStatus = "";
    public String reportStatus = "";
    private PopupWindow replyPopupWindow;
    private PopupWindow reportPopupWindow;
    public int screenWidth;
    public int screenHeight;

    /**
     * ??????????????????????????????
     */
    private boolean isRequest;
    private boolean isRefreshing;
    /**
     * ?????????????????????
     */
    private boolean isSearch;
    /**
     * ??????????????????????????????
     */
    private boolean isFlag;
    private boolean isStop;

    /**
     * ????????????ui
     */
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 0) {
                assistInfoListAdapter.notifyDataSetChanged();
                srlRefresh.setRefreshing(false);
            } else if (msg.what == 1) {
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

        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        assert wm != null;
        screenWidth = wm.getDefaultDisplay().getWidth();
        screenHeight = wm.getDefaultDisplay().getHeight();
        initView();
        initData();
        if (Utils.isNetVisible(AssistListActivity.this)) {
            doRequestStatus.start();
        }
    }

    private void initView() {
        initReplyPopupWindow();
        initReportPopupWindow();
        srlRefresh.setColorSchemeResources(R.color.main_color);
        srlRefresh.setDistanceToTriggerSync(0);
        // ???????????????????????????
        srlRefresh.setProgressBackgroundColorSchemeResource(R.color.white);
        // ?????????????????????
        srlRefresh.setSize(2);
        srlRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isRefreshing = true;
                page = 0;
                loadData(false);
            }
        });

        rvList.setItemAnimator(new DefaultItemAnimator());
        rvList.setLayoutManager(new CLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvList.setOnScrollListener(new RecyclerView.OnScrollListener() {
            int lastVisibleItem;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //??????RecyclerView????????? ????????????????????????????????????????????????ITEM????????????
                if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && lastVisibleItem + 1 == assistInfoListAdapter.getItemCount()
                        && !isRefreshing) {
                    //????????????????????????
                    assistInfoListAdapter.changeMoreStatus(AssistInfoListAdapter.LOADING_MORE);
                    if (isRequest) {
                        page++;
                        loadData(false);
                    } else {
                        assistInfoListAdapter.changeMoreStatus(AssistInfoListAdapter.NO_LOAD_MORE);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                //?????????????????????ITEM
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();
            }
        });
    }

    private void initData() {
        assistList = new ArrayList<>();
        assistInfoListAdapter = new AssistInfoListAdapter(AssistListActivity.this, assistList);
        dao = MyApplication.getInstances().getDaoSession().getAssistanceDataInfoDao();
        rvList.setAdapter(assistInfoListAdapter);
        page = 0;
        loadData(true);
    }

    /**
     * ????????????????????????????????????????????????
     */
    Thread doRequestStatus = new Thread(new Runnable() {
        @Override
        public void run() {
            Log.e(TAG, "doRequestStatus????????????");
            while (!isStop && !isFlag) {
                isFlag = true;
                final RequestBean requestBean = new RequestBean();
                //????????????????????????????????????id
                requestBean.InfoIDS = getNoReplyList();
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
                            Log.e(TAG,"?????????????????????" + result);
                            AssistInfoReplyStatusBean assistInfoReplyStatusBean = gson.fromJson(result, AssistInfoReplyStatusBean.class);
                            if ("1".equals(assistInfoReplyStatusBean.Code)) {
                                if (assistInfoReplyStatusBean.data.size() > 0) {
                                    for (int i = 0; i < assistInfoReplyStatusBean.data.size(); i++) {
                                        AssistanceDataInfo assistanceDataInfo = queryData2(assistInfoReplyStatusBean.data.get(i).InfoID);
                                        assistanceDataInfo.setReplyStatus("1");
                                        assistanceDataInfo.setReplyContent(assistInfoReplyStatusBean.data.get(i).ReplyContent);
                                        updateData(assistanceDataInfo);
                                        for (int j = 0; j < assistList.size(); j++) {
                                            if (assistList.get(j).getInfoId().equals(assistInfoReplyStatusBean.data.get(i).InfoID)) {
                                                assistList.set(j, assistanceDataInfo);
                                            }
                                        }
                                        //??????????????????????????????
                                        handler.sendEmptyMessage(0);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e("??????-???????????????-????????????", "GetInfoReplyStatus");
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

    /**
     * @return  ????????????????????????????????????id
     */
    private String getNoReplyList() {
        String requestInfoIds = "";
        for (int i = 0; i < assistList.size(); i++) {
            if ("0".equals(assistList.get(i).getReplyStatus())) {
                if (assistList.size() - 1 == i) {
                    requestInfoIds = requestInfoIds + assistList.get(i).getInfoId();
                } else {
                    requestInfoIds = requestInfoIds + assistList.get(i).getInfoId() + ",";
                }
            }
        }
        Log.e(TAG,"??????????????????" + requestInfoIds);
        return requestInfoIds;
    }

    /**
     * ?????????????????????
     */
    private void updateData(AssistanceDataInfo assistanceDataInfo) {
        dao.update(assistanceDataInfo);
    }

    @OnClick({R.id.rl_test_time, R.id.rl_report_status, R.id.rl_reply_status, R.id.btn_initiate_assistance, R.id.btn_upload_data, R.id.btn_back, R.id.btn_search})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            //??????????????????
            case R.id.rl_test_time:
                //DatePickerDialog.THEME_DEVICE_DEFAULT_LIGHT
                showDatePickerDialog(AssistListActivity.this, R.style.MyDatePickerDialogTheme, tvChooseTestTime, Calendar.getInstance());
                break;
            //????????????????????????
            case R.id.rl_report_status:
                showReportStatusDialog();
                break;
            //????????????????????????
            case R.id.rl_reply_status:
                showReplyStatusDialog();
                break;
            //????????????
            case R.id.btn_initiate_assistance:
                Intent intent = new Intent(AssistListActivity.this, InitiateAssistanceActivity.class);
                startActivityForResult(intent, 0);
                break;
            //????????????
            case R.id.btn_upload_data:
                //???????????????????????????????????????
                uploadAllAssist();
                break;
            case R.id.btn_back:
                finish();
                break;
            //????????????
            case R.id.btn_search:
                page = 0;
//                searchAssistData();
                loadData(true);
                break;
        }
    }

    /**
     * @return  ???????????????????????????????????????
     */
    private List<AssistanceDataInfo> queryNoReport() {
        return dao.queryBuilder().where(AssistanceDataInfoDao.Properties.ReportStatus.eq("0")).list();
    }

    /**
     * ??????????????????????????????
     */
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
                    assert srcBytes != null;
                    String result = new String(srcBytes);
                    AssistListBean responseBean = gson.fromJson(result, AssistListBean.class);
                    if ("1".equals(responseBean.Code)) {
                        AssistanceDataInfo assistanceDataInfo = queryData2(requestBean.InfoID);
                        assistanceDataInfo.setReportStatus("1");
                        updateData(assistanceDataInfo);
                        handler.sendEmptyMessage(0);
                    } else if ("2".equals(responseBean.Code)) {
                        //?????????????????????
                        AssistanceDataInfo assistanceDataInfo = queryData2(requestBean.InfoID);
                        assistanceDataInfo.setReportStatus("1");
                        updateData(assistanceDataInfo);
                        handler.sendEmptyMessage(0);
                    } else {
                        Utils.showToast(AssistListActivity.this, responseBean.Message);
                    }

                } catch (Exception e) {
                    Log.e("??????-???????????????-????????????", "AppInfoList");
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Utils.showToast(AssistListActivity.this, getString(R.string.check_net_retry));
            }
        });
    }

    /**
     * ??????InfoId??????????????????????????????
     * @param infoId    ?????????Id
     * @return  ????????????????????????
     */
    private AssistanceDataInfo queryData2(String infoId) {
        List<AssistanceDataInfo> assistanceDataInfo = dao.queryBuilder().where(AssistanceDataInfoDao.Properties.InfoId.eq(infoId)).list();
        return assistanceDataInfo.get(0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //?????????????????????????????????????????????
        if (requestCode == 0 && resultCode == 1) {
            loadData(true);
        } else if (requestCode == 0 && resultCode == 10) {
            loadData(true);
        } else if (requestCode == 0 && resultCode == 100) {
            loadData(true);
        }
    }

    /**
     * ????????????
     *
     * @param activity
     * @param themeResId
     * @param textView
     * @param calendar
     */
    public static void showDatePickerDialog(final Context activity, int themeResId, final TextView textView, Calendar calendar) {
        // ??????????????????DatePickerDialog???????????????????????????????????????
        new DatePickerDialog(activity, themeResId, new DatePickerDialog.OnDateSetListener() {
            // ???????????????(How the parent is notified that the date is set.)
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // ????????????????????????????????????????????????????????????
                String time = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth + " " + "00:00:00";
                String time2 = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth + " " + "23:59:59";
                textView.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                startTime = Utils.getTime(time);
                endTime = Utils.getTime(time2);
            }
        }
                // ??????????????????
                , calendar.get(Calendar.YEAR)
                , calendar.get(Calendar.MONTH)
                , calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showReportStatusDialog() {
        reportPopupWindow.showAsDropDown(rlReportStatus);
    }

    private void initReplyPopupWindow() {
        View replyView = LayoutInflater.from(AssistListActivity.this).inflate(R.layout.dialog_select_reply_content_layout, null);
        final TextView tvReplied = (TextView) replyView.findViewById(R.id.tv_replied);
        final TextView tvNoReply = (TextView) replyView.findViewById(R.id.tv_no_reply);
        replyPopupWindow = new PopupWindow(replyView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        // ????????????????????????????????????????????????????????????????????????????????????????????????,
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

    private void initReportPopupWindow() {
        View reportView = LayoutInflater.from(AssistListActivity.this).inflate(R.layout.dialog_select_report_content_layout, null);
        final TextView tvReported = (TextView) reportView.findViewById(R.id.tv_reported);
        final TextView tvNoReport = (TextView) reportView.findViewById(R.id.tv_no_report);
        reportPopupWindow = new PopupWindow(reportView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        // ????????????????????????????????????????????????????????????????????????????????????????????????,
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

    private void showReplyStatusDialog() {
        replyPopupWindow.showAsDropDown(rlReplyStatus);
    }

    /**
     * ???????????????????????????
     */
    private void uploadAllAssist() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<AssistanceDataInfo> tempList = queryNoReport();
                if (tempList.size() == 0) {
                    //???????????????????????????????????????
                    handler.sendEmptyMessage(1);
                    return;
                }
                //??????????????????
                for (int i = 0; i < tempList.size(); i++) {
                    RequestBean requestBean = new RequestBean();
                    requestBean.InfoDevID = Constant.DeviceId;
                    requestBean.InfoID = tempList.get(i).getInfoId();
                    requestBean.InfoTime = Utils.formatTimeStamp(tempList.get(i).getTestTime());
                    requestBean.InfoUName = tempList.get(i).getTestName();
                    requestBean.InfoAddress = tempList.get(i).getTestPosition();
                    requestBean.InfoLength = tempList.get(i).getCableLength();
                    requestBean.InfoLineType = tempList.get(i).getCableType();
                    requestBean.InfoFaultType = tempList.get(i).getFaultType();
                    requestBean.InfoFaultLength = tempList.get(i).getFaultLength();
                    requestBean.InfoMemo = tempList.get(i).getShortNote();
                    requestBean.InfoCiChang = tempList.get(i).getDataCollection();
                    requestBean.InfoCiCangVol = tempList.get(i).getMagneticFieldGain() + "";
                    requestBean.InfoShengYinVol = tempList.get(i).getVoiceGain() + "";
                    requestBean.InfoLvBo = tempList.get(i).getFilterMode() + "";
                    requestBean.InfoYuYan = tempList.get(i).getLanguage();
                    uploadInfo(requestBean);
                }
            }
        }).start();
    }

    /**
     * ????????????
     *
     * @param isFlag ????????????????????????
     */
    @SuppressLint("StaticFieldLeak")
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
                return getTwentyRec(page, startTime, endTime, replyStatus, reportStatus);
            }

            @Override
            protected void onPostExecute(List<AssistanceDataInfo> tempAssistList) {
                if (page == 0) {
                    assistList.clear();
                }
                assistList.addAll(tempAssistList);
                isRequest = tempAssistList.size() >= Constant.PageSize;
                //????????????
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
                //????????????
                if (!isRequest) {
                    assistInfoListAdapter.changeMoreStatus(AssistInfoListAdapter.NO_LOAD_MORE);
                }
                //????????????????????????
                if (!isSearch && startTime == 0 && assistList.size() == 0 && page == 0) {
                    //??????????????????20?????????
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
     * ??????????????????
     */
    public List<AssistanceDataInfo> getTwentyRec(int page, long startTime, long endTime, String replyStatus, String reportStatus) {
        List<AssistanceDataInfo> listMsg;
        if (startTime == 0 && "".equals(replyStatus) && "".equals(reportStatus)) {
            listMsg = dao.queryBuilder()
                    .offset(page * Constant.PageSize)
                    .orderAsc(AssistanceDataInfoDao.Properties.ReplyStatus, AssistanceDataInfoDao.Properties.ReportStatus)
                    .orderDesc(AssistanceDataInfoDao.Properties.TestTime)
                    .limit(Constant.PageSize)
                    .list();
        } else if (startTime == 0 && "".equals(replyStatus)) {
            listMsg = dao.queryBuilder().where(AssistanceDataInfoDao.Properties.ReportStatus.eq(reportStatus))
                    .offset(page * Constant.PageSize)
                    .orderAsc(AssistanceDataInfoDao.Properties.ReplyStatus, AssistanceDataInfoDao.Properties.ReportStatus)
                    .orderDesc(AssistanceDataInfoDao.Properties.TestTime)
                    .limit(Constant.PageSize)
                    .list();
        } else if (startTime == 0 && "".equals(reportStatus)) {
            listMsg = dao.queryBuilder().where(AssistanceDataInfoDao.Properties.ReplyStatus.eq(replyStatus))
                    .offset(page * Constant.PageSize)
                    .orderAsc(AssistanceDataInfoDao.Properties.ReplyStatus, AssistanceDataInfoDao.Properties.ReportStatus)
                    .orderDesc(AssistanceDataInfoDao.Properties.TestTime)
                    .limit(Constant.PageSize)
                    .list();
        } else if (startTime == 0) {
            listMsg = dao.queryBuilder().where(AssistanceDataInfoDao.Properties.ReplyStatus.eq(replyStatus), AssistanceDataInfoDao.Properties.ReportStatus.eq(reportStatus))
                    .offset(page * Constant.PageSize)
                    .orderAsc(AssistanceDataInfoDao.Properties.ReplyStatus, AssistanceDataInfoDao.Properties.ReportStatus)
                    .orderDesc(AssistanceDataInfoDao.Properties.TestTime)
                    .limit(Constant.PageSize)
                    .list();
        } else if ("".equals(replyStatus) && !"".equals(reportStatus)) {
            listMsg = dao.queryBuilder().where(AssistanceDataInfoDao.Properties.TestTime.between(startTime, endTime), AssistanceDataInfoDao.Properties.ReportStatus.eq(reportStatus))
                    .offset(page * Constant.PageSize)
                    .orderAsc(AssistanceDataInfoDao.Properties.ReplyStatus, AssistanceDataInfoDao.Properties.ReportStatus)
                    .orderDesc(AssistanceDataInfoDao.Properties.TestTime)
                    .limit(Constant.PageSize)
                    .list();
        } else if ("".equals(replyStatus)) {
            listMsg = dao.queryBuilder().where(AssistanceDataInfoDao.Properties.TestTime.between(startTime, endTime))
                    .offset(page * Constant.PageSize)
                    .orderAsc(AssistanceDataInfoDao.Properties.ReplyStatus, AssistanceDataInfoDao.Properties.ReportStatus)
                    .orderDesc(AssistanceDataInfoDao.Properties.TestTime)
                    .limit(Constant.PageSize)
                    .list();
        } else if (!"".equals(reportStatus)) {
            listMsg = dao.queryBuilder().where(AssistanceDataInfoDao.Properties.TestTime.between(startTime, endTime), AssistanceDataInfoDao.Properties.ReplyStatus.eq(replyStatus), AssistanceDataInfoDao.Properties.ReportStatus.eq(reportStatus))
                    .offset(page * Constant.PageSize)
                    .orderAsc(AssistanceDataInfoDao.Properties.ReplyStatus, AssistanceDataInfoDao.Properties.ReportStatus)
                    .orderDesc(AssistanceDataInfoDao.Properties.TestTime)
                    .limit(Constant.PageSize)
                    .list();
        } else {
            listMsg = dao.queryBuilder().where(AssistanceDataInfoDao.Properties.TestTime.between(startTime, endTime), AssistanceDataInfoDao.Properties.ReplyStatus.eq(replyStatus))
                    .offset(page * Constant.PageSize)
                    .orderAsc(AssistanceDataInfoDao.Properties.ReplyStatus, AssistanceDataInfoDao.Properties.ReportStatus)
                    .orderDesc(AssistanceDataInfoDao.Properties.TestTime)
                    .limit(Constant.PageSize)
                    .list();
        }
        return listMsg;

    }

    /**
     * ?????????????????????????????????
     */
    private void requestAssist() {
        final RequestBean requestBean = new RequestBean();
        requestBean.InfoDevID = Constant.DeviceId;
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
                            if ("1".equals(assistListBean.Code)) {
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
                                    handler.sendEmptyMessage(0);
                                } else {
                                    handler.sendEmptyMessage(2);
                                }
                                handler.sendEmptyMessage(4);
                            } else {
                                Message message = new Message();
                                message.obj = assistListBean.Message;
                                message.what = 3;
                                handler.sendMessage(message);
                            }
                        }
                    }).start();

                } catch (Exception e) {
                    Log.e("??????-???????????????-????????????", "AppInfoList");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Utils.showToast(AssistListActivity.this, getString(R.string.check_net_retry));
                handler.sendEmptyMessage(4);
            }
        });

    }

    @Override
    protected void onStop() {
        //????????????
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
