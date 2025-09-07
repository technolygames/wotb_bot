package logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;

import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.stream.Collectors;

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
                huc.setReadTimeout(30000);
                huc.setRequestMethod("GET");
                huc.setRequestProperty("Accept","application/json");

                int responseCode=huc.getResponseCode();

                if(responseCode==HttpURLConnection.HTTP_OK){
                    try(InputStreamReader isr=new InputStreamReader(huc.getInputStream(),StandardCharsets.UTF_8)){
                        return new BufferedReader(isr).lines().collect(Collectors.joining(System.lineSeparator()));
                    }
                }else if(responseCode==429){
                    String retryAfterHeader=huc.getHeaderField("Retry-After");
                    long waitSeconds=5;
                    if(retryAfterHeader!=null){
                        waitSeconds=Long.parseLong(retryAfterHeader);
                    }
                    uc.log(Level.WARNING,"WebControl: Intento "+attempt+"/"+maxRetries+": API devolvió 429. Esperando "+waitSeconds+"s.");
                    Thread.sleep(waitSeconds*1000);
                }else if(responseCode>=HttpURLConnection.HTTP_INTERNAL_ERROR){
                    uc.log(Level.WARNING,"WebControl: Intento "+attempt+"/"+maxRetries+": API devolvió error de servidor HTTP "+responseCode+" para "+link);
                }else{
                    uc.log(Level.WARNING,"WebControl: API devolvió HTTP "+responseCode+" para "+link+". No se reintentará.");
                    return null;
                }
            }catch(SocketTimeoutException e){
                uc.log(Level.WARNING,"WebControl: Intento "+attempt+"/"+maxRetries+": SocketTimeoutException para "+link);
                if(attempt==maxRetries){
                    uc.log(Level.SEVERE,"WebControl: SocketTimeoutException final tras "+maxRetries+" intentos.",e);
                }
            }catch(IOException|URISyntaxException e){
                uc.log(Level.SEVERE,"WebControl: "+e.getClass().getName()+" irrecuperable para "+link,e);
                return null;
            }catch(InterruptedException e){
                Thread.currentThread().interrupt();
                uc.log(Level.WARNING,"WebControl: Hilo interrumpido para "+link,e);
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
        while(!requestTimestamps.isEmpty()&&requestTimestamps.peek()<=currentTime-intervalMillis){
            requestTimestamps.poll();
        }

        while(requestTimestamps.size()>=maxRequestsPerInterval){
            long oldestTimestampInWindow=requestTimestamps.peek();
            long timeToWait=(oldestTimestampInWindow+intervalMillis)-currentTime;

            if(timeToWait>0){
                wait(timeToWait);
            }
            
            currentTime=System.currentTimeMillis();
            while(!requestTimestamps.isEmpty()&&requestTimestamps.peek()<=currentTime-intervalMillis){
                requestTimestamps.poll();
            }
        }

        requestTimestamps.add(System.currentTimeMillis());
    }
}