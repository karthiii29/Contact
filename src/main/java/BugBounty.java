import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import okhttp3.Request;
import okhttp3.Response;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BugBounty {
    private static final String BUG_SEARCH_URL = "https://bugs.zohosecurity.com/api/v1/searchbug";
    private static final String BUG_DETAILS_URL = "https://bugs.zohosecurity.com//api/v1/bug?bug_id=";
    private static final String AUTH_TOKEN = "Zoho-oauthtoken 1000.b94db5058a45e58e32e88c6a4edb41ea.44b0931ddac76b6cdcff71783dc008df"; // Replace with actual token
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static List<String> getBugIds() throws IOException {
        // Build query parameters
        Map<String, String> params = new LinkedHashMap<>();
        params.put("offset", "0");
        params.put("limit", "4");
        params.put("department_type", "Zoho");
        params.put("service_id", "[101000000005071]");
        params.put("status_id", "[101000000004039]");

        // Create query string efficiently using StringJoiner
        String query = params.entrySet().stream()
                .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" +
                        URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&", "?", ""));

        // Set up HTTP connection
        URL url = new URL(BUG_SEARCH_URL + query);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Authorization", AUTH_TOKEN);
        conn.setConnectTimeout(5000); // Add timeout
        conn.setReadTimeout(5000);

        // Process response
        try {
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }

            // Parse JSON response using try-with-resources for automatic resource management
            try (var inputStream = conn.getInputStream()) {
                JsonNode root = MAPPER.readTree(inputStream);
                JsonNode bugArray = root.path("value");

                // Use Stream API for efficient collection
                List<String> bugIds = new ArrayList<>();
                bugArray.forEach(bugNode -> {
                    JsonNode bugIdNode = bugNode.path("bug_id");
                    if (!bugIdNode.isMissingNode()) {
                        bugIds.add(bugIdNode.asText());
                    }
                });
                return bugIds;
            }
        } finally {
            conn.disconnect(); // Ensure connection is closed
        }
    }

    public static List<Map<String, String>> getBugDetails(List<String> bugIds) throws IOException {
        List<Map<String, String>> bugData = new ArrayList<>();
        for (String bugId : bugIds) {
            bugData.add(bugDetails(bugId));
        }
        return bugData;
    }

    public static void exportToExcel(List<Map<String, String>> bugList, String filePath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Bug Details");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Wrap style
            CellStyle wrapStyle = workbook.createCellStyle();
            wrapStyle.setWrapText(true);
            wrapStyle.setVerticalAlignment(VerticalAlignment.TOP);

            // Header row
            Row headerRow = sheet.createRow(0);
            List<String> headers = List.copyOf(bugList.get(0).keySet());
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            for (int i = 0; i < bugList.size(); i++) {
                Row row = sheet.createRow(i + 1);
                Map<String, String> bug = bugList.get(i);
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(bug.get(headers.get(j)));
                    cell.setCellStyle(wrapStyle);
                }
            }

            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream out = new FileOutputStream(filePath)) {
                workbook.write(out);
            }

            System.out.println("Excel written to: " + filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Map<String, String> bugDetails(String bugId) throws IOException {
        OkHttpClient client = new OkHttpClient();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> bugDetails = new LinkedHashMap<>();

        try {
            Request request = new Request.Builder()
                    .url("https://bugs.zohosecurity.com/api/v1/bug?bug_id=" + bugId)
                    .get()
                    .addHeader("Authorization", AUTH_TOKEN) // 🔁 Use real token
                    .build();

            try (Response response = client.newCall(request).execute()) {

                assert response.body() != null;
                String bodyString = response.body().string();// ✅ Read actual content
                JsonNode root = mapper.readTree(bodyString);

                // Navigate to bug array
                JsonNode bugNode = root.path("bug").path("value").get(0);
                bugDetails.put("bug_id", bugNode.path("bug_id").asText());
                bugDetails.put("service_name", bugNode.path("service").path("service_name").asText());
                bugDetails.put("title", bugNode.path("title").asText());
                bugDetails.put("description", bugNode.path("description").asText());
                bugDetails.put("status_name", bugNode.path("status").path("status_name").asText());
                bugDetails.put("category_name", bugNode.path("owasp_category").path("category_name").asText());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return bugDetails;
    }

    private static void getBugIdsWithLimit(int maxTotal) {
        int offset = 0;
        int limit = 70; // Zoho API's page size
        int totalFetched = 0;

        List<String> bugIds = new ArrayList<>();
        OkHttpClient client = new OkHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        while (true) {
            try {
                // Prepare query params
                Map<String, String> params = new LinkedHashMap<>();
                params.put("offset", String.valueOf(offset));
                params.put("limit", String.valueOf(limit));
                params.put("department_type", "Zoho");
                params.put("service_id", "[101000000005071]");
                params.put("status_id", "[101000000004039]");

                // Build query string
                StringBuilder query = new StringBuilder("?");
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    if (query.length() > 1) query.append("&");
                    query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
                    query.append("=");
                    query.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
                }

                String fullUrl = "https://bugs.zohosecurity.com/api/v1/searchbug" + query;

                Request request = new Request.Builder()
                        .url(fullUrl)
                        .get()
                        .addHeader("Authorization", AUTH_TOKEN) // Replace with real token
                        .addHeader("Cookie", "JSESSIONID=FD25733D1C15A87AF70B23DC028B6F90; _zcsr_tmp=e8b5ed31-4731-425d-8853-32e8a2fe9c4d; cbountycsr=e8b5ed31-4731-425d-8853-32e8a2fe9c4d; zalb_34b826b3bd=4e70fa5cf2d9002758e78478d91f84a8") // Replace with real cookies
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.code() == 200 && response.body() != null) {
                        String json = response.body().string();
                        JsonNode root = mapper.readTree(json);
                        JsonNode bugs = root.path("value");

                        if (!bugs.isArray() || bugs.isEmpty()) break;

                        for (JsonNode bug : bugs) {
                            bugIds.add(bug.path("bug_id").asText());
                            totalFetched++;
                            if (totalFetched >= maxTotal) break;
                        }

                        if (totalFetched == maxTotal) break;

                        offset += limit;
                    } else {
                        System.err.println("Failed at offset " + offset + " → " + response.code());
                        break;
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }

// ✅ Final Result
        System.out.println("Total Bugs Collected: " + bugIds.size());
        bugIds.forEach(System.out::println);
    }

    public static List<String> getAllBugIds() {
        List<String> bugIds = new ArrayList<>();
        int offset = 0;
        int limit = 70; // You can adjust the batch size

        OkHttpClient client = new OkHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        while (true) {
            try {
                // Prepare query params
                Map<String, String> params = new LinkedHashMap<>();
                params.put("offset", String.valueOf(offset));
                params.put("limit", String.valueOf(limit));
                params.put("department_type", "Zoho");
                params.put("service_id", "[101000000005161]");  // 101000002102021,101000000005161
                params.put("status_id", "[101000000004039]");
                params.put("owasp_id", "[101000001064047,101000001064033,101000001101417,101000001064029,101000000005007,101000000005053]");

                // Build query string
                String query = params.entrySet().stream()
                        .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                        .collect(Collectors.joining("&", "?", ""));

                String fullUrl = "https://bugs.zohosecurity.com/api/v1/searchbug" + query;

                Request request = new Request.Builder()
                        .url(fullUrl)
                        .get()
                        .addHeader("Authorization", AUTH_TOKEN)
                        .addHeader("Cookie", "JSESSIONID=FD25733D1C15A87AF70B23DC028B6F90; _zcsr_tmp=e8b5ed31-4731-425d-8853-32e8a2fe9c4d; cbountycsr=e8b5ed31-4731-425d-8853-32e8a2fe9c4d; zalb_34b826b3bd=4e70fa5cf2d9002758e78478d91f84a8") // Optional: if required
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful() || response.body() == null) {
                        System.err.println("Failed to fetch data: HTTP " + response.code());
                        break;
                    }

                    String json = response.body().string();
                    JsonNode root = mapper.readTree(json);
                    JsonNode bugArray = root.path("value");

                    if (!bugArray.isArray() || bugArray.isEmpty()) {
                        break; // No more bugs
                    }

                    for (JsonNode bug : bugArray) {
                        String bugId = bug.path("bug_id").asText();
                        if (!bugId.isEmpty()) {
                            bugIds.add(bugId);
                        }
                    }

                    offset += limit;

                }

            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }

        return bugIds;
    }

    public static void main(String[] args) throws IOException {
//        getBugIdsWithLimit(100);
        List<String> bugIds = getAllBugIds();
        List<Map<String, String>> bugList = getBugDetails(bugIds);
        exportToExcel(bugList, "/Users/karthik-19462/Documents/Common/Bug_Report_1.xlsx");
        System.out.println(bugIds.size());

    }
}