package com.imt.musiclamp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.TransactionTooLargeException;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.zxing.WriterException;
import com.imt.musiclamp.elementClass.StrUrl;
import com.imt.musiclamp.elementClass.StringURL;
import com.imt.musiclamp.elementClass.UserServer;
import com.imt.musiclamp.fragment.FragmentLogin;
import com.imt.musiclamp.service.ServerSocketService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.UUID;

import QR.BitmapUtil;

/**
 * Created by dtw on 15/3/18.
 */
public class SetUserInfoActivity extends Activity implements View.OnClickListener {

    private ImageView back;
    private LinearLayout change_password;

    ImageView head;
    TextView nickName, info;
    TextView general;
    View passL;
    String userID = ((MyApplication) getApplication()).userID;
    Handler handler = new Handler();
    //    String headIcon = "http://sc.jb51.net/uploads/allimg/131110/2-131110225G41K.jpg";
    String actionUrl = "http://git.imt66.com:8080/phone/fileupload1.do";
//    String actionUrl = "http://192.168.1.136:8080/superapp/fileupload1.do";
    String headUri = "";
    final int GetChangeText = 88;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setuserinfo);

        back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(this);

        change_password = (LinearLayout) findViewById(R.id.change_password);
        change_password.setOnClickListener(this);

        findViewById(R.id.nametouch).setOnClickListener(this);
        findViewById(R.id.infotouch).setOnClickListener(this);
        findViewById(R.id.gendertouch).setOnClickListener(this);
        findViewById(R.id.qrtouch).setOnClickListener(this);
        head = (ImageView) findViewById(R.id.head);
        nickName = (TextView) findViewById(R.id.nickname);
        general = (TextView) findViewById(R.id.gender);
        info = (TextView) findViewById(R.id.userinfo);
        Bundle bundle = getIntent().getExtras();
        headUri = bundle.getString("userHeadUrl");
        ((TextView)findViewById(R.id.userID)).setText(MyApplication.userID);
        Glide.with(this)
                .load(headUri)
                .centerCrop()
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                head.setImageResource(R.drawable.defalthead);
                            }
                        });
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        return false;
                    }
                })
                .crossFade()
                .into(head);

        nickName.setText(bundle.getString("nickname"));

        Log.v("setgetsex", String.valueOf(bundle.getString("gender")));

        general.setText(bundle.getString("gender"));
        info.setText(bundle.getString("userInfo"));
        final String userPhone = getIntent().getExtras().getString("userPhone");


        findViewById(R.id.sethead).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra("crop", "true");
/*保存到SD*/
                intent.putExtra("output", getCacheDir());
/*设置图片像素*/
                intent.putExtra("outputX", 100);
                intent.putExtra("outputY", 100);
/*设置图片格式*/
                intent.putExtra("outputFormat", "JPEG");
/* 设置比例 1:1 */
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                startActivityForResult(intent, 1);


//                Intent intentFromGallery = new Intent();
//                intentFromGallery.setType("image/*");
//                intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(intentFromGallery, 1);
            }
        });


    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==4) {
            head.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(head.getDrawingCache());
            head.setDrawingCacheEnabled(false);
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null, null));
            bundle.putString("userHeadUrl", uri.toString());
            bundle.putString("nickname", nickName.getText().toString());
            bundle.putString("gender", general.getText().toString());
            bundle.putString("userInfo", info.getText().toString());
//        bundle.putString("userPhone", userPhone);

            intent.putExtras(bundle);
            String urlTemp = new StringURL(StringURL.changeUserInfo).setGender(general.getText().toString()).setNickName(nickName.getText().toString()).setUserID(userID).toString();
            Log.v("", urlTemp);
            new UserServer(SetUserInfoActivity.this, handler, urlTemp) {

                @Override
                public void httpBack(JSONObject jsonObject) {

                    if (StrUrl.getResult(jsonObject).equals("1")) {
                        Toast.makeText(SetUserInfoActivity.this, "提交用户信息成功", Toast.LENGTH_SHORT).show();
                        Log.v("", jsonObject.toString());
                    }
                }
            };
            new UserServer(SetUserInfoActivity.this, handler, new StringURL(StringURL.upSign).setUserID(MyApplication.userID).setSing(info.getText().toString()).toString()) {

                @Override
                public void httpBack(JSONObject jsonObject) throws JSONException {
                    Log.v("upsign", jsonObject.toString());
                }
            };
            SetUserInfoActivity.this.setResult(RESULT_OK, intent);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri;
        if (data == null) {
            return;
        }
        switch (requestCode) {
            case 1:
                uri = data.getData();
                Log.v("uri", uri.toString());

                ContentResolver cr = this.getContentResolver();
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));

