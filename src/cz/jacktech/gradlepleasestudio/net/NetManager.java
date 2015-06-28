package cz.jacktech.gradlepleasestudio.net;

import retrofit.RestAdapter;

/**
 * Created by nsaagent on 28.6.15.
 */
public class NetManager {

    private static final String BINTRAY_API_ENDPOINT = "https://api.bintray.com";

    private IJCenterApi mCenterSearch;

    public NetManager(){
        mCenterSearch = new RestAdapter.Builder()
                .setEndpoint(BINTRAY_API_ENDPOINT)
                .build()
                .create(IJCenterApi.class);
    }

    public IJCenterApi api(){
        return mCenterSearch;
    }

}
