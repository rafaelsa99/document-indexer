import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */

public class CorpusReader {
        // le o ficheiro de dados e insere as variaveis nos documentos
        List<Document> documentList = new ArrayList<>();
        public void loadFile(String filename) throws IOException {

            File excelFile = new File(filename);
            FileInputStream fis = new FileInputStream(excelFile);

            // we create an XSSF Workbook object for our XLSX Excel File
            Workbook workbook = new XSSFWorkbook(fis);
            // we get first sheet
            Sheet sheet = workbook.getSheetAt(0);
            // we iterate on rows
            String idDoc = "";
            String titleDoc = "";
            String abstractDoc = "";

            //percorre as folhas do excel
            for (Row row : sheet) {
                // iterate on cells for the current row
                Iterator<Cell> cellIterator = row.cellIterator();
                //percorre as linhas

                Document doc = new Document(idDoc,titleDoc,abstractDoc);
                documentList.add(doc);

                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    //columns excel
                    // 0-> sha ; 1-> source_x ; 2-> title ; 3-> doi; 4-> pmcid ; 5-> pubmed_id ; 6-> license
                    // 7-> abstract ; 8-> publish_time ; 9-> authors ;
                    if (cell.getColumnIndex() == 2){
                        titleDoc = cell.toString();
                    } else if (cell.getColumnIndex() == 3)
                        idDoc = cell.toString();
                    else if (cell.getColumnIndex() == 7)
                        if (!cell.toString().isEmpty()) //adicionar apenas entradas não nulas
                            abstractDoc = cell.toString();
                }
            }
            workbook.close();
            fis.close();

            //percorrer a lista de documentos para testar se o código em cima está correto
            /*
            for (Document document : documentList) {
                System.out.println("ID " + document.getId() + " | Abstract " + document.getAbstrct() + " | Title " + document.getTitle() + "\n");
            }*/
            //System.out.println("ID " + document.getId() + " | Abstract " + document.getAbstrct() + " | Title " + document.getTitle() + "\n");

            //ver tamanho inicial
            //System.out.println("Tamanho inicial " + documentList.size());
            /*
            for (Document document : documentList) {

                if (document.getId().equals("10.1007/s00134-020-05985-9"))
                    System.out.println("ID " + document.getId() + " | Abstract " + document.getAbstrct() + " | Title " + document.getTitle() + "\n");

            }*/
            //remover as entradas nulas do campo abstracto
            //documentList.removeIf(document -> document.getAbstrct().isBlank());
            //o programa está a meter por default abstract quando não tem nada
            //documentList.removeIf(document -> document.getAbstrct().equals("abstract"));

            //~temos 29507 linhas
            //System.out.println("Tamanho " + documentList.size());
        }

}
