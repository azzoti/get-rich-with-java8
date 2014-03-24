package etf.analyzer;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;

public class WebClient extends HttpClient implements AutoCloseable {

    private static final long DEFAULT_TIMEOUT = 10000;

    public WebClient() {
        try {
            start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            this.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void downloadStringAsync(String url, Consumer<DownloadStringAsyncCompletedArgs> consumer) {
        
        newRequest(url)
        .timeout(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)
        .send(new BufferingResponseListener() {
            @Override
            public void onComplete(Result result) {
                if (!result.isFailed() && result.getResponse().getStatus() == 200) {
                    consumer.accept(new DownloadStringAsyncCompletedArgs(getContentAsString(), null));
                } else {
                    consumer.accept(new DownloadStringAsyncCompletedArgs(null, "ERROR"));
                }
            }
        });
    }
}
