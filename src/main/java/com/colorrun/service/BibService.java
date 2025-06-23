package com.colorrun.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.colorrun.model.Course;
import com.colorrun.model.Participant;
import com.colorrun.model.User;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;

public class BibService {

    public byte[] generateBibPdf(User user, Course course, Participant participant) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Polices
        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // Titre
        Text title = new Text("ColorRun").setFont(font).setFontSize(36);
        document.add(new Paragraph(title).setTextAlignment(TextAlignment.CENTER));

        // Nom de la course
        Text courseName = new Text(course.getNom()).setFont(regularFont).setFontSize(24);
        document.add(new Paragraph(courseName).setTextAlignment(TextAlignment.CENTER).setMarginTop(20));

        // Nom du participant
        Text participantName = new Text(user.getFirstName() + " " + user.getLastName()).setFont(regularFont).setFontSize(20);
        document.add(new Paragraph(participantName).setTextAlignment(TextAlignment.CENTER).setMarginTop(40));

        // Numéro de dossard
        Text bibNumberLabel = new Text("Dossard #").setFont(regularFont).setFontSize(18);
        Text bibNumber = new Text(String.valueOf(participant.getId())).setFont(font).setFontSize(72);
        Paragraph p = new Paragraph().add(bibNumberLabel).add(bibNumber).setTextAlignment(TextAlignment.CENTER).setMarginTop(50);
        document.add(p);

        document.close();
        return baos.toByteArray();
    }
} 