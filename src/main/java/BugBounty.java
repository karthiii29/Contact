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
    private static final String AUTH_TOKEN = "Zoho-oauthtoken 1000.0ec960b36b7d62620454efb714934522.a1fa26df9e180d6a0f1ca01aa7049165"; // Replace with actual token
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static List<String> getBugIds() throws IOException {
        // Build query parameters
        Map<String, String> params = new LinkedHashMap<>();
        params.put("offset", "1");
        params.put("limit", "70");
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

    public static void main(String[] args) throws IOException {
        List<String> bugIds = getBugIds();
        List<Map<String, String>> bugList = getBugDetails(bugIds);
        exportToExcel(bugList, "/Users/karthik-19462/Documents/Common/Bug_Report.xlsx");
        System.out.println(getBugIds());
    }
}