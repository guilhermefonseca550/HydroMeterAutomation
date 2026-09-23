package com.water_meter.automation.Runner;

import ch.qos.logback.classic.spi.EventArgUtil;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.water_meter.automation.Interfaces.StrategyInterface;
import com.water_meter.automation.Services.GoogleDriveAuthService;
import com.water_meter.automation.Strategies.DefaultStrategy;
import com.water_meter.automation.Strategies.VeredasStrategy;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.SQLOutput;
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
                .setFields("nextPageToken, files(id, name, mimeType, createdTime, imageMediaMetadata)")
                .execute();


        List<File> driveFiles = result.getFiles();

        if (driveFiles == null || driveFiles.isEmpty()) {
            System.out.println("Nenhuma imagem para processar na pasta.");
            return;
        }

        List<HydroMeterReading> readingList = new ArrayList<>();


        System.out.println("Processando e extraindo datas...");
        System.out.println("Processando datas do Drive...");
        for (File driveFile : driveFiles) {
            // Agora é só uma linha chamando o novo metodo e criando o seu "Record"
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
        int index = 0; // Controla a posição cronológica
        for (HydroMeterReading reading : readingList) {

            String originalName = reading.driveFiles().getName();
            String extension = originalName.substring(originalName.lastIndexOf("."));
            String newName = chosenRule.generateNewName(index, reading.originalDate()) +  extension;

            File atualizacaoMetadados = new File();
            atualizacaoMetadados.setName(newName);
            drive.files().update(reading.driveFiles().getId(), atualizacaoMetadados).execute();
            System.out.println("Renomeando: " + reading.driveFiles().getName() + " -> " + newName);



            index++;
        }

    }


    // --- MÉTODOS AUXILIARES ---

    private Date getGoogleFileDate(File driveFile) {
        try {
            // 1. Tenta pegar a data da câmera lida pelo próprio Google (Plano A)
            if (driveFile.getImageMediaMetadata() != null && driveFile.getImageMediaMetadata().getTime() != null) {
                String textDate = driveFile.getImageMediaMetadata().getTime();

                // O padrão EXIF na nuvem sempre usa dois pontos até na data: "yyyy:MM:dd HH:mm:ss"
                SimpleDateFormat formatoExif = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                return formatoExif.parse(textDate);
            }

            // 2. Se a foto não tiver data original, usamos a data de Criação no Drive (Plano B)
            if (driveFile.getCreatedTime() != null) {
                // O getCreatedTime() retorna um tipo específico do Google.
                // O .getValue() extrai os milissegundos exatos, que o java.util.Date entende.
                return new Date(driveFile.getCreatedTime().getValue());
            }
        } catch (Exception e) {
            System.out.println("Erro ao converter data do arquivo " + driveFile.getName());
        }

        // 3. Plano C (emergência para não quebrar a automação)
        return new Date();
    }
}