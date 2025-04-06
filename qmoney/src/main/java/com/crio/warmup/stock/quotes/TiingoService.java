
package com.crio.warmup.stock.quotes;

import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.lang.reflect.Array;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class TiingoService implements StockQuotesService {

 private RestTemplate restTemplate;
  protected TiingoService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }
  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Implement getStockQuote method below that was also declared in the interface.

  // Note:
  // 1. You can move the code from PortfolioManagerImpl#getStockQuote inside newly created method.
  // 2. Run the tests using command below and make sure it passes.
  //    ./gradlew test --tests TiingoServiceTest
  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to) throws JsonMappingException, JsonProcessingException,StockQuoteServiceException
     {/* 
    String tiingoURL = buildURL(symbol, from, to);
    String responseString=null;
    
      responseString = restTemplate.getForObject(tiingoURL, String.class);
    
    
    TiingoCandle[] tiingoCandleArray;
   
      tiingoCandleArray = getObjectMapper().readValue(responseString, TiingoCandle[].class);
      
    return Arrays.stream(tiingoCandleArray).sorted(Comparator.comparing(Candle::getDate))
        .collect(Collectors.toList());*/
        List<Candle> stocksStartToEndDate;
        if(from.compareTo(to)>=0)throw new RuntimeException();

        String url=buildURL(symbol, from, to);
        String stocks=restTemplate.getForObject(url,String.class);
        TiingoCandle[] stocksStartToEndDateArray=getObjectMapper().readValue(stocks,TiingoCandle[].class);
        if(stocksStartToEndDateArray!=null){
          stocksStartToEndDate=Arrays.asList(stocksStartToEndDateArray);
        }
        else{
          stocksStartToEndDate=Arrays.asList(new TiingoCandle[0]);
        }
  
        return stocksStartToEndDate;
   }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Write a method to create appropriate url to call the Tiingo API.
  protected String buildURL(String symbol, LocalDate startDate, LocalDate endDate) {

    String uriTemplate = "https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?" + "startDate="
        + startDate + "&endDate=" + endDate + "&token=" + "a634b6a6017a394d360b58067273f63a6b2fce53";
    return uriTemplate;
  }
}
