package pdfts.examples;

import com.snowtide.pdf.OutputHandler;
import com.snowtide.pdf.PDFTextStream;
import com.snowtide.pdf.Page;
import com.snowtide.pdf.layout.TextUnit;
import com.snowtide.pdf.layout.TextUnitImpl;

import java.io.*;
import java.nio.channels.FileChannel.MapMode;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * This example captures PDF text content, and builds an XHTML document to 
 * mimic the HTML view that Google offers for indexed PDF documents.
 * </p>
 * 
 * @version ©2004-2012 Snowtide Informatics Systems, Inc.
 */
public class GoogleHTMLOutputHandler extends OutputHandler {
    private Document doc;
    private Element pageElt;
    private Element pdfElt;
    private final HashMap attrs = new HashMap();
    
    private float top = 0;
    private float pageHeight;
    
    /**
     * Main method for command-line execution.  Usage:
     * <p>
     * <pre>java GoogleHTMLOutputHandler [input_pdf_file] [output_html_path]</pre>
     * </p>
     */
    public static void main (String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: java GoogleHTMLOutputHandler [input_pdf_file] [output_html_path]");
            System.exit(1);
        }
        
        FileInputStream fin = new FileInputStream(new File(args[0]));
        PDFTextStream pdfts = new PDFTextStream(fin.getChannel().map(MapMode.READ_ONLY, 0, new File(args[0]).length()), "test");
        GoogleHTMLOutputHandler tgt = new GoogleHTMLOutputHandler();
        pdfts.pipe(tgt);
        Writer out = new OutputStreamWriter(new FileOutputStream(new File(args[1])));
        XMLFormExport.serializeXMLDocument(tgt.getHTMLDocument(), out);
        out.flush();
        out.close();
    }
    
    public GoogleHTMLOutputHandler () throws ParserConfigurationException, FactoryConfigurationError {
        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    }
    
    /**
     * Returns the XHTML document that is built up by this OutputHandler.
     */
    public Document getHTMLDocument () {
        return doc;
    }

    public void startPage (Page page) {
        /*if (pageElt != null) {
            pdfElt.appendChild(buildHR(doc));
        }
        
        pdfElt.appendChild(buildTextElt(doc, "div", "Page " + (page.getPageNumber() + 1), null));        
        pdfElt.appendChild(buildHR(doc));
        
        pageElt = buildTextElt(doc, "div", null, attrs);
        pdfElt.appendChild(pageElt);*/
        
        pageHeight = page.getPageHeight();
        
        top += 50;
    }
    
    public void endPage (Page page) {
        top += page.getPageHeight();
    }

    public void startPDF (String pdfName, File pdfFile) {
        pdfElt = doc.createElement("html");
        doc.appendChild(pdfElt);        
    }

    public void textUnit (TextUnit tu) {        
        attrs.clear();
        attrs.put("style", "position:absolute;top:" + (top + pageHeight - tu.ypos()) + 
                ";left:" + tu.xpos() + ";font-size:" + ((TextUnitImpl)tu).lineHeight());
        
        String txt = tu.getCharacterSequence() == null ? 
                Character.toString((char)tu.getCharCode()) : new String(tu.getCharacterSequence());
                
        pdfElt.appendChild(buildTextElt(doc, "span", txt, attrs));
    }

    private static Element buildHR (Document doc) {
        return doc.createElement("hr");
    }
    
    private static Element buildTextElt (Document doc, String elttype, String contents, Map attributes) {
        Element te = doc.createElement(elttype);
        if (contents != null && contents.length() > 0) te.appendChild(doc.createTextNode(contents));
        
        if (attributes != null) {
            Entry attr;
            for (Iterator iter = attributes.entrySet().iterator(); iter.hasNext(); ) {
                attr = (Entry)iter.next();
                te.setAttribute(String.valueOf(attr.getKey()), String.valueOf(attr.getValue()));
            }
        }
        
        return te;
    }
}
