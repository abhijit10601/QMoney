
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerApplication {



   public static String getToken() {
      return "a634b6a6017a394d360b58067273f63a6b2fce53";
   
    }


    //token changed

   public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
      List<String> listOfSymbols = new ArrayList<>();
      List<PortfolioTrade> portfolioTrades = getObjectMapper()
          .readValue(resolveFileFromResources(args[0]), new TypeReference<List<PortfolioTrade>>() {});
      for (PortfolioTrade portfolioTrade : portfolioTrades) {
        listOfSymbols.add(portfolioTrade.getSymbol());
      }
      return listOfSymbols;
    }
  
    private static void printJsonObject(Object object) throws IOException {
      Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
      ObjectMapper mapper = new ObjectMapper();
      logger.info(mapper.writeValueAsString(object));
    }

    private static String readFileAsString(String fileName) throws IOException, URISyntaxException {
      return new String(Files.readAllBytes(resolveFileFromResources(fileName).toPath()), "UTF-8");
    }
  //token renewal happend 
  // failed due to knfdk
private static File resolveFileFromResources(String filename) throws URISyntaxException{
   return Paths.get(Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
}

private static ObjectMapper getObjectMapper(){
   ObjectMapper om=new ObjectMapper();
   om.registerModule(new JavaTimeModule());
   return om;
}


public static List<String> debugOutputs() {

   String valueOfArgument0 = "trades.json";
   String resultOfResolveFilePathArgs0 =
       "/home/crio-user/workspace/axitchandora-ME_QMONEY_V2/qmoney/bin/main/trades.json";
   String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@1a482e36";
   String functionNameFromTestFileInStackTrace = "mainReadFile";
   String lineNumberFromTestFileInStackTrace = "29";


   return Arrays.asList(
       new String[] {valueOfArgument0, resultOfResolveFilePathArgs0, toStringOfObjectMapper,
           functionNameFromTestFileInStackTrace, lineNumberFromTestFileInStackTrace});
 }


  // TODO: CRIO_TASK_MODULE_REST_API
  //  Find out the closing price of each stock on the end_date and return the list
  //  of all symbols in ascending order by its close value on end date.

  // Note:
  // 1. You may have to register on Tiingo to get the api_token.
  // 2. Look at args parameter and the module instructions carefully.
  // 2. You can copy relevant code from #mainReadFile to parse the Json.
  // 3. Use RestTemplate#getForObject in order to call the API,
  //    and deserialize the results in List<Candle>

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
   final String tiingoToken="a634b6a6017a394d360b58067273f63a6b2fce53";
    
    // ./gradlew run --args="trades.json 2020-01-01"
    List<PortfolioTrade> pT=readTradesFromJson(args[0]);
    LocalDate endDate=LocalDate.parse(args[1]);
    
    RestTemplate rT=new RestTemplate();

    List<TotalReturnsDto> tR=new ArrayList<>();
    

    for(PortfolioTrade p:pT){
      String tiingoURL=prepareUrl(p, endDate, tiingoToken);
      TiingoCandle[] tC=rT.getForObject(tiingoURL, TiingoCandle[].class);
      tR.add(new TotalReturnsDto(p.getSymbol(), tC[tC.length-1].getClose()));

    } 
    
   Collections.sort(tR,(a,b)-> Double.compare(a.getClosingPrice(), b.getClosingPrice()));

   //Sort the stocks in ascending order of their closing price.Return the list of sorted stocks.
   List<String> ans=new ArrayList<>();

   for( TotalReturnsDto t:tR){
      ans.add(t.getSymbol());
   }

    return ans;
  }

  // TODO:
  //  After refactor, make sure that the tests pass by using these two commands
  //  ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
  //  ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile
  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
     
   List<PortfolioTrade> pT=getObjectMapper().readValue(resolveFileFromResources(filename), new TypeReference<List<PortfolioTrade>>() {});

   return pT;
  }


  // TODO:
  //  Build the Url using given parameters and use this function in your code to cann the API.
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
     return "https://api.tiingo.com/tiingo/daily/"+trade.getSymbol()+"/prices?startDate="+trade.getPurchaseDate()+"&endDate="+endDate+ "&token="+ token;

  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Now that you have the list of PortfolioTrade and their data, calculate annualized returns
  //  for the stocks provided in the Json.
  //  Use the function you just wrote #calculateAnnualizedReturns.
  //  Return the list of AnnualizedReturns sorted by annualizedReturns in descending order.

  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.




  // TODO:
  //  Ensure all tests are passing using below command
  //  ./gradlew test --tests ModuleThreeRefactorTest
  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
     return candles.get(0).getOpen();
  }


  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
     return candles.get(candles.size()-1).getClose();
  }


  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
     RestTemplate restTemplate=new RestTemplate();
     String tiingoRestURL=prepareUrl(trade, endDate, token);
     TiingoCandle[] tiingoCandleArray=restTemplate.getForObject(tiingoRestURL, TiingoCandle[].class);

     List<Candle> candleList=new ArrayList<>();
     if(tiingoCandleArray!=null){
      for(TiingoCandle candle:tiingoCandleArray){
         candleList.add(candle);
      }
     }
     return candleList;
  }

 

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Return the populated list of AnnualizedReturn for all stocks.
  //  Annualized returns should be calculated in two steps:
  //   1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  //      1.1 Store the same as totalReturns
  //   2. Calculate extrapolated annualized returns by scaling the same in years span.
  //      The formula is:
  //      annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  //      2.1 Store the same as annualized_returns
  //  Test the same using below specified command. The build should be successful.
  //     ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {
     // return new AnnualizedReturn("", 0.0, 0.0);
     double totalReturn=(sellPrice-buyPrice)/buyPrice;
     double totalNumDays=ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate);
     double totalNumYears=totalNumDays/365.24;
     double inverse=1/totalNumYears;
     double annualizedReturns=Math.pow((1+totalReturn),inverse)-1;
     return new AnnualizedReturn(trade.getSymbol(), annualizedReturns, totalReturn);
  }


  public static List<AnnualizedReturn> mainCalculateSingleReturn (String[] args)
  throws IOException, URISyntaxException {
   
   List<PortfolioTrade> portfolioTrades=readTradesFromJson(args[0]);
   List<AnnualizedReturn> annualizedReturns=new ArrayList<>();
   LocalDate localDate=LocalDate.parse(args[1]);

   for(PortfolioTrade portfolioTrade:portfolioTrades){
      List<Candle> candles=fetchCandles(portfolioTrade, localDate, getToken());
      AnnualizedReturn annualizedReturn=calculateAnnualizedReturns(localDate, portfolioTrade, getOpeningPriceOnStartDate(candles), getClosingPriceOnEndDate(candles));
      annualizedReturns.add(annualizedReturn);
   }
   
   return annualizedReturns.stream()
        .sorted((a1, a2) -> Double.compare(a2.getAnnualizedReturn(), a1.getAnnualizedReturn()))
        .collect(Collectors.toList());
   
}

























  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
       String file = args[0];
       PortfolioManager portfolioManager =PortfolioManagerFactory.getPortfolioManager(new RestTemplate());
       LocalDate endDate = LocalDate.parse(args[1]);
       String contents = readFileAsString(file);
       ObjectMapper objectMapper = getObjectMapper();
       List<PortfolioTrade> portfolioTrades =objectMapper.readValue(contents, new TypeReference<List<PortfolioTrade>>() {});
       return portfolioManager.calculateAnnualizedReturn(portfolioTrades, endDate);
  }


  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());


    printJsonObject(mainReadQuotes(args));


    printJsonObject(mainCalculateSingleReturn(args));



    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }
}

