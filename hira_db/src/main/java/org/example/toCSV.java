package org.example;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class toCSV {

    public static void main(String[] args){
        try{
            //api 호출
            String apiUrl = "http://apis.data.go.kr/B551182/nonPaymentDamtInfoService/getNonPaymentItemHospDtlList?ServiceKey=ZvxuqXudXyJxpegU2E%2BXzezdVIGuyeQZq4cdez1NhjHdr0KUJTjrjF6NdbaKnznGIRFbWQlXvcFaXKkdwCsXKA%3D%3D&pageNo=2&numOfRows=10";
            String response = sendGetRequest(apiUrl);

            //csv 파일로
            saveToCSV(response, "condition");
        }catch (Exception e){
            System.err.println("에러" + e.getMessage());
        }
    }

    //HTTP GET 요청
    public static String sendGetRequest(String apiUrl) throws Exception{
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/xml"); //xml 데이터 요청

        int responseCode = conn.getResponseCode();
        if (responseCode == 200){ // HTTP 응답 ok
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        }else{
            throw new Exception("GET 요청 실패 " + responseCode);
        }
    }

    //csv 파일 저장
    public static void saveToCSV(String xmlData, String condition) throws Exception{
        // 파일 이름
        String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String fileName = currentDate + "_" + condition + ".csv";

        try(PrintWriter writer = new PrintWriter(new FileWriter(fileName))){
            //파일 헤더
            writer.println("adtEndDd,adtFrDd,clCd,clCdNm,curAmt,npayCd,npayKorNm,sgguCd,sgguCdNm,sidoCd,sidoCdNm,sno,urlAddr,yadmNm,yadmNpayCdNm,ykiho");

            //xml 데이터 파싱
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new java.io.ByteArrayInputStream(xmlData.getBytes()));

            NodeList items = document.getElementsByTagName("item");
            for (int i = 0 ; i < items.getLength(); i++){
                Element item = (Element) items.item(i);
                writer.println(String.join(",",
                        getElementText(item, "adtEndDd"),
                        getElementText(item, "adtFrDd"),
                        getElementText(item, "clCd"),
                        getElementText(item, "clCdNm"),
                        getElementText(item, "curAmt"),
                        getElementText(item, "npayCd"),
                        getElementText(item, "npayKorNm"),
                        getElementText(item, "sgguCd"),
                        getElementText(item, "sgguCdNm"),
                        getElementText(item, "sidoCd"),
                        getElementText(item, "sidoCdNm"),
                        getElementText(item, "sno"),
                        getElementText(item, "urlAddr"),
                        getElementText(item, "yadmNm"),
                        getElementText(item, "yadmNpayCdNm"),
                        getElementText(item, "ykiho")
                ));
            }

            System.out.println(fileName+"생성");
        }
    }

    // xml 태그 값 추출
    private static String getElementText(Element parent, String tagName){
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0){
            return nodes.item(0).getTextContent();
        }
        return "";
    }
}
