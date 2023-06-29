package threads;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import logic.ApiRequest;

public class Thread1 implements Runnable{
    InputStream is;
    OutputStream os;

    public Thread1(InputStream is,OutputStream os){
        this.is=is;
        this.os=os;
    }

    @Override
    public void run(){
        try{
            byte[] buffer=new byte[2048];
            for(int read;(read=is.read(buffer))>0;){
                os.write(buffer,0,read);
            }
        }catch(IOException e){
            ApiRequest.LOGGER.config(e.fillInStackTrace().toString());
        }
    }
}
