package com.water_meter.automation.Strategies;

import com.water_meter.automation.Interfaces.StrategyInterface;


import java.text.SimpleDateFormat;

public class DefaultStrategy implements StrategyInterface{

    int index = 1;
    @Override
   public String generateNewName(int timeIndex, java.util.Date photoDate){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");




        int houseNumber = timeIndex + 1;
        return String.format("Leitura_%02d_%s.HEIC", houseNumber, sdf.format(photoDate));
    }

}