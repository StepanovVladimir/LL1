package com.company;

public class Grammer
{
    public String NonTerminal;
    public String Terminal;
    public boolean isSetDir = false;
    public Integer dirNum = -1;
    public Integer Shift = -1;
    public Integer stack = -1;
    public Integer Error = 1;
    public Integer EndState = -1;
}
