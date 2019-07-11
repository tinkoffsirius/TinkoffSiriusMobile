package com.anikinkirill.tinkoffsiriusmobile;

import android.text.format.Time;

/**
 * CREATED BY ANIKINKIRILL
 */

public class Constants {

    public static final String CONSTANTS = "constants";
    public static final String USERS = "users";
    public static final String HISTORY = "history";
    public static final String LATITUDE = "latitude";
    public static final String LONGITTUDE = "longitude";
    public static final String START_COORDINATES = "start_coordinates";
    public static final String END_COORDINATES = "end_coordinates";
    public static final String ORDERS = "orders";
    public static final String TIME = "time";
    public static final String LOGIN = "login";
    public static final String SOLUTION = "solution";
    public static final String AGENTS = "agents";
    public static String CURRENT_USER_ID = "current_user_id";

    public static final String BASE_URL = "https://tinkoffsiriusmobile.firebaseio.com";


    /**
     * Функция возвращает правильную дату сегодняшнего дня
     * на телефоне
     * @return      current_date
     */

    public static String setDate(){
        String date = "";
        Time time=new Time(Time.getCurrentTimezone());
        time.setToNow();
        if(time.monthDay<10){
            date +="0"+time.monthDay+"_";
        }else{
            date +=time.monthDay+"_";
        }
        if((time.month+1)<10){
            date +="0"+(time.month+1)+"_";
        }else{
            date +=(time.month+1)+"_";
        }
        date +=time.year;
        return date;
    }

}
