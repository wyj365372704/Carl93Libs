package lib.carl93.networkaccesslib;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Carl on 2016-09-09 009.
 */
public class NetworkAccessTool {
    private final String TAG = NetworkAccessTool.class.getSimpleName();
    private static NetworkAccessTool netWorkAccessTool;
    private OkHttpClient client;
    private Handler handler;

    private static boolean debugMode = true;

    private NetworkAccessTool() {
        client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).build();
        handler = new Handler(Looper.getMainLooper());
    }

    private static NetworkAccessTool getInstance() {
        if (null == netWorkAccessTool) {
            synchronized (NetworkAccessTool.class) {
                netWorkAccessTool = new NetworkAccessTool();
            }
        }
        return netWorkAccessTool;
    }

    public Call asynGet(String url, Map<String, String> params, final int requestTag, final NetworkAccessTool.CallBack callBack) {
        Request request = new Request.Builder().url(getFullUrl(url, params, false)).get().build();

        Call call = client.newCall(request);

        onStart(callBack, requestTag);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                onError(callBack, e.getMessage(), requestTag);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    onSuccess(callBack, response.body().string().trim(), requestTag);
                } else {
                    onError(callBack, response.message(), requestTag);
                }
            }
        });
        return call;
    }

    public Call asynPost(String url, Map<String, String> params, Map<String, File> files, final int requestTag, final NetworkAccessTool.CallBack callBack) {
        RequestBody requestBody;
        if (files != null) {
            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.setType(MultipartBody.FORM);
            if (null != params) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    builder.addFormDataPart(entry.getKey(), entry.getValue().toString());
                }
            }
            for (Map.Entry<String, File> entry : files.entrySet()) {
                String key = entry.getKey();
                File file = entry.getValue();
                builder.addFormDataPart(key, file.getName(), RequestBody.create(MediaType.parse(file.getName()), file));
            }
            requestBody = builder.build();
        } else {
            FormBody.Builder builder = new FormBody.Builder();
            if (null != params) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    builder.add(entry.getKey(), entry.getValue().toString());
                }
            }
            requestBody = builder.build();
        }

        Request request = new Request.Builder().url(url).post(requestBody).build();

        Call call = client.newCall(request);

        onStart(callBack, requestTag);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                onError(callBack, e.getMessage(), requestTag);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    onSuccess(callBack, response.body().string().trim(), requestTag);
                } else {
                    onError(callBack, response.message(), requestTag);
                }
            }
        });
        return call;
    }

    public void download(final String url, final File target, final DownloadCallBack callback, int requestTag) {
        FileDownloadTask task = new FileDownloadTask(url, target, requestTag, callback);
        task.execute();
    }

    public void cancelCall(Call call) {
        if (null != call)
            call.cancel();
    }

    private void onStart(final NetworkAccessTool.CallBack callBack, final int requestTag) {
        if (debugMode) {
            Log.i(TAG, "---->onStart: the requestTag is " + requestTag);
        }
        if (null != callBack) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callBack.onStart(requestTag);
                }
            });
        }
    }

    private void onSuccess(final NetworkAccessTool.CallBack callBack, final String data, final int requestTag) {
        if (debugMode) {
            Log.i(TAG, "---->onSuccess: the requestTag is " + requestTag + " ,the data is " + data);
        }
        if (null != callBack) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callBack.onSuccess(data, requestTag);
                }
            });
        }
    }

    private void onError(final NetworkAccessTool.CallBack callBack, final String msg, final int requestTag) {
        if (debugMode) {
            Log.i(TAG, "---->onError: the requestTag is " + requestTag + " ,the msg is " + msg);
        }
        if (null != callBack) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callBack.onError(msg, requestTag);
                }
            });
        }
    }

    public interface CallBack {
        /**
         * Running on UI thread.
         *
         * @param requestTag
         */
        void onStart(int requestTag);

        /**
         * Running on UI thread.
         *
         * @param data
         * @param requestTag
         */
        void onSuccess(String data, int requestTag);

        /**
         * Running on UI thread.
         *
         * @param msg
         * @param requestTag
         */
        void onError(String msg, int requestTag);
    }

    public interface DownloadCallBack extends CallBack {
        void onProgress(int progress, float networkSpeed);
    }

    public static class Builder {
        public NetworkAccessTool build() {
            return getInstance();
        }

        public Builder debugMode(boolean mode) {
            debugMode = mode;
            return this;
        }
    }

    private String getFullUrl(String url, Map<String, String> params, boolean urlEncoder) {
        StringBuffer urlFull = new StringBuffer();
        urlFull.append(url);
        if (params != null && params.size() > 0) {
            urlFull.append("?");
            for (Map.Entry<String, String> param : params.entrySet()) {
                String key = param.getKey();
                String value = param.getValue();
                if (urlEncoder) {//只对key和value编码
                    try {
                        key = URLEncoder.encode(key, "UTF-8");
                        value = URLEncoder.encode(value, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                urlFull.append(key).append("=").append(value).append("&");
            }
            urlFull.deleteCharAt(urlFull.length() - 1);
        }
        return urlFull.toString();
    }


    class FileDownloadTask extends AsyncTask<Void, Long, Boolean> {

        private DownloadCallBack callback;
        private String url;
        private File target;
        private Call call;
        private int requestTag;
        //开始下载时间，用户计算加载速度
        private long previousTime;

        public FileDownloadTask(String url, File target, int requestTag, DownloadCallBack callback) {
            this.url = url;
            this.callback = callback;
            this.target = target;
            this.requestTag = requestTag;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!target.getParentFile().exists()) {
                target.getParentFile().mkdirs();
            } else {
                if (target.exists())
                    target.delete();
            }
            previousTime = System.currentTimeMillis();
            call = client.newCall(new Request.Builder() .url(url).build());
            if(callback!=null)
                callback.onStart(requestTag);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean suc = false;
            try {
                Response response = call.execute();
                long total = response.body().contentLength();
                saveFile(response);
                if (total == target.length()) {
                    suc = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                suc = false;
            }
            return suc;
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            super.onProgressUpdate(values);
            if (callback != null && values != null && values.length >= 2) {

                long sum = values[0];
                long total = values[1];

                int progress = (int) (sum * 100.0f / total);

                //计算下载速度
                long totalTime = (System.currentTimeMillis() - previousTime) / 1000;
                if (totalTime == 0) {
                    totalTime += 1;
                }
                float networkSpeed = sum / totalTime / 1024f ;
                callback.onProgress(progress, networkSpeed);
            }
        }

        @Override
        protected void onPostExecute(Boolean suc) {
            super.onPostExecute(suc);
            if (suc) {
                if (callback != null) {
                    callback.onSuccess(target.getAbsolutePath(), requestTag);
                }
            } else {
                if (callback != null) {
                    callback.onError("", requestTag);
                }
            }
        }

        public String saveFile(Response response) throws IOException {
            InputStream is = null;
            byte[] buf = new byte[1024];
            int len = 0;
            FileOutputStream fos = null;
            try {
                is = response.body().byteStream();
                final long total = response.body().contentLength();
                long sum = 0;

                target.createNewFile();

                fos = new FileOutputStream(target);
                while ((len = is.read(buf)) != -1) {
                    sum += len;
                    fos.write(buf, 0, len);

                    if (callback != null) {
                        publishProgress(sum, total);
                    }
                }
                fos.flush();

                return target.getAbsolutePath();
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                }
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                }
            }
        }
    }
}