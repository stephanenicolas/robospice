package com.octo.android.robospice;

import java.util.Set;

import org.springframework.web.client.RestTemplate;

import com.octo.android.robospice.ContentService;
import com.octo.android.robospice.request.CachedContentRequest;
import com.octo.android.robospice.request.RequestListener;
import com.octo.android.robospice.request.springandroid.RestContentRequest;

/**
 * This class offers a {@link ContentService} that injects a {@link RestTemplate} from spring android into every
 * {@link RestContentRequest} it has to execute.
 * 
 * Developpers will have to implement {@link #createRestTemplate()} in addition to the usual
 * {@link #createCacheManager(android.app.Application)} methods to create a {@link RestTemplate} and configure it.
 * 
 * @author sni
 * 
 */
public abstract class SpringAndroidContentService extends ContentService {

    private RestTemplate restTemplate;

    @Override
    public void onCreate() {
        super.onCreate();
        restTemplate = createRestTemplate();
    }

    public abstract RestTemplate createRestTemplate();

    
    @Override
    public void addRequest( CachedContentRequest< ? > request, Set< RequestListener< ? >> listRequestListener ) {
        if ( request.getContentRequest() instanceof RestContentRequest ) {
            ( (RestContentRequest< ? >) request.getContentRequest() ).setRestTemplate( restTemplate );
        }
        super.addRequest( request, listRequestListener );
    }
    

}
