package NutriPlan.Service;

import NutriPlan.Repository.FoodInfoRepository;
import NutriPlan.Repository.UserDietPlanRepository;
import NutriPlan.Repository.UserRepository;
import NutriPlan.model.Dao.FoodInfo;
import NutriPlan.model.Dao.User;
import NutriPlan.model.Dao.UserDietPlan;
import NutriPlan.model.Dto.FoodNutrientDto;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.Integer.parseInt;


@Service
@EnableCaching
public class FoodService {


    private FoodInfoRepository foodInfoRepository;

    @Autowired
    private UserDietPlanRepository userDietPlanRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${api.url}")
    private String apiUrl;

    @Value("${api.key}")
    private String apiKey;

    @Autowired
    public FoodService(FoodInfoRepository foodInfoRepository) {
        this.foodInfoRepository = foodInfoRepository;
    }

//    @PostConstruct
//    public void init() {
//        try {
//            System.out.println("Initializing food data fetch...");
//            fetchAndSaveAllFoodData();
//            System.out.println("Food data fetch complete.");
//        } catch (Exception e) {
//            System.err.println("Error during food data initialization: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
    @Cacheable(value = "foodSearchCache", key = "#foodName", unless = "#result == null || #result.isEmpty()")
    public List<FoodNutrientDto> searchFood(String foodName) throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        String encodedFoodName = URLEncoder.encode(foodName, StandardCharsets.UTF_8);

        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                //.queryParam("serviceKey", URLEncoder.encode(apiKey, StandardCharsets.UTF_8))
                .queryParam("serviceKey",apiKey)
                .queryParam("FOOD_NM_KR", encodedFoodName)
                .queryParam("pageNo",1)
                .queryParam("numOfRows",20)
                .queryParam("type", "xml")
                .build(false)
                .toUriString();
        URI uri = new URI(url);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Accept", "application/xml");
            headers.add("Content-Type", "application/xml; charset=UTF-8");


            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, byte[].class);
            String response = new String(responseEntity.getBody(), StandardCharsets.UTF_8);


            if (response == null || response.isEmpty()) {
                throw new RuntimeException("API 응답이 비어있습니다.");
            }

            return parseXmlResponseUsingSAX(response);

        } catch (Exception e) {
            System.err.println("API 호출 오류: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("음식 정보를 불러오는 중 오류가 발생했습니다. 상세 메시지: " + e.getMessage());
        }
    }

    private List<FoodNutrientDto> parseXmlResponseUsingSAX(String response) throws Exception {
        List<FoodNutrientDto> foodList = new ArrayList<>();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();

        DefaultHandler handler = new DefaultHandler() {
            FoodNutrientDto currentFood = null;
            StringBuilder content = new StringBuilder();

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                if ("item".equals(qName)) {
                    currentFood = new FoodNutrientDto();
                }
                content.setLength(0);
            }

            @Override
            public void characters(char[] ch, int start, int length) {
                content.append(ch, start, length);
            }

            @Override
            public void endElement(String uri, String localName, String qName) {
                if (currentFood == null) return;

                switch (qName) {
                    case "FOOD_NM_KR" -> currentFood.setFoodName(content.toString());
                    case "SERVING_SIZE" -> currentFood.setServingSize(parseInt(content.toString()));
                    case "AMT_NUM1" -> currentFood.setKcal(parseDouble(content.toString()));
                    case "AMT_NUM7" -> currentFood.setCarbohydrate(parseDouble(content.toString()));
                    case "AMT_NUM3" -> currentFood.setProtein(parseDouble(content.toString()));
                    case "AMT_NUM4" -> currentFood.setFat(parseDouble(content.toString()));
                    case "item" -> foodList.add(currentFood);
                }
            }
        };

        ByteArrayInputStream inputStream = new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8));
        saxParser.parse(inputStream, handler);

        return foodList;
    }
    @Transactional
    public void saveFoodForUser(int userId, FoodNutrientDto foodDto,int mealTime) {


        if (userId == 0) {
            throw new RuntimeException("User ID cannot be null");
        }

        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        User user = userOptional.get();
        System.out.println("User found: " + user.getNick());

        Optional<FoodInfo> existingFood = foodInfoRepository.findByFoodName(foodDto.getFoodName());


        FoodInfo food;
        if (existingFood.isPresent()) {
            food = existingFood.get();
        } else {

            FoodInfo newFood = new FoodInfo();
            newFood.setFoodName(foodDto.getFoodName());
            newFood.setServingSize(foodDto.getServingSize());
            newFood.setKcal(foodDto.getKcal());
            newFood.setCarbohydrate(foodDto.getCarbohydrate());
            newFood.setProtein(foodDto.getProtein());
            newFood.setFat(foodDto.getFat());


            food = foodInfoRepository.save(newFood);
            System.out.println("Saved FoodInfo: " + food.getFoodName());
        }

        UserDietPlan dietPlan = new UserDietPlan(user, food, LocalDate.now(), mealTime);
        System.out.println("Created UserDietPlan: " + dietPlan);


        userDietPlanRepository.save(dietPlan);
        System.out.println("UserDietPlan saved: " + dietPlan);
    }

    private double parseDouble(String value) {
        try {
            return value.isEmpty() ? 0.0 : Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private int parseInt(String value) {
        try {
            String numericValue = value.replaceAll("[^0-9]", "");
            return numericValue.isEmpty() ? 0 : Integer.parseInt(numericValue);
        } catch (NumberFormatException e) {
            System.err.println("Serving size 변환 오류: " + value);
            return 0;
        }
    }


//    @Transactional
//    public void fetchAndSaveAllFoodData() {
//        RestTemplate restTemplate = new RestTemplate();
//        int pageNo = 1;
//        int numOfRows = 50;
//        boolean hasMoreData = true;
//        String api = "9hpTRisxBmOuhCEtV50bcIa4oegL9TzKCxdxEsNjyuJd6h2AWaRRJKLknN2mjObWPGjW7VB1LSkMX/7HGSDEBg==";
//
//        while (hasMoreData) {
//            try {
//                String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
//                        .queryParam("serviceKey", api)
//                        .queryParam("pageNo", pageNo)
//                        .queryParam("numOfRows", numOfRows)
//                        .queryParam("type", "xml")
//                        .build(false)
//                        .toUriString();
//
//                System.out.println("API 호출 URL: " + url);
//
//                HttpHeaders headers = new HttpHeaders();
//                headers.add("Accept", "application/xml");
//                HttpEntity<String> entity = new HttpEntity<>(headers);
//
//                ResponseEntity<byte[]> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
//                String response = new String(responseEntity.getBody(), StandardCharsets.UTF_8);
//
//                System.out.println("API 응답 데이터: " + response);
//
//
//                if (response.contains("<item>")) {
//                    System.out.println("item 태그가 발견되었습니다.");
//                } else {
//                    System.err.println("item 태그가 없습니다.");
//                }
//
//                if (response == null || response.isEmpty()) {
//                    System.err.println("API 응답이 비어있습니다.");
//                    break;
//
//                }
//
//                System.out.println("API 응답 수신 완료, 파싱 시작...");
//
//
//                List<FoodNutrientDto> foodList = parseXmlResponseUsingSAX(response);
//                System.out.println("파싱된 데이터 개수: " + foodList.size());
//
//
//                saveFoodListToDatabase(foodList);
//
//
//                if (foodList.size() < numOfRows) {
//                    hasMoreData = false; // 마지막 페이지 도달
//                } else {
//                    pageNo++;
//                }
//
//            } catch (Exception e) {
//                System.err.println("API 호출 오류: " + e.getMessage());
//                e.printStackTrace();
//                hasMoreData = false; // 오류 발생 시 종료
//            }
//        }
//        System.out.println("fetchAndSaveAllFoodData process completed.");
//    }
//    @Transactional
//    protected void saveFoodListToDatabase(List<FoodNutrientDto> foodList) {
//        System.out.println("데이터 저장 시작...");
//        for (FoodNutrientDto foodDto : foodList) {
//            try {
//                Optional<FoodInfo> existingFood = foodInfoRepository.findByFoodName(foodDto.getFoodName());
//                if (existingFood.isEmpty()) {
//                    // 새로운 데이터 저장
//                    FoodInfo newFood = new FoodInfo();
//                    newFood.setFoodName(foodDto.getFoodName());
//                    newFood.setServingSize(foodDto.getServingSize());
//                    newFood.setKcal(foodDto.getKcal());
//                    newFood.setCarbohydrate(foodDto.getCarbohydrate());
//                    newFood.setProtein(foodDto.getProtein());
//                    newFood.setFat(foodDto.getFat());
//
//                    foodInfoRepository.save(newFood);
//                    System.out.println("저장된 음식: " + newFood.getFoodName());
//                } else {
//                    System.out.println("이미 존재하는 음식: " + foodDto.getFoodName());
//                }
//            } catch (Exception e) {
//                System.err.println("음식 저장 중 오류: " + e.getMessage());
//            }
//        }
//        System.out.println("데이터 저장 완료.");
//    }



}