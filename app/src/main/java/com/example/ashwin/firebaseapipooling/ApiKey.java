package com.example.ashwin.firebaseapipooling;

/**
 * Created by ashwin on 18/11/16.
 */

public class ApiKey
{
    //name and address string
    private String api = "";
    private int rank = 0;

    public ApiKey()
    {
      /*Blank default constructor essential for Firebase*/
    }

    public ApiKey(String api, int rank)
    {
        this.api = api;
        this.rank = rank;
    }

    public String getApi()
    {
        return api;
    }

    public void setApi(String api)
    {
        this.api = api;
    }

    public int getRank()
    {
        return rank;
    }

    public void setRank(int rank)
    {
        this.rank = rank;
    }

    @Override
    public String toString()
    {
        return "Api Key:  api: "+api+ ", rank: " +rank;
    }
}
