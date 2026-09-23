package com.water_meter.automation.Strategies;

import com.water_meter.automation.Interfaces.StrategyInterface;

import java.text.SimpleDateFormat;
import java.util.Date;

public class VeredasStrategy implements StrategyInterface {

    private final int floors;
    private final int apartmentsPerFloor;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public VeredasStrategy(int floors, int apartmentsPerFloor) {
        this.floors = floors;
        this.apartmentsPerFloor = apartmentsPerFloor;
    }

    @Override
    public String generateNewName(int timeIndex, Date photoDate) {
        // timeIndex vem na ordem "unidade por unidade, andar por andar" (a ordem original da captura)
        int apartment = timeIndex / floors;   // 0-based
        int floor = timeIndex % floors;    // 0-based
        int buildingNumber = (timeIndex / 20) + 1;
        int localIndex = timeIndex % 20;

        int finalAp = (localIndex / 5) + 1;
        int tensAp = (localIndex % 5) * 10;


        int apNumber = tensAp + finalAp;

        String formatedDate = dateFormat.format(photoDate);

        // zero-padding garante que a ordenação alfabética dos arquivos já saia na ordem certa (por andar)
        return String.format("apto_%02d_%02d", buildingNumber, apNumber);
    }
}