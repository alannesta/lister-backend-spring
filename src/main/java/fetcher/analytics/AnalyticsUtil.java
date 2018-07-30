package fetcher.analytics;

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
import com.google.api.services.analyticsreporting.v4.model.Report;
import com.google.api.services.analyticsreporting.v4.model.ReportRequest;
import com.google.api.services.analyticsreporting.v4.model.ReportRow;
import fetcher.models.LeaderboardRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class AnalyticsUtil {
    private static final String APPLICATION_NAME = "Lister Watch Report";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String KEY_FILE_LOCATION = "/key.json";
    private static final String VIEW_ID = "154153016";
    private static final Integer LEADERBOARD_CRITERIA = 4;
    private static final Long DATE_SPAN = 30L;    // default date span for leaderboard (30 day leaderboard)

    private AnalyticsReporting analyticsReporting;

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
        }

    }



    public List<LeaderboardRecord> getUserFavoriteReport() throws IOException {
        // Create the DateRange object.
        DateRange dateRange = new DateRange();
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(DATE_SPAN);

        dateRange.setStartDate(start.toString());
        dateRange.setEndDate(end.toString());

        // Create the Metrics object.
        Metric parseCount = new Metric()
                .setExpression("ga:uniqueEvents")
                .setAlias("count");

        Dimension eventName = new Dimension().setName("ga:eventLabel");

        OrderBy orderBy = new OrderBy();
        orderBy.setFieldName("ga:uniqueEvents");
        orderBy.setSortOrder("descending");

        String filterExpression = "ga:eventAction==parse movie";

        // Create the ReportRequest object.
        ReportRequest request = new ReportRequest()
                .setViewId(VIEW_ID)
                .setDateRanges(Arrays.asList(dateRange))
                .setMetrics(Arrays.asList(parseCount))
                .setFiltersExpression(filterExpression)
                .setOrderBys(Arrays.asList(orderBy))
                .setDimensions(Arrays.asList(eventName));

        ArrayList<ReportRequest> requests = new ArrayList<ReportRequest>();
        requests.add(request);

        // Create the GetReportsRequest object.
        GetReportsRequest getReport = new GetReportsRequest()
                .setReportRequests(requests);

        // Call the batchGet method.
        GetReportsResponse response = analyticsReporting.reports().batchGet(getReport).execute();

        // Return the response.
        //return response;

        List<LeaderboardRecord> results = new ArrayList<>();

        for (Report report : response.getReports()) {
            for (ReportRow row : report.getData().getRows()) {
                if (Integer.valueOf(row.getMetrics().get(0).getValues().get(0)) >= LEADERBOARD_CRITERIA) {
                    results.add(toEntity(row, end));
                }
            }
        }
        return results;
    }

    private LeaderboardRecord toEntity(ReportRow row, LocalDate now) {
        List<String> movieNames = row.getDimensions();
        List<DateRangeValues> parseCounts = row.getMetrics();
        log.debug(movieNames.get(0) + ": " + parseCounts.get(0).getValues().get(0));

        LeaderboardRecord leaderboardRecord = LeaderboardRecord.builder()
                .movieName(movieNames.get(0))
                .parseCount(Integer.valueOf(parseCounts.get(0).getValues().get(0)))
                .dateSpan(DATE_SPAN)
                .dateUpdated(Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .build();
        return leaderboardRecord;
    }

    ///**
    // * Parses and prints the Analytics Reporting API V4 response.
    // *
    // * @param response An Analytics Reporting API V4 response.
    // */
    //private static void printResponse(GetReportsResponse response) {
    //
    //    for (Report report : response.getReports()) {
    //        ColumnHeader header = report.getColumnHeader();
    //        List<String> dimensionHeaders = header.getDimensions();
    //        List<MetricHeaderEntry> metricHeaders = header.getMetricHeader().getMetricHeaderEntries();
    //        List<ReportRow> rows = report.getData().getRows();
    //
    //        if (rows == null) {
    //            System.out.println("No data found for " + VIEW_ID);
    //            return;
    //        }
    //
    //        for (ReportRow row : rows) {
    //            List<String> dimensions = row.getDimensions();
    //            List<DateRangeValues> metrics = row.getMetrics();
    //
    //            for (int i = 0; i < dimensionHeaders.size() && i < dimensions.size(); i++) {
    //                System.out.println(dimensionHeaders.get(i) + ": " + dimensions.get(i));
    //            }
    //
    //            for (int j = 0; j < metrics.size(); j++) {
    //                System.out.print("Date Range (" + j + "): ");
    //                DateRangeValues values = metrics.get(j);
    //                for (int k = 0; k < values.getValues().size() && k < metricHeaders.size(); k++) {
    //                    System.out.println(metricHeaders.get(k).getName() + ": " + values.getValues().get(k));
    //                }
    //            }
    //        }
    //    }
    //}

    public static void main(String[] args) {
        try {
            AnalyticsUtil util = new AnalyticsUtil();

            util.getUserFavoriteReport();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
