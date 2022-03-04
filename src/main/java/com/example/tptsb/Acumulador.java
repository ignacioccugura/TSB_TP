package com.example.tptsb;

public class Acumulador
{
    private long total;
    private String id;

    public Acumulador(){
        this.total = 1;
    }
    public long getTotal(){
        return total;
    }
    public void incrementar(){
        total++;
    }
    public void setId(String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }

    public void setTotal(long total)
    {
        this.total = total;
    }
}
