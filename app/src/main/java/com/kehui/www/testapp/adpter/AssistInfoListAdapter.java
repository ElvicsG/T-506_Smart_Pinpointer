package com.kehui.www.testapp.adpter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kehui.www.testapp.R;
import com.kehui.www.testapp.database.AssistanceDataInfo;
import com.kehui.www.testapp.util.Utils;
import com.kehui.www.testapp.view.AssistInfoDetailsActivity;
import com.kehui.www.testapp.view.AssistListActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author jwj
 * @date 2018/04/25
 */
public class AssistInfoListAdapter extends RecyclerView.Adapter {

    private AssistListActivity context;
    private List<AssistanceDataInfo> assistList;
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;

    /**
     * 正在加载中
     */
    public static final int LOADING_MORE = 1;
    /**
     * 没有加载更多 隐藏
     */
    public static final int NO_LOAD_MORE = 2;

    /**
     * 上拉加载更多状态-默认为0
     */
    private int mLoadMoreStatus = 0;

    public AssistInfoListAdapter(AssistListActivity context, List<AssistanceDataInfo> assistList) {
        this.context = context;
        this.assistList = assistList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_assist_info_list, parent, false);

            return new ViewHolder(itemView);
        } else if (viewType == TYPE_FOOTER) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.layout_load_more_footview, parent, false);
            return new FooterViewHolder(itemView);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ViewHolder) {
            final ViewHolder viewHolder = (ViewHolder) holder;
            viewHolder.tvTestTime.setText(Utils.formatTimeStamp(assistList.get(position).getTestTime()));
            viewHolder.tvTestName.setText(assistList.get(position).getTestName().trim());
            if ("0".equals(assistList.get(position).getReplyStatus())) {
                //未回复
                viewHolder.ivReplyStatus.setImageResource(R.drawable.ic_assist_no_reply);
                viewHolder.tvReplyStatus.setTextColor(context.getResources().getColor(R.color.yellow2));
                viewHolder.tvReplyStatusText.setTextColor(context.getResources().getColor(R.color.yellow2));
                viewHolder.tvReplyStatusTextColon.setTextColor(context.getResources().getColor(R.color.yellow2));
                viewHolder.tvReplyStatus.setText(context.getString(R.string.no_reply));
            } else {
                viewHolder.ivReplyStatus.setImageResource(R.drawable.ic_assist_replied);
                viewHolder.tvReplyStatus.setTextColor(context.getResources().getColor(R.color.blue5));
                viewHolder.tvReplyStatusText.setTextColor(context.getResources().getColor(R.color.blue5));
                viewHolder.tvReplyStatusTextColon.setTextColor(context.getResources().getColor(R.color.blue5));
                viewHolder.tvReplyStatus.setText(context.getString(R.string.replied));
            }
            if ("1".equals(assistList.get(position).getReportStatus())) {
                //1已上报 0未上报 从服务器取下来的都是上报后的
                viewHolder.tvReportStatus.setTextColor(context.getResources().getColor(R.color.blue5));
                viewHolder.tvReportStatusText.setTextColor(context.getResources().getColor(R.color.blue5));
                viewHolder.tvReportStatusTextColon.setTextColor(context.getResources().getColor(R.color.blue5));
                viewHolder.tvReportStatus.setText(context.getString(R.string.reported));
            } else {
                viewHolder.tvReportStatus.setTextColor(context.getResources().getColor(R.color.white));
                viewHolder.tvReportStatusText.setTextColor(context.getResources().getColor(R.color.white));
                viewHolder.tvReportStatusTextColon.setTextColor(context.getResources().getColor(R.color.white));
                viewHolder.tvReportStatus.setText(context.getString(R.string.no_report));
            }
            viewHolder.rlContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra("infoId", assistList.get(position).getInfoId());
                    intent.setClass(context, AssistInfoDetailsActivity.class);
                    context.startActivityForResult(intent, 0);
                }
            });

        } else if (holder instanceof FooterViewHolder) {
            FooterViewHolder footerViewHolder = (FooterViewHolder) holder;
            switch (mLoadMoreStatus) {
                case LOADING_MORE:
                    footerViewHolder.mPbLoad.setVisibility(View.VISIBLE);
                    footerViewHolder.mTvLoadText.setText(context.getString(R.string.loading_more));
                    break;
                case NO_LOAD_MORE:
                    //隐藏加载更多
                    footerViewHolder.mPbLoad.setVisibility(View.GONE);
                    footerViewHolder.mTvLoadText.setText(context.getString(R.string.no_load_more));
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return assistList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {

        if (position + 1 == getItemCount()) {
            //最后一个item设置为footerView
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    /**
     * @param status    加载更多状态
     */
    public void changeMoreStatus(int status) {
        mLoadMoreStatus = status;
        notifyDataSetChanged();
    }

    class FooterViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.pbLoad)
        ProgressBar mPbLoad;
        @BindView(R.id.tvLoadText)
        TextView mTvLoadText;
        @BindView(R.id.loadLayout)
        LinearLayout mLoadLayout;

        FooterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_reply_status)
        ImageView ivReplyStatus;
        @BindView(R.id.tv_test_time)
        TextView tvTestTime;
        @BindView(R.id.tv_test_name)
        TextView tvTestName;
        @BindView(R.id.ll_container)
        LinearLayout llContainer;
        @BindView(R.id.tv_report_status_text)
        TextView tvReportStatusText;
        @BindView(R.id.tv_report_status_text_colon)
        TextView tvReportStatusTextColon;
        @BindView(R.id.tv_report_status)
        TextView tvReportStatus;
        @BindView(R.id.tv_reply_status_text)
        TextView tvReplyStatusText;
        @BindView(R.id.tv_reply_status_text_colon)
        TextView tvReplyStatusTextColon;
        @BindView(R.id.tv_reply_status)
        TextView tvReplyStatus;
        @BindView(R.id.rl_container)
        LinearLayout rlContainer;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}
