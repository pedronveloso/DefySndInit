package com.pedronveloso.sndinitdefy;

import android.util.Log;

/**
 * Created by:
 * User: Pedro Veloso
 * Date: 4/11/11
 * Time: 12:39 PM
 */
public class Utils {

    /**
        * Debug function
        *
        * @param message debug message
        * @param type    Log type (verbose, error, ... )
        */
       public static void debugFunc(String message, int type) {
           if (Constants.DEBUGGING) {
               switch (type) {
                   case Log.ERROR:
                       Log.e(Constants.LOG_TAG, message);
                       break;
                   case Log.DEBUG:
                       Log.d(Constants.LOG_TAG, message);
                       break;
                   case Log.INFO:
                       Log.i(Constants.LOG_TAG, message);
                       break;
                   case Log.VERBOSE:
                       Log.v(Constants.LOG_TAG, message);
                       break;
                   case Log.WARN:
                       Log.w(Constants.LOG_TAG, message);
                       break;
                   default:
                       Log.v(Constants.LOG_TAG, message);
                       break;
               }
           }
       }


}
