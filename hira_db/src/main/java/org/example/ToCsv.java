package org.example;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ToCsv {

    public final static String apiUrl = "http://apis.data.go.kr/B551182/nonPaymentDamtInfoService/getNonPaymentItemHospDtlList?ServiceKey=ZvxuqXudXyJxpegU2E%2BXzezdVIGuyeQZq4cdez1NhjHdr0KUJTjrjF6NdbaKnznGIRFbWQlXvcFaXKkdwCsXKA%3D%3D&pageNo=2&numOfRows=10";

    public static void main(String[] args){
        try{
            //api 호출
//            String apiUrl =
            String response = sendGetRequest(apiUrl);

            //csv 파일로
            saveToCSV(response, "condition");
        }catch (Exception e){
            System.err.println("에러" + e.getMessage());
        }
    }

    //HTTP GET 요청
    public static String sendGetRequest(String apiUrl) /*throws Exception*/ {
        URL url = null;
        HttpURLConnection conn = null;
        BufferedReader in = null;
        String s = null;
        try {
            url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/xml"); //xml 데이터 요청

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) { // HTTP 응답 ok if문 수정
                in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                s = response.toString();
            } else {
//                // throw new Exception("GET 요청 실패 " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return s;
    }

    //csv 파일 저장
    public static void saveToCSV(String xmlData, String condition) throws Exception{
        // 파일 이름
        String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String fileName = currentDate + "_" + condition + ".csv"; //버퍼 사용 'append'

        try(PrintWriter writer = new PrintWriter(new FileWriter(fileName))){
            //파일 헤더
            writer.println("adtEndDd,adtFrDd,clCd,clCdNm,curAmt,npayCd,npayKorNm,sgguCd,sgguCdNm,sidoCd,sidoCdNm,sno,urlAddr,yadmNm,yadmNpayCdNm,ykiho");
            // final로 선언해두기

            //xml 데이터 파싱
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new java.io.ByteArrayInputStream(xmlData.getBytes())); //변수로 선언 후 잡아주기

            NodeList items = document.getElementsByTagName("item");
            Element item =null;
            for (int i = 0 ; i < items.getLength(); i++){
                item = (Element) items.item(i);
                writer.println(String.join(",",
                        getElementText(item, "adtEndDd"),
                        getElementText(item, "adtFrDd"),
                        getElementText(item, "clCd"),
                        getElementText(item, "clCdNm"), //string nm
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

            // System.out.println(fileName+"생성");
            // Log4j 이용해서 출력(상태)
        } // close 확인해보기 -> 안잡아줘도 되는지
    }

    // xml 태그 값 추출
    private static String getElementText(Element parent, String tagName){
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0){
            return nodes.item(0).getTextContent();
        } //if문 제거 -> try 문으로
        return "";
    }

}
