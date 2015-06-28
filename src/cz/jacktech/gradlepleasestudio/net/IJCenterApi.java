package cz.jacktech.gradlepleasestudio.net;

import cz.jacktech.gradlepleasestudio.net.data.JCenterPackageFile;
import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.GET;
import retrofit.http.Query;

import java.util.List;

/**
 * Created by nsaagent on 28.6.15.
 */
public interface IJCenterApi {

    @GET("/search/file")
    public void searchPackage(@Query("name") String searchQuery, Callback<List<JCenterPackageFile>> callback);

}
