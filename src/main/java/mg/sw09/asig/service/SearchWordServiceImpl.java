package mg.sw09.asig.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import mg.sw09.asig.entity.PopularWordDto;
import mg.sw09.asig.mapper.SearchWordMapper;

@Service
public class SearchWordServiceImpl implements SearchWordService {

    private final SearchWordMapper searchWordMapper;

    // 실제 서비스 키 (기존 컨트롤러에서 사용하던 값)
    private static final String SERVICE_KEY =
            "540ee9ab1a71353d67520d92c26d11550d8b93c0da6d2842d3ef1cd3db5894c2";

    public SearchWordServiceImpl(SearchWordMapper searchWordMapper) {
        this.searchWordMapper = searchWordMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PopularWordDto> getPopularWordList(String memId) {
        if (memId == null) {
            // 전체 인기 검색어
            return searchWordMapper.allAgeList();
        }
        //현재 주민번호가 암호화되었기에 나이 추출 불가. 추후에 calculateAge로 직접 나이 추출하는 로직 필요
        //int memAge = searchWordMapper.selectAge(memId);
        return searchWordMapper.list(20);
    }

    @Override
    @Transactional
    public SearchResult searchAndRecord(String pwWord, String pageNo, String memId) {
        // 공백 제거
        String normalizedWord = pwWord.replaceAll("\\s+", "");

        // 로그인 했으면 popular_word 테이블에 기록
        if (memId != null && !memId.isEmpty()) {
            //int memAge = searchWordMapper.selectAge(memId);
            PopularWordDto dto = new PopularWordDto();
            dto.setPw_word(normalizedWord);
            dto.setPw_age(20);
            dto.setMem_id(memId);
            searchWordMapper.insert(dto);
        }

        ApiResponse apiResponse = callOpenApi(normalizedWord, pageNo);
        List<Map<String, Object>> resultList = convertToViewList(apiResponse.items);

        return new SearchResult(
                resultList,
                normalizedWord,
                apiResponse.totalCount,
                pageNo
        );
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult searchOnly(String pwWord, String pageNo) {
        String normalizedWord = pwWord.replaceAll("\\s+", "");
        ApiResponse apiResponse = callOpenApi(normalizedWord, pageNo);
        List<Map<String, Object>> resultList = convertToViewList(apiResponse.items);

        return new SearchResult(
                resultList,
                normalizedWord,
                apiResponse.totalCount,
                pageNo
        );
    }

    // ===== 내부용 DTO =====
    private static class ApiResponse {
        final List<HashMap<String, String>> items;
        final String totalCount;

        ApiResponse(List<HashMap<String, String>> items, String totalCount) {
            this.items = items;
            this.totalCount = totalCount;
        }
    }

    // ===== OpenAPI 호출 + XML 파싱 =====

    private ApiResponse callOpenApi(String pwWord, String pageNo) {
        String xmlData = fetchXmlFromApi(pwWord, pageNo);
        try {
            return parseXml(xmlData);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse(new LinkedList<>(), "0");
        }
    }

    private String fetchXmlFromApi(String pwWord, String pageNo) {
        StringBuilder sb = new StringBuilder();
        try {
            StringBuilder urlBuilder = new StringBuilder(
                    "https://api.seibro.or.kr/openapi/service/FnTermSvc/getFinancialTermMeaning");
            urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + SERVICE_KEY);
            urlBuilder.append("&" + URLEncoder.encode("term", "UTF-8") + "=" + URLEncoder.encode(pwWord, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode(pageNo, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "="
                    + URLEncoder.encode("10", "UTF-8"));

            URL url = new URL(urlBuilder.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/json");

            BufferedReader rd;
            if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private ApiResponse parseXml(String data) throws Exception {

        List<HashMap<String, String>> resultMap = new LinkedList<>();

        InputSource is = new InputSource(new StringReader(data));
        Document document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(is);

        XPath xpath = XPathFactory.newInstance().newXPath();

        // item 노드들
        NodeList nodeList = (NodeList) xpath
                .compile("/response/body/items/item")
                .evaluate(document, XPathConstants.NODESET);
        int nodeListCount = nodeList.getLength();

        for (int i = 0; i < nodeListCount; i++) {
            NodeList childNode = nodeList.item(i).getChildNodes();
            HashMap<String, String> nodeMap = new HashMap<>();
            int childNodeCount = childNode.getLength();
            for (int j = 0; j < childNodeCount; j++) {
                nodeMap.put(childNode.item(j).getNodeName(), childNode.item(j).getTextContent());
            }
            resultMap.add(nodeMap);
        }

        // totalCount
        String totalCountValue = "0";
        NodeList nodeList2 = (NodeList) xpath
                .compile("/response/body/totalCount")
                .evaluate(document, XPathConstants.NODESET);
        if (nodeList2.getLength() > 0) {
            Node totalCountNode = nodeList2.item(0);
            totalCountValue = totalCountNode.getTextContent();
        }

        return new ApiResponse(resultMap, totalCountValue);
    }

    // XML 파싱 결과를 뷰에서 쓰기 좋은 형태로 변환
    private List<Map<String, Object>> convertToViewList(List<HashMap<String, String>> list) {
        List<Map<String, Object>> resultList = new LinkedList<>();

        for (Map<String, String> tmpMap : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("word", tmpMap.get("fnceDictNm"));
            map.put("content", tmpMap.get("ksdFnceDictDescContent"));
            resultList.add(map);
        }

        return resultList;
    }
}