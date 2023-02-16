package de.mimuc.senseeverything.sensor.implementation.deepactivity;

import android.accessibilityservice.AccessibilityService;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.room.Room;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.mimuc.senseeverything.db.AppDatabase;
import de.mimuc.senseeverything.db.LogData;

public class WindowContentAccessibilityService extends AccessibilityService {

    public static final String TAG = "WindowContentAccess.Se.";

    private ExecutorService myExecutor;
    private Gson gson;
    private AppDatabase db;

    @Override
    public void onCreate() {
        super.onCreate();
        myExecutor = Executors.newSingleThreadExecutor();
        gson = new Gson();
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "senseeverything-roomdb").build();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo != null) {
            Log.i(TAG, accessibilityNodeInfo.toString());

            // deep-crawl
            myExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    // do stuff here
                    try {
                        Map<String,Object> nestedDataMap = logAccNodeInfoRecurisve(accessibilityNodeInfo, -1, true);
                        LogData logData = new LogData(accessibilityEvent.getEventTime(),"deepactivity", gson.toJson(nestedDataMap));
                        db.logDataDao().insertAll(logData);
                        Log.i(TAG,"Logged successful: "+nestedDataMap.get("ourId"));
                    } catch(Exception e){
                        Log.e(TAG,"deepcrawl crashed",e);
                    }
                }
            });
        } else {
            Log.i(TAG,"AccessibilityNode was null");
        }
    }


    /**
     *
     * @param nodeInfo
     * @param parentId
     * @param logDeep whether all referenced components should be logged in depth, or only a reference to them.
     */
    private Map<String,Object> logAccNodeInfoRecurisve(AccessibilityNodeInfo nodeInfo, int parentId, boolean logDeep){
        // log stuff
        Map<String,Object> dataMap = new HashMap<>();
        dataMap.put("children",new ArrayList<>());

        int ourId = nodeInfo.hashCode();
        dataMap.put("ourId",ourId);
        dataMap.put("parentId",parentId);

        // -- simple stuff
        grabSimpleDatatypes(dataMap,nodeInfo);

        // -- complex data
        // TODO implement these
//        nodeInfo.getWindow();
//        nodeInfo.getActionList();
//        nodeInfo.getBoundsInScreen();
//        nodeInfo.getCollectionInfo();
//        nodeInfo.getCollectionItemInfo();
//        nodeInfo.getExtras();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            nodeInfo.getExtraRenderingInfo();
//        }
//        nodeInfo.getLabelFor();
//        nodeInfo.getLabeledBy();
//        nodeInfo.getRangeInfo();
//        nodeInfo.getParent();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            nodeInfo.getTouchDelegateInfo();
//        }
//        nodeInfo.getTraversalAfter();
//        nodeInfo.getTraversalBefore();
//        nodeInfo.getBoundsInParent();


        // call next ones recursive
        for(int i=0; i<nodeInfo.getChildCount(); i++){
            Map<String,Object> childData = logAccNodeInfoRecurisve(nodeInfo.getChild(i), ourId, true);
            ((ArrayList)dataMap.get("children")).add(childData);
        }

        return dataMap;
    }

    private void grabSimpleDatatypes(Map<String,Object> dataMap, AccessibilityNodeInfo nodeInfo){
        String uniqueId = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            uniqueId = nodeInfo.getUniqueId();
        }
        dataMap.put("uniqueId",uniqueId);

        String className = String.valueOf(nodeInfo.getClassName());
        dataMap.put("className",className);

        String text = String.valueOf(nodeInfo.getText());
        dataMap.put("text",text);

        int childCount = nodeInfo.getChildCount();
        dataMap.put("childCount",childCount);

        List<String> availableExtraData = nodeInfo.getAvailableExtraData();
        dataMap.put("availableExtraData",availableExtraData);

        String contentDescription = String.valueOf(nodeInfo.getContentDescription());
        dataMap.put("contentDescription",contentDescription);

        int drawingOrder = nodeInfo.getDrawingOrder();
        dataMap.put("drawingOrder",drawingOrder);

        String hintText = String.valueOf(nodeInfo.getHintText());
        dataMap.put("hintText",hintText);

        int inputType = nodeInfo.getInputType();
        dataMap.put("inputType",inputType);

        String error = String.valueOf(nodeInfo.getError());
        dataMap.put("error",error);

        int liveRegion = nodeInfo.getLiveRegion();
        dataMap.put("liveRegion",liveRegion);

        String packageName= String.valueOf(nodeInfo.getPackageName());
        dataMap.put("packageName",packageName);

        int maxTextLength = nodeInfo.getMaxTextLength();
        dataMap.put("maxTextLength",maxTextLength);

        String stateDescription = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            stateDescription = String.valueOf(nodeInfo.getStateDescription());
        }
        dataMap.put("stateDescription",stateDescription);

        String paneTitle = null;
        String tooltipText = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            paneTitle = String.valueOf(nodeInfo.getPaneTitle());
            tooltipText = String.valueOf(nodeInfo.getTooltipText());
        }
        dataMap.put("paneTitle",paneTitle);
        dataMap.put("tooltipText",tooltipText);

        int textSelectionEnd = nodeInfo.getTextSelectionEnd();
        dataMap.put("textSelectionEnd",textSelectionEnd);

        int textSeleectionStart = nodeInfo.getTextSelectionStart();
        dataMap.put("textSeleectionStart",textSeleectionStart);

        String viewIdResourceName = nodeInfo.getViewIdResourceName();
        dataMap.put("viewIdResourceName",viewIdResourceName);

        int movementGranularities = nodeInfo.getMovementGranularities();
        dataMap.put("movementGranularities",movementGranularities);
    }


    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i(TAG,"onServiceConnected()");
    }


    @Override
    public void onInterrupt() {
        Log.i(TAG,"onInterrupt()");
    }
}
