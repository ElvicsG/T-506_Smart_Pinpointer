package com.kehui.www.testapp.view;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.XXXX.dao.db.AssistanceDataInfoDao;
import com.google.gson.Gson;
import com.kehui.www.testapp.R;
import com.kehui.www.testapp.application.Constant;
import com.kehui.www.testapp.application.MyApplication;
import com.kehui.www.testapp.application.URLs;
import com.kehui.www.testapp.bean.AssistListBean;
import com.kehui.www.testapp.bean.RequestBean;
import com.kehui.www.testapp.database.AssistanceDataInfo;
import com.kehui.www.testapp.retrofit.APIService;
import com.kehui.www.testapp.util.DES3Utils;
import com.kehui.www.testapp.util.Utils;

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
 * 协助详情页面
 */

public class AssistInfoDetailsActivity extends BaseActivity {
    @BindView(R.id.tv_test_time_value)
    TextView tvTestTimeValue;
    @BindView(R.id.tv_test_name)
    TextView tvTestName;
    @BindView(R.id.tv_test_position)
    TextView tvTestPosition;
    @BindView(R.id.tv_cable_length)
    TextView tvCableLength;
    @BindView(R.id.tv_cable_type)
    TextView tvCableType;
    @BindView(R.id.tv_fault_type)
    TextView tvFaultType;
    @BindView(R.id.tv_fault_length)
    TextView tvFaultLength;
    @BindView(R.id.tv_short_note)
    TextView tvShortNote;
    @BindView(R.id.tv_report_status)
    TextView tvReportStatus;
    @BindView(R.id.tv_reply_content)
    TextView tvReplyContent;
    @BindView(R.id.btn_commit)
    Button btnCommit;
    @BindView(R.id.btn_back)
    Button btnBack;
    @BindView(R.id.tv_reply_status)
    TextView tvReplyStatus;
    private AssistanceDataInfoDao dao;
    private String infoId;
    private List<AssistanceDataInfo> assistanceDataInfos;
    AssistanceDataInfo dataInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assist_info_details);
        ButterKnife.bind(this);
        dao = MyApplication.getInstances().getDaoSession().getAssistanceDataInfoDao();
        infoId = getIntent().getStringExtra("infoId");
        queryData2();
        updateView();
    }

    private void updateView() {
        tvTestTimeValue.setText(Utils.formatTimeStamp(dataInfo.getTestTime()));
        tvTestName.setText(dataInfo.getTestName().trim());
        tvTestPosition.setText(dataInfo.getTestPosition().trim());
        tvCableLength.setText(dataInfo.getCableLength().trim());
        tvCableType.setText(dataInfo.getCableType().trim());
        tvFaultType.setText(dataInfo.getFaultType().trim());
        tvFaultLength.setText(dataInfo.getFaultLength().trim());
        tvShortNote.setText(dataInfo.getShortNote().trim());
        tvReportStatus.setText(dataInfo.getReportStatus().trim());
        tvReplyStatus.setText(dataInfo.getReplyStatus().trim());
        tvReplyContent.setText(dataInfo.getReplyContent().trim());
        if (dataInfo.getReportStatus().equals("0")) {
            btnCommit.setVisibility(View.VISIBLE);
            tvReportStatus.setText(getString(R.string.no_report));
            tvReportStatus.setTextColor(getResources().getColor(R.color.yellow2));
        } else {
            btnCommit.setVisibility(View.GONE);
            tvReportStatus.setText(getString(R.string.reported));
            tvReportStatus.setTextColor(getResources().getColor(R.color.blue5));
        }
        if (dataInfo.getReplyStatus().equals("0")) {
            tvReplyStatus.setText(getString(R.string.no_reply));
            tvReplyStatus.setTextColor(getResources().getColor(R.color.yellow2));
        } else {
            tvReplyStatus.setText(getString(R.string.replied));
            tvReplyStatus.setTextColor(getResources().getColor(R.color.blue5));
        }
    }

    private void queryData2() {
        assistanceDataInfos = dao.queryBuilder().where(AssistanceDataInfoDao.Properties.InfoId.eq(infoId)).list();
        dataInfo = assistanceDataInfos.get(0);
        Log.e("姓名", assistanceDataInfos.get(0).getTestName());
    }

    @OnClick({R.id.btn_commit, R.id.btn_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_commit:

                RequestBean requestBean = new RequestBean();
                requestBean.InfoDevID = Constant.DeviceId;
                requestBean.InfoID = infoId;
                requestBean.InfoTime = Utils.formatTimeStamp(dataInfo.getTestTime());
                requestBean.InfoUName = dataInfo.getTestName();
                requestBean.InfoAddress = dataInfo.getTestPosition();
                requestBean.InfoLength = dataInfo.getCableLength();
                requestBean.InfoLineType = dataInfo.getCableType();
                requestBean.InfoFaultType = dataInfo.getFaultType();
                requestBean.InfoFaultLength = dataInfo.getFaultLength();
                requestBean.InfoCiChang = dataInfo.getDataCollection();
                requestBean.InfoCiCangVol = dataInfo.getMagneticFieldGain() + "";//磁场增益
                requestBean.InfoShengYinVol = dataInfo.getVoiceGain() + "";//声音增益
                requestBean.InfoLvBo = dataInfo.getFilterMode() + "";//滤波模式
                requestBean.InfoYuYan = dataInfo.getLanguage();//语言类型
                requestBean.InfoMemo = dataInfo.getShortNote();
                uploadInfo(requestBean);
                break;
            case R.id.btn_back:
                setResult(100);
                finish();
                break;
        }
    }

    private void uploadInfo(final RequestBean requestBean) {
        final Gson gson = new Gson();
        String json = gson.toJson(requestBean);
        json = DES3Utils.encryptMode(MyApplication.keyBytes, json.getBytes());
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
                    byte[] srcBytes = DES3Utils.decryptMode(MyApplication.keyBytes, response.body());
                    String result = new String(srcBytes);
                    AssistListBean responseBean = gson.fromJson(result, AssistListBean.class);
                    if (responseBean.Code.equals("1")) {
                        Utils.showToast(AssistInfoDetailsActivity.this, getString(R.string.upload_success));
                        AssistanceDataInfo assistanceDataInfo = queryData2(requestBean.InfoID);
                        assistanceDataInfo.setReportStatus("1");
                        updateData(assistanceDataInfo);
                        setResult(10);
                        finish();
                    } else {
                        Utils.showToast(AssistInfoDetailsActivity.this, responseBean.Message);
                    }
                } catch (Exception e) {
                    Log.e("打印-请求报异常-检查代码", "AppInfoList");
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Utils.showToast(AssistInfoDetailsActivity.this, getString(R.string.check_net_retry));
            }
        });
    }

    //更据InfoId找到数据库的一条数据
    private AssistanceDataInfo queryData2(String infoId) {
        List<AssistanceDataInfo> assistanceDataInfos = dao.queryBuilder().where(AssistanceDataInfoDao.Properties.InfoId.eq(infoId)).list();
        AssistanceDataInfo dataInfo = assistanceDataInfos.get(0);
        return dataInfo;
    }

    //修改数据库字段
    private void updateData(AssistanceDataInfo assistanceDataInfo) {
        dao.update(assistanceDataInfo);
    }
}
