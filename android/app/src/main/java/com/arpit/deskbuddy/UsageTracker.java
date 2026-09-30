package com.arpit.deskbuddy;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import java.util.List;

public class UsageTracker {
    private final UsageStatsManager usageStatsManager;

    public UsageTracker(Context context) {
        usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
    }

    public String getUsageData() {
        long endTime = System.currentTimeMillis();
        long startTime = endTime - (1000 * 60 * 60 * 24); // Last 24 hours
        
        List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
        if (stats == null || stats.isEmpty()) return "No usage data";
        
        StringBuilder builder = new StringBuilder();
        for (UsageStats stat : stats) {
            if (stat.getTotalTimeInForeground() > 60000) { // Only apps used for > 1 min
                long minutes = stat.getTotalTimeInForeground() / 60000;
                builder.append(stat.getPackageName()).append(": ").append(minutes).append("m\n");
            }
        }
        return builder.toString().isEmpty() ? "No significant usage" : builder.toString();
    }
}