//                    BitmapFactory.Options options = new BitmapFactory.Options();
//                    options.inSampleSize=2;//图片高宽度都为原来的二分之一，即图片大小为原来的大小的四分之一
////                    options.inTempStorage = new byte[5*1024]; //设置16MB的临时存储空间（不过作用还没看出来，待验证）
//                    Bitmap bitmap = BitmapFactory.decodeFile(uri.toString(), options);

                    int w = bitmap.getWidth(); // 得到图片的宽，高
                    int h = bitmap.getHeight();
                    w = w > h ? 100 * w / h : 100;
                    h = h > w ? 100 * h / w : 100;
                    Bitmap bitmap1 = ThumbnailUtils.extractThumbnail(bitmap, w, h);

                    int wh = w > h ? h : w;// 裁切后所取的正方形区域边长

                    int retX = w > h ? (w - h) / 2 : 0;//基于原图，取正方形左上角x坐标
                    int retY = w > h ? 0 : (h - w) / 2;

                    //下面这句是关键
                    bitmap1 = Bitmap.createBitmap(bitmap1, retX, retY, wh, wh, null, false);
//                    bitmap = zoomBitmap(bitmap, 300,300);

                    head.setImageBitmap(bitmap1);
//                    ImageView imageView = (ImageView) findViewById(R.id.head);
                /* 将Bitmap设定到ImageView */
//                    imageView.setImageBitmap(bitmap);
                    uploadFile(actionUrl, bitmap1);
