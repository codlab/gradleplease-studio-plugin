package cz.jacktech.gradlepleasestudio.net.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by nsaagent on 28.6.15.
 */
public class JCenterPackageFile {

    @SerializedName("package")
    public String packageName;
    public String version;
    public String created;

}
