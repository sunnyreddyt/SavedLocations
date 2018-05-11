package dynamic.android.locations;

import java.util.ArrayList;

/**
 * Created by Sarveshwar on 30/04/18.
 */


interface PermissionResultCallback
{
    void PermissionGranted(int request_code);
    void PartialPermissionGranted(int request_code, ArrayList<String> granted_permissions);
    void PermissionDenied(int request_code);
    void NeverAskAgain(int request_code);
}
