package main;

import logic.BotLogic;
import logic.Concurrency;

/**
 * Main method
 * 
 * @author erick
 */
public class Main{
    protected Main(){}
    public static void main(String[] args){
        new BotLogic().run();
        new Concurrency().run();
    }
}