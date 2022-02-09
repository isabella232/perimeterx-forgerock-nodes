package com.perimeterx.BD.nodes.PX.Activities;

import com.perimeterx.BD.nodes.PX.PXConstants;
import com.perimeterx.BD.nodes.PX.PXContext;

public class ActivityFactory {
    public static Activity createActivity(final String activityType, final String appId, final PXContext ctx) {
        ActivityDetails details;
        switch (activityType) {
            case PXConstants.ACTIVITY_BLOCKED:
                details = new BlockActivityDetails(ctx);
                break;
            case PXConstants.ACTIVITY_PAGE_REQUESTED:
                details = new PageRequestedActivityDetails(ctx);
                break;
            default:
                throw new IllegalArgumentException("No such activity: " + activityType);
        }
        return new Activity(activityType, appId, ctx, details);
    }
}
