
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {


  private RestTemplate restTemplate;
  private StockQuotesService stockQuotesService;
  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }
  
  public PortfolioManagerImpl(StockQuotesService stockQuotesService) {
    this.stockQuotesService = stockQuotesService;
  }

  /* calculateAnnualizedReturn Supportive function*/
  private Double getOpeningPriceOnStartDate(List<Candle> candles) {
    return candles.get(0).getOpen();
  }

  private Double getClosingPriceOnEndDate(List<Candle> candles) {
    return candles.get(candles.size() - 1).getClose();
  }

  private AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate, PortfolioTrade trade,
      Double buyPrice, Double sellPrice) {
    double total_num_years = DAYS.between(trade.getPurchaseDate(), endDate) / 365.2422;
    double totalReturns = (sellPrice - buyPrice) / buyPrice;
    double annualized_returns = Math.pow((1.0 + totalReturns), (1.0 / total_num_years)) - 1;
    return new AnnualizedReturn(trade.getSymbol(), annualized_returns, totalReturns);
  }

  /*Implementaion method of INTERFACE */
  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades, LocalDate endDate)throws Exception{
    List<AnnualizedReturn> annualizedReturns=new ArrayList<>();
    for(PortfolioTrade portfolioTrade:portfolioTrades){
      List<Candle> candles=getStockQuote(portfolioTrade.getSymbol(), portfolioTrade.getPurchaseDate(), endDate);
      AnnualizedReturn annualizedReturn=calculateAnnualizedReturns( endDate,portfolioTrade,getOpeningPriceOnStartDate(candles),getClosingPriceOnEndDate(candles));
      annualizedReturns.add(annualizedReturn);
    }
    /* //METHOD 1
    annualizedReturns.sort(Comparator.comparing(AnnualizedReturn::getAnnualizedReturn));
    Collections.reverse(annualizedReturns);
    */

    /* METHOD 2
      annualizedReturns.stream().sorted(getComparator()).collect(Collectors.toList());
     */

     //METHOD 3 using comparator given by CRIO
    annualizedReturns.sort(getComparator());
    return annualizedReturns;

      }

  //TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  //    Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  //    into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  //    clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest

  //CHECKSTYLE:OFF


 /*GetObject Mapper*/



  private Comparator<AnnualizedReturn> getComparator() {
    //Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed() function provided by crio
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException, StockQuoteServiceException {
    /*  String tiingoRestURL = buildUri(symbol, from, to);
    TiingoCandle[] tiingoCandleArray =
    restTemplate.getForObject(tiingoRestURL, TiingoCandle[].class);
    if (tiingoCandleArray == null)
    return new ArrayList<>();
    return Arrays.stream(tiingoCandleArray).collect(Collectors.toList());*/
   
    return stockQuotesService.getStockQuote(symbol, from, to);
    
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
       String uriTemplate = "https:api.tiingo.com/tiingo/daily/$SYMBOL/prices?"
            + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";

            String token="a634b6a6017a394d360b58067273f63a6b2fce53";
            String url=uriTemplate.replace("$APIKEY",token).replace("$SYMBOL",symbol).replace("$STARTDATE",startDate.toString()).replace("$ENDDATE",endDate.toString());
            return url;
          }
}
