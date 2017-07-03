package sample;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Service {

    public String createIndex(String docPath, String indexPath) throws Exception {
        // Store the index in memory:
        Directory directory = FSDirectory.open(Paths.get(indexPath));
        // To store an index on disk, use this instead:
        //Analyzer analyzer = new StandardAnalyzer();
        List<String> log = new ArrayList<String>();

        Analyzer analyzer = new IKAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter iwriter = new IndexWriter(directory, config);

        File dir = new File(docPath);
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            System.out.println(files[i].getName());
            Document doc = new Document();
            String body = "";
            String fileName = files[i].getName();
            switch (fileName.substring(fileName.lastIndexOf('.')+1)) {
                case "doc":
                    body = wordReader(docPath + fileName);
                    log.add("读取doc文件:" + files[i].getName());
                    System.out.println("读取doc文件:" + files[i].getName());
                    break;
                case "html":
                    body = htmlReader(docPath + fileName, "gbk");
                    log.add("读取html文件:" + files[i].getName());
                    System.out.println("读取html文件:" + files[i].getName());
                    break;
                case "pdf":
                    body = pdfReader(docPath + fileName);
                    log.add("读取pdf文件：" + files[i].getName());
                    System.out.println("读取pdf文件：" + files[i].getName());
                    break;
                case "ppt":
                    body = pptReader(docPath + fileName);
                    log.add("读取ppt文件："+files[i].getName());
                    System.out.println("读取ppt文件："+files[i].getName());
                    break;
                case "txt":
                    body = txtReader(docPath + fileName, "gbk");
                    log.add("读取txt文件:"+files[i].getName());
                    System.out.println("读取TXT文件:"+files[i].getName());
                    break;
                case "xls":
                    body = excelReader(docPath + fileName);
                    log.add("读取xls文件:"+files[i].getName());
                    System.out.println("读取xls文件:"+files[i].getName());
                    break;
                case "xml":
                    body = htmlReader(docPath + fileName, "utf-8");
                    log.add("读取xml文件:" + files[i].getName());
                    System.out.println("读取xml文件:" + files[i].getName());
                    break;
                default:

            }
            doc.add(new StringField("path", docPath + fileName, Field.Store.YES));
            doc.add(new TextField("body", body, Field.Store.YES));
            iwriter.addDocument(doc);
            log.add("建立索引，文件:" + files[i].getName());
            System.out.println("建立索引文件:" + files[i].getName());
        }

        iwriter.close();
        directory.close();
        log.add("索引完成!");
        System.out.println("indexing finished!");

        JSONObject json = new JSONObject();
        json.put("log", log);
        return new String(json.toString().getBytes("UTF-8"), "UTF-8");
    }

    public String search(String indexPath, String queryStr) throws IOException, JSONException, InvalidTokenOffsetsException {
        // Now search the index:
        JSONArray results = new JSONArray();

        Directory directory = FSDirectory.open(Paths.get(indexPath));
        Analyzer analyzer = new IKAnalyzer();
        DirectoryReader ireader = DirectoryReader.open(directory);
        IndexSearcher isearcher = new IndexSearcher(ireader);
        // Parse a simple query that searches for "text":
        QueryParser parser = new QueryParser("body", analyzer);
        org.apache.lucene.search.Query query = null;
        try {
            query = parser.parse(queryStr);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        SimpleHTMLFormatter simpleHTMLFormatter =  new SimpleHTMLFormatter("<font color='red'>", "</font>");
        Highlighter highlighter = new Highlighter(simpleHTMLFormatter, new QueryScorer(query));

        ScoreDoc[] hits = isearcher.search(query, 1000).scoreDocs;
        for (int i = 0; i < hits.length; i++) {
            JSONObject json = new JSONObject();
            Document hitDoc = isearcher.doc(hits[i].doc);

            TokenStream tokenStream = analyzer.tokenStream("body",new StringReader(hitDoc.get("body")));
            String highLightText = highlighter.getBestFragment(tokenStream, hitDoc.get("body"));

            json.put("path", hitDoc.get("path"));
            json.put("body", hitDoc.get("body"));
            results.put(json);
            System.out.println(i + ":" + hitDoc.get("path"));
        }
        ireader.close();
        directory.close();
        JSONObject response = new JSONObject();
        response.put("count", hits.length);
        response.put("results", results);
        return new String(response.toString().getBytes("UTF-8"), "UTF-8");
    }
    /**
     * TXT
     *
     * @param
     * @param
     * @return
     * @throws IOException
     */
    public String txtReader(String fileName, String charSet) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), charSet));
        String line = new String();
        String temp = new String();
        while ((line = reader.readLine()) != null) {
            temp += line;
        }
        reader.close();
        return temp;
    }


    public String htmlReader(String fileName, String charSet) throws IOException {
        String htmlStr = txtReader(fileName, charSet);
        String regEx_script="<script[^>]*?>[\\s\\S]*?<\\/script>"; //����script��������ʽ
        String regEx_style="<style[^>]*?>[\\s\\S]*?<\\/style>"; //����style��������ʽ
        String regEx_html="<[^>]+>"; //����HTML��ǩ��������ʽ

        Pattern p_script=Pattern.compile(regEx_script,Pattern.CASE_INSENSITIVE);
        Matcher m_script=p_script.matcher(htmlStr);
        htmlStr=m_script.replaceAll(""); //����script��ǩ

        Pattern p_style=Pattern.compile(regEx_style,Pattern.CASE_INSENSITIVE);
        Matcher m_style=p_style.matcher(htmlStr);
        htmlStr=m_style.replaceAll(""); //����style��ǩ

        Pattern p_html=Pattern.compile(regEx_html,Pattern.CASE_INSENSITIVE);
        Matcher m_html=p_html.matcher(htmlStr);
        htmlStr=m_html.replaceAll(" "); //����html��ǩ

        return htmlStr.trim(); //�����ı��ַ�
    }
    /**
     * word
     *
     * @param fileName
     *
     * @return
     * @throws IOException
     */
    public String wordReader(String fileName) throws IOException {
        String bodyText = null;
        FileInputStream is = new FileInputStream(fileName);
        bodyText = new WordExtractor(is).getText();
        return bodyText;
    }

    /**
     * Excel
     *
     * @param fileName
     *
     * @return
     * @throws IOException
     */
    public static String excelReader(String fileName) throws IOException {
        InputStream path = new FileInputStream(fileName);
        String content = null;
        HSSFWorkbook wb = new HSSFWorkbook(path);
        ExcelExtractor extractor = new ExcelExtractor(wb);
        extractor.setFormulasNotResults(true);
        extractor.setIncludeSheetNames(false);
        content = extractor.getText();
        return content;
    }

    /**
     * PDF
     *
     * @param fileName·��
     * @return
     * @throws Exception
     */
    public static String pdfReader(String fileName) throws Exception {
        StringBuffer content = new StringBuffer("");// �ĵ�����
        FileInputStream fis = new FileInputStream(fileName);
        PDFParser p = new PDFParser(fis);
        p.parse();
        PDFTextStripper ts = new PDFTextStripper();
        content.append(ts.getText(p.getPDDocument()));
        fis.close();
        return content.toString().trim();
    }

    /**
     * ppt
     *
     * @param fileName
     * @return
     * @throws Exception
     */
    public static String pptReader(String fileName) throws Exception {
        FileInputStream fis = new FileInputStream(fileName);
        PowerPointExtractor powerPointExtractor = new PowerPointExtractor(fis);
        return powerPointExtractor.getText();
    }
}

