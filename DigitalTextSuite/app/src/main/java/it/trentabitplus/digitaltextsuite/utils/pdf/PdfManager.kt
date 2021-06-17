package it.trentabitplus.digitaltextsuite.utils.pdf

import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.property.TextAlignment
import java.io.File

/**
 * This class uses the "i text pdf" library to write
 * on a pdf file and save the file in external storage
 *
 * @author Andrea Pepe
 */
class PdfManager {

    companion object{
        private const val PDF_EXTENSION = ".pdf"

        /**
         * Write on a pdf file
         * @param title : name of the pdf file
         * @param text : text that has to be written in the file
         * @param folder : File object representing a folder where to create the file
         *
         * @author Andrea Pepe
         */
        fun transformToPdf(title: String, text: String, folder: File) {

            val fileLocation = File(folder, title + PDF_EXTENSION).canonicalPath

            // create an instance of PdfDocument at fileLocation location
            val pdfDocument = PdfDocument(PdfWriter(fileLocation))
            pdfDocument.defaultPageSize = PageSize.A4

            val document = Document(pdfDocument)
            val paragraph = Paragraph(text)
            paragraph.setFontSize(16f)
            paragraph.setTextAlignment(TextAlignment.LEFT)
            document.add(paragraph)

            //This will create a file at your fileLocation, specified while creating PdfDocument instance
            document.close()
        }
    }


}