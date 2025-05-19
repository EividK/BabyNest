package com.assignment_two_starter.service;


import com.assignment_two_starter.dto.OrderDTO;
import com.assignment_two_starter.dto.OrderItemDTO;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class InvoiceService {

    public byte[] generateInvoice(OrderDTO order) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();
            document.add(new Paragraph("Invoice for Order #" + order.getOrderId()));
            document.add(new Paragraph("Order Date: " + order.getOrderDate()));
            document.add(new Paragraph("Customer: " + order.getCustomerName()));
            document.add(new Paragraph("Shipping Address: " + order.getShippingAddress()));
            document.add(new Paragraph("Payment Status: " + order.getPaymentStatus()));
            document.add(new Paragraph("Total Amount: " + order.getTotalAmount()));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Order Items:"));
            for (OrderItemDTO item : order.getOrderItems()) {
                document.add(new Paragraph(
                        item.getProductName() + "    |  Quantity: " + item.getQuantity() + "    |  Unit Price: " + item.getUnitPrice() +
                                "    |  Total Price: " + item.getTotalPrice()
                ));
            }
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }
}
