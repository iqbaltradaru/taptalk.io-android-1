package com.moselo.HomingPigeon.Helper;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * reference: https://stackoverflow.com/a/23215152/817837
 * Created by Fadhlan on 6/16/17.
 */

public class TimeAgo {
    public static final List<Long> times = Arrays.asList(
            TimeUnit.DAYS.toMillis(365),
            TimeUnit.DAYS.toMillis(30),
            TimeUnit.DAYS.toMillis(7),
            TimeUnit.DAYS.toMillis(1),
            TimeUnit.HOURS.toMillis(1),
            TimeUnit.MINUTES.toMillis(1),
            TimeUnit.SECONDS.toMillis(1));
    public static final List<String> timesString = Arrays.asList(
            "year", "month", "week", "day", "hour", "minute", "second");

    public static final int TYPE_DEFAULT = 0;
    public static final int TYPE_ONLINE_STATUS = 1;

    public static String toDuration(long time) {
        long duration;
        Calendar past = Calendar.getInstance();
        past.setTime(new Date(time));
        duration = Calendar.getInstance().getTimeInMillis() - past.getTimeInMillis();

        StringBuffer res = new StringBuffer();
        for (int i = 0; i < TimeAgo.times.size(); i++) {
            Long current = TimeAgo.times.get(i);
            long temp = duration / current;
            if (temp > 0) {
                if (TimeAgo.timesString.get(i).equals("second"))
                    res.append("");
                else {
                    res.append(temp).append(" ").append(TimeAgo.timesString.get(i)).append(
                            temp != 1 ? "s" : "").append(" ago");
                }
                break;
            }
        }
        if ("".equals(res.toString()))
            return "just now";
        else
            return res.toString();
    }

    public static String durationString(long timestamp, int type) {
        long timeGap;
        long timeNow = Calendar.getInstance().getTimeInMillis();
        Calendar past = Calendar.getInstance();
        past.setTime(new Date(timestamp));
        timeGap = timeNow - past.getTimeInMillis();

        long midnightTimeGap;
        Calendar midnightToday = Calendar.getInstance();
        midnightToday.setTime(new Date(timeNow));
        midnightToday.set(Calendar.HOUR, -12);
        midnightToday.set(Calendar.MINUTE, 0);
        midnightToday.set(Calendar.SECOND, 0);
        midnightTimeGap = timeNow - midnightToday.getTimeInMillis();

        if (timestamp == 0) {
            return "";
        } else if (timeGap <= midnightTimeGap) {
            if (timeGap < TimeAgo.times.get(5)) {
                if (type == TYPE_DEFAULT)
                    return "Just now";
                else
                    return "Last Seen Recently";
            } else if (timeGap < TimeAgo.times.get(4)) {
                long numberOfMinutes = timeGap / TimeAgo.times.get(5);
                if (timeGap < TimeUnit.MINUTES.toMillis(2))
                    return numberOfMinutes + " minute ago";
                else
                    return numberOfMinutes + " minutes ago";
            } else {
                long numberOfHour = timeGap / TimeAgo.times.get(4);
                if (timeGap < TimeUnit.HOURS.toMillis(2))
                    return numberOfHour + " hour ago";
                else
                    return numberOfHour + " hours ago";
            }
        } else if (timeGap <= TimeAgo.times.get(3) + midnightTimeGap) {
            Date yesterdayTime = new Date(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("HH : mm", Locale.ENGLISH);
            String time = sdf.format(yesterdayTime);

            if (type == TYPE_DEFAULT)
                return "Yesterday at " + time;
            else
                return "Last seen yesterday at " + time;
        } else if (timeGap <= TimeAgo.times.get(3) * 6 + midnightTimeGap) {
            long numberOfDays = timeGap / TimeAgo.times.get(3);
            if (timeGap < TimeUnit.DAYS.toMillis(2))
                return numberOfDays + " day ago";
            else
                return numberOfDays + " days ago";
        } else if (timeGap <= TimeAgo.times.get(2)) {
            if (type == TYPE_DEFAULT)
                return "A week ago";
            else
                return "Last seen a week ago";
        } else {
            Date date = new Date(timestamp);
            if (type == TYPE_DEFAULT) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm");
                String dateString = sdf.format(date);
                return dateString;
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
                String dateString = sdf.format(date);
                return "Last seen " + dateString;
            }
        }
    }

    public static String durationString(long timestamp) {
        return durationString(timestamp, TYPE_DEFAULT);
    }
}