package fetcher.analytics;

import fetcher.models.LeaderboardRecord;
import fetcher.services.UserWatchRecord;
import lombok.extern.slf4j.Slf4j;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;
import com.google.api.services.analyticsreporting.v4.AnalyticsReportingScopes;
import com.google.api.services.analyticsreporting.v4.model.DateRange;
import com.google.api.services.analyticsreporting.v4.model.DateRangeValues;
import com.google.api.services.analyticsreporting.v4.model.Dimension;
import com.google.api.services.analyticsreporting.v4.model.GetReportsRequest;
import com.google.api.services.analyticsreporting.v4.model.GetReportsResponse;
import com.google.api.services.analyticsreporting.v4.model.Metric;
import com.google.api.services.analyticsreporting.v4.model.OrderBy;
import com.google.api.services.analyticsreporting.v4.model.ReportRequest;
import com.google.api.services.analyticsreporting.v4.model.ReportRow;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AnalyticsUtil {
    private static final String APPLICATION_NAME = "Lister Watch Report";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String KEY_FILE_LOCATION = "/key.json";
    private static final String VIEW_ID = "154153016";
    private static final Integer PARSE_COUNT_THRESHOLD = 3;
    private static final Integer VIEW_COUNT_THRESHOLD = 2;
    private static final Long DATE_SPAN = 30L;    // default date span for leaderboard (30 day leaderboard)

    private AnalyticsReporting analyticsReporting;
    private LocalDate currentTime;

    public AnalyticsUtil() {
        try {
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            GoogleCredential credential = GoogleCredential
                    .fromStream(AnalyticsUtil.class.getResourceAsStream(KEY_FILE_LOCATION))
                    .createScoped(AnalyticsReportingScopes.all());

            // Construct the Analytics Reporting service object.
            analyticsReporting = new AnalyticsReporting.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME).build();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Fail to initalize analytics reporting client");
        }

    }

    public List<LeaderboardRecord> getUserFavoriteReport() throws IOException {
        return toDBEntitys(getReportsResponse().getReports().get(0).getData().getRows());
    }

    public List<UserWatchRecord> getUserFavoriteReportRPC() throws IOException {
        return toRPCEntitys(getReportsResponse().getReports().get(0).getData().getRows());
    }

    public List<UserWatchRecord> getUserFavoriteReportRPC(long dateSpan) throws IOException {
        return toRPCEntitys(getReportsResponse(dateSpan).getReports().get(0).getData().getRows());
    }

    private GetReportsResponse getReportsResponse() throws IOException {
        return getReportsResponse(30L);
    }

    private GetReportsResponse getReportsResponse(long dateSpan) throws IOException {
        // Create the DateRange object.
        DateRange dateRange = new DateRange();
        currentTime = LocalDate.now();
        Assert.notNull(dateSpan, "date range cannot be null, eg. 30, 60 days");
        LocalDate start = currentTime.minusDays(dateSpan);

        dateRange.setStartDate(start.toString());
        dateRange.setEndDate(currentTime.toString());

        Metric parseCount = new Metric()
                .setExpression("ga:uniqueEvents")
                .setAlias("count");

        Dimension eventName = new Dimension().setName("ga:eventLabel");
        Dimension eventAction = new Dimension().setName("ga:eventAction");

        OrderBy orderBy = new OrderBy();
        orderBy.setFieldName("ga:uniqueEvents");
        orderBy.setSortOrder("descending");

        String filterExpression = "ga:eventAction!@search,ga:eventAction!@weichat";

        // Create the ReportRequest object.
        ReportRequest request = new ReportRequest()
                .setViewId(VIEW_ID)
                .setDateRanges(Arrays.asList(dateRange))
                .setMetrics(Arrays.asList(parseCount))
                .setFiltersExpression(filterExpression)
                .setOrderBys(Arrays.asList(orderBy))
                .setDimensions(Arrays.asList(eventName, eventAction));

        ArrayList<ReportRequest> requests = new ArrayList<ReportRequest>();
        requests.add(request);

        // Create the GetReportsRequest object.
        GetReportsRequest getReport = new GetReportsRequest()
                .setReportRequests(requests);

        // Call the batchGet method.
        return analyticsReporting.reports().batchGet(getReport).execute();
    }

    private List<LeaderboardRecord> toDBEntitys(List<ReportRow> rows) {
        List<LeaderboardRecord> results = new ArrayList<>();

        // first pass, set parse count. filter via threshold
        for (ReportRow row : rows) {
            List<String> dimensions = row.getDimensions();
            List<DateRangeValues> counts = row.getMetrics();
            Integer count = Integer.valueOf(row.getMetrics().get(0).getValues().get(0));
            //log.debug(movieNames.get(0) + ": " + parseCounts.get(0).getValues().get(0));
            if (dimensions.get(1).contains("parse")) {
                LeaderboardRecord leaderboardRecord = LeaderboardRecord.builder()
                        .movieName(dimensions.get(0))
                        .parseCount(count)
                        .dateSpan(DATE_SPAN)
                        .dateUpdated(Date.from(currentTime.atStartOfDay(ZoneId.systemDefault()).toInstant()))
                        .build();
                results.add(leaderboardRecord);
            }
        }

        // second pass, set view count
        for (ReportRow row : rows) {
            List<String> dimensions = row.getDimensions();
            List<DateRangeValues> counts = row.getMetrics();
            //log.debug(movieNames.get(0) + ": " + parseCounts.get(0).getValues().get(0));
            if (dimensions.get(1).contains("watch")) {
                for (LeaderboardRecord record : results) {
                    if (record.getMovieName().equals(dimensions.get(0))) {
                        record.setViewCount(Integer.valueOf(counts.get(0).getValues().get(0)));
                    }
                }
            }
        }

        return results.stream().filter(record -> record.getParseCount() > PARSE_COUNT_THRESHOLD || record
                .getViewCount() > VIEW_COUNT_THRESHOLD).collect(Collectors.toList());
    }

    private List<UserWatchRecord> toRPCEntitys(List<ReportRow> rows) {
        List<UserWatchRecord> results = new ArrayList<>();

        // first pass, set view count. filter via threshold
        for (ReportRow row : rows) {
            List<String> dimensions = row.getDimensions();
            List<DateRangeValues> counts = row.getMetrics();
            Integer parseCount = Integer.valueOf(row.getMetrics().get(0).getValues().get(0));
            //log.debug(movieNames.get(0) + ": " + parseCounts.get(0).getValues().get(0));
            if (dimensions.get(1).contains("parse")) {
                UserWatchRecord userWatchRecord = UserWatchRecord.newBuilder()
                        .setMovieName(dimensions.get(0))
                        .setParseCount(parseCount)
                        .build();
                results.add(userWatchRecord);
            }
        }

        // second pass, set watch count
        for (ReportRow row : rows) {
            List<String> dimensions = row.getDimensions();
            List<DateRangeValues> counts = row.getMetrics();
            Integer viewCount = Integer.valueOf(counts.get(0).getValues().get(0));
            //log.debug(movieNames.get(0) + ": " + parseCounts.get(0).getValues().get(0));
            if (dimensions.get(1).contains("watch")) {
                results = results.stream().map(record -> {
                    if (record.getMovieName().equals(dimensions.get(0))) {
                        return record.toBuilder().setViewCount(viewCount).build();
                    }
                    return record;
                }).collect(Collectors.toList());
            }
        }

        return results.stream().filter(record -> (record.getParseCount() > PARSE_COUNT_THRESHOLD && record.getViewCount() > 0) || record
                .getViewCount() > VIEW_COUNT_THRESHOLD).collect(Collectors.toList());
    }
}
