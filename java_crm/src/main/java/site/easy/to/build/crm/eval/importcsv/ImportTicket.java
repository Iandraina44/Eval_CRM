package site.easy.to.build.crm.eval.importcsv;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import site.easy.to.build.crm.entity.Ticket;

public class ImportTicket {
    public boolean importTicket(MultipartFile file,char separator) {
        

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             @SuppressWarnings("deprecation")
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withDelimiter(separator))) {  // Spécifiez le séparateur ici

            for (CSVRecord csvRecord : csvParser) {
                // T instance = clazz.getDeclaredConstructor().newInstance();

                Ticket ticket = new Ticket();
                String subject = csvRecord.get("subject").trim();
                String  description= csvRecord.get("description").trim();
                String status = csvRecord.get("status").trim();
                String priority = csvRecord.get("priority").trim();
                String manager = csvRecord.get("manager").trim();
                String employee = csvRecord.get("employee").trim();
                String customer = csvRecord.get("customer").trim();
                String createdAt = csvRecord.get("created_at").trim();
                String depense= csvRecord.get("depense").trim();



            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la lecture du fichier CSV : " + e.getMessage());
        }


        return true;

    }
}
