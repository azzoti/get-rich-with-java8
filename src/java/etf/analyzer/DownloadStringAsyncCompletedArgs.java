package etf.analyzer;

public class DownloadStringAsyncCompletedArgs {
    private String result; 
    private String error;
    public DownloadStringAsyncCompletedArgs(String result, String error) {
        super();
        this.result = result;
        this.error = error;
    }
    public String getResult() {
        return result;
    }
    public String getError() {
        return error;
    }
    
}