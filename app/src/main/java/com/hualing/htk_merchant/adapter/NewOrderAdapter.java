package com.hualing.htk_merchant.adapter;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hualing.htk_merchant.R;
import com.hualing.htk_merchant.activities.MainActivity;
import com.hualing.htk_merchant.entity.OrderRecordEntity;
import com.hualing.htk_merchant.global.GlobalData;
import com.hualing.htk_merchant.model.CommonMsg;
import com.hualing.htk_merchant.model.OrderProduct;
import com.hualing.htk_merchant.util.JPushUtil;
import com.hualing.htk_merchant.utils.AsynClient;
import com.hualing.htk_merchant.utils.GsonHttpResponseHandler;
import com.hualing.htk_merchant.utils.MyHttpConfing;
import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NewOrderAdapter extends BaseAdapter {

    private static final int ZI_XING = 0;
    static final int WAI_ZHAO = 1;
    public List<OrderRecordEntity.DataBean> getmData() {
        return mData;
    }

    public void setmData(List<OrderRecordEntity.DataBean> mData) {
        this.mData = mData;
    }

    private List<OrderRecordEntity.DataBean> mData;
    private MainActivity context;
    private Button mDot1;

    public NewOrderAdapter(MainActivity context,Button mDot1){
        this.context = context;
        this.mDot1=mDot1;
        mData=new ArrayList<OrderRecordEntity.DataBean>();
    }

    public void setNewData(){
        RequestParams params = AsynClient.getRequestParams();
        params.put("userId", GlobalData.userID);
        params.put("statusCode", 0);

        AsynClient.post(MyHttpConfing.getNewOrderList, context, params, new GsonHttpResponseHandler() {
            @Override
            protected Object parseResponse(String rawJsonData) throws Throwable {
                return null;
            }

            @Override
            public void onFailure(int statusCode, String rawJsonData, Object errorResponse) {
                Log.e("rawJsonData======",""+rawJsonData);
            }

            @Override
            public void onSuccess(int statusCode, String rawJsonResponse, Object response) {
                Log.e("rawJsonResponse======",""+rawJsonResponse);

                Gson gson = new Gson();
                OrderRecordEntity orderRecordEntity = gson.fromJson(rawJsonResponse, OrderRecordEntity.class);
                if (orderRecordEntity.getCode() == 100) {
                    mData = orderRecordEntity.getData();
                }
                else{
                    mData.clear();
                }
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getCount() {
        int size = mData.size();
        mDot1.setText("新订单("+size+")");
        return size;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView==null){
            convertView = context.getLayoutInflater().inflate(R.layout.item_new_order,parent,false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        final OrderRecordEntity.DataBean orderRecord = mData.get(position);
        holder.orderNumberTV.setText("订单:"+orderRecord.getOrderNumber());
        holder.statusCodeTV.setText("订单状态（待接单）");
        holder.orderTimeTV.setText(orderRecord.getOrderTime());
        holder.receiptNameTV.setText(orderRecord.getReceiptName().substring(0,1)+(orderRecord.getSex()==0?"女士":"先生"));
        holder.receivingCallTV.setText(orderRecord.getReceivingCall());
        holder.shippingAddressTV.setText(orderRecord.getShippingAddress());

        NewOrderProductAdapter opAdapter = holder.orderProductAdapter;
        opAdapter = new NewOrderProductAdapter(context);
        opAdapter.setNewData(orderRecord.getProductLists());
        opAdapter.notifyDataSetChanged();
        holder.orderProductLV.setAdapter(opAdapter);
        context.setListViewHeightBasedOnChildren(holder.orderProductLV);
        holder.priceCanheTV.setText(orderRecord.getPriceCanhe()+"￥");
        holder.deliveryFeeTV.setText(orderRecord.getDeliveryFee()+"￥");
        holder.remarkTV.setText(orderRecord.getRemark());
        holder.paidTV.setText("（已支付）￥"+orderRecord.getOrderAmount());

        int deliveryFlag = 0;
        if(holder.ziXingRB.isChecked())
            deliveryFlag = ZI_XING;
        else
            deliveryFlag = WAI_ZHAO;
        final int finalDeliveryFlag = deliveryFlag;
        holder.acceptBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmOrder(orderRecord.getOrderNumber(),position, finalDeliveryFlag);
            }
        });
        holder.refuseBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelOrder(orderRecord.getOrderNumber(),orderRecord.getAccountToken(),position);
            }
        });
        return convertView;
    }

    class ViewHolder {

        @BindView(R.id.orderNumber_tv)
        TextView orderNumberTV;
        @BindView(R.id.statusCode_tv)
        TextView statusCodeTV;
        @BindView(R.id.orderTime_tv)
        TextView orderTimeTV;
        @BindView(R.id.receiptName_tv)
        TextView receiptNameTV;
        @BindView(R.id.receivingCall_tv)
        TextView receivingCallTV;
        @BindView(R.id.shippingAddress_tv)
        TextView shippingAddressTV;
        @BindView(R.id.orderProduct_lv)
        ListView orderProductLV;
        private NewOrderProductAdapter orderProductAdapter;
        @BindView(R.id.priceCanhe_tv)
        TextView priceCanheTV;
        @BindView(R.id.deliveryFee_tv)
        TextView deliveryFeeTV;
        @BindView(R.id.remark_tv)
        TextView remarkTV;
        @BindView(R.id.paid_tv)
        TextView paidTV;
        @BindView(R.id.ziXing_rb)
        RadioButton ziXingRB;
        @BindView(R.id.waiZhao_rb)
        RadioButton waiZhaoRB;
        @BindView(R.id.accept_but)
        Button acceptBut;
        @BindView(R.id.refuse_but)
        Button refuseBut;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    /**
     *  确认订单
     * @param orderNumber
     * @param position
     */
    private void confirmOrder(final String orderNumber, final int position, final int deliveryFlag){
        context.showLoadingDialog(context,"接单中");
        RequestParams params = AsynClient.getRequestParams();
        params.put("orderNumber", orderNumber);
        AsynClient.post(MyHttpConfing.confirmTheOrder, context, params, new GsonHttpResponseHandler() {
            @Override
            protected Object parseResponse(String rawJsonData) throws Throwable {
                return null;
            }

            @Override
            public void onFailure(int statusCode, String rawJsonData, Object errorResponse) {
                Log.e("rawJsonData===",""+rawJsonData);
                context.hideLoadingDialog();
            }

            @Override
            public void onSuccess(int statusCode, String rawJsonResponse, Object response) {
                Log.e("rawJsonResponse===",""+rawJsonResponse);
                //Log.i(MyHttpConfing.tag, rawJsonResponse);

                Gson gson = new Gson();
                CommonMsg commonMsg = gson.fromJson(rawJsonResponse, CommonMsg.class);
                if(commonMsg.getCode()==0){
                    //mData.remove(position);
                    //notifyDataSetChanged();
                    if(deliveryFlag==ZI_XING)
                        ziXingDelivery(orderNumber,position);
                    else
                        context.showMessage("外招配送功能尚未开通");
                }

                context.showMessage(commonMsg.getMessage());
                context.hideLoadingDialog();
            }
        });
    }

    /**
     * 取消订单
     * @param orderNumber
     * @param position
     */
    private void cancelOrder(String orderNumber,  String token, final int position) {
        context.showLoadingDialog(context,"拒单中");
        RequestParams params = AsynClient.getRequestParams();
        params.put("orderNumber", orderNumber);
        params.put("content", "其他原因");
        params.put("mark", 0);
        params.put("accountToken", token);
        AsynClient.post(MyHttpConfing.cancelOrder, context, params, new GsonHttpResponseHandler() {
            @Override
            protected Object parseResponse(String rawJsonData) throws Throwable {
                return null;
            }

            @Override
            public void onFailure(int statusCode, String rawJsonData, Object errorResponse) {
                Log.e("rawJsonData===",""+rawJsonData);
                context.hideLoadingDialog();
            }

            @Override
            public void onSuccess(int statusCode, String rawJsonResponse, Object response) {
                Log.e("rawJsonResponse===",""+rawJsonResponse);
                //Log.i(MyHttpConfing.tag, rawJsonResponse);

                Gson gson = new Gson();
                CommonMsg commonMsg = gson.fromJson(rawJsonResponse, CommonMsg.class);
                mData.remove(position);
                notifyDataSetChanged();
                JPushUtil.sendNotification(context, GlobalData.userName, "商家接单app", "取消订单成功", JPushUtil.CANNEL_ORDER);
                context.showMessage(commonMsg.getMessage());
                context.hideLoadingDialog();
            }
        });

    }

    /**
     * 自行配送
     * @param orderNumber
     * @param position
     */
    private void ziXingDelivery(String orderNumber, final int position){
        RequestParams params = AsynClient.getRequestParams();
        params.put("orderNumber", orderNumber);
        AsynClient.post(MyHttpConfing.orderItemsToShip, context, params, new GsonHttpResponseHandler() {
            @Override
            protected Object parseResponse(String rawJsonData) throws Throwable {
                return null;
            }

            @Override
            public void onFailure(int statusCode, String rawJsonData, Object errorResponse) {
                Log.e("rawJsonData===",""+rawJsonData);

            }

            @Override
            public void onSuccess(int statusCode, String rawJsonResponse, Object response) {
                Log.e("rawJsonResponse===",""+rawJsonResponse);
                //Log.i(MyHttpConfing.tag, rawJsonResponse);

                Gson gson = new Gson();
                CommonMsg commonMsg = gson.fromJson(rawJsonResponse, CommonMsg.class);
                if(commonMsg.getCode()==0){
                    mData.remove(position);
                    notifyDataSetChanged();
                    JPushUtil.sendNotification(context, GlobalData.userName, "商家接单app", "确认订单成功", JPushUtil.CONFIRM_ORDER);
                    context.getmViewPager().setCurrentItem(1);
                }

                context.showMessage(commonMsg.getMessage());

            }
        });
    }
}
