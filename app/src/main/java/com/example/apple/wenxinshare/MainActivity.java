package com.example.apple.wenxinshare;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXEmojiObject;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXMusicObject;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXVideoObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final java.lang.String APP_ID = "";
    private IWXAPI wxapi;
    private CheckBox mShareFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wxapi = WXAPIFactory.createWXAPI(this, APP_ID);
        //将APP_ID注册到微信中
        wxapi.registerApp(APP_ID);
        mShareFriends = (CheckBox) findViewById(R.id.cb_share_friends);
    }

    /**
     * 启动微信客户端
     * @param view
     */
    public void onclick_Launch_wenxin(View view){
        Toast.makeText(MainActivity.this, String.valueOf(wxapi.openWXApp()), Toast.LENGTH_SHORT).show();
    }

    /**
     * 为请求生成一个唯一的标识
     */
    private String buildTransaction(final String type) {
        return (type==null)?String.valueOf(System.currentTimeMillis()):type+ System.currentTimeMillis();
    }
    /**
     * 发送文本
     * @param view
     */
    public void onclick_send_text(View view){
        //创建EditText控件 用于输入文本
        final EditText editText = new EditText(this);
        editText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        editText.setText("默认的分享文本!");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle("共享文本");
        //将editText控件与对话框绑定
        builder.setView(editText);
        builder.setMessage("请输入要分享的文本");
        builder.setPositiveButton("分享", new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = editText.getText().toString();
                if (text == null || text.length() == 0) {
                    return;
                }
                //1.初始化创建一个用于封装待分享文本的WXTextObject对象
                WXTextObject textObject = new WXTextObject();
                textObject.text = text;
                //2.创建WXMediaMessage对象，该对象用于android客户端向微信发送数据
                WXMediaMessage msg = new WXMediaMessage();
                msg.mediaObject = textObject;
                msg.description=text;
                //3.创建一个用于请求微信客户端的SendMessageToWX.Req对象
                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.message = msg;
                req.transaction = buildTransaction("text");
                req.scene = mShareFriends.isChecked() ? SendMessageToWX.Req.WXSceneTimeline :
                        SendMessageToWX.Req.WXSceneSession;
                //4.发送给微信客户端
                Toast.makeText(MainActivity.this,String.valueOf(wxapi.sendReq(req)),Toast.LENGTH_SHORT).show();

            }
        });
        builder.setNegativeButton("取消",null);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //将bitmap转换为byte格式的数组
    private byte[] bmpToByteArray(final Bitmap bitmap, final boolean needRecycle) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        if (needRecycle) {
            bitmap.recycle();
        }
        byte[] result = outputStream.toByteArray();
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
    /**
     * 发送二进制图像
     * @param view
     */
    public void onclick_send_binary_image(View view){
        //1.获取Bitmap对象
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        //2.创建WXImageObject对象 包装bitmap
        WXImageObject imageObject = new WXImageObject(bitmap);
        //3.创建WXMediaMessage对象，并包装WXImageObject对象
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imageObject;
        //4.压缩图像
        Bitmap thubBmp = Bitmap.createScaledBitmap(bitmap, 120, 150, true);
        //释放图像所占用的内存资源
        bitmap.recycle();
        msg.thumbData = bmpToByteArray(thubBmp, true);//设置缩略图
        //5.创建对象，用于发送数据
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("img");
        req.message = msg;
        req.scene = mShareFriends.isChecked() ? SendMessageToWX.Req.WXSceneTimeline :
                SendMessageToWX.Req.WXSceneSession;
        Toast.makeText(MainActivity.this, String.valueOf(wxapi.sendReq(req)), Toast.LENGTH_SHORT).show();
        finish();
    }
    /**
     * 发送本地图像
     * @param view
     */
    public void onclick_send_local_image(View view){
        //1.判断图像文件是否存在
        String path = "";
        File file = new File(path);
        if (!file.exists()) {
            Toast.makeText(MainActivity.this, "文件不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        //2.创建WXImageObject对象 包装bitmap
        WXImageObject imageObject = new WXImageObject();
        //设置路径
        imageObject.setImagePath(path);
        //3.创建WXMediaMessage对象，并包装WXImageObject对象
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imageObject;
        //4.压缩图像
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        Bitmap thubBmp = Bitmap.createScaledBitmap(bitmap, 120, 150, true);
        msg.thumbData = bmpToByteArray(bitmap, true);
        //释放图像所占用的内存资源
        bitmap.recycle();
        msg.thumbData = bmpToByteArray(thubBmp, true);//设置缩略图
        //5.创建对象，用于发送数据
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("img");
        req.message = msg;
        req.scene = mShareFriends.isChecked() ? SendMessageToWX.Req.WXSceneTimeline :
                SendMessageToWX.Req.WXSceneSession;
        Toast.makeText(MainActivity.this, String.valueOf(wxapi.sendReq(req)), Toast.LENGTH_SHORT).show();
        finish();
    }
    /**
     * 发送Url图像
     * @param view
     */
    public void onclick_send_url_image(View view){
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    //1.设置URL路径
                    String url = "";
                    //2.创建WXImageObject对象
                    WXImageObject imageObject = new WXImageObject();
                    imageObject.setImagePath(url);
                    //3.创建WXMediaMessage对象，并包装WXImageObject对象
                    WXMediaMessage msg = new WXMediaMessage();
                    msg.mediaObject = imageObject;
                    //4.压缩图像
                    Bitmap bitmap = null;
                    bitmap = BitmapFactory.decodeStream(new URL(url).openStream());
                    Bitmap thubBmp = Bitmap.createScaledBitmap(bitmap, 120, 150, true);
                    msg.thumbData = bmpToByteArray(bitmap, true);
                    //释放图像所占用的内存资源
                    bitmap.recycle();
                    msg.thumbData = bmpToByteArray(thubBmp, true);//设置缩略图
                    //5.创建对象，用于发送数据
                    SendMessageToWX.Req req = new SendMessageToWX.Req();
                    req.transaction = buildTransaction("img");
                    req.message = msg;
                    req.scene = mShareFriends.isChecked() ? SendMessageToWX.Req.WXSceneTimeline :
                            SendMessageToWX.Req.WXSceneSession;
                    Toast.makeText(MainActivity.this, String.valueOf(wxapi.sendReq(req)), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }
    /**
     * 发送Url音频
     * @param view
     */
    public void onclick_send_url_audio(View view){
        //1.创建WXMusicObject对象 用来指定音频URL
        WXMusicObject music = new WXMusicObject();
        music.musicUrl = "http://music.baidu.com/song/999104?pst=sug";
        //2.创建WXMediaMessage对象
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = music;
        msg.title = "小苹果";
        msg.description = "演唱你：简介";
        //3.设置缩略图
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.music);
        msg.thumbData = bmpToByteArray(bitmap, true);
        //4.创建SendMessageToWX.Req 对象
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("music");
        req.message = msg;
        req.scene = mShareFriends.isChecked() ? SendMessageToWX.Req.WXSceneTimeline :
                SendMessageToWX.Req.WXSceneSession;
        Toast.makeText(MainActivity.this, String.valueOf(wxapi.sendReq(req)), Toast.LENGTH_SHORT).show();
    }
    /**
     * 发送Url视频
     * @param view
     */
    public void onclick_send_url_video(View view){
        //1.WXVideoObject 用来指定视频URL
        WXVideoObject video = new WXVideoObject();
        video.videoUrl = "http://v.youku.com/.....";
        //2.创建WXMediaMessage对象
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = video;
        msg.title = "视频";
        msg.description = "简介";
        //3.设置缩略图
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.video);
        msg.thumbData = bmpToByteArray(bitmap, true);
        //4.创建SendMessageToWX.Req 对象
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("video");
        req.message = msg;
        req.scene = mShareFriends.isChecked() ? SendMessageToWX.Req.WXSceneTimeline :
                SendMessageToWX.Req.WXSceneSession;
        Toast.makeText(MainActivity.this, String.valueOf(wxapi.sendReq(req)), Toast.LENGTH_SHORT).show();
        finish();
    }
    /**
     * 发送Url
     * @param view
     */
    public void onclick_send_url(View view){
        //1.WXWebpageObject 用来指定URL
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = "http://www.baidu.com";
        //2.创建WXMediaMessage对象
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = "百度首页";
        msg.description = "简介";
        //3.设置缩略图
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.video);
        msg.thumbData = bmpToByteArray(bitmap, true);
        //4.创建SendMessageToWX.Req 对象
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = msg;
        req.scene = mShareFriends.isChecked() ? SendMessageToWX.Req.WXSceneTimeline :
                SendMessageToWX.Req.WXSceneSession;
        Toast.makeText(MainActivity.this, String.valueOf(wxapi.sendReq(req)), Toast.LENGTH_SHORT).show();
        finish();
    }
    /**
     * 发送表情
     * @param view
     */
    public void onclick_send_emotion(View view){
        //1.WXEmojiObject 用来指定URL
        WXEmojiObject emoji = new WXEmojiObject();
        String EMOJI_FILE_PATH = "";
        emoji.emojiPath =EMOJI_FILE_PATH;
        //2.创建WXMediaMessage对象
        WXMediaMessage msg = new WXMediaMessage(emoji);
        msg.title = "biaoqing";
        msg.description = "简介";
        //3.设置缩略图
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.emotion);
        msg.thumbData = bmpToByteArray(bitmap, true);
        //4.创建SendMessageToWX.Req 对象
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("emotion");
        req.message = msg;
        req.scene = mShareFriends.isChecked() ? SendMessageToWX.Req.WXSceneTimeline :
                SendMessageToWX.Req.WXSceneSession;
        Toast.makeText(MainActivity.this, String.valueOf(wxapi.sendReq(req)), Toast.LENGTH_SHORT).show();
        finish();
    }

}