//                    bitmap=null;
                } catch (OutOfMemoryError e) {
                    Toast.makeText(SetUserInfoActivity.this, "图片太大", Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

//                cropRawPhoto(uri);

                break;
            case 2:
//                Bundle extras = data.getExtras();
////                if (extras != null) {
//                    Bitmap photo = extras.getParcelable(data.getData());
//                    head.setImageBitmap(photo);
////                }
//                Log.v("uri", uri.toString());
//                ContentResolver cd = this.getContentResolver();
//                try {
//                    Bitmap bitmap = BitmapFactory.decodeStream(cd.openInputStream(data.getExtras().get);
//                    ImageView imageView = (ImageView) findViewById(R.id.head);
//                /* 将Bitmap设定到ImageView */
//                    imageView.setImageBitmap(bitmap);
//
//                } catch (FileNotFoundException e) {
//                    Log.e("Exception", e.getMessage(), e);
//                }

                break;
            case GetChangeText:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    Log.v("changetext", bundle.getString("action"));
                    switch (bundle.getString("action")) {
                        case "name":
                            nickName.setText(bundle.getString("name"));
                            break;
                        case "gender":
                            general.setText(bundle.getString("gender"));
                            break;
                        case "info":
                            info.setText(bundle.getString("info"));
                            break;
                    }
                }
                break;
        }
    }

    public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidht = ((float) w / width);
        float scaleHeight = ((float) h / height);
        matrix.postScale(scaleWidht, scaleHeight);
        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height,
                matrix, true);
        return newbmp;
    }

    public void cropRawPhoto(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
//       intent.putExtra("outputX", output_X);
//        intent.putExtra("outputY", output_Y);

        intent.putExtra("return-data", true);
        startActivityForResult(intent, 2);
    }

    private String uploadFile(String serverUrl, final Bitmap bitmap) {
        int TIME_OUT = 10 * 10000000; // 超时时间
        final String CHARSET = "utf-8"; // 设置编码
        String FAILURE = "0";
        final String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
        final String PREFIX = "--", LINE_END = "\r\n";
        String CONTENT_TYPE = "multipart/form-data"; // 内容类型
        try {
            URL url = new URL(serverUrl);
            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setReadTimeout(TIME_OUT);
//            conn.setConnectTimeout(TIME_OUT);
            conn.setDoInput(true); // 允许输入流
            conn.setDoOutput(true); // 允许输出流
            conn.setUseCaches(false); // 不允许使用缓存
            conn.setRequestMethod("POST"); // 请求方式
            conn.setRequestProperty("Charset", CHARSET); // 设置编码
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary="
                    + BOUNDARY);
            if (bitmap != null) {
                /**
                 * 当文件不为空，把文件包装并且上传
                 */
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            OutputStream outputSteam = conn.getOutputStream();

                            DataOutputStream dos = new DataOutputStream(outputSteam);
                            StringBuffer sb = new StringBuffer();
                            sb.append(PREFIX);
                            sb.append(BOUNDARY);
                            sb.append(LINE_END);
                            /**
                             * 这里重点注意： name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件
                             * filename是文件的名字，包含后缀名的 比如:abc.png
                             */
                            sb.append("Content-Disposition: form-data; name=\"img\"; filename=\""
                                    + System.currentTimeMillis()+".png" + "\"" + LINE_END);
                            sb.append("Content-Type: application/octet-stream; charset="
                                    + CHARSET + LINE_END);

                            sb.append(LINE_END);
                            dos.write(sb.toString().getBytes());
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            InputStream is = new ByteArrayInputStream(baos.toByteArray());
                            byte[] bytes = new byte[1024];
                            int len = 0;
                            while ((len = is.read(bytes)) != -1) {
                                dos.write(bytes, 0, len);
                            }
                            is.close();
                            dos.write(LINE_END.getBytes());
                            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END)
                                    .getBytes();
                            dos.write(end_data);
                            dos.flush();
                            /**
                             * 获取响应码 200=成功 当响应成功，获取响应的流
                             */
                            int res = conn.getResponseCode();
                            if (res == 200) {
                                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                                String lines = reader.readLine();
//                                while ((lines = reader.readLine()) != null) {
                                Log.i("json:", lines);
//                                }

                                JSONObject jsonObject = new JSONObject(lines);
                                if (jsonObject.getString("result").equals("1")) {
                                    headUri = jsonObject.getString("value");
                                    new UserServer(SetUserInfoActivity.this, handler, new StringURL(StringURL.upHeadURL).setUserID(MyApplication.userID).setHeadUrl(headUri).toString()) {

                                        @Override
                                        public void httpBack(JSONObject jsonObject) throws JSONException {

                                        }
                                    };

                                }
                            }
                            Log.v("rescode", res + "");
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return FAILURE;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, ChangeInfoActivity.class);
        Bundle bundle = new Bundle();
        switch (v.getId()) {
            case R.id.nametouch:
                bundle.putString("action", "name");
                bundle.putString("name",nickName.getText().toString());
                intent.putExtras(bundle);
                startActivityForResult(intent, GetChangeText);
                break;
            case R.id.gendertouch:
                bundle.putString("action", "gender");
                bundle.putString("gender", general.getText().toString());
                intent.putExtras(bundle);
                startActivityForResult(intent, GetChangeText);
                break;
            case R.id.infotouch:
                bundle.putString("action", "info");
                bundle.putString("info",info.getText().toString());
                intent.putExtras(bundle);
                startActivityForResult(intent, GetChangeText);
                break;
            case R.id.qrtouch:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(Create2QR());
                builder.show();
                break;
            case R.id.back :
                this.finish();
                break;
            case R.id.change_password :
                Intent i = new Intent(SetUserInfoActivity.this, MobActivity.class);
                i.setFlags(2);
                startActivity(i);
                break;
        }
    }
    public ImageView Create2QR() {
        ImageView qrImg = new ImageView(this);
//        String uri = "IMTuserID:"+((MyApplication)getActivity().getApplication()).userID;
        String uri = new StringURL(StringURL.QR).setUserID(MyApplication.userID).toString();
//      Bitmap bitmap = BitmapUtil.create2DCoderBitmap(uri, mScreenWidth/2, mScreenWidth/2);
        Bitmap bitmap;
        try {
            bitmap = BitmapUtil.createQRCode(uri, getWindow().getWindowManager().getDefaultDisplay().getWidth());

            if (bitmap != null) {
                qrImg.setImageBitmap(bitmap);
            }

        } catch (WriterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return qrImg;
    }
}

