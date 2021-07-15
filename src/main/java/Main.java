import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;


public class Main {
    private final static String HOST = "api.openweathermap.org";
    private final static String DATA = "data";
    private final static String VERSION = "2.5";
    private final static String FORECAST = "forecast";
    private final static String APPID =  "0c756b426b4a789928817a84761c6958";
    private final static String METRIC = "metric";
    private static String result;

    public static void main(String[] args) throws IOException, SQLException {
        if (!Repository.connect()) {
            throw  new RuntimeException("Не удалось подключиться к БД!");
        }
        Scanner in = new Scanner(System.in);

        label:
        while (true) {

            menu();
            String choose = in.nextLine();

            switch (choose) {
                case "1": {

                    System.out.print("Введите город: ");
                    String city = in.nextLine();
                    String body = getJsonWeather(city);

                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    if (body.contains("\"cod\":\"404\"")) {
                        System.out.println("city not found");
                    } else {
                        WeatherResponse weatherResponse = gson.fromJson(body, WeatherResponse.class);
                        for (WeatherResponse.WeatherList el : weatherResponse.getList()) {
                            Repository.addWeather(weatherResponse.getCity().getName(),el.getDtTxt(), el.getWeather().get(0).getDescription(), el.getMain().getTemp());
                            System.out.printf("В городе %s на дату %s ожидается %s, температура - %s%n", weatherResponse.getCity().getName(), el.getDtTxt(), el.getWeather().get(0).getDescription(), el.getMain().getTemp());
                        }
                    }
                    System.out.println("Продолжить? Да(y)/Нет(n)");
                    String cont = in.nextLine();
                    if (cont.equals("n")) {
                        break label;
                    }
                    break;
                }
                case "2": {
                    System.out.println("Введите город");
                    String city = in.nextLine();
                    System.out.println("Введите дату в формате yyyy-mm-dd");
                    String data = in.nextLine();
                    List<String> result = Repository.getWeather(city, data);
                    for (String str : result) {
                        System.out.println(str);
                    }

                    break;
                }
                case "3":
                    break label;
            }

        }
        in.close();
        Repository.disconnect();
    }

    @NotNull
    private static String getJsonWeather(String CITY) throws IOException {
        OkHttpClient client = new OkHttpClient();

        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host(HOST)
                .addPathSegment(DATA)
                .addPathSegment(VERSION)
                .addPathSegment(FORECAST)
                .addQueryParameter("q", CITY)
                .addQueryParameter("appid", APPID)
                .addQueryParameter("units", METRIC)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return Objects.requireNonNull(response.body()).string();
    }
    public static void menu(){
        System.out.println("ПРОГНОЗ ПОГОДЫ");
        System.out.println("Выберите вариант:");
        System.out.println("1. Из интернет");
        System.out.println("2. Из базы");
        System.out.println("3. Завершить работу");
    }
}
