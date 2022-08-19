import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class BooleanSearchEngine implements SearchEngine {
    private Map<String, List<PageEntry>> responsesToTheRequest = new HashMap<>();

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        // прочтите тут все pdf и сохраните нужные данные,
        // тк во время поиска сервер не должен уже читать файлы
        File[] folderEntries = pdfsDir.listFiles();
        for (File pdf : folderEntries) {
            var doc = new PdfDocument(new PdfReader(pdf));

            for (int i = 1; i < doc.getNumberOfPages(); i++) {
                int pageNumber = i;
                PdfPage page = doc.getPage(i);
                var text = PdfTextExtractor.getTextFromPage(page);
                var words = text.split("\\P{IsAlphabetic}+");

                Map<String, Integer> freqs = new HashMap<>();
                for (var word : words) {
                    if (word.isEmpty()) {
                        continue;
                    }
                    freqs.put(word.toLowerCase(), freqs.getOrDefault(word.toLowerCase(), 0) + 1);
                }
                for (String w : freqs.keySet()) {
                    if (responsesToTheRequest.containsKey(w)) {
                        responsesToTheRequest.get(w).add(new PageEntry(pdf.getName(), pageNumber, freqs.get(w)));
                    } else {
                        List<PageEntry> list = new ArrayList<>();
                        PageEntry pageEntry = new PageEntry(pdf.getName(), pageNumber, freqs.get(w));
                        list.add(pageEntry);
                        responsesToTheRequest.put(w, list);
                    }
                }
            }
        }
    }

    @Override
    public List<PageEntry> search(String word) {
        // тут реализуйте поиск по слову
        if (responsesToTheRequest.containsKey(word)) {
            Collections.sort(responsesToTheRequest.get(word));
            return responsesToTheRequest.get(word);
        }
        return Collections.emptyList();
    }
}