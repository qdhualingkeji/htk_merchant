package com.hualing.htk_merchant.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ImageUtil {

    private final int UP_PORT = 21;
    private final int DOWN_PORT = 65;
    private final String USERNAME = "htk";
    private final String PASSWORD = "123456";
    private final String ADRESS = "120.27.5.36";
    /**
     * 上传图片方式
     * **/
    public static final int FROMALBUM=0;//选择从相册上传
    public static final int FROMTAKE=1;//选择拍照上传
    /**
     * 从哪里跳转来的
     */
    public static final int ADDFROM=0;//从添加图片跳转
    public static final int EDITFROM=1;//从编辑图片跳转

    private FutureTask<Boolean> uploadTask;

    public ImageUtil(){

    }

    /**
     * Description: 向FTP服务器上传文件
     * @param hostname FTP服务器hostname
     * @param port FTP服务器端口
     * @param username FTP登录账号
     * @param password FTP登录密码
     * @param folder FTP服务器保存目录，是linux下的目录形式,如/photo/
     * @param filename 上传到FTP服务器上的文件名,是自己定义的名字，
     * @param input 输入流
     * @return 成功返回true，否则返回false
     */
    private boolean uploadFileToFTP(String hostname, int port, String username,
                               String password, String folder, String filename, InputStream input) {
        boolean success = false;
        FTPClient ftp = new FTPClient();
        try {
            int reply;
            ftp.connect(hostname, port);// 连接FTP服务器
            // 如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
            ftp.login(username, password);//登录
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return success;
            }
            //不加这一句上传后图片会显示损坏
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.changeWorkingDirectory(folder);
            //设置成其他端口的时候要添加这句话
            ftp.enterLocalPassiveMode();
            ftp.storeFile(filename, input);
            input.close();
            ftp.logout();
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                }
            }
        }
        return success;
    }

    public boolean upLoading(String filePath, final String folder, final String filename) {
        try {
            final FileInputStream in = new FileInputStream(filePath);
            //TODO ftp的连接方式用户名 密码等信息

            Callable<Boolean> callable = new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    if (!Thread.currentThread().isInterrupted()){
                        Log.e("1111111","111111111");
                        return uploadFileToFTP(ADRESS, UP_PORT, USERNAME, PASSWORD,
                                folder,filename, in);
                    }
                    Log.e("2222222","222222222");
                    //正常地终止也返回true
                    return true;
                }
            };

            Log.e("3333","333333");
            uploadTask = new FutureTask<Boolean>(callable);
            new Thread(uploadTask).start();

            //图片上传超过10s则超时,此方法会等待任务结束（正常完成，取消和异常等）
            return uploadTask.get(100000, TimeUnit.MILLISECONDS);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
            Log.w("UploadMethod:", "上传时间超过10s");
        }catch (Exception e) {
            e.printStackTrace();
            Log.e("UploadMethod:", "上传异常");
        }
        return false;
    }

    /**
     * 如果上传任务未完成，可取消
     */
    public void cancelUpload(){
        if (uploadTask.cancel(true)){
            Log.i("UploadMethod：", "取消上传成功");
        }
        else {
            Log.i("UploadMethod：", "取消上传失败");
        }
    }

    /**
     *
     * <p>
     * Description: 获取手机外插SD 存储路径
     * <p>
     * @date 2017-4-18
     * @author
     * @param
     * @return
     */
    public static String getOuterSDPath()
    {
        String sdcard_path = null;
        String sd_default = Environment.getExternalStorageDirectory()
                .getAbsolutePath();
        Log.e("sd_default===", sd_default);
        // sd_default = /storage/emulated/0
        if (sd_default.endsWith("/"))
        {
            sd_default = sd_default.substring(0, sd_default.length() - 1);
        }
        // 得到路径
        try
        {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            String line;
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null)
            {
                Log.e("line===", line);
                if (line.contains("secure"))
                    continue;
                if (line.contains("asec"))
                    continue;
                if (line.contains("fat") && line.contains("/mnt/"))
                {
                    String columns[] = line.split(" ");
                    if (columns != null && columns.length > 1)
                    {
                        if (sd_default.trim().equals(columns[1].trim()))
                        {
                            continue;
                        }
                        sdcard_path = columns[1];
                    }
                }
                else if (line.contains("fuse") && line.contains("/mnt/"))
                {
                    String columns[] = line.split(" ");
                    if (columns != null && columns.length > 1)
                    {
                        if (sd_default.trim().equals(columns[1].trim()))
                        {
                            continue;
                        }
                        sdcard_path = columns[1];
                    }
                }
                else if (line.contains("extSdCard"))
                {
                    String columns[] = line.split(" ");
                    if (columns != null && columns.length > 1)
                    {
                        if (sd_default.trim().equals(columns[1].trim()))
                        {
                            continue;
                        }
                        sdcard_path = columns[1];
                    }
                }
            }
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.e("DownloadPathUtil===", sdcard_path);

        return sdcard_path;
    }

    public static String getPhotoPath(Intent data, Activity activity, int requestCode) {
        // TODO Auto-generated method stub
        String path=null;
        try {
            ContentResolver resolver = activity.getContentResolver();
            File tempPhoto=new File(Environment.getExternalStorageDirectory()+"/PhotosTemp", "tempPhoto.jpg");
            Uri originalUri=null;
            if(requestCode==FROMALBUM)
                originalUri = data.getData(); //获得图片的uri
            else if(requestCode==FROMTAKE)
                originalUri = Uri.parse(android.provider.MediaStore.Images.Media.insertImage(activity.getContentResolver(), tempPhoto.getAbsolutePath(), null, null));
            Bitmap bm = MediaStore.Images.Media.getBitmap(resolver, originalUri); //显得到bitmap图片
            // 这里开始的第二部分，获取图片的路径：
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = activity.managedQuery(originalUri, proj, null, null, null);
            //按我个人理解 这个是获得用户选择的图片的索引值
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            //最后根据索引值获取图片路径
            path = cursor.getString(column_index);
            Log.e("Lostinai====",path);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return path;
    }

}
