package com.water_meter.automation.Runner;


import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.water_meter.automation.Interfaces.StrategyInterface;
import com.water_meter.automation.Services.GoogleDriveAuthService;
import com.water_meter.automation.Strategies.DefaultStrategy;
import com.water_meter.automation.Strategies.VeredasStrategy;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import java.util.Scanner;

@Component
public class HidroMeterAutomation implements CommandLineRunner {



    private final GoogleDriveAuthService driveService;

    public record HydroMeterReading(File driveFiles, Date originalDate) {}

    public HidroMeterAutomation(GoogleDriveAuthService driveService) {
        this.driveService = driveService;
    }


    @Override
    public void run(String... args) throws Exception {
        System.out.println("Iniciando a automação de organização de hidrômetros...");
        Drive drive = driveService.getDriveService();


        Scanner input = new Scanner(System.in);

        System.out.println("insira o folder id aqui: ");
        String folderId = input.next();

        String query = "'" + folderId + "' in parents and mimeType contains 'image/' and trashed = false";

        FileList result = drive.files().list()
                .setQ(query)
                .setPageSize(1000)
                .setFields("nextPageToken, files(id, name, mimeType, createdTime, imageMediaMetadata)")
                .execute();


        List<File> driveFiles = result.getFiles();

        if (driveFiles == null || driveFiles.isEmpty()) {
            System.out.println("Nenhuma imagem para processar na pasta!");
            return;
        }

        List<HydroMeterReading> readingList = new ArrayList<>();


        System.out.println("Processando e extraindo datas...");
        System.out.println("Processando datas do Drive...");
        for (File driveFile : driveFiles) {

            Date originalDate = getGoogleFileDate(driveFile);
            readingList.add(new HydroMeterReading(driveFile, originalDate));
        }


System.out.println("Qual condomínio? 1-Padrão, 2-veredas");
int opcao = input.nextInt();

        StrategyInterface chosenRule;

        if (opcao == 1) {
            readingList.sort(Comparator.comparing(HydroMeterReading::originalDate).reversed());
            chosenRule = new DefaultStrategy();
        } else {
            readingList.sort(Comparator.comparing(HydroMeterReading::originalDate));
            chosenRule = new VeredasStrategy(5, 4);
        }

        int index = 0;
        for (HydroMeterReading reading : readingList) {


            String originalName = reading.driveFiles().getName();
            String extension = originalName.substring(originalName.lastIndexOf(""));
            String newName = chosenRule.generateNewName(index, reading.originalDate()) +  extension;

            File metaDataAtt = new File();
            metaDataAtt.setName(newName);
            drive.files().update(reading.driveFiles().getId(), metaDataAtt).execute();
            System.out.println("Renomeando: " + reading.driveFiles().getName() + " -> " + newName);



            index++;
        }

    }


    // --- MÉTODOS AUXILIARES ---

    private Date getGoogleFileDate(File driveFile) {
        try {

            if (driveFile.getImageMediaMetadata() != null && driveFile.getImageMediaMetadata().getTime() != null) {
                String textDate = driveFile.getImageMediaMetadata().getTime();


                SimpleDateFormat formatoExif = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                return formatoExif.parse(textDate);
            }


            if (driveFile.getCreatedTime() != null) {

                return new Date(driveFile.getCreatedTime().getValue());
            }
        } catch (Exception e) {
            System.out.println("Erro ao converter data do arquivo " + driveFile.getName());
        }


        return new Date();
    }
}