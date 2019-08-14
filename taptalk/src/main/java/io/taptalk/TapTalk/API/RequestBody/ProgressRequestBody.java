package io.taptalk.TapTalk.API.RequestBody;

import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.annotation.Nullable;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class ProgressRequestBody extends RequestBody {
    private static final String TAG = ProgressRequestBody.class.getSimpleName();
    private File mFile;
    private String mPath;
    private UploadCallbacks mListener;

    //ini untuk ngeakomodasi content lain selain gambar
    private String content_type;

    private static final int DEFAULT_BUFFER_SIZE = 2048;

    public interface UploadCallbacks {
        void onProgressUpdate(int percentage, long bytes);

        void onError();

        void onFinish();
    }

    public ProgressRequestBody(final File mFile, String content_type, final UploadCallbacks mListener) {
        this.content_type = content_type;
        this.mFile = mFile;
        this.mListener = mListener;
    }

    public ProgressRequestBody(final File mFile, String content_type) {
        this.content_type = content_type;
        this.mFile = mFile;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return MediaType.parse(content_type);
    }

    @Override
    public long contentLength() throws IOException {
        return mFile.length();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        long fileLength = mFile.length();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        FileInputStream in = new FileInputStream(mFile);
        long uploaded = 0;

        try {
            int read;
            Handler handler = new Handler(Looper.getMainLooper());
            while ((read = in.read(buffer)) != -1) {
                handler.post(new ProgressUpdater(uploaded, fileLength));
                uploaded += read;
                sink.write(buffer, 0, read);
            }
        } finally {
            in.close();
            if (null != mListener)
                mListener.onFinish();
        }
    }

    private class ProgressUpdater implements Runnable {
        private long mUploaded;
        private long mTotal;

        public ProgressUpdater(long uploaded, long total) {
            mUploaded = uploaded;
            mTotal = total;
        }

        @Override
        public void run() {
            if (null != mListener)
                mListener.onProgressUpdate((int) (100 * mUploaded / mTotal), mUploaded);
        }
    }
}
