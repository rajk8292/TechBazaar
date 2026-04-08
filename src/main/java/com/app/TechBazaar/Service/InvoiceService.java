package com.app.TechBazaar.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.app.TechBazaar.Model.Orders;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

@Service
public class InvoiceService {

	public ByteArrayInputStream generateInvoice(Orders order) {

	    Document document = new Document(PageSize.A4, 36, 36, 60, 36);
	    ByteArrayOutputStream out = new ByteArrayOutputStream();

	    try {
	        PdfWriter writer = PdfWriter.getInstance(document, out);
	        document.open();

	        Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, BaseColor.WHITE);
	        Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
	        Font normalFont = new Font(Font.FontFamily.HELVETICA, 11);

	        // ===== HEADER =====
	        PdfPTable header = new PdfPTable(1);
	        header.setWidthPercentage(100);

	        PdfPCell headerCell = new PdfPCell(new Phrase("TECHBAZAAR INVOICE", titleFont));
	        headerCell.setBackgroundColor(new BaseColor(25, 118, 210));
	        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
	        headerCell.setPadding(15);
	        headerCell.setBorder(Rectangle.NO_BORDER);

	        header.addCell(headerCell);
	        document.add(header);

	        document.add(new Paragraph(" "));

	        // ===== COMPANY + INVOICE INFO =====
	        PdfPTable infoTable = new PdfPTable(2);
	        infoTable.setWidthPercentage(100);

	        PdfPCell companyCell = new PdfPCell();
	        companyCell.setBorder(Rectangle.NO_BORDER);
	        companyCell.addElement(new Paragraph("TechBazaar Pvt Ltd", boldFont));
	        companyCell.addElement(new Paragraph("Lucknow, India", normalFont));
	        companyCell.addElement(new Paragraph("support@techbazaar.com", normalFont));

	        PdfPCell invoiceCell = new PdfPCell();
	        invoiceCell.setBorder(Rectangle.NO_BORDER);
	        invoiceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
	        invoiceCell.addElement(new Paragraph("Invoice No: " + order.getOrderNumber(), boldFont));
	        invoiceCell.addElement(new Paragraph("Order Date: " + order.getOrderedAt(), normalFont));
	        invoiceCell.addElement(new Paragraph("Payment: " + order.getPaymentStatus(), normalFont));

	        infoTable.addCell(companyCell);
	        infoTable.addCell(invoiceCell);
	        document.add(infoTable);

	        document.add(new Paragraph(" "));

	        // ===== BILLING SECTION =====
	        PdfPTable billTable = new PdfPTable(1);
	        billTable.setWidthPercentage(100);

	        PdfPCell billCell = new PdfPCell();
	        billCell.setPadding(10);
	        billCell.setBackgroundColor(new BaseColor(245, 245, 245));
	        billCell.addElement(new Paragraph("BILL TO:", boldFont));
	        billCell.addElement(new Paragraph(order.getFullName(), normalFont));
	        billCell.addElement(new Paragraph(order.getAddress(), normalFont));
	        billCell.addElement(new Paragraph(order.getCity() + ", " +
	                order.getState() + " - " + order.getPincode(), normalFont));
	        billCell.addElement(new Paragraph("Phone: " + order.getPhone(), normalFont));
	        billCell.setBorder(Rectangle.NO_BORDER);

	        billTable.addCell(billCell);
	        document.add(billTable);

	        document.add(new Paragraph(" "));

	        // ===== PRODUCT TABLE =====
	        PdfPTable table = new PdfPTable(4);
	        table.setWidthPercentage(100);
	        table.setSpacingBefore(10);

	        String[] headers = {"Product", "Price", "Qty", "Subtotal"};

	        for (String h : headers) {
	            PdfPCell headerCell2 = new PdfPCell(new Phrase(h,
	                    new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE)));
	            headerCell2.setBackgroundColor(new BaseColor(25, 118, 210));
	            headerCell2.setHorizontalAlignment(Element.ALIGN_CENTER);
	            headerCell2.setPadding(8);
	            table.addCell(headerCell2);
	        }

	        table.addCell(order.getProductName());
	        table.addCell("₹" + order.getPrice());
	        table.addCell(String.valueOf(order.getQuantity()));
	        table.addCell("₹" + order.getSubtotal());

	        document.add(table);

	        document.add(new Paragraph(" "));

	        // ===== SUMMARY BOX =====
	        PdfPTable summary = new PdfPTable(2);
	        summary.setWidthPercentage(40);
	        summary.setHorizontalAlignment(Element.ALIGN_RIGHT);

	        summary.addCell("Shipping:");
	        summary.addCell("₹" + order.getShippingCharge());

	        summary.addCell("Discount:");
	        summary.addCell("- ₹" + order.getDiscountAmount());

	        PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL:", boldFont));
	        totalLabel.setBorder(Rectangle.TOP);
	        summary.addCell(totalLabel);

	        PdfPCell totalValue = new PdfPCell(new Phrase("₹" + order.getFinalAmount(), boldFont));
	        totalValue.setBorder(Rectangle.TOP);
	        summary.addCell(totalValue);

	        document.add(summary);

	        document.add(new Paragraph(" "));

	        // ===== WATERMARK PAID =====
	        if (order.getPaymentStatus().toString().equals("SUCCESS")) {
	            Phrase watermark = new Phrase("PAID",
	                    new Font(Font.FontFamily.HELVETICA, 60, Font.BOLD,
	                            new BaseColor(200, 200, 200)));

	            ColumnText.showTextAligned(writer.getDirectContentUnder(),
	                    Element.ALIGN_CENTER,
	                    watermark,
	                    300, 400, 45);
	        }

	        document.add(new Paragraph(" "));
	        document.add(new Paragraph("Thank you for shopping with TechBazaar 💙",
	                new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC)));

	        document.close();

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return new ByteArrayInputStream(out.toByteArray());
	}
}