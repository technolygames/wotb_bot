package logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;

import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class WebControl{
    private final UtilityClass uc=new UtilityClass();
    private final Queue<Long> requestTimestamps=new LinkedList<>();

    /**
     * @param link
     * @return
     */
    public String getData(String link){
        int maxRetries=3;
        int attempt=0;
        long delayMillis=2000;

        while(attempt<maxRetries){
            attempt++;
            try{
                acquirePermit(10,1000);

                URI uri=new URI(link);
                URL u=uri.toURL();
                HttpURLConnection huc=(HttpURLConnection)u.openConnection();
                huc.setConnectTimeout(15000);
                huc.setReadTimeout(45000);
                huc.setRequestMethod("GET");
                huc.setRequestProperty("Accept","application/json");
                huc.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

                int responseCode=huc.getResponseCode();
                switch(responseCode){
                    case HttpURLConnection.HTTP_OK->{
                        String result=readStream(huc.getInputStream());
                        return (result!=null)?result:null;
                    }
                    case 429->{
                        String retryAfterHeader=huc.getHeaderField("Retry-After");
                        long waitSeconds=5;
                        if(retryAfterHeader!=null){
                            waitSeconds=Long.parseLong(retryAfterHeader);
                        }
                        uc.log(Level.WARNING,"WebControl: Intento "+attempt+"/"+maxRetries+": API devolvió 429. Esperando "+waitSeconds+"s.");
                        Thread.sleep(waitSeconds*1000);
                        continue;
                    }
                    default->{
                        String errorMsg=readStream(huc.getErrorStream());
                        uc.log(Level.WARNING,"WebControl: API devolvió HTTP "+responseCode+" para "+link+". Respuesta: "+errorMsg);
                        return null;
                    }
                }
            }catch(SocketTimeoutException|UncheckedIOException e){
                Throwable realCause=(e instanceof UncheckedIOException)?e.getCause():e;
                uc.log(Level.WARNING,"WebControl: Intento "+attempt+"/"+maxRetries+": Timeout detectado: "+realCause.getMessage());

                if(attempt==maxRetries){
                    uc.log(Level.SEVERE,"WebControl: Timeout final tras "+maxRetries+" intentos.",realCause);
                }
            }catch(InterruptedException e){
                Thread.currentThread().interrupt();
                uc.log(Level.WARNING,"WebControl: Hilo interrumpido para "+link,e);
                return null;
            }catch(IOException e){
                uc.log(Level.WARNING,"WebControl: Error de E/S en intento "+attempt+": "+e.getMessage());
                if(attempt==maxRetries)return null;
            }catch(Exception e){
                uc.log(Level.SEVERE,"WebControl: Error inesperado irrecuperable",e);
                return null;
            }

            if(attempt<maxRetries){
                try{
                    Thread.sleep(delayMillis);
                    delayMillis*=2;
                }catch(InterruptedException e){
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }

        uc.log(Level.SEVERE,"WebControl: Fallaron todos los "+maxRetries+" intentos para el enlace: "+link);
        return null;
    }

    /**
     * @param maxRequestsPerInterval
     * @param intervalMillis
     * @exception InterruptedException
     */
    public synchronized void acquirePermit(int maxRequestsPerInterval,long intervalMillis) throws InterruptedException{
        long currentTime=System.currentTimeMillis();

        requestTimestamps.removeIf(timestamp->timestamp<=currentTime-intervalMillis);

        while(requestTimestamps.size()>=maxRequestsPerInterval){
            long oldest=requestTimestamps.peek();
            long timeToWait=(oldest+intervalMillis)-System.currentTimeMillis();

            if(timeToWait>0){
                try{
                    wait(timeToWait);
                }catch(InterruptedException e){
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }

            long now=System.currentTimeMillis();
            requestTimestamps.removeIf(ts->ts<=now-intervalMillis);
        }

        requestTimestamps.add(System.currentTimeMillis());
        notifyAll();
    }
    
    /**
     * @param is
     * @return
     */
    private String readStream(InputStream is) throws IOException,UncheckedIOException{
        if(is==null)return null;
        StringBuilder sb=new StringBuilder();
        try(InputStreamReader isr=new InputStreamReader(is,StandardCharsets.UTF_8);
                BufferedReader br=new BufferedReader(isr)){
            String line;
            while((line=br.readLine())!=null){
                sb.append(line).append(System.lineSeparator());
            }
        }
        return sb.toString();
    }
}