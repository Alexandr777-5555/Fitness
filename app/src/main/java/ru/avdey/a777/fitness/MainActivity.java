package ru.avdey.a777.fitness;


import android.content.SharedPreferences;
import android.os.AsyncTask;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/*



 */


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // переменные

    SharedPreferences sharedPref;                     //  переменная для сохранения json
    private ListView listView ;                      // список
    ArrayList sheduleList;                          // переменная где будем хранить элементы объектов
    Button buttonLoad;                             // кнопка для загрузки даныых по сети
    Button buttonShow;                            // показать загруженное расписание


    /**
     * В методе onCreate() создается активити
     * @param savedInstanceState параметры объекта Bundle активности
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // найдем список на активности
        listView = (ListView) findViewById(R.id.list);


        // найдем кнопку "ПОЛУЧИТЬ РАСПИСАНИЕ"

        buttonLoad=(Button) findViewById(R.id.schedule);
        buttonLoad.setOnClickListener(this);


        // найдем кнопку "ЗАГРУЗИТЬ"

        buttonShow=(Button) findViewById(R.id.load);
        buttonShow.setOnClickListener(this);

        sheduleList = new ArrayList(); // проинициализируем список

    }


    /**
     * метод onClick() выполняет действия по нажатию на выбранную кнопку
     * @param v представление кнопки
     */

    @Override
    public void onClick(View v) {

        switch (v.getId()) {       // получаенм ID кнопки на которую нажал

            case R.id.schedule:  //  представление "ПОЛУЧИТЬ РАСПИСАНИЕ"
                new ScheduleUpdate().execute(); //выполнить задачу по получению расписания с сервера
                break;

            case R.id.load:   // представление "ЗАГРУЗИТЬ"
                // загрузим данные из SharedPreferences
                sharedPref = getPreferences(MODE_PRIVATE);
                String sh=sharedPref.getString("jsondata","");

                if(sh==""){  // если ничего не загрузили из сети выведем сообщение
                    Toast.makeText(this, "Данные не загружены", Toast.LENGTH_SHORT).show();
                }

                stringToArray(sh); // преобразуем к массиву

                updateUI();      // обновим list view

                break;
        }

    }


    // внутренник класс ScheduleUpdate для получения данных по сети

    private class ScheduleUpdate extends AsyncTask<Void, Void, Void> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";

        // выполняется основная работа с сетью в методе doInBackground()
        @Override
        protected Void doInBackground(Void... voids) {
            // получаем данные с внешнего ресурса
            try {

                URL url = new URL("https://sample.fitnesskit-admin.ru/schedule/get_group_lessons_v2/1/");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                resultJson = buffer.toString();  // буфер к строке


                // будем сохранять весь JSON в SharedPreferences

                sharedPref = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("jsondata", resultJson);
                editor.commit();                                  // закрепим транзакцию

                stringToArray(resultJson);  // преобразуем к массиву

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }


        /**
         * метод onPostExecute выполняется в главном потоке
         * @param strJson
         */

        @Override
        protected void onPostExecute(Void strJson) {
            super.onPostExecute(strJson);
            updateUI();
        }
    } // окончание внутреннего класса


    /**
     * метод stringToArray преобразует строку Json в массив объектов
     * @param strjson строка Json
     */
    private void stringToArray(String strjson) {

        if (strjson != null) { // если данные получили с серверка тогда

            try {
                JSONArray jsonArray = new JSONArray(strjson); // приведем к массиву

                // пройдемся по всему массиву json
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject zanatie = jsonArray.getJSONObject(i);
                    String name = zanatie.getString("name");
                    String startTime = zanatie.getString("startTime");
                    String endTime = zanatie.getString("endTime");
                    String teacher = zanatie.getString("teacher");
                    String place = zanatie.getString("place");
                    String description = zanatie.getString("description");
                    String weekDay = zanatie.getString("weekDay");


                    HashMap<String, String> day = new HashMap<>();

                    // в объект одного дня положим значения
                    day.put("name", name);
                    day.put("startTime", startTime);
                    day.put("endTime", endTime);
                    day.put("place", place);
                    day.put("teacher", teacher);
                    day.put("description", description);
                    day.put("weekDay", weekDay);

                    sheduleList.add(day);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


// метод updateUI() для обновления спискового представления
    private void updateUI(){


        ListAdapter adapter = new SimpleAdapter(
                MainActivity.this, sheduleList,
                R.layout.list_item, new String[]{"name", "startTime", "endTime",
                "place" ,"teacher" , "description" , "weekDay"}, new int[]{R.id.name,
                R.id.startTime, R.id.endTime, R.id.place, R.id.teacher , R.id.description ,R.id.weekDay});

        listView.setAdapter(adapter);


    }

} // окончание класса MainActivity

