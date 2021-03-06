package com.hualing.htk_merchant.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import com.hualing.htk_merchant.R;
import com.hualing.htk_merchant.util.AllActivitiesHolder;
import com.hualing.htk_merchant.util.ImageUtil;

import butterknife.OnClick;

public class UploadPhotoActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initLogic() {
        /*
        ImageUtil upload = new ImageUtil();
        //File file = new File("/mnt/m_external_sd/DCIM/Camera/zhoukaixiang.jpg");
        boolean bool = upload.upLoading("/mnt/m_external_sd/DCIM/Camera/zhoukaixiang.jpg", "D:/Resource/htkApp/upload/shop/takeout/", "aaaaa.jpg");
        Log.e("bool===",""+bool);
        */
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                int uploadFrom = getIntent().getIntExtra("uploadFrom", 0);
                if(uploadFrom==ImageUtil.FROMALBUM)
                    uploadFromAlbum();
                else if(uploadFrom==ImageUtil.FROMTAKE)
                    uploadFromTake();
            }
        },1000);
    }

    @Override
    protected void getDataFormWeb() {

    }

    @Override
    protected void debugShow() {

    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_upload_phone;
    }

    /**
     * 从相册上传
     * **/
    public void uploadFromAlbum() {
        // TODO Auto-generated method stub
        /*
        Intent intent=new Intent();
        intent.setType("image/");
        intent.setAction(intent.ACTION_GET_CONTENT);
        */
        Intent intent = new Intent(Intent.ACTION_PICK);//intent  action属性
        intent.setType("image/*");//选择图片
        startActivityForResult(intent, ImageUtil.FROMALBUM);
    }

    /**
     * 拍照上传
     * **/
    public void uploadFromTake() {
        // TODO Auto-generated method stub
        //下面是调用系统的照相机拍照
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //启动拍照设备，等待处理拍照结果
        startActivityForResult(intent,  ImageUtil.FROMTAKE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //拍摄图片返回情况
        int activityFrom = getIntent().getIntExtra("activityFrom", 0);
        Intent intent=new Intent(UploadPhotoActivity.this,activityFrom==ImageUtil.ADDFROM?AddProductActivity.class:EditProductActivity.class);

        String productParamsJOStr = getIntent().getStringExtra("productParamsJOStr");
        intent.putExtra("productParamsJOStr", productParamsJOStr);

        if(activityFrom==ImageUtil.EDITFROM) {
            int productId = getIntent().getIntExtra("productId", 0);
            intent.putExtra("productId",productId);
        }

        String productPropertyJAStr = getIntent().getStringExtra("productPropertyJAStr");
        intent.putExtra("productPropertyJAStr", productPropertyJAStr);

        if(resultCode==RESULT_OK){
            String tempPhotoPath = null;
            if(requestCode==ImageUtil.FROMALBUM)
                tempPhotoPath=ImageUtil.getPhotoPath(data,UploadPhotoActivity.this,ImageUtil.FROMALBUM);
            else if(requestCode==ImageUtil.FROMTAKE)
                tempPhotoPath=ImageUtil.getPhotoPath(data,UploadPhotoActivity.this,ImageUtil.FROMTAKE);
            intent.putExtra("tempPhotoPath", tempPhotoPath);
        }
        intent.putExtra("reload",true);
        startActivity(intent);
        finish();
    }
}
