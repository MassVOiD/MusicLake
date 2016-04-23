package com.cyl.music_hnust;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cyl.music_hnust.Json.JsonParsing;
import com.cyl.music_hnust.bean.User;
import com.cyl.music_hnust.bean.UserStatus;
import com.cyl.music_hnust.http.HttpByGet;
import com.cyl.music_hnust.http.HttpUtil;
import com.cyl.music_hnust.view.RoundedImageView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;


/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends AppCompatActivity {


    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private ImageView mImg_bg;
    private View mProgressView;
    private View mLoginFormView;
    Button mEmailSignInButton;
    private ImageButton back;
    private RoundedImageView User_Img;
    private static ProgressDialog loadingDialog;
    private static Context mContext;
    private String urlpath = "http://119.29.27.116/hcyl/music_BBS/operate.php?GetUserinfo&user_id=";
    private String imgpath = "http://119.29.27.116/hcyl/music_BBS";
    private static String tempPath =
            Environment.getExternalStorageDirectory()+"/hkmusic/cache"
                    + "/temp.png";
    private boolean LoginSuccess = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loadingDialog=new ProgressDialog(this);
        loadingDialog.setTitle("登录中...");
        loadingDialog.setCancelable(false);
        mContext =getApplicationContext();


        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.username);
        User_Img = (RoundedImageView) findViewById(R.id.user_img);


        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    mEmailSignInButton.setText("登录");
                    //mEmailSignInButton.setText("登录中...");
                    attemptLogin();
                    return true;
                }
                mEmailSignInButton.setText("登录");
                return false;
            }
        });

        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mEmailSignInButton.setText("登录中...");
                attemptLogin();
            }
        });

        back = (ImageButton) findViewById(R.id.backImageButton);
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mLoginFormView = findViewById(R.id.login_form);

        mImg_bg = (ImageView) findViewById(R.id.Img_bg);

    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;


            cancel = true;
        }

        // Check for a valid username address.
        if (TextUtils.isEmpty(username)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(username)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;

            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            mEmailSignInButton.setText("登录");
            focusView.requestFocus();
        } else {
            mAuthTask = new UserLoginTask(username, password);
            mAuthTask.execute((Void) null);
        }
    }


    String json=null;
    private boolean isEmailValid(String username) {
        //TODO: Replace this with your own logic
        return username.length() > 9;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }




    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };
        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mPassword;
        private String login_status;
        private String userinfo;

        UserLoginTask(String username, String password) {
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

//            try {
//                // Simulate network access.
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                return false;
//            }
            login_status = "网络请求失败";
            userinfo = HttpByGet.LoginByGet(mUsername, mPassword, 1, null);
            Log.e("result ", userinfo + "");
            if (userinfo != null) {
                String status = null;
                try {
                    final JSONObject jsonObject = new JSONObject(userinfo);
                    status = jsonObject.getString("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.e("status", status);
                if ("success".equals(status)) {
                    login_status = "登录成功";

                    User userinfo3 = JsonParsing.Userinfo(userinfo);
                    String resultcode = HttpByGet.LoginByGet(mUsername, mPassword, 2, userinfo3);

                    Log.e("resultcode", resultcode);
                } else if ("fail".equals(status)) {
                    login_status = "帐号不存在或密码错误";
                }

            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                if ("登录成功".equals(login_status)) {
                    User userinfo2 = JsonParsing.Userinfo(userinfo);
                    //   map.put("pw", mPassword);
                   // userinfo2.setUser_img(tempPath);
                    UserStatus.savaUserInfo(getApplicationContext(), userinfo2);
                    LoginSuccess = true;
                    loadingDialog.dismiss();
                    finish();
                } else {
                    loadingDialog.dismiss();
                    mEmailSignInButton.setText("登录");
                    Toast.makeText(getApplicationContext(), login_status, Toast.LENGTH_SHORT).show();
                }
            } else {
                loadingDialog.dismiss();
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog.show();
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

    /**
     * @param url
     *            要下载的文件URL
     * @throws Exception
     */
    public static void downloadFile(String url) throws Exception {

        AsyncHttpClient client = new AsyncHttpClient();
        // 指定文件类型
        String[] allowedContentTypes = new String[] { "image/png", "image/jpeg" };
        // 获取二进制数据如图片和其他文件
        client.get(url, new BinaryHttpResponseHandler(allowedContentTypes) {

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] binaryData) {


                // TODO Auto-generated method stub
                // 下载成功后需要做的工作
           //     progress.setProgress(0);
                //
                Log.e("binaryData:", "共下载了：" + binaryData.length);
                //
                Bitmap bmp = BitmapFactory.decodeByteArray(binaryData, 0,
                        binaryData.length);

                File file = new File(tempPath);
                // 压缩格式
                Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
                // 压缩比例
                int quality = 100;
                try {
                    // 若存在则删除
                    if (file.exists())
                        file.delete();
                    // 创建文件
                    file.createNewFile();
                    //
                    OutputStream stream = new FileOutputStream(file);
                    // 压缩输出
                    bmp.compress(format, quality, stream);
                    // 关闭
                    stream.close();
                    //
                    Toast.makeText(mContext, "下载成功\n" + tempPath,
                            Toast.LENGTH_LONG).show();

                    loadingDialog.dismiss();


                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  byte[] binaryData, Throwable error) {

                loadingDialog.dismiss();
                // TODO Auto-generated method stub
                Toast.makeText(mContext, "下载失败", Toast.LENGTH_LONG).show();
            }

//            @Override
//            public void onProgress(int bytesWritten, int totalSize) {
//                // TODO Auto-generated method stub
//                super.onProgress(bytesWritten, totalSize);
//                int count = (int) ((bytesWritten * 1.0 / totalSize) * 100);
//                // 下载进度显示
//                progress.setProgress(count);
//                Log.e("下载 Progress>>>>>", bytesWritten + " / " + totalSize);
//
//            }

            @Override
            public void onRetry(int retryNo) {
                // TODO Auto-generated method stub
                super.onRetry(retryNo);
                // 返回重试次数
            }

        });
    }
}